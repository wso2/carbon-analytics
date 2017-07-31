package org.wso2.carbon.event.simulator.core.factories;

import org.wso2.carbon.event.simulator.core.api.FilesApiService;
import org.wso2.carbon.event.simulator.core.impl.FilesApiServiceImpl;

public class FilesApiServiceFactory {
    private final static FilesApiService service = new FilesApiServiceImpl();

    public static FilesApiService getFilesApi() {
        return service;
    }
}
