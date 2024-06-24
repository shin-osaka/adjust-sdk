package com.osaka.testapp;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.osaka.sdk.Adjust;
import com.osaka.sdk.AdjustAdRevenue;
import com.osaka.sdk.AdjustAttribution;
import com.osaka.sdk.AdjustConfig;
import com.osaka.sdk.AdjustEvent;
import com.osaka.sdk.AdjustEventFailure;
import com.osaka.sdk.AdjustEventSuccess;
import com.osaka.sdk.AdjustPurchase;
import com.osaka.sdk.AdjustPurchaseVerificationResult;
import com.osaka.sdk.AdjustSessionFailure;
import com.osaka.sdk.AdjustSessionSuccess;
import com.osaka.sdk.AdjustPlayStoreSubscription;
import com.osaka.sdk.AdjustTestOptions;
import com.osaka.sdk.AdjustThirdPartySharing;
import com.osaka.sdk.LogLevel;
import com.osaka.sdk.OnAttributionChangedListener;
import com.osaka.sdk.OnDeeplinkResponseListener;
import com.osaka.sdk.OnEventTrackingFailedListener;
import com.osaka.sdk.OnEventTrackingSucceededListener;
import com.osaka.sdk.OnDeeplinkResolvedListener;
import com.osaka.sdk.OnPurchaseVerificationFinishedListener;
import com.osaka.sdk.OnSessionTrackingFailedListener;
import com.osaka.sdk.OnSessionTrackingSucceededListener;
import com.osaka.test_options.TestConnectionOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.osaka.testapp.MainActivity.baseUrl;
import static com.osaka.testapp.MainActivity.gdprUrl;

/**
 * Created by nonelse on 10.03.17.
 */

public class AdjustCommandExecutor {
    private static final String TAG = "AdjustCommandExecutor";
    private Context context;
    private String basePath;
    private String gdprPath;
    private String subscriptionPath;
    private String purchaseVerificationPath;
    private SparseArray<AdjustEvent> savedEvents = new SparseArray<>();
    private SparseArray<AdjustConfig> savedConfigs = new SparseArray<>();
    private Command command;

    public AdjustCommandExecutor(Context context) {
        this.context = context;
    }

    public void executeCommand(final Command sentCommand) {
        new Handler(Looper.getMainLooper()).post(() -> {
            command = sentCommand;
            switch (sentCommand.methodName) {
                // case "factory": factory(); break;
                case "testOptions": testOptions(); break;
                case "config": config(); break;
                case "start": start(); break;
                case "event": event(); break;
                case "trackEvent": trackEvent(); break;
                case "resume": resume(); break;
                case "pause": pause(); break;
                case "setEnabled": setEnabled(); break;
                case "setReferrer": setReferrer(); break;
                case "setOfflineMode": setOfflineMode(); break;
                case "sendFirstPackages": sendFirstPackages(); break;
                case "addSessionCallbackParameter": addSessionCallbackParameter(); break;
                case "addSessionPartnerParameter": addSessionPartnerParameter(); break;
                case "removeSessionCallbackParameter": removeSessionCallbackParameter(); break;
                case "removeSessionPartnerParameter": removeSessionPartnerParameter(); break;
                case "resetSessionCallbackParameters": resetSessionCallbackParameters(); break;
                case "resetSessionPartnerParameters": resetSessionPartnerParameters(); break;
                case "setPushToken": setPushToken(); break;
                // case "teardown": teardown(); break;
                case "openDeeplink": openDeeplink(); break;
                case "sendReferrer": sendReferrer(); break;
                case "gdprForgetMe": gdprForgetMe(); break;
                case "disableThirdPartySharing": disableThirdPartySharing(); break;
                case "thirdPartySharing" : thirdPartySharing(); break;
                case "measurementConsent" : measurementConsent(); break;
                case "trackAdRevenue": trackAdRevenue(); break;
                case "trackAdRevenueV2" : trackAdRevenueV2(); break;
                case "trackSubscription": trackSubscription(); break;
                case "verifyPurchase": verifyPurchase(); break;
                case "processDeeplink" : processDeeplink(); break;
                //case "testBegin": testBegin(); break;
                // case "testEnd": testEnd(); break;
            }
        });
    }
/*
    private void factory() {
        if (command.containsParameter("basePath")) {
            this.basePath = command.getFirstParameterValue("basePath");
        }
        if (command.containsParameter("timerInterval")) {
            long timerInterval = Long.parseLong(command.getFirstParameterValue("timerInterval"));
            AdjustFactory.setTimerInterval(timerInterval);
        }
        if (command.containsParameter("timerStart")) {
            long timerStart = Long.parseLong(command.getFirstParameterValue("timerStart"));
            AdjustFactory.setTimerStart(timerStart);
        }
        if (command.containsParameter("sessionInterval")) {
            long sessionInterval = Long.parseLong(command.getFirstParameterValue("sessionInterval"));
            AdjustFactory.setSessionInterval(sessionInterval);
        }
        if (command.containsParameter("subsessionInterval")) {
            long subsessionInterval = Long.parseLong(command.getFirstParameterValue("subsessionInterval"));
            AdjustFactory.setSubsessionInterval(subsessionInterval);
        }
    }
*/

