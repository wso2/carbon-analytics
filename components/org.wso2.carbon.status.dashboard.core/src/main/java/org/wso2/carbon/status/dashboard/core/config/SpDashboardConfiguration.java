package org.wso2.carbon.status.dashboard.core.config;

import java.util.Set;

/**
 * .
 */
public class SpDashboardConfiguration {
    DBQueries dbQueries;
    int pollingInterval;

    public DBQueries getDatabaseConfigurations(String type) {
        return dbQueries;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }
}
