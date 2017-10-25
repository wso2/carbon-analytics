package org.wso2.carbon.stream.processor.auth.rest.api.factories;

import org.wso2.carbon.stream.processor.auth.rest.api.UserstoreApiService;
import org.wso2.carbon.stream.processor.auth.rest.api.impl.UserstoreApiServiceImpl;

public class UserstoreApiServiceFactory {
    private static final UserstoreApiService service = new UserstoreApiServiceImpl();

    public static UserstoreApiService getUserstoreApi() {
        return service;
    }
}
