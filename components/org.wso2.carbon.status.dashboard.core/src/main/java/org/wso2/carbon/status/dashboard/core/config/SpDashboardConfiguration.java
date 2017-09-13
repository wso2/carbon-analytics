package org.wso2.carbon.status.dashboard.core.config;


/**
 * .
 */
public class SpDashboardConfiguration {
    DBMapping dbMapping;
    DBQueries dbQueries;
    int pollingInterval;

    public DBQueries getDBQueries(String type) {
        return dbQueries;
    }

    public DBMapping getDbMapping(String type) {
        return dbMapping;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }
}
