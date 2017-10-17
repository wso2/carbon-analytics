package org.wso2.carbon.das.jobmanager.core.factories;

import org.wso2.carbon.das.jobmanager.core.ResourceManagerApiService;
import org.wso2.carbon.das.jobmanager.core.impl.ResourceManagerApiServiceImpl;

public class ResourceManagerApiServiceFactory {
    private final static ResourceManagerApiService service = new ResourceManagerApiServiceImpl();

    public static ResourceManagerApiService getResourceManagerApi() {
        return service;
    }
}
