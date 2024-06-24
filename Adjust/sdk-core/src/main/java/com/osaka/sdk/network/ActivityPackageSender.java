package com.osaka.sdk.network;

import android.content.Context;
import android.net.Uri;

import com.osaka.sdk.ActivityKind;
import com.osaka.sdk.ActivityPackage;
import com.osaka.sdk.AdjustAttribution;
import com.osaka.sdk.AdjustFactory;
import com.osaka.sdk.AdjustSigner;
import com.osaka.sdk.Constants;
import com.osaka.sdk.ILogger;
import com.osaka.sdk.ResponseData;
import com.osaka.sdk.TrackingState;
import com.osaka.sdk.Util;
import com.osaka.sdk.scheduler.SingleThreadCachedScheduler;
import com.osaka.sdk.scheduler.ThreadExecutor;
import com.osaka.sdk.network.UtilNetworking.IHttpsURLConnectionProvider;
import com.osaka.sdk.network.UtilNetworking.IConnectionOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

public class ActivityPackageSender implements IActivityPackageSender {
    private String basePath;
    private String gdprPath;
    private String subscriptionPath;
    private String purchaseVerificationPath;
    private String clientSdk;

    private ILogger logger;
    private ThreadExecutor executor;
    private UrlStrategy urlStrategy;
    private IHttpsURLConnectionProvider httpsURLConnectionProvider;
    private IConnectionOptions connectionOptions;
    private Context context;

    public ActivityPackageSender(final String adjustUrlStrategy,
                                 final String basePath,
                                 final String gdprPath,
                                 final String subscriptionPath,
                                 final String purchaseVerificationPath,
                                 final String clientSdk,
                                 final Context context)
    {
        this.basePath = basePath;
        this.gdprPath = gdprPath;
        this.subscriptionPath = subscriptionPath;
        this.purchaseVerificationPath = purchaseVerificationPath;
        this.clientSdk = clientSdk;
        this.context = context;

        logger = AdjustFactory.getLogger();
        executor = new SingleThreadCachedScheduler("ActivityPackageSender");
        urlStrategy = new UrlStrategy(
                AdjustFactory.getBaseUrl(),
                AdjustFactory.getGdprUrl(),
                AdjustFactory.getSubscriptionUrl(),
                AdjustFactory.getPurchaseVerificationUrl(),
                adjustUrlStrategy);
        httpsURLConnectionProvider = AdjustFactory.getHttpsURLConnectionProvider();
        connectionOptions = AdjustFactory.getConnectionOptions();
    }

    @Override
    public void sendActivityPackage(final ActivityPackage activityPackage,
                                    final Map<String, String> sendingParameters,
                                    final ResponseDataCallbackSubscriber responseCallback)
    {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                responseCallback.onResponseDataCallback(
                        sendActivityPackageSync(activityPackage, sendingParameters));
            }
        });
    }

    @Override
    public ResponseData sendActivityPackageSync(final ActivityPackage activityPackage,
                                                final Map<String, String> sendingParameters)
    {
        AdjustSigner.sign(activityPackage.getParameters(),
                activityPackage.getActivityKind().toString(),
                activityPackage.getClientSdk(),
                context,
                logger);

        boolean retryToSend;
        ResponseData responseData;
        do {
            responseData =
                    ResponseData.buildResponseData(activityPackage, sendingParameters);

            tryToGetResponse(responseData);

            retryToSend = shouldRetryToSend(responseData);
        } while (retryToSend);

        return responseData;
    }

    private boolean shouldRetryToSend(final ResponseData responseData) {
        if (!responseData.willRetry) {
            logger.debug("Will not retry with current url strategy");
            urlStrategy.resetAfterSuccess();
            return false;
        }

        if (urlStrategy.shouldRetryAfterFailure(responseData.activityKind)) {
            logger.error("Failed with current url strategy, but it will retry with new");
            return true;
        } else {
            logger.error("Failed with current url strategy and it will not retry");
            //  Stop retrying with different type and return to caller
            return false;
        }
    }

    private void tryToGetResponse(final ResponseData responseData) {
        DataOutputStream dataOutputStream = null;

        try {
            ActivityPackage activityPackage = responseData.activityPackage;
            Map<String, String> activityPackageParameters =
                    new HashMap<>(activityPackage.getParameters());
            Map<String, String> sendingParameters = responseData.sendingParameters;

            String authorizationHeader = buildAndExtractAuthorizationHeader(
                    activityPackageParameters,
                    activityPackage.getActivityKind());

            boolean shouldUseGET =
                    responseData.activityPackage.getActivityKind() == ActivityKind.ATTRIBUTION;
            final String urlString;
            if (shouldUseGET) {
                urlString = generateUrlStringForGET(activityPackage.getActivityKind(),
                                                    activityPackage.getPath(),
                                                    activityPackageParameters,
                                                    sendingParameters);
            } else {
                urlString = generateUrlStringForPOST(activityPackage.getActivityKind(),
                                                     activityPackage.getPath());
            }

            final URL url = new URL(urlString);
            final HttpsURLConnection connection =
                    httpsURLConnectionProvider.generateHttpsURLConnection(url);

            // get and apply connection options (default or for tests)
            connectionOptions.applyConnectionOptions(connection, activityPackage.getClientSdk());

            if (authorizationHeader != null) {
                connection.setRequestProperty("Authorization", authorizationHeader);
            }

            if (shouldUseGET) {
                dataOutputStream = configConnectionForGET(connection);
            } else {
                dataOutputStream = configConnectionForPOST(connection,
                                                           activityPackageParameters,
                                                           sendingParameters);
            }

            // read connection response
            Integer responseCode = readConnectionResponse(connection, responseData);

            responseData.success =
                    responseData.jsonResponse != null
                            && responseData.retryIn == null
                            && responseCode != null
                            && responseCode.intValue() == HttpsURLConnection.HTTP_OK;
            // it is only processed by the server if it contains
            //  a JSON response *AND* does not contain a retry_in
            responseData.willRetry =
                    responseData.jsonResponse == null  || responseData.retryIn != null;

            if (responseData.jsonResponse == null) {
                responseData.activityPackage.addError(ErrorCodes.NULL_JSON_RESPONSE);
            } else if (responseData.retryIn != null) {
                responseData.activityPackage.addError(ErrorCodes.SERVER_RETRY_IN);
            }

        } catch (final UnsupportedEncodingException exception) {

            localError(exception, "Failed to encode parameters", responseData, ErrorCodes.UNSUPPORTED_ENCODING_EXCEPTION);

        } catch (final MalformedURLException exception) {

            localError(exception, "Malformed URL", responseData, ErrorCodes.MALFORMED_URL_EXCEPTION);

        } catch (final ProtocolException exception) {

            localError(exception, "Protocol Error", responseData, ErrorCodes.PROTOCOL_EXCEPTION);

        } catch (final SocketTimeoutException exception) {

            // timeout is remote/network related -> did not fail locally
            remoteError(exception, "Request timed out", responseData, ErrorCodes.SOCKET_TIMEOUT_EXCEPTION);

        } catch (final SSLHandshakeException exception) {

            // failed due certificate from the server -> did not fail locally
            remoteError(exception, "Certificate failed", responseData, ErrorCodes.SSL_HANDSHAKE_EXCEPTION);

        } catch (final IOException exception) {

            // IO is the network -> did not fail locally
            remoteError(exception, "Request failed", responseData, ErrorCodes.IO_EXCEPTION);

        } catch (final Throwable t) {

            // not sure if error is local or not -> assume it is local
            localError(t, "Sending SDK package", responseData, ErrorCodes.THROWABLE);

        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.flush();
                    dataOutputStream.close();
                }
            } catch (final IOException ioException) {
                String errorMessage = errorMessage(ioException,
                        "Flushing and closing connection output stream",
                        responseData.activityPackage);
                logger.error(errorMessage);
            }
        }
    }

    private void localError(Throwable throwable, String description, ResponseData responseData, int errorCode) {
        String finalMessage = errorMessage(throwable, description, responseData.activityPackage);

        logger.error(finalMessage);
        responseData.message = finalMessage;

        responseData.willRetry = false;

        responseData.activityPackage.addError(errorCode);
    }

    private void remoteError(Throwable throwable, String description, ResponseData responseData, Integer errorCode) {
        String finalMessage = errorMessage(throwable, description, responseData.activityPackage)
                + " Will retry later";

        logger.error(finalMessage);
        responseData.message = finalMessage;

        responseData.willRetry = true;

        responseData.activityPackage.addError(errorCode);
    }

    private String errorMessage(final Throwable throwable,
                                final String description,
                                final ActivityPackage activityPackage)
    {
        final String failureMessage = activityPackage.getFailureMessage();
        final String reasonString = Util.getReasonString(description, throwable);
        return Util.formatString("%s. (%s)", failureMessage, reasonString);
    }

    private String generateUrlStringForGET(final ActivityKind activityKind,
                                           final String activityPackagePath,
                                           final Map<String, String> activityPackageParameters,
                                           final Map<String, String> sendingParameters)
            throws MalformedURLException
    {
        String targetUrl = urlStrategy.targetUrlByActivityKind(activityKind);

        // extra path, if present, has the format '/X/Y'
        String urlWithPath =
                urlWithExtraPathByActivityKind(activityKind, targetUrl);

        final URL urlObject = new URL(urlWithPath);
        final Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(urlObject.getProtocol());
        uriBuilder.encodedAuthority(urlObject.getAuthority());
        uriBuilder.path(urlObject.getPath());
        uriBuilder.appendPath(activityPackagePath);

        for (final Map.Entry<String, String> entry : activityPackageParameters.entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        if (sendingParameters != null) {
            for (final Map.Entry<String, String> entry: sendingParameters.entrySet()) {
                uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        logger.debug("Making request to url: %s", uriBuilder.toString());

        return uriBuilder.build().toString();
    }

    private String generateUrlStringForPOST(final ActivityKind activityKind,
                                            final String activityPackagePath)
    {
        String targetUrl =
                urlStrategy.targetUrlByActivityKind(activityKind);

        // extra path, if present, has the format '/X/Y'
        String urlWithPath =
                urlWithExtraPathByActivityKind(activityKind, targetUrl);


        // 'targetUrl' does not end with '/', but activity package paths that are sent by POST
        //  do start with '/', so it's not added om between
        String urlString = Util.formatString("%s%s", urlWithPath, activityPackagePath);

        logger.debug("Making request to url : %s", urlString);

        return urlString;
    }

    private String urlWithExtraPathByActivityKind(final ActivityKind activityKind,
                                                  final String targetUrl)
    {
        if (activityKind == ActivityKind.GDPR) {
            return gdprPath != null ? targetUrl + gdprPath : targetUrl;
        } else if (activityKind == ActivityKind.SUBSCRIPTION) {
            return subscriptionPath != null ? targetUrl + subscriptionPath : targetUrl;
        } else if (activityKind == ActivityKind.PURCHASE_VERIFICATION) {
            return purchaseVerificationPath != null ? targetUrl + purchaseVerificationPath : targetUrl;
        } else {
            return basePath != null ? targetUrl + basePath : targetUrl;
        }
    }

    private DataOutputStream configConnectionForGET(final HttpsURLConnection connection)
            throws ProtocolException
    {
        // set default GET configuration options
        connection.setRequestMethod("GET");

        return null;
    }

    private DataOutputStream configConnectionForPOST(final HttpsURLConnection connection,
                                                     final Map<String, String> activityPackageParameters,
                                                     final Map<String, String> sendingParameters)
            throws ProtocolException,
            UnsupportedEncodingException,
            IOException
    {
        // set default POST configuration options
        connection.setRequestMethod("POST");
        // don't allow caching, that is controlled by retrying mechanisms
        connection.setUseCaches(false);
        // necessary to read the response
        connection.setDoInput(true);
        // necessary to pass the body to the connection
        connection.setDoOutput(true);

        // build POST body
        final String postBodyString = generatePOSTBodyString(
                activityPackageParameters,
                sendingParameters);

        logger.debug("Post body: %s", postBodyString);

        if (postBodyString == null) {
            return null;
        }

        // write POST body to connection
        final DataOutputStream dataOutputStream =
                new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes(postBodyString);

        return dataOutputStream;
    }

    private String generatePOSTBodyString(final Map<String, String> parameters,
                                          final Map<String, String> sendingParameters)
            throws UnsupportedEncodingException
    {
        if (parameters.isEmpty()) {
            return null;
        }

        final StringBuilder postStringBuilder = new StringBuilder();

        injectParametersToPOSTStringBuilder(parameters, postStringBuilder);
        injectParametersToPOSTStringBuilder(sendingParameters, postStringBuilder);

        // delete last added &
        if (postStringBuilder.length() > 0
                && postStringBuilder.charAt(postStringBuilder.length() - 1) == '&')
        {
            postStringBuilder.deleteCharAt(postStringBuilder.length() - 1);
        }
        return postStringBuilder.toString();
    }

    private void injectParametersToPOSTStringBuilder(
            final Map<String, String> parametersToInject,
            final StringBuilder postStringBuilder)
            throws UnsupportedEncodingException
    {
        if (parametersToInject == null || parametersToInject.isEmpty()) {
            return;
        }

        for (final Map.Entry<String, String> entry : parametersToInject.entrySet()) {
            final String encodedName = URLEncoder.encode(entry.getKey(), Constants.ENCODING);
            final String value = entry.getValue();
            final String encodedValue = value != null
                    ? URLEncoder.encode(value, Constants.ENCODING) : "";
            postStringBuilder.append(encodedName);
            postStringBuilder.append("=");
            postStringBuilder.append(encodedValue);
            postStringBuilder.append("&");
        }
    }

    Integer readConnectionResponse(final HttpsURLConnection connection,
                                final ResponseData responseData)
    {
        final StringBuilder responseStringBuilder = new StringBuilder();
        Integer responseCode = null;

        // connect and read response to string builder
        try {
            connection.connect();

            responseCode = connection.getResponseCode();
            final InputStream inputStream;

            if (responseCode.intValue() >= Constants.MINIMAL_ERROR_STATUS_CODE) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }

            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseStringBuilder.append(line);
            }
        } catch (final IOException ioException) {
            String errorMessage = errorMessage(ioException,
                    "Connecting and reading response",
                    responseData.activityPackage);
            logger.error(errorMessage);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (responseStringBuilder.length() == 0) {
            logger.error( "Empty response string buffer");
            return responseCode;
        }

        if (responseCode == 429) {
            logger.error("Too frequent requests to the endpoint (429)");
            return responseCode;
        }

        // extract response string from string builder
        final String responseString = responseStringBuilder.toString();
        logger.debug("Response string: %s", responseString);

        parseResponse(responseData, responseString);

        final String responseMessage = responseData.message;
        if (responseMessage == null) {
            return responseCode;
        }

        // log response message
        if (responseCode != null && responseCode.intValue() == HttpsURLConnection.HTTP_OK) {
            logger.info("Response message: %s", responseMessage);
        } else {
            logger.error("Response message: %s", responseMessage);
        }

        return responseCode;
    }

    private void parseResponse(final ResponseData responseData, final String responseString) {
        if (responseString.length() == 0) {
            logger.error("Empty response string");
            return;
        }

        JSONObject jsonResponse = null;
        // convert string response to JSON object
        try {
            jsonResponse = new JSONObject(responseString);
        } catch (final JSONException jsonException) {
            String errorMessage = errorMessage(jsonException,
                    "Failed to parse JSON response",
                    responseData.activityPackage);
            logger.error(errorMessage);
        }

        if (jsonResponse == null) {
            return;
        }

        responseData.jsonResponse = jsonResponse;

        responseData.message = UtilNetworking.extractJsonString(jsonResponse,"message");
        responseData.adid = UtilNetworking.extractJsonString(jsonResponse, "adid");
        responseData.timestamp = UtilNetworking.extractJsonString(jsonResponse, "timestamp");
        String trackingState =
                UtilNetworking.extractJsonString(jsonResponse, "tracking_state");
        if (trackingState != null) {
            if (trackingState.equals("opted_out")) {
                responseData.trackingState = TrackingState.OPTED_OUT;
            }
        }

        responseData.askIn = UtilNetworking.extractJsonLong(jsonResponse, "ask_in");
        responseData.retryIn = UtilNetworking.extractJsonLong(jsonResponse, "retry_in");
        responseData.continueIn = UtilNetworking.extractJsonLong(jsonResponse, "continue_in");

        JSONObject attributionJson = jsonResponse.optJSONObject("attribution");
        responseData.attribution = AdjustAttribution.fromJson(
                attributionJson,
                responseData.adid,
                Util.getSdkPrefixPlatform(clientSdk));

        responseData.resolvedDeeplink = UtilNetworking.extractJsonString(jsonResponse,"resolved_click_url");
    }

    private String buildAndExtractAuthorizationHeader(final Map<String, String> parameters,
                                                      final ActivityKind activityKind) {
        String activityKindString = activityKind.toString();

        String adjSigningId = extractAdjSigningId(parameters);
        String secretId = extractSecretId(parameters);
        String headersId = extractHeadersId(parameters);
        String signature = extractSignature(parameters);
        String algorithm = extractAlgorithm(parameters);
        String nativeVersion = extractNativeVersion(parameters);

        String authorizationHeader = buildAuthorizationHeaderV2WithAdjSigningId(signature, adjSigningId,
                headersId, algorithm, nativeVersion);
        if (authorizationHeader != null) {
            return authorizationHeader;
        }

        authorizationHeader = buildAuthorizationHeaderV2WithSecretId(signature, secretId, headersId,
                algorithm, nativeVersion);
        if (authorizationHeader != null) {
            return authorizationHeader;
        }

        String appSecret = extractAppSecret(parameters);
        return buildAuthorizationHeaderV1(parameters, appSecret, secretId, activityKindString);
    }

    private String buildAuthorizationHeaderV1(final Map<String, String> parameters,
                                              final String appSecret,
                                              final String secretId,
                                              final String activityKindString)
    {
        // check if the secret exists and it's not empty
        if (appSecret == null || appSecret.length() == 0) {
            return null;
        }
        String appSecretName = "app_secret";

        Map<String, String> signatureDetails = getSignature(parameters, activityKindString, appSecret);

        String algorithm = "sha256";
        String signature = Util.sha256(signatureDetails.get("clear_signature"));
        String fields = signatureDetails.get("fields");

        String secretIdHeader = Util.formatString("secret_id=\"%s\"", secretId);
        String signatureHeader = Util.formatString("signature=\"%s\"", signature);
        String algorithmHeader = Util.formatString("algorithm=\"%s\"", algorithm);
        String fieldsHeader = Util.formatString("headers=\"%s\"", fields);

        String authorizationHeader = Util.formatString("Signature %s,%s,%s,%s",
                secretIdHeader, signatureHeader, algorithmHeader, fieldsHeader);
        logger.verbose("authorizationHeader: %s", authorizationHeader);

        return authorizationHeader;
    }

    private String buildAuthorizationHeaderV2WithAdjSigningId(final String signature,
                                                              final String adjSigningId,
                                                              final String headersId,
                                                              final String algorithm,
                                                              final String nativeVersion)
    {
        if (adjSigningId == null || signature == null || headersId == null) {
            return null;
        }

        String signatureHeader = Util.formatString("signature=\"%s\"", signature);
        String adjSigningIdHeader  = Util.formatString("adj_signing_id=\"%s\"", adjSigningId);
        String idHeader        = Util.formatString("headers_id=\"%s\"", headersId);
        String algorithmHeader = Util.formatString("algorithm=\"%s\"", algorithm != null ? algorithm : "adj1");
        String nativeVersionHeader = Util.formatString("native_version=\"%s\"", nativeVersion != null ? nativeVersion : "");

        String authorizationHeader = Util.formatString("Signature %s,%s,%s,%s,%s",
                signatureHeader, adjSigningIdHeader, algorithmHeader, idHeader, nativeVersionHeader);

        logger.verbose("authorizationHeader: %s", authorizationHeader);

        return authorizationHeader;
    }

    private String buildAuthorizationHeaderV2WithSecretId(final String signature,
                                                          final String secretId,
                                                          final String headersId,
                                                          final String algorithm,
                                                          final String nativeVersion)
    {
        if (secretId == null || signature == null || headersId == null) {
            return null;
        }

        String signatureHeader = Util.formatString("signature=\"%s\"", signature);
        String secretIdHeader  = Util.formatString("secret_id=\"%s\"", secretId);
        String idHeader        = Util.formatString("headers_id=\"%s\"", headersId);
        String algorithmHeader = Util.formatString("algorithm=\"%s\"", algorithm != null ? algorithm : "adj1");
        String nativeVersionHeader = Util.formatString("native_version=\"%s\"", nativeVersion != null ? nativeVersion : "");

        String authorizationHeader = Util.formatString("Signature %s,%s,%s,%s,%s",
                signatureHeader, secretIdHeader, algorithmHeader, idHeader, nativeVersionHeader);

        logger.verbose("authorizationHeader: %s", authorizationHeader);

        return authorizationHeader;
    }

    private Map<String, String> getSignature(final Map<String, String> parameters,
                                             final String activityKindString,
                                             final String appSecret)
    {
        String activityKindName = "activity_kind";
        String activityKindValue = activityKindString;

        String createdAtName = "created_at";
        String createdAt = parameters.get(createdAtName);

        String deviceIdentifierName = getValidIdentifier(parameters);
        String deviceIdentifier = parameters.get(deviceIdentifierName);

        String sourceName = "source";
        String sourceValue = parameters.get(sourceName);

        String payloadName = "payload";
        String payloadValue = parameters.get(payloadName);

        Map<String, String> signatureParams = new HashMap<String, String>();

        signatureParams.put("app_secret", appSecret);
        signatureParams.put(createdAtName, createdAt);
        signatureParams.put(activityKindName, activityKindValue);
        signatureParams.put(deviceIdentifierName, deviceIdentifier);

        if (sourceValue != null) {
            signatureParams.put(sourceName, sourceValue);
        }

        if (payloadValue != null) {
            signatureParams.put(payloadName, payloadValue);
        }

        String fields = "";
        String clearSignature = "";

        for (Map.Entry<String, String> entry : signatureParams.entrySet())  {
            if (entry.getValue() != null) {
                fields += entry.getKey() + " ";
                clearSignature += entry.getValue();
            }
        }

        // Remove last empty space.
        fields = fields.substring(0, fields.length() - 1);

        HashMap<String, String> signature = new HashMap<String, String>();

        signature.put("clear_signature", clearSignature);
        signature.put("fields", fields);

        return signature;
    }

    private String getValidIdentifier(final Map<String, String> parameters) {
        String googleAdIdName = "gps_adid";
        String fireAdIdName = "fire_adid";
        String androidIdName = "android_id";
        String androidUUIDName= "android_uuid";

        if (parameters.get(googleAdIdName) != null) {
            return googleAdIdName;
        }
        if (parameters.get(fireAdIdName) != null) {
            return fireAdIdName;
        }
        if (parameters.get(androidIdName) != null) {
            return androidIdName;
        }
        if (parameters.get(androidUUIDName) != null) {
            return androidUUIDName;
        }

        return null;
    }

    private static String extractAppSecret(final Map<String, String> parameters) {
        return parameters.remove("app_secret");
    }

    private static String extractSecretId(final Map<String, String> parameters) {
        return parameters.remove("secret_id");
    }

    private static String extractSignature(final Map<String, String> parameters) {
        return parameters.remove("signature");
    }

    private static String extractAlgorithm(final Map<String, String> parameters) {
        return parameters.remove("algorithm");
    }

    private static String extractNativeVersion(final Map<String, String> parameters) {
        return parameters.remove("native_version");
    }

    private static String extractHeadersId(final Map<String, String> parameters) {
        return parameters.remove("headers_id");
    }

    private static String extractAdjSigningId(final Map<String, String> parameters) {
        return parameters.remove("adj_signing_id");
    }


}
