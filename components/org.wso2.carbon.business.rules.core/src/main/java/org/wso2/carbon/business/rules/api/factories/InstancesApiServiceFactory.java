package org.wso2.carbon.business.rules.api.factories;

import org.wso2.carbon.business.rules.api.InstancesApiService;
import org.wso2.carbon.business.rules.api.impl.InstancesApiServiceImpl;

public class InstancesApiServiceFactory {
    private final static InstancesApiService service = new InstancesApiServiceImpl();

    public static InstancesApiService getInstancesApi() {
        return service;
    }
}
