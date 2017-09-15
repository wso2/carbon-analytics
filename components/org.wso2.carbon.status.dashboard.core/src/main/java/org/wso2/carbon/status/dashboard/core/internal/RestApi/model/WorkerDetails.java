package org.wso2.carbon.status.dashboard.core.internal.RestApi.model;

import java.util.Map;

public class WorkerDetails {

    Map<String,String> metricsMap;
    String HAStatus;
    String lastUpdatedTime;
    boolean isStatsEnabled;
    Map<String,Integer> siddhiApps;
    String clusterID;
}
