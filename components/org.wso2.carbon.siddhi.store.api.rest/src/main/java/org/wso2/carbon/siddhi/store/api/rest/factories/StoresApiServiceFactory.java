package org.wso2.carbon.siddhi.store.api.rest.factories;

import org.wso2.carbon.siddhi.store.api.rest.StoresApiService;
import org.wso2.carbon.siddhi.store.api.rest.impl.StoresApiServiceImpl;

public class StoresApiServiceFactory {
    private final static StoresApiService service = new StoresApiServiceImpl();

    public static StoresApiService getStoresApi() {
        return service;
    }
}
