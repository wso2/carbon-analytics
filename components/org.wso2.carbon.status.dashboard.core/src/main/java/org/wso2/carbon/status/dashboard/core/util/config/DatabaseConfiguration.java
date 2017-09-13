package org.wso2.carbon.status.dashboard.core.util.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

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
