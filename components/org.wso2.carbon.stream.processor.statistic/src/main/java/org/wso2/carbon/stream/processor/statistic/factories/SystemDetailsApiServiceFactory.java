package org.wso2.carbon.stream.processor.statistic.factories;

import org.wso2.carbon.stream.processor.statistic.api.SystemDetailsApiService;
import org.wso2.carbon.stream.processor.statistic.impl.SystemDetailsApiServiceImpl;

public class SystemDetailsApiServiceFactory {
    private final static SystemDetailsApiService service = new SystemDetailsApiServiceImpl();

    public static SystemDetailsApiService getSystemDetailsApi() {
        return service;
    }
}
