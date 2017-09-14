package org.wso2.carbon.status.dashboard.core.config;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;


/**
 * Config Reader service.
 */
public class ConfigReaderService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigReaderService.class);
    private static ConfigReaderService instance =new ConfigReaderService();
    private ConfigProvider configProvider;

    private ConfigReaderService(){
    }
    public static ConfigReaderService getInstance() {
        return instance;
    }

    @Reference(
            name = "org.wso2.carbon.config",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            unbind = "unregisterConfigProvider"
    )
    private void registerConfigProvider(ConfigProvider configurationProvider) {
        configProvider = configurationProvider;
//        logger.info("config provider------------------------------------>"+configProvider);
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }
}