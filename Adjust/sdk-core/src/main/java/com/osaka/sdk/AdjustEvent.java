package com.osaka.sdk;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by pfms on 05/11/14.
 */
public class AdjustEvent {
    String eventToken;
    Double revenue;
    String currency;
    Map<String, String> callbackParameters;
    Map<String, String> partnerParameters;
    String orderId;
    String callbackId;
    String productId;
    String purchaseToken;

    private static ILogger logger = AdjustFactory.getLogger();

    public AdjustEvent(String eventToken) {
        if (!checkEventToken(eventToken, logger)) return;

        this.eventToken = eventToken;
    }

    public void setRevenue(double revenue, String currency) {
        if (!checkRevenue(revenue, currency)) return;

        this.revenue = revenue;
        this.currency = currency;
    }

    public void addCallbackParameter(String key, String value) {
        if (!Util.isValidParameter(key, "key", "Callback")) return;
        if (!Util.isValidParameter(value, "value", "Callback")) return;

        if (callbackParameters == null) {
            callbackParameters = new LinkedHashMap<String, String>();
        }

        String previousValue = callbackParameters.put(key, value);

        if (previousValue != null) {
            logger.warn("Key %s was overwritten", key);
        }
    }

    public void addPartnerParameter(String key, String value) {
        if (!Util.isValidParameter(key, "key", "Partner")) return;
        if (!Util.isValidParameter(value, "value", "Partner")) return;

        if (partnerParameters == null) {
            partnerParameters = new LinkedHashMap<String, String>();
        }

        String previousValue = partnerParameters.put(key, value);

        if (previousValue != null) {
            logger.warn("Key %s was overwritten", key);
        }
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public boolean isValid() {
        return eventToken != null;
    }

    public String getEventToken() {
        return eventToken;
    }

    public Double getRevenue() {
        return revenue;
    }

    public String getCurrency() {
        return currency;
    }

    public Map<String, String> getCallbackParameters() {
        return callbackParameters;
    }

    public Map<String, String> getPartnerParameters() {
        return partnerParameters;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public String getProductId() {
        return productId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    private static boolean checkEventToken(String eventToken, ILogger logger) {
        if (eventToken == null) {
            logger.error("Missing Event Token");
            return false;
        }
        if (eventToken.length() <= 0) {
            logger.error("Event Token can't be empty");
            return false;
        }
        return true;
    }

    private boolean checkRevenue(Double revenue, String currency) {
        if (revenue != null) {
            if (revenue < 0.0) {
                logger.error("Invalid amount %.5f", revenue);
                return false;
            }

            if (currency == null) {
                logger.error("Currency must be set with revenue");
                return false;
            }
            if (currency.equals("")) {
                logger.error("Currency is empty");
                return false;
            }

        } else if (currency != null) {
            logger.error("Revenue must be set with currency");
            return false;
        }
        return true;
    }
}
