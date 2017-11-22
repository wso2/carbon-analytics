package org.wso2.carbon.event.simulator.core.factories;

import org.wso2.carbon.event.simulator.core.api.DatabaseApiService;
import org.wso2.carbon.event.simulator.core.impl.DatabaseApiServiceImpl;

public class DatabaseApiServiceFactory {
    private final static DatabaseApiService service = new DatabaseApiServiceImpl();

    public static DatabaseApiService getConnectToDatabaseApi() {
        return service;
    }
}
