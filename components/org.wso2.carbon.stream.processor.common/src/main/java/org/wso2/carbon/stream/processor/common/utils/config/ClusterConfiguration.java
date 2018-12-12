package org.wso2.carbon.stream.processor.common.utils.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

import java.util.Map;

/**
 * {@code ClusterConfiguration} is a bean class for cluster configurations.
 */
@Configuration(namespace = "cluster.config", description = "WSO2 cluster configurations")
public class ClusterConfiguration {

    @Element(description = "Cluster enabled status", required = false)
    private boolean enabled;
    @Element(description = "Cluster group id", required = false)
    private String groupId;
    @Element(description = "Cluster coordination strategy", required = false)
    private String coordinationStrategyClass;
    @Element(description = "Cluster strategy config", required = false)
    private Map<String, String> strategyConfig;


    public boolean isEnabled() {
        return enabled;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getCoordinationStrategyClass() {
        return coordinationStrategyClass;
    }

    public Map<String, String> getStrategyConfig() {
        return strategyConfig;
    }
}
