package org.wso2.carbon.status.dashboard.core.config;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;


/**
 * Config Reader service.
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.db.config",
        immediate = true
)
public class ConfigReaderService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigReaderService.class);
    private static ConfigReaderService instance = new ConfigReaderService();
    private static ConfigProvider configProvider;

    public ConfigReaderService(){
    }
    public static ConfigReaderService getInstance() {
        return instance;
    }

    @Activate
    protected void start(BundleContext bundleContext) {
    }

    @Reference(
            name = "org.wso2.carbon.config",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            unbind = "unregisterConfigProvider"
    )
    public void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }
}