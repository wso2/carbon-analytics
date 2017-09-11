package org.wso2.carbon.status.dashboard.core.factories;


import org.wso2.carbon.status.dashboard.core.api.WorkersApiService;
import org.wso2.carbon.status.dashboard.core.impl.WorkersApiServiceImpl;

/**
 * .
 */
public class WorkersApiServiceFactory {
    private static final WorkersApiService service = new WorkersApiServiceImpl();

    public static WorkersApiService getWorkersApi() {
        return service;
    }
}