    private void testOptions() {
        AdjustTestOptions testOptions = new AdjustTestOptions();
        testOptions.baseUrl = baseUrl;
        testOptions.gdprUrl = gdprUrl;
        testOptions.subscriptionUrl = baseUrl; // TODO: for now, consider making it separate
        testOptions.purchaseVerificationUrl = baseUrl; // TODO: for now, consider making it separate
        if (command.containsParameter("basePath")) {
            basePath = command.getFirstParameterValue("basePath");
            gdprPath = command.getFirstParameterValue("basePath");
            subscriptionPath = command.getFirstParameterValue("basePath");
            purchaseVerificationPath = command.getFirstParameterValue("basePath");
        }
        if (command.containsParameter("timerInterval")) {
            long timerInterval = Long.parseLong(command.getFirstParameterValue("timerInterval"));
            testOptions.timerIntervalInMilliseconds = timerInterval;
        }
        if (command.containsParameter("timerStart")) {
            long timerStart = Long.parseLong(command.getFirstParameterValue("timerStart"));
            testOptions.timerStartInMilliseconds = timerStart;
        }
        if (command.containsParameter("sessionInterval")) {
            long sessionInterval = Long.parseLong(command.getFirstParameterValue("sessionInterval"));
            testOptions.sessionIntervalInMilliseconds = sessionInterval;
        }
        if (command.containsParameter("subsessionInterval")) {
            long subsessionInterval = Long.parseLong(command.getFirstParameterValue("subsessionInterval"));
            testOptions.subsessionIntervalInMilliseconds = subsessionInterval;
        }
        if (command.containsParameter("tryInstallReferrer")) {
            String tryInstallReferrerString = command.getFirstParameterValue("tryInstallReferrer");
            Boolean tryInstallReferrerBoolean = Util.strictParseStringToBoolean(tryInstallReferrerString);
            if (tryInstallReferrerBoolean != null) {
                testOptions.tryInstallReferrer = tryInstallReferrerBoolean;
            }
        }
        if (command.containsParameter("noBackoffWait")) {
            String noBackoffWaitString = command.getFirstParameterValue("noBackoffWait");
            Boolean noBackoffWaitBoolean = Util.strictParseStringToBoolean(noBackoffWaitString);
            if (noBackoffWaitBoolean != null) {
                testOptions.noBackoffWait = noBackoffWaitBoolean;
            }
        }
        boolean useTestConnectionOptions = false;
        if (command.containsParameter("teardown")) {
            List<String> teardownOptions = command.parameters.get("teardown");
            for (String teardownOption : teardownOptions) {
                if (teardownOption.equals("resetSdk")) {
                    testOptions.teardown = true;
                    testOptions.basePath = basePath;
                    testOptions.gdprPath = gdprPath;
                    testOptions.subscriptionPath = subscriptionPath;
                    testOptions.purchaseVerificationPath = purchaseVerificationPath;
                    useTestConnectionOptions = true;
                    testOptions.tryInstallReferrer = false;
                }
                if (teardownOption.equals("deleteState")) {
                    testOptions.context = this.context;
                }
                if (teardownOption.equals("resetTest")) {
                    savedEvents.clear();
                    savedConfigs.clear();
                    testOptions.timerIntervalInMilliseconds = (long) -1;
                    testOptions.timerStartInMilliseconds = (long) -1;
                    testOptions.sessionIntervalInMilliseconds = (long) -1;
                    testOptions.subsessionIntervalInMilliseconds = (long) -1;
                }
                if (teardownOption.equals("sdk")) {
                    testOptions.teardown = true;
                    testOptions.basePath = null;
                    testOptions.gdprPath = null;
                    testOptions.subscriptionPath = null;
                    testOptions.purchaseVerificationPath = null;
                }
                if (teardownOption.equals("test")) {
                    savedEvents = null;
                    savedConfigs = null;
                    testOptions.timerIntervalInMilliseconds = (long) -1;
                    testOptions.timerStartInMilliseconds = (long) -1;
                    testOptions.sessionIntervalInMilliseconds = (long) -1;
                    testOptions.subsessionIntervalInMilliseconds = (long) -1;
                }
            }
        }
        Adjust.setTestOptions(testOptions);
        if (useTestConnectionOptions) {
            TestConnectionOptions.setTestConnectionOptions();
        }
    }

