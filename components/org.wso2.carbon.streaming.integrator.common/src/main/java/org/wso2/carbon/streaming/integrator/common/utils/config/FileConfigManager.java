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

import io.siddhi.core.util.SiddhiConstants;
import io.siddhi.core.util.config.ConfigManager;
import io.siddhi.core.util.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.streaming.integrator.common.utils.SPConstants.DATASOURCES_ROOT_ELEMENT;
import static org.wso2.carbon.streaming.integrator.common.utils.SPConstants.DATASOURCE_NAMESPACE;
import static org.wso2.carbon.streaming.integrator.common.utils.SPConstants.EXTENSIONS_NAMESPACE;
import static org.wso2.carbon.streaming.integrator.common.utils.SPConstants.REFS_NAMESPACE;
import static org.wso2.carbon.streaming.integrator.common.utils.SPConstants.SIDDHI_PROPERTIES_NAMESPACE;

/**
 * Siddhi File Configuration Manager.
 */
public class FileConfigManager implements ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileConfigManager.class);

    private ConfigProvider configProvider;
    private List<Extension> extensions = new ArrayList<>();
    private List<Reference> references = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();

    public FileConfigManager(ConfigProvider configProvider) {
        this.configProvider = configProvider;
        init();
    }

    private void init() {
        if (configProvider != null) {
            initializeExtensions();
            initializeReferences();
            initializeProperties();
        }
    }

    private void initializeProperties() {
        // load siddhi properties
        try {
            Object siddhiPropertiesConf = configProvider.getConfigurationObject(SIDDHI_PROPERTIES_NAMESPACE);
            HashMap propertiesMap;
            if (siddhiPropertiesConf == null || siddhiPropertiesConf instanceof Map) {
                propertiesMap = ((HashMap) siddhiPropertiesConf);
                if (propertiesMap != null && propertiesMap.size() > 0) {
                    this.properties = propertiesMap;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Matching siddhi property is looked for under namespace '" +
                                SIDDHI_PROPERTIES_NAMESPACE + "'.");
                    }
                } else {
                    RootConfiguration rootConfiguration =
                            configProvider.getConfigurationObject(RootConfiguration.class);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Matching siddhi property is looked for under namespace " +
                                "'siddhi.properties'.");
                    }
                    this.properties = rootConfiguration.getProperties();
                }
            } else {
                throw new ConfigurationException("The first level under 'dataPartitioning' namespace should " +
                        "be a map of type <sting, string>");
            }
        } catch (ConfigurationException e) {
            LOGGER.error("Could not initiate the siddhi configuration object, " + e.getMessage(), e);
        }
    }

    private void initializeReferences() {
        try {
            ArrayList<Reference> references = configProvider
                    .getConfigurationObjectList(REFS_NAMESPACE, Reference.class);
            if (!references.isEmpty()) {
                this.references = references;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Matching references is loaded from under namespace 'refs'.");
                }
            } else {
                RootConfiguration rootConfiguration = configProvider
                        .getConfigurationObject(RootConfiguration.class);
                this.references = rootConfiguration.getRefs();
                LOGGER.debug("Matching references is loaded from under namespace 'siddhi.extensions'.");
            }
        } catch (Exception e) {
            LOGGER.error("Could not initiate the refs configuration object, " + e.getMessage(), e);
        }
    }

    private void initializeExtensions() {
        try {
            // Process system configs
            ArrayList<Extension> extensions = configProvider
                    .getConfigurationObjectList(EXTENSIONS_NAMESPACE, Extension.class);
            if (!extensions.isEmpty()) {
                this.extensions = extensions;
                LOGGER.debug("Matching extensions system configurations is loaded from under namespace " +
                        "'extensions'.");
            } else {
                RootConfiguration rootConfiguration = configProvider.
                        getConfigurationObject(RootConfiguration.class);
                this.extensions = rootConfiguration.getExtensions();
                LOGGER.debug("Matching extensions system configurations is loaded from under namespace " +
                        "'siddhi.extensions'.");
            }
        } catch (Exception e) {
            LOGGER.error("Could not initiate the extensions configuration object, " + e.getMessage(), e);
        }
    }

    @Override
    public ConfigReader generateConfigReader(String namespace, String name) {
        List datasourceConfigs;
        Map datasourceConnectionProperties;
        for (Extension extension : this.extensions) {
            ExtensionChildConfiguration childConfiguration = extension.getExtension();
            if (childConfiguration.getNamespace().equals(namespace) &&
                    childConfiguration.getName().equals(name) &&
                    childConfiguration.getProperties() != null) {
                return new FileConfigReader(childConfiguration.getProperties());
            }
        }
        if (namespace.equalsIgnoreCase(DATASOURCES_ROOT_ELEMENT)) {
            try {
                datasourceConfigs = (List) ((HashMap) configProvider.getConfigurationObject(DATASOURCES_ROOT_ELEMENT))
                        .get(DATASOURCE_NAMESPACE);
                    for (Object datasourceConfig : datasourceConfigs) {
                        if (((HashMap) datasourceConfig).get("name").equals(name)) {
                            datasourceConnectionProperties = (Map) ((HashMap) ((HashMap<?, ?>) datasourceConfig).
                                    get("definition")).get("configuration");
                            return new FileConfigReader(datasourceConnectionProperties);
                        }
                    }
            } catch (ConfigurationException e) {
                LOGGER.error("Error occurred while reading the datasource configurations from deployment.yaml", e);
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
        for (Reference reference : references) {
            ReferenceChildConfiguration childConf = reference.getReference();
            if (childConf.getName().equals(name)) {
                Map<String, String> referenceConfigs = new HashMap<>();
                referenceConfigs.put(SiddhiConstants.ANNOTATION_ELEMENT_TYPE, childConf.getType());
                if (childConf.getProperties() != null) {
                    referenceConfigs.putAll(childConf.getProperties());
                }
                return referenceConfigs;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Could not find a matching reference for name: '" + name + "'!");
        }
        return new HashMap<>();
    }

    @Override
    public String extractProperty(String name) {
        String property = this.properties.get(name);
        if (property == null && "shardId".equalsIgnoreCase(name)) {
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Could not find a matching configuration for property name: " + name + "");
        }
        return property;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }
}
