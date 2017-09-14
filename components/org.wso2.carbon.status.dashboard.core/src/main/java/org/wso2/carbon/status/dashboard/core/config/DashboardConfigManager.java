package org.wso2.carbon.status.dashboard.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Status Dashboard Configuration Manager.
 */
public class DashboardConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardConfigManager.class);

    public static Map<String,Object> generateConfigReader(String type) {

        Map<String,Object> dbConfigurationMap=new HashMap<>();

        if (ConfigReaderService.getInstance().getConfigProvider() != null) {
            try {
                SpDashboardConfiguration dashboardConfiguration = ConfigReaderService.getInstance().getConfigProvider().getConfigurationObject(SpDashboardConfiguration.class);
                if (null != dashboardConfiguration && null != dashboardConfiguration.getDbConfigs()) {
                    for (DatabaseConfiguration dbConfig :  dashboardConfiguration.getDbConfigs()) {
                        DatabaseChildConfiguration childConfiguration = dbConfig.getDatabaseConfiguration();
                        if (null != childConfiguration && null != childConfiguration.getType() && childConfiguration
                                .getType().equals(type) && null != childConfiguration.getProperties()
                                && null != childConfiguration.getTypeMappings()) {

                            DBQueries dbQueries = new DBQueries(childConfiguration.getProperties());
                            DBMapping dbMapping = new DBMapping(childConfiguration.getTypeMappings());
                            dbConfigurationMap.put("DBQueries",dbQueries);
                            dbConfigurationMap.put("DBMappings",dbMapping);
//                            LOGGER.info("DBQueries------------------->" + dbQueries);
                            return dbConfigurationMap;
                        }
                    }
                }
            } catch (ConfigurationException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Couldn't find a matching configuration for type: " +
                    type +"!");
        }
        return new HashMap<>();
    }
}
