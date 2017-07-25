package org.wso2.carbon.event.simulator.core.factories;

import org.wso2.carbon.event.simulator.core.api.ConnectToDatabaseApiService;
import org.wso2.carbon.event.simulator.core.impl.ConnectToDatabaseApiServiceImpl;

public class ConnectToDatabaseApiServiceFactory {
    private final static ConnectToDatabaseApiService service = new ConnectToDatabaseApiServiceImpl();

    public static ConnectToDatabaseApiService getConnectToDatabaseApi() {
        return service;
    }
}
