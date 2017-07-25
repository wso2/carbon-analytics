package org.wso2.carbon.event.simulator.core.factories;

import org.wso2.carbon.event.simulator.core.api.SingleApiService;
import org.wso2.carbon.event.simulator.core.impl.SingleApiServiceImpl;

public class SingleApiServiceFactory {
    private final static SingleApiService service = new SingleApiServiceImpl();

    public static SingleApiService getSingleApi() {
        return service;
    }
}
