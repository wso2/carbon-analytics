package org.wso2.carbon.analytics.auth.rest.api.factories;

import org.wso2.carbon.analytics.auth.rest.api.LoginApiService;
import org.wso2.carbon.analytics.auth.rest.api.impl.LoginApiServiceImpl;

/**
 * Factory for Login API.
 */
public class LoginApiServiceFactory {
    private static final LoginApiService service = new LoginApiServiceImpl();

    public static LoginApiService getLoginApi() {
        return service;
    }
}
