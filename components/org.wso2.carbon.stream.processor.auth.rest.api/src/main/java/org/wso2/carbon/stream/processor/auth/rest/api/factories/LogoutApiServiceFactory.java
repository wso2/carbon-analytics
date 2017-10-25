package org.wso2.carbon.stream.processor.auth.rest.api.factories;

import org.wso2.carbon.stream.processor.auth.rest.api.LogoutApiService;
import org.wso2.carbon.stream.processor.auth.rest.api.impl.LogoutApiServiceImpl;

public class LogoutApiServiceFactory {
    private static final LogoutApiService service = new LogoutApiServiceImpl();

    public static LogoutApiService getLogoutApi() {
        return service;
    }
}
