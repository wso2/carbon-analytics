package org.wso2.carbon.sp.jobmanager.core.factories;

import org.wso2.carbon.sp.jobmanager.core.api.ManagersApiService;
import org.wso2.carbon.sp.jobmanager.core.impl.ManagersApiServiceImpl;


public class ManagersApiServiceFactory {
    private final static ManagersApiService service = new ManagersApiServiceImpl();

    public static ManagersApiService getManagersApi() {
        return service;
    }
}
