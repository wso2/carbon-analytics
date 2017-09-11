package org.wso2.carbon.stream.processor.statistic.factories;


import org.wso2.carbon.stream.processor.statistic.api.StatisticsApiService;
import org.wso2.carbon.stream.processor.statistic.impl.StatisticsApiServiceImpl;

public class StatisticsApiServiceFactory {
    private final static StatisticsApiService service = new StatisticsApiServiceImpl();

    public static StatisticsApiService getStatisticsApi() {
        return service;
    }
}
