package org.wso2.carbon.event.simulator.core.factories;

import org.wso2.carbon.event.simulator.core.api.FeedApiService;
import org.wso2.carbon.event.simulator.core.impl.FeedApiServiceImpl;

public class FeedApiServiceFactory {
    private final static FeedApiService service = new FeedApiServiceImpl();

    public static FeedApiService getFeedApi() {
        return service;
    }
}
