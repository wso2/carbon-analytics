package org.wso2.carbon.status.dashboard.core.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.status.dashboard.core.config.DatabaseChildConfiguration;

/**
 * Config bean for databaseConfiguration.
 */
@Configuration(description = "Database related configurations")
public class DatabaseConfiguration {

    @Element(description = "A string field")
    private DatabaseChildConfiguration databaseConfiguration = new DatabaseChildConfiguration();

    public DatabaseChildConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

}
