package org.wso2.carbon.sp.jobmanager.core.factories;

import org.wso2.carbon.sp.jobmanager.core.api.WorkersApiService;
import org.wso2.carbon.sp.jobmanager.core.impl.WorkersApiServiceImpl;

public class WorkersApiServiceFactory {
    private final static WorkersApiService service = new WorkersApiServiceImpl();

    public static WorkersApiService getWorkersApi() {
        return service;
    }
}
