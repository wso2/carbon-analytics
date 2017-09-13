package org.wso2.carbon.status.dashboard.core.util.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.ArrayList;
import java.util.List;


@Configuration(namespace = "wso2.status.dashboard", description = "SP Status Dashboard Configuration Parameters")
public class SpDashboardConfiguration {

    public SpDashboardConfiguration(){
        dbConfigs = new ArrayList<>();
    }

    @Element(description = "polling interval to get real-time statistics of worker")
    public int pollingInterval = 5;

    @Element(description = "Database related configurations")
    public List<DatabaseConfiguration> dbConfigs = new ArrayList<DatabaseConfiguration>();


//    public int getPollingInterval() {
//        return pollingInterval;
//    }
//
//
//    public List<DatabaseConfiguration> getDbConfigs() {
//        return dbConfigs;
//    }

}
