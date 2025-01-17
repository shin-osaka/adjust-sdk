//
//  ActivityPackage.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.osaka.sdk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ActivityPackage implements Serializable {
    private static final long serialVersionUID = -35935556512024097L;

    @SuppressWarnings("unchecked")
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("path", String.class),
            new ObjectStreamField("clientSdk", String.class),
            new ObjectStreamField("parameters", (Class<Map<String,String>>)(Class)Map.class),
            new ObjectStreamField("activityKind", ActivityKind.class),
            new ObjectStreamField("suffix", String.class),
            new ObjectStreamField("callbackParameters", (Class<Map<String,String>>)(Class)Map.class),
            new ObjectStreamField("partnerParameters", (Class<Map<String,String>>)(Class)Map.class),
            new ObjectStreamField("retryCount", int.class),
            new ObjectStreamField("firstErrorCode", int.class),
            new ObjectStreamField("lastErrorCode", int.class),
            new ObjectStreamField("waitBeforeSendTimeSeconds", double.class),
    };

    private transient int hashCode;

    // data
    private String path;
    private String clientSdk;
    private Map<String, String> parameters;

    // logs
    private ActivityKind activityKind = ActivityKind.UNKNOWN;
    private String suffix;

    // delay
    private Map<String, String> callbackParameters;
    private Map<String, String> partnerParameters;

    // callbacks
    private OnPurchaseVerificationFinishedListener purchaseVerificationCallback;

    private int retries;
    private long clickTimeInMilliseconds;
    private long clickTimeInSeconds;
    private long installBeginTimeInSeconds;
    private long clickTimeServerInSeconds;
    private long installBeginTimeServerInSeconds;
    private String installVersion;
    private Boolean googlePlayInstant;
    private Boolean isClick;
    private int retryCount;
    private int firstErrorCode;
    private int lastErrorCode;
    private double waitBeforeSendTimeSeconds;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClientSdk() {
        return clientSdk;
    }

    public void setClientSdk(String clientSdk) {
        this.clientSdk = clientSdk;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setCallbackParameters(Map<String, String> callbackParameters) {
        this.callbackParameters = callbackParameters;
    }

    public void setPartnerParameters(Map<String, String> partnerParameters) {
        this.partnerParameters = partnerParameters;
    }

    public ActivityKind getActivityKind() {
        return activityKind;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getRetries() {
        return retries;
    }
    public int increaseRetries() {
        retries++;
        return retries;
    }

    public long getClickTimeInMilliseconds() {
        return this.clickTimeInMilliseconds;
    }

    public void setClickTimeInMilliseconds(long clickTimeInMilliseconds) {
        this.clickTimeInMilliseconds = clickTimeInMilliseconds;
    }

    public long getClickTimeInSeconds() {
        return this.clickTimeInSeconds;
    }

    public void setClickTimeInSeconds(long clickTimeInSeconds) {
        this.clickTimeInSeconds = clickTimeInSeconds;
    }

    public long getInstallBeginTimeInSeconds() {
        return this.installBeginTimeInSeconds;
    }

    public void setInstallBeginTimeInSeconds(long installBeginTimeInSeconds) {
        this.installBeginTimeInSeconds = installBeginTimeInSeconds;
    }

    public long getClickTimeServerInSeconds() {
        return this.clickTimeServerInSeconds;
    }

    public void setClickTimeServerInSeconds(long clickTimeServerInSeconds) {
        this.clickTimeServerInSeconds = clickTimeServerInSeconds;
    }

    public long getInstallBeginTimeServerInSeconds() {
        return this.installBeginTimeServerInSeconds;
    }

    public void setInstallBeginTimeServerInSeconds(long installBeginTimeServerInSeconds) {
        this.installBeginTimeServerInSeconds = installBeginTimeServerInSeconds;
    }

    public String getInstallVersion() {
        return this.installVersion;
    }

    public void setInstallVersion(String installVersion) {
        this.installVersion = installVersion;
    }

    public Boolean getGooglePlayInstant() {
        return this.googlePlayInstant;
    }

    public void setGooglePlayInstant(Boolean googlePlayInstant) {
        this.googlePlayInstant = googlePlayInstant;
    }

    public Boolean getIsClick() {
        return this.isClick;
    }

    public void setIsClick(Boolean isClick) {
        this.isClick = isClick;
    }
    
    public OnPurchaseVerificationFinishedListener getPurchaseVerificationCallback() {
        return this.purchaseVerificationCallback;
    }

    public void setPurchaseVerificationCallback(final OnPurchaseVerificationFinishedListener callback) {
        this.purchaseVerificationCallback = callback;
    }

    public Map<String, String> getCallbackParameters() {
        return callbackParameters;
    }

    public Map<String, String> getPartnerParameters() {
        return partnerParameters;
    }

    public ActivityPackage(ActivityKind activityKind) {
        this.activityKind = activityKind;
    }

    public String toString() {
        return Util.formatString("%s%s", activityKind.toString(), suffix);
    }

    public String getExtendedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Util.formatString("Path:      %s\n", path));
        builder.append(Util.formatString("ClientSdk: %s\n", clientSdk));

        if (parameters != null) {
            builder.append("Parameters:");
            SortedMap<String,String> sortedParameters = new TreeMap<String,String>(parameters);
            List<String> stringsToExclude = Arrays.asList("app_secret", "secret_id", "adj_signing_id");
            for (Map.Entry<String,String> entry : sortedParameters.entrySet() ) {
                String key = entry.getKey();
                if (stringsToExclude.contains(key)) {
                    continue;
                }
                builder.append(Util.formatString("\n\t%-16s %s", key,  entry.getValue()));
            }
        }
        return builder.toString();
    }

    public String getFailureMessage() {
        return Util.formatString("Failed to track %s%s", activityKind.toString(), suffix);
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getFirstErrorCode() {
        return firstErrorCode;
    }

    public int getLastErrorCode() {
        return lastErrorCode;
    }

    public double getWaitBeforeSendTimeSeconds() {
        return waitBeforeSendTimeSeconds;
    }

    public void setWaitBeforeSendTimeSeconds(double waitSeconds) {
        waitBeforeSendTimeSeconds = waitSeconds;
    }

    public void addError(int errorCode) {
        retryCount++;
        if (firstErrorCode == 0) {
            firstErrorCode = errorCode;
        } else {
            lastErrorCode = errorCode;
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField fields = stream.readFields();

        path = Util.readStringField(fields, "path", null);
        clientSdk = Util.readStringField(fields, "clientSdk", null);
        parameters = Util.readObjectField(fields, "parameters", null);
        activityKind = Util.readObjectField(fields, "activityKind", ActivityKind.UNKNOWN);
        suffix = Util.readStringField(fields, "suffix", null);
        callbackParameters = Util.readObjectField(fields, "callbackParameters", null);
        partnerParameters = Util.readObjectField(fields, "partnerParameters", null);
        retryCount = Util.readIntField(fields, "errorCount", 0);
        firstErrorCode = Util.readIntField(fields, "firstErrorCode", 0);
        lastErrorCode = Util.readIntField(fields, "lastErrorCode", 0);
        waitBeforeSendTimeSeconds = Util.readDoubleField(fields, "waitBeforeSendTimeSeconds", 0.0);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        ActivityPackage otherActivityPackage = (ActivityPackage) other;

        if (!Util.equalString(path, otherActivityPackage.path))         return false;
        if (!Util.equalString(clientSdk, otherActivityPackage.clientSdk))    return false;
        if (!Util.equalObject(parameters, otherActivityPackage.parameters))   return false;
        if (!Util.equalEnum(activityKind, otherActivityPackage.activityKind)) return false;
        if (!Util.equalString(suffix, otherActivityPackage.suffix))       return false;
        if (!Util.equalObject(callbackParameters, otherActivityPackage.callbackParameters))   return false;
        if (!Util.equalObject(partnerParameters, otherActivityPackage.partnerParameters))   return false;
        if (!Util.equalInt(retryCount, otherActivityPackage.retryCount))   return false;
        if (!Util.equalInt(firstErrorCode, otherActivityPackage.firstErrorCode))   return false;
        if (!Util.equalInt(lastErrorCode, otherActivityPackage.lastErrorCode))   return false;
        if (!Util.equalsDouble(waitBeforeSendTimeSeconds, otherActivityPackage.waitBeforeSendTimeSeconds)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = 17;
            hashCode = Util.hashString(path, hashCode);
            hashCode = Util.hashString(clientSdk, hashCode);
            hashCode = Util.hashObject(parameters, hashCode);
            hashCode = Util.hashEnum(activityKind, hashCode);
            hashCode = Util.hashString(suffix, hashCode);
            hashCode = Util.hashObject(callbackParameters, hashCode);
            hashCode = Util.hashObject(partnerParameters, hashCode);
            hashCode = 37 * hashCode + retryCount;
            hashCode = 37 * hashCode + firstErrorCode;
            hashCode = 37 * hashCode + lastErrorCode;
            hashCode = Util.hashDouble(waitBeforeSendTimeSeconds, hashCode);
        }
        return hashCode;
    }
}
