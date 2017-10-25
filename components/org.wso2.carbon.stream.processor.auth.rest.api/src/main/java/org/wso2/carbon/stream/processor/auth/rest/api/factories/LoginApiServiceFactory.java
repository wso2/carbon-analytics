package org.wso2.carbon.stream.processor.auth.rest.api.factories;

import org.wso2.carbon.stream.processor.auth.rest.api.LoginApiService;
import org.wso2.carbon.stream.processor.auth.rest.api.impl.LoginApiServiceImpl;

public class LoginApiServiceFactory {
    private static final LoginApiService service = new LoginApiServiceImpl();

    public static LoginApiService getLoginApi() {
        return service;
    }
}
