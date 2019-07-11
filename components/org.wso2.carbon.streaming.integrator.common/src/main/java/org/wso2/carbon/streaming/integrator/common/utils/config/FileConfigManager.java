/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.streaming.integrator.common.utils.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import io.siddhi.core.util.SiddhiConstants;
import io.siddhi.core.util.config.ConfigManager;
import io.siddhi.core.util.config.ConfigReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Siddhi File Configuration Manager.
 */
public class FileConfigManager implements ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileConfigManager.class);

    private ConfigProvider configProvider;

    public FileConfigManager(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public ConfigReader generateConfigReader(String namespace, String name) {
        if (configProvider != null) {
            try {
                RootConfiguration rootConfiguration = configProvider.getConfigurationObject(RootConfiguration.class);
                if (null != rootConfiguration && null != rootConfiguration.getExtensions()) {
                    for (Extension extension : rootConfiguration.getExtensions()) {
                        ExtensionChildConfiguration childConfiguration = extension.getExtension();
                        if (null != childConfiguration && null != childConfiguration.getName() && childConfiguration
                                .getName().equals(name) && null != childConfiguration.getNamespace() &&
                                childConfiguration.getNamespace().equals(namespace)
                                && null != childConfiguration.getProperties()) {
                            return new FileConfigReader(childConfiguration.getProperties());
                        }
                    }
                }
            } catch (ConfigurationException e) {
                LOGGER.error("Could not initiate the siddhi configuration object, " + e.getMessage(), e);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Could not find a matching configuration for name: " +
                    name + "and namespace: " + namespace + "!");
        }
        return new FileConfigReader(new HashMap<>());
    }

    @Override
    public Map<String, String> extractSystemConfigs(String name) {
        if (configProvider != null) {
            try {
                RootConfiguration rootConfiguration = configProvider.getConfigurationObject(RootConfiguration.class);
                if (null != rootConfiguration && null != rootConfiguration.getRefs()) {
                    for (Reference ref : rootConfiguration.getRefs()) {
                        ReferenceChildConfiguration childConfiguration = ref.getReference();
                        if (null != childConfiguration && null != childConfiguration.getName()
                                && childConfiguration.getName().equals(name)) {
                            Map<String, String> referenceConfigs = new HashMap<>();
                            referenceConfigs.put(SiddhiConstants.ANNOTATION_ELEMENT_TYPE, childConfiguration.getType());
                            if (childConfiguration.getProperties() != null) {
                                referenceConfigs.putAll(childConfiguration.getProperties());
                            }
                            return referenceConfigs;
                        }
                    }
                }
            } catch (ConfigurationException e) {
                LOGGER.error("Could not initiate the siddhi configuration object, " + e.getMessage(), e);
            }
        }
        return new HashMap<>();
    }

    @Override
    public String extractProperty(String name) {
        String property = null;
        if (configProvider != null) {
            try {
                RootConfiguration rootConfiguration =
                        configProvider.getConfigurationObject(RootConfiguration.class);
                if (null != rootConfiguration && null != rootConfiguration.getProperties()) {
                    property =  rootConfiguration.getProperties().get(name);
                }
            } catch (ConfigurationException e) {
                LOGGER.error("Could not initiate the siddhi configuration object, " + e.getMessage(), e);
            }

            if (property == null && "shardId".equalsIgnoreCase(name)) {
                try {
                    ClusterConfig clusterConfig =
                            configProvider.getConfigurationObject(ClusterConfig.class);
                    if (clusterConfig != null) {
                        if (clusterConfig.getGroupId() != null && clusterConfig.isEnabled()) {
                            return clusterConfig.getGroupId();
                        }
                    }
                } catch (ConfigurationException e) {
                    LOGGER.error("Could not initiate the cluster.config configuration object, " + e.getMessage(), e);
                }

                try {
                    CarbonConfiguration carbonConfiguration =
                            configProvider.getConfigurationObject(CarbonConfiguration.class);
                    if (carbonConfiguration != null && carbonConfiguration.getId() != null) {
                        return carbonConfiguration.getId();
                    }
                } catch (ConfigurationException e) {
                    LOGGER.error("Could not initiate the wso2.carbon configuration object, " + e.getMessage(), e);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Could not find a matching configuration for property name: " + name + "");
        }
        return property;
    }
}
