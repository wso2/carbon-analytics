package org.wso2.carbon.status.dashboard.core.config;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Status Dashboard Configuration Manager.
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.db.config.manager",
        immediate = true
)
public class DashboardConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardConfigManager.class);

    @Activate
    protected void start(BundleContext bundleContext) {
        generateConfigReader("mysql");
    }

    public static Map<String, Object> generateConfigReader(String type) {

        Map<String, Object> dbConfigurationMap = new HashMap<>();

        if (ConfigReaderService.getInstance().getConfigProvider() != null) {
            try {
                SpDashboardConfiguration dashboardConfiguration = ConfigReaderService.getInstance()
                        .getConfigProvider().getConfigurationObject(SpDashboardConfiguration.class);
                LOGGER.info("#############" + dashboardConfiguration.toString());
            } catch (ConfigurationException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Couldn't find a matching configuration for type: " +
                    type + "!");
        }
        return new HashMap<>();
    }
}
