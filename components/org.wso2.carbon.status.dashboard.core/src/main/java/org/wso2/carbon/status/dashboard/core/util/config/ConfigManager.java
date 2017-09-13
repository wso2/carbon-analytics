package org.wso2.carbon.status.dashboard.core.util.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import java.util.HashMap;

/**
 * Status Dashboard Configuration Manager.
 */
public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private ConfigProvider configProvider;

    public ConfigManager(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public ConfigReader generateConfigReader(String type) {
        if (configProvider != null) {
            try {
                SpDashboardConfiguration dashboardConfiguration = (org.wso2.carbon.status.dashboard.core.util.config.SpDashboardConfiguration) configProvider.getConfigurationObject("");
                if (null != dashboardConfiguration && null != dashboardConfiguration.dbConfigs) {
                    for (DatabaseConfiguration dbConfig :  dashboardConfiguration.dbConfigs) {
                        DatabaseChildConfiguration childConfiguration = dbConfig.getDatabaseConfiguration();
                        if (null != childConfiguration && null != childConfiguration.getType() && childConfiguration
                                .getType().equals(type) && null != childConfiguration.getProperties()) {
                            return new ConfigReader(childConfiguration.getProperties());
                        }
                    }
                }
            } catch (ConfigurationException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Couldn't find a matching configuration for type: " +
                    type +"!");
        }
        return new FileConfigReader(new HashMap<>());
    }
}
