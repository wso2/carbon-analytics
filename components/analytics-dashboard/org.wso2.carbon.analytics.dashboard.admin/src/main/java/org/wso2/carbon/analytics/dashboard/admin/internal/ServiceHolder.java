package org.wso2.carbon.analytics.dashboard.admin.internal;

import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;

public class ServiceHolder {

    private static SecureAnalyticsDataService analyticsDataService;

    public static void setAnalyticsDataService(SecureAnalyticsDataService analyticsDataService) {
        ServiceHolder.analyticsDataService = analyticsDataService;
    }

    public static SecureAnalyticsDataService getAnalyticsDataService() {
        return analyticsDataService;
    }
}