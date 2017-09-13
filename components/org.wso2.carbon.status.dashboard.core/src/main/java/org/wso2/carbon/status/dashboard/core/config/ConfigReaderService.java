package org.wso2.carbon.status.dashboard.core.config;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.wso2.carbon.config.provider.ConfigProvider;

/**
 * .
 */
public class ConfigReaderService {
    private static ConfigReaderService instance = new ConfigReaderService();
    private static ConfigProvider configProvider;
    public static ConfigReaderService getInstance() {
        return instance;
    }

    @Reference(
            name = "org.wso2.carbon.config",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            unbind = "unregisterConfigProvider"
    )
    private void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public static ConfigProvider getConfigProvider() {
        return configProvider;
    }
}
