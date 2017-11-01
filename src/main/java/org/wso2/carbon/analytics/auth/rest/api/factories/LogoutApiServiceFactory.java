package org.wso2.carbon.analytics.auth.rest.api.factories;

import org.wso2.carbon.analytics.auth.rest.api.LogoutApiService;
import org.wso2.carbon.analytics.auth.rest.api.impl.LogoutApiServiceImpl;

/**
 * Factory for Logout API.
 */
public class LogoutApiServiceFactory {
    private static final LogoutApiService service = new LogoutApiServiceImpl();

    public static LogoutApiService getLogoutApi() {
        return service;
    }
}