    private void config() {
        int configNumber = 0;
        if (command.parameters.containsKey("configName")) {
            String configName = command.getFirstParameterValue("configName");
            configNumber = Integer.parseInt(configName.substring(configName.length() - 1));
        }

        AdjustConfig adjustConfig;

        if (savedConfigs.indexOfKey(configNumber) >= 0) {
            adjustConfig = savedConfigs.get(configNumber);
        } else {
            String environment = command.getFirstParameterValue("environment");
            String appToken = command.getFirstParameterValue("appToken");
            Context context = this.context;
            if ("null".equalsIgnoreCase(command.getFirstParameterValue("context"))) {
                context = null;
            }
            adjustConfig = new AdjustConfig(context, appToken, environment);
//            String logLevel = command.getFirstParameterValue("logLevel");
//            adjustConfig.setLogLevel(LogLevel.valueOf(logLevel));
            adjustConfig.setLogLevel(LogLevel.VERBOSE);

            savedConfigs.put(configNumber, adjustConfig);
        }

        if (command.containsParameter("logLevel")) {
            String logLevelS = command.getFirstParameterValue("logLevel");
            LogLevel logLevel = null;
            switch (logLevelS) {
                case "verbose": logLevel = LogLevel.VERBOSE;
                    break;
                case "debug": logLevel = LogLevel.DEBUG;
                    break;
                case "info": logLevel = LogLevel.INFO;
                    break;
                case "warn": logLevel = LogLevel.WARN;
                    break;
                case "error": logLevel = LogLevel.ERROR;
                    break;
                case "assert": logLevel = LogLevel.ASSERT;
                    break;
                case "suppress": logLevel = LogLevel.SUPRESS;
                    break;
            }
            Log.d("TestApp", logLevel.toString());
            adjustConfig.setLogLevel(logLevel);
        }

        if (command.containsParameter("sdkPrefix")) {
            String sdkPrefix = command.getFirstParameterValue("sdkPrefix");
            adjustConfig.setSdkPrefix(sdkPrefix);
        }

        if (command.containsParameter("defaultTracker")) {
            String defaultTracker = command.getFirstParameterValue("defaultTracker");
            adjustConfig.setDefaultTracker(defaultTracker);
        }

//        if (command.containsParameter("externalDeviceId")) {
//            String externalDeviceId = command.getFirstParameterValue("externalDeviceId");
//            adjustConfig.setExternalDeviceId(externalDeviceId);
//        }


        if (command.parameters.containsKey("appSecret")) {
            List<String> appSecretArray = command.parameters.get("appSecret");
            try {
                long secretId = Long.parseLong(appSecretArray.get(0));
                long info1 = Long.parseLong(appSecretArray.get(1));
                long info2 = Long.parseLong(appSecretArray.get(2));
                long info3 = Long.parseLong(appSecretArray.get(3));
                long info4 = Long.parseLong(appSecretArray.get(4));
                adjustConfig.setAppSecret(secretId, info1, info2, info3, info4);
            } catch (Exception ignored) {

            }
        }

        if (command.containsParameter("delayStart")) {
            String delayStartS = command.getFirstParameterValue("delayStart");
            double delayStart = Double.parseDouble(delayStartS);
            adjustConfig.setDelayStart(delayStart);
        }

        if (command.containsParameter("deviceKnown")) {
            String deviceKnownS = command.getFirstParameterValue("deviceKnown");
            boolean deviceKnown = "true".equals(deviceKnownS);
            adjustConfig.setDeviceKnown(deviceKnown);
        }

        if (command.containsParameter("needsCost")) {
            String needsCostS = command.getFirstParameterValue("needsCost");
            boolean needsCost = "true".equals(needsCostS);
            adjustConfig.setNeedsCost(needsCost);
        }

        if (command.containsParameter("eventBufferingEnabled")) {
            String eventBufferingEnabledS = command.getFirstParameterValue("eventBufferingEnabled");
            boolean eventBufferingEnabled = "true".equals(eventBufferingEnabledS);
            adjustConfig.setEventBufferingEnabled(eventBufferingEnabled);
        }

        if (command.containsParameter("sendInBackground")) {
            String sendInBackgroundS = command.getFirstParameterValue("sendInBackground");
            boolean sendInBackground = "true".equals(sendInBackgroundS);
            adjustConfig.setSendInBackground(sendInBackground);
        }

        if (command.containsParameter("userAgent")) {
            String userAgent = command.getFirstParameterValue("userAgent");
            adjustConfig.setUserAgent(userAgent);
        }

        if (command.containsParameter("externalDeviceId")) {
            String externalDeviceId = command.getFirstParameterValue("externalDeviceId");
            adjustConfig.setExternalDeviceId(externalDeviceId);
        }

        if (command.containsParameter("coppaCompliant")) {
            String coppaCompliantS = command.getFirstParameterValue("coppaCompliant");
            boolean coppaCompliant = "true".equals(coppaCompliantS);
            adjustConfig.setCoppaCompliantEnabled(coppaCompliant);
        }

        if (command.containsParameter("playStoreKids")) {
            String playStoreKidsS = command.getFirstParameterValue("playStoreKids");
            boolean playStoreKids = "true".equals(playStoreKidsS);
            adjustConfig.setPlayStoreKidsAppEnabled(playStoreKids);
        }

        if (command.containsParameter("finalAttributionEnabled")) {
            String finalAttributionEnabledS = command.getFirstParameterValue("finalAttributionEnabled");
            boolean finalAttributionEnabled = "true".equals(finalAttributionEnabledS);
            adjustConfig.setFinalAttributionEnabled(finalAttributionEnabled);
        }

        if(command.containsParameter("deferredDeeplinkCallback")) {
            adjustConfig.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
                @Override
                public boolean launchReceivedDeeplink(Uri deeplink) {
                    if (deeplink == null) {
                        Log.d("TestApp", "Deeplink Response, uri = null");
                        return false;
                    }

                    Log.d("TestApp", "Deeplink Response, uri = " + deeplink.toString());

                    return deeplink.toString().startsWith("adjusttest");
                }
            });
        }

        if (command.containsParameter("attributionCallbackSendAll")) {
            final String localBasePath = basePath;
            adjustConfig.setOnAttributionChangedListener(new OnAttributionChangedListener() {
                @Override
                public void onAttributionChanged(AdjustAttribution attribution) {
                    Log.d("TestApp", "attribution = " + attribution.toString());

                    MainActivity.testLibrary.setInfoToSend(attribution.toMap());
                    MainActivity.testLibrary.sendInfoToServer(localBasePath);
                }
            });
        }

        if (command.containsParameter("sessionCallbackSendSuccess")) {
            final String localBasePath = basePath;
            adjustConfig.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
                @Override
                public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                    Log.d("TestApp", "session_success = " + sessionSuccessResponseData.toString());

                    MainActivity.testLibrary.addInfoToSend("message", sessionSuccessResponseData.message);
                    MainActivity.testLibrary.addInfoToSend("timestamp", sessionSuccessResponseData.timestamp);
                    MainActivity.testLibrary.addInfoToSend("adid", sessionSuccessResponseData.adid);
                    if (sessionSuccessResponseData.jsonResponse != null) {
                        MainActivity.testLibrary.addInfoToSend("jsonResponse", sessionSuccessResponseData.jsonResponse.toString());
                    }
                    MainActivity.testLibrary.sendInfoToServer(localBasePath);
                }
            });
        }

        if (command.containsParameter("sessionCallbackSendFailure")) {
            final String localBasePath = basePath;
            adjustConfig.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
                @Override
                public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                    Log.d("TestApp", "session_fail = " + sessionFailureResponseData.toString());

                    MainActivity.testLibrary.addInfoToSend("message", sessionFailureResponseData.message);
                    MainActivity.testLibrary.addInfoToSend("timestamp", sessionFailureResponseData.timestamp);
                    MainActivity.testLibrary.addInfoToSend("adid", sessionFailureResponseData.adid);
                    MainActivity.testLibrary.addInfoToSend("willRetry", String.valueOf(sessionFailureResponseData.willRetry));
                    if (sessionFailureResponseData.jsonResponse != null) {
                        MainActivity.testLibrary.addInfoToSend("jsonResponse", sessionFailureResponseData.jsonResponse.toString());
                    }
                    MainActivity.testLibrary.sendInfoToServer(localBasePath);
                }
            });
        }

        if (command.containsParameter("eventCallbackSendSuccess")) {
            final String localBasePath = basePath;
            adjustConfig.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
                @Override
                public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                    Log.d("TestApp", "event_success = " + eventSuccessResponseData.toString());

                    MainActivity.testLibrary.addInfoToSend("message", eventSuccessResponseData.message);
                    MainActivity.testLibrary.addInfoToSend("timestamp", eventSuccessResponseData.timestamp);
                    MainActivity.testLibrary.addInfoToSend("adid", eventSuccessResponseData.adid);
                    MainActivity.testLibrary.addInfoToSend("eventToken", eventSuccessResponseData.eventToken);
                    MainActivity.testLibrary.addInfoToSend("callbackId", eventSuccessResponseData.callbackId);
                    if (eventSuccessResponseData.jsonResponse != null ) {
                        MainActivity.testLibrary.addInfoToSend("jsonResponse", eventSuccessResponseData.jsonResponse.toString());
                    }
                    MainActivity.testLibrary.sendInfoToServer(localBasePath);
                }
            });
        }

        if (command.containsParameter("eventCallbackSendFailure")) {
            final String localBasePath = basePath;
            adjustConfig.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
                @Override
                public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                    Log.d("TestApp", "event_fail = " + eventFailureResponseData.toString());

                    MainActivity.testLibrary.addInfoToSend("message", eventFailureResponseData.message);
                    MainActivity.testLibrary.addInfoToSend("timestamp", eventFailureResponseData.timestamp);
                    MainActivity.testLibrary.addInfoToSend("adid", eventFailureResponseData.adid);
                    MainActivity.testLibrary.addInfoToSend("eventToken", eventFailureResponseData.eventToken);
                    MainActivity.testLibrary.addInfoToSend("callbackId", eventFailureResponseData.callbackId);
                    MainActivity.testLibrary.addInfoToSend("willRetry", String.valueOf(eventFailureResponseData.willRetry));
                    if (eventFailureResponseData.jsonResponse != null) {
                        MainActivity.testLibrary.addInfoToSend("jsonResponse", eventFailureResponseData.jsonResponse.toString());
                    }
                    MainActivity.testLibrary.sendInfoToServer(localBasePath);
                }
            });
        }

        if (command.containsParameter("deferredDeeplinkCallback")) {
            String launchDeferredDeeplinkS = command.getFirstParameterValue("deferredDeeplinkCallback");
            final boolean launchDeferredDeeplink = "true".equals(launchDeferredDeeplinkS);
            final String localBasePath = basePath;
            adjustConfig.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
                @Override
                public boolean launchReceivedDeeplink(Uri deeplink) {
                    Log.d("TestApp", "deferred_deep_link = " + deeplink.toString());
                    MainActivity.testLibrary.addInfoToSend("deeplink", deeplink.toString());
                    MainActivity.testLibrary.sendInfoToServer(localBasePath);
                    return launchDeferredDeeplink;
                }
            });
        }
    }

    private void start() {
        config();
        int configNumber = 0;
        if (command.parameters.containsKey("configName")) {
            String configName = command.getFirstParameterValue("configName");
            configNumber = Integer.parseInt(configName.substring(configName.length() - 1));
        }

        AdjustConfig adjustConfig = savedConfigs.get(configNumber);

        //adjustConfig.setBasePath(basePath);
        Adjust.onCreate(adjustConfig);

        this.savedConfigs.remove(0);
    }

    private void event() throws NullPointerException {
        int eventNumber = 0;
        if (command.parameters.containsKey("eventName")) {
            String eventName = command.getFirstParameterValue("eventName");
            eventNumber = Integer.parseInt(eventName.substring(eventName.length() - 1));
        }

        AdjustEvent adjustEvent;
        if (savedEvents.indexOfKey(eventNumber) >= 0) {
            adjustEvent = savedEvents.get(eventNumber);
        } else {
            String eventToken = command.getFirstParameterValue("eventToken");
            adjustEvent = new AdjustEvent(eventToken);
            savedEvents.put(eventNumber, adjustEvent);
        }

        if (command.parameters.containsKey("revenue")) {
            List<String> revenueParams = command.parameters.get("revenue");
            String currency = revenueParams.get(0);
            double revenue = Double.parseDouble(revenueParams.get(1));
            adjustEvent.setRevenue(revenue, currency);
        }

        if (command.parameters.containsKey("callbackParams")) {
            List<String> callbackParams = command.parameters.get("callbackParams");
            for (int i = 0; i < callbackParams.size(); i = i + 2) {
                String key = callbackParams.get(i);
                String value = callbackParams.get(i + 1);
                adjustEvent.addCallbackParameter(key, value);
            }
        }
        if (command.parameters.containsKey("partnerParams")) {
            List<String> partnerParams = command.parameters.get("partnerParams");
            for (int i = 0; i < partnerParams.size(); i = i + 2) {
                String key = partnerParams.get(i);
                String value = partnerParams.get(i + 1);
                adjustEvent.addPartnerParameter(key, value);
            }
        }
        if (command.parameters.containsKey("orderId")) {
            String orderId = command.getFirstParameterValue("orderId");
            adjustEvent.setOrderId(orderId);
        }
        if (command.parameters.containsKey("callbackId")) {
            String callbackId = command.getFirstParameterValue("callbackId");
            adjustEvent.setCallbackId(callbackId);
        }
        if (command.parameters.containsKey("productId")) {
            String productId = command.getFirstParameterValue("productId");
            adjustEvent.setProductId(productId);
        }
        if (command.parameters.containsKey("purchaseToken")) {
            String purchaseToken = command.getFirstParameterValue("purchaseToken");
            adjustEvent.setPurchaseToken(purchaseToken);
        }

//        Adjust.trackEvent(adjustEvent);
    }

    private void trackEvent() {
        event();
        int eventNumber = 0;
        if (command.parameters.containsKey("eventName")) {
            String eventName = command.getFirstParameterValue("eventName");
            eventNumber = Integer.parseInt(eventName.substring(eventName.length() - 1));
        }

        AdjustEvent adjustEvent = savedEvents.get(eventNumber);
        Adjust.trackEvent(adjustEvent);

        this.savedEvents.remove(0);
    }

    private void setReferrer() {
        String referrer = command.getFirstParameterValue("referrer");
        Adjust.setReferrer(referrer, this.context);
    }

    private void pause() {
        Adjust.onPause();
    }

    private void resume() {
        Adjust.onResume();
    }

    private void setEnabled() {
        Boolean enabled = Boolean.valueOf(command.getFirstParameterValue("enabled"));
        Adjust.setEnabled(enabled);
    }

    private void setOfflineMode() {
        Boolean enabled = Boolean.valueOf(command.getFirstParameterValue("enabled"));
        Adjust.setOfflineMode(enabled);
    }

    private void sendFirstPackages() {
        Adjust.sendFirstPackages();
    }

    private void addSessionCallbackParameter() {
        if (command.containsParameter("KeyValue")) {
            List<String> keyValuePairs = command.parameters.get("KeyValue");
            for (int i = 0; i<keyValuePairs.size() ; i = i+2) {
                String key = keyValuePairs.get(i);
                String value = keyValuePairs.get(i+1);
                Adjust.addSessionCallbackParameter(key, value);
            }
        }
    }

    private void addSessionPartnerParameter() {
        if (command.containsParameter("KeyValue")) {
            List<String> keyValuePairs = command.parameters.get("KeyValue");
            for (int i = 0; i<keyValuePairs.size() ; i = i+2) {
                String key = keyValuePairs.get(i);
                String value = keyValuePairs.get(i+1);
                Adjust.addSessionPartnerParameter(key, value);
            }
        }
    }

    private void removeSessionCallbackParameter() {
        if (command.containsParameter("key")) {
            List<String> keys = command.parameters.get("key");
            for (int i = 0; i<keys.size() ; i = i+1) {
                String key = keys.get(i);
                Adjust.removeSessionCallbackParameter(key);
            }
        }
    }

    private void removeSessionPartnerParameter() {
        if (command.containsParameter("key")) {
            List<String> keys = command.parameters.get("key");
            for (int i = 0; i<keys.size() ; i = i+1) {
                String key = keys.get(i);
                Adjust.removeSessionPartnerParameter(key);
            }
        }
    }

    private void resetSessionCallbackParameters() {
        Adjust.resetSessionCallbackParameters();
    }

    private void resetSessionPartnerParameters() {
        Adjust.resetSessionPartnerParameters();
    }

    private void setPushToken() {
        String token = command.getFirstParameterValue("pushToken");

        Adjust.setPushToken(token, context);
    }
