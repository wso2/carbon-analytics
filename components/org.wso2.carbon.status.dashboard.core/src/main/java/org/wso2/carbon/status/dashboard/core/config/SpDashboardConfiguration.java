package org.wso2.carbon.status.dashboard.core.config;

import java.util.Set;

/**
 * Created by yasara on 9/13/17.
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
