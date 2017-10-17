package io.swagger.api.factories;

import io.swagger.api.ResourceManagerApiService;
import io.swagger.api.impl.ResourceManagerApiServiceImpl;

public class ResourceManagerApiServiceFactory {
    private final static ResourceManagerApiService service = new ResourceManagerApiServiceImpl();

    public static ResourceManagerApiService getResourceManagerApi() {
        return service;
    }
}