/*
    private void teardown() throws NullPointerException {
        String deleteStateString = command.getFirstParameterValue("deleteState");
        boolean deleteState = Boolean.parseBoolean(deleteStateString);

        Log.d("TestApp", "calling teardown with delete state");
        AdjustFactory.teardown(this.context, deleteState);
    }
*/
    private void openDeeplink() {
        String deeplink = command.getFirstParameterValue("deeplink");

        Adjust.appWillOpenUrl(Uri.parse(deeplink), this.context);
    }

    private void sendReferrer() {
        String referrer = command.getFirstParameterValue("referrer");

        Adjust.setReferrer(referrer, this.context);
    }

    private void gdprForgetMe() {
        Adjust.gdprForgetMe(this.context);
    }

    private void disableThirdPartySharing() {
        Adjust.disableThirdPartySharing(this.context);
    }

    private void thirdPartySharing() {
        String isEnabledString =
                command.getFirstParameterValue("isEnabled");
        Boolean isEnabledBoolean =
                Util.strictParseStringToBoolean(isEnabledString);

        AdjustThirdPartySharing adjustThirdPartySharing =
                new AdjustThirdPartySharing(isEnabledBoolean);

        if (command.parameters.containsKey("granularOptions")) {
            List<String> granularOptions = command.parameters.get("granularOptions");
            for (int i = 0; i < granularOptions.size(); i = i + 3) {
                String partnerName = granularOptions.get(i);
                String key = granularOptions.get(i + 1);
                String value = granularOptions.get(i + 2);
                adjustThirdPartySharing.addGranularOption(partnerName, key, value);
            }
        }

        if (command.parameters.containsKey("partnerSharingSettings")) {
            List<String> partnerSharingSettings = command.parameters.get("partnerSharingSettings");
            for (int i = 0; i < partnerSharingSettings.size(); i = i + 3) {
                String partnerName = partnerSharingSettings.get(i);
                String key = partnerSharingSettings.get(i + 1);
                String valueString = partnerSharingSettings.get(i + 2);
                Boolean valueBoolean = Util.strictParseStringToBoolean(valueString);
                if (valueBoolean != null) {
                    adjustThirdPartySharing.addPartnerSharingSetting(partnerName, key, valueBoolean);
                }
            }
        }

        Adjust.trackThirdPartySharing(adjustThirdPartySharing);
    }

    private void measurementConsent() {
        String measurementConsentString =
                command.getFirstParameterValue("isEnabled");
        boolean measurementConsent = "true".equals(measurementConsentString);

        Adjust.trackMeasurementConsent(measurementConsent);
    }

    private void trackAdRevenue() {
        String adRevenueSource = command.getFirstParameterValue("adRevenueSource");
        String adRevenueJsonString = command.getFirstParameterValue("adRevenueJsonString");

        try {
            JSONObject adRevenueJson = new JSONObject(adRevenueJsonString);
            Adjust.trackAdRevenue(adRevenueSource, adRevenueJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void trackAdRevenueV2() {
        String adRevenueSource = command.getFirstParameterValue("adRevenueSource");
        AdjustAdRevenue adjustAdRevenue = new AdjustAdRevenue(adRevenueSource);

        if (command.parameters.containsKey("revenue")) {
            List<String> revenueParams = command.parameters.get("revenue");
            String currency = revenueParams.get(0);
            Double revenue = Double.valueOf(revenueParams.get(1));
            adjustAdRevenue.setRevenue(revenue, currency);
        }

        if (command.parameters.containsKey("adImpressionsCount")) {
            Integer adImpressionsCount =
                    Integer.valueOf(command.getFirstParameterValue("adImpressionsCount"));
            adjustAdRevenue.setAdImpressionsCount(adImpressionsCount);
        }

        if (command.parameters.containsKey("adRevenueNetwork")) {
            String adRevenueNetwork = command.getFirstParameterValue("adRevenueNetwork");
            adjustAdRevenue.setAdRevenueNetwork(adRevenueNetwork);
        }

        if (command.parameters.containsKey("adRevenueUnit")) {
            String adRevenueUnit = command.getFirstParameterValue("adRevenueUnit");
            adjustAdRevenue.setAdRevenueUnit(adRevenueUnit);
        }

        if (command.parameters.containsKey("adRevenuePlacement")) {
            String adRevenuePlacement = command.getFirstParameterValue("adRevenuePlacement");
            adjustAdRevenue.setAdRevenuePlacement(adRevenuePlacement);
        }

        if (command.parameters.containsKey("callbackParams")) {
            List<String> callbackParams = command.parameters.get("callbackParams");
            for (int i = 0; i < callbackParams.size(); i = i + 2) {
                String key = callbackParams.get(i);
                String value = callbackParams.get(i + 1);
                adjustAdRevenue.addCallbackParameter(key, value);
            }
        }
        if (command.parameters.containsKey("partnerParams")) {
            List<String> partnerParams = command.parameters.get("partnerParams");
            for (int i = 0; i < partnerParams.size(); i = i + 2) {
                String key = partnerParams.get(i);
                String value = partnerParams.get(i + 1);
                adjustAdRevenue.addPartnerParameter(key, value);
            }
        }

        Adjust.trackAdRevenue(adjustAdRevenue);
    }

    private void trackSubscription() {
        long price = Long.parseLong(command.getFirstParameterValue("revenue"));
        String currency = command.getFirstParameterValue("currency");
        long purchaseTime = Long.parseLong(command.getFirstParameterValue("transactionDate"));
        String sku = command.getFirstParameterValue("productId");
        String signature = command.getFirstParameterValue("receipt");
        String purchaseToken = command.getFirstParameterValue("purchaseToken");
        String orderId = command.getFirstParameterValue("transactionId");

        AdjustPlayStoreSubscription subscription = new AdjustPlayStoreSubscription(
                price,
                currency,
                sku,
                orderId,
                signature,
                purchaseToken);
        subscription.setPurchaseTime(purchaseTime);

        if (command.parameters.containsKey("callbackParams")) {
            List<String> callbackParams = command.parameters.get("callbackParams");
            for (int i = 0; i < callbackParams.size(); i = i + 2) {
                String key = callbackParams.get(i);
                String value = callbackParams.get(i + 1);
                subscription.addCallbackParameter(key, value);
            }
        }
        if (command.parameters.containsKey("partnerParams")) {
            List<String> partnerParams = command.parameters.get("partnerParams");
            for (int i = 0; i < partnerParams.size(); i = i + 2) {
                String key = partnerParams.get(i);
                String value = partnerParams.get(i + 1);
                subscription.addPartnerParameter(key, value);
            }
        }

        Adjust.trackPlayStoreSubscription(subscription);
    }

    private void verifyPurchase() {
        String sku = command.getFirstParameterValue("productId");
        String purchaseToken = command.getFirstParameterValue("purchaseToken");

        final String localBasePath = basePath;
        AdjustPurchase purchase = new AdjustPurchase(sku, purchaseToken);
        Adjust.verifyPurchase(purchase, new OnPurchaseVerificationFinishedListener() {
            @Override
            public void onVerificationFinished(AdjustPurchaseVerificationResult result) {
                MainActivity.testLibrary.addInfoToSend("verification_status", result.getVerificationStatus());
                MainActivity.testLibrary.addInfoToSend("code", String.valueOf(result.getCode()));
                MainActivity.testLibrary.addInfoToSend("message", result.getMessage());
                MainActivity.testLibrary.sendInfoToServer(localBasePath);
            }
        });
    }

    private void processDeeplink() {
        String deeplink = command.getFirstParameterValue("deeplink");
        Uri deeplinkUri = Uri.parse(deeplink);
        final String localBasePath = basePath;
        Adjust.processDeeplink(deeplinkUri, context, new OnDeeplinkResolvedListener() {
            @Override
            public void onDeeplinkResolved(String resolvedLink) {
                MainActivity.testLibrary.addInfoToSend("resolved_link", resolvedLink);
                MainActivity.testLibrary.sendInfoToServer(localBasePath);
            }
        });
    }
/*
    private void testBegin() {
        if (command.containsParameter("teardown")) {
            this.basePath = command.getFirstParameterValue("basePath");
        }

        AdjustTestOptions teardownOption = new AdjustTestOptions();
        teardownOption.teardown = true;
        teardownOption.context = this.context;

        AdjustFactory.teardown(this.context);
        AdjustFactory.setTimerInterval(-1);
        AdjustFactory.setTimerStart(-1);
        AdjustFactory.setSessionInterval(-1);
        AdjustFactory.setSubsessionInterval(-1);
        savedEvents = new HashMap<Integer, AdjustEvent>();
        savedConfigs = new HashMap<Integer, AdjustConfig>();
    }

    private void testEnd() {
        AdjustFactory.teardown(this.context, true);
    }
    */
}
