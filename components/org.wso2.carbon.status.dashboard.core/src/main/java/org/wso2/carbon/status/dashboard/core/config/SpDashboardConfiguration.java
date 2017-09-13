package org.wso2.carbon.status.dashboard.core.config;

import java.util.Set;

/**
 * Created by yasara on 9/13/17.
 */
public class SpDashboardConfiguration {
    Set<DatabaseConfiguration> databaseConfigurations;
    int pollingInterval;

    public Set<DatabaseConfiguration> getDatabaseConfigurations() {
        return databaseConfigurations;
    }
}
