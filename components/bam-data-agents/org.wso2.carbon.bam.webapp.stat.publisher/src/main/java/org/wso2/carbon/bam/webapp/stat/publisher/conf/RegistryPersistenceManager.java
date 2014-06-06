/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.bam.webapp.stat.publisher.conf;

import org.wso2.carbon.bam.data.publisher.util.*;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.StreamDefinitionCreatorUtil;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.WebappAgentUtil;
import org.wso2.carbon.bam.webapp.stat.publisher.util.TenantEventConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.util.WebappStatisticsPublisherConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
* Purpose of this class is to handle bam configurations data,
* This class will write and read from the registry. When the server starts up this class will be called
*/
public class RegistryPersistenceManager {

    private static RegistryService registryService;
    private static InternalEventingConfigData eventingConfigData = new InternalEventingConfigData();
    public static final String EMPTY_STRING = "";

    public static void setRegistryService(RegistryService registryServiceParam) {
        registryService = registryServiceParam;
    }

    /**
     * Updates configuration property to a new value.
     *
     * @param propertyName Name of the property to be updated.
     * @param value        New value of the property
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    public void updateConfigurationProperty(String propertyName, Object value, String registryPath)
            throws RegistryException {
        String resourcePath = registryPath + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        Resource resource;
        if (!registry.resourceExists(resourcePath)) {
            resource = registry.newResource();
            resource.addProperty(propertyName, String.valueOf(value));
            registry.put(resourcePath, resource);
        } else {
            resource = registry.get(resourcePath);
            resource.setProperty(propertyName, String.valueOf(value));
            registry.put(resourcePath, resource);
        }
    }

    /**
     * Loads configuration from Registry.
     */
    public ServiceEventingConfigData load() {
        ServiceEventingConfigData eventingConfigData = new ServiceEventingConfigData();
        // First set it to defaults, but do not persist
        eventingConfigData.setPublishingEnabled(WebappAgentUtil.getPublishingEnabled());
        eventingConfigData.setWebappStatsEnabled(false);
        eventingConfigData.setMsgDumpingEnabled(false);
        eventingConfigData.setUrl(EMPTY_STRING);
        eventingConfigData.setPassword(EMPTY_STRING);
        eventingConfigData.setUserName(EMPTY_STRING);
        eventingConfigData.setProperties(new Property[0]);

        // then load it from registry
        try {
            String serviceStatsStatus = getConfigurationProperty(
                    WebappStatisticsPublisherConstants.WEBAPP_STATISTICS_REG_PATH,
                    WebappStatisticsPublisherConstants.ENABLE_WEBAPP_STATS_EVENTING);

            String bamUrl = getConfigurationProperty(WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH,
                    BAMDataPublisherConstants.BAM_URL);
            String bamUserName = getConfigurationProperty(WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH,
                    BAMDataPublisherConstants.BAM_USER_NAME);
            String bamPassword = getConfigurationProperty(WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH,
                    BAMDataPublisherConstants.BAM_PASSWORD);

            String streamName = getConfigurationProperty(WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH,
                    BAMDataPublisherConstants.STREAM_NAME);
            String version = getConfigurationProperty(WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH,
                    BAMDataPublisherConstants.VERSION);
            String description = getConfigurationProperty(WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH,
                    BAMDataPublisherConstants.DESCRIPTION);
            String nickName = getConfigurationProperty(WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH,
                    BAMDataPublisherConstants.NICK_NAME);

            Properties properties = getAllConfigProperties(WebappStatisticsPublisherConstants.WEBAPP_PROPERTIES_REG_PATH);

            if (serviceStatsStatus != null && bamUrl != null && bamUserName != null && bamPassword != null) {

                eventingConfigData.setWebappStatsEnabled(Boolean.parseBoolean(serviceStatsStatus));
                eventingConfigData.setUrl(bamUrl);
                eventingConfigData.setUserName(bamUserName);
                eventingConfigData.setPassword(bamPassword);
                eventingConfigData.setStreamName(streamName);
                eventingConfigData.setVersion(version);
                eventingConfigData.setDescription(description);
                eventingConfigData.setNickName(nickName);

                if (properties != null) {
                    List<Property> propertyDTOList = new ArrayList<Property>();
                    String[] keys = properties.keySet().toArray(new String[properties.size()]);
                    for (int i = keys.length - 1; i >= 0; i--) {
                        String key = keys[i];
                        Property propertyDTO = new Property();
                        propertyDTO.setKey(key);
                        propertyDTO.setValue(((List<String>) properties.get(key)).get(0));
                        propertyDTOList.add(propertyDTO);
                    }

                    eventingConfigData.setProperties(propertyDTOList.toArray(new Property[propertyDTOList.size()]));
                }


                // Handle the case with both stats and activity enabled
                InternalEventingConfigData eventConfigNStreamDef = null;
                    StreamDefinition streamDefinition = StreamDefinitionCreatorUtil.getStreamDefinition(
                            eventingConfigData);
                    eventConfigNStreamDef = fillEventingConfigData(eventingConfigData);
                    eventConfigNStreamDef.setStreamDefinition(streamDefinition);
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();       //todo
                Map<Integer, InternalEventingConfigData> tenantEventConfigData = TenantEventConfigData.getTenantSpecificEventingConfigData();
                tenantEventConfigData.put(tenantId, eventConfigNStreamDef);

            } else { // Registry does not have eventing config. Set to defaults.
                update(eventingConfigData);
            }
        } catch (Exception ignored) {
            // If something went wrong, then we have the default, or whatever loaded so far
        }
        return eventingConfigData;
    }


    public InternalEventingConfigData fillEventingConfigData(ServiceEventingConfigData eventingConfigData) {
        InternalEventingConfigData eventConfigNStreamDef = new InternalEventingConfigData();
        eventConfigNStreamDef.setDescription(eventingConfigData.getDescription());
        eventConfigNStreamDef.setMsgDumpingEnabled(eventingConfigData.isMsgDumpingEnabled());
        eventConfigNStreamDef.setNickName(eventingConfigData.getNickName());
        eventConfigNStreamDef.setPassword(eventingConfigData.getPassword());
        eventConfigNStreamDef.setProperties(eventingConfigData.getProperties());
        eventConfigNStreamDef.setWebappStatsEnabled(eventingConfigData.isWebappStatsEnabled());
        eventConfigNStreamDef.setStreamName(eventingConfigData.getStreamName());
        eventConfigNStreamDef.setUrl(eventingConfigData.getUrl());
        eventConfigNStreamDef.setUserName(eventingConfigData.getUserName());
        eventConfigNStreamDef.setVersion(eventingConfigData.getVersion());

        return eventConfigNStreamDef;

    }

    /**
     * Updates all properties of a resource
     *
     * @param properties
     * @param registryPath
     */
    public void updateAllProperties(Properties properties, String registryPath)
            throws RegistryException {
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.
                getThreadLocalCarbonContext().getTenantId());

        // Always creating a new resource because properties should be replaced and overridden
        Resource resource = registry.newResource();

        resource.setProperties(properties);
        registry.put(registryPath, resource);
    }

    /**
     * Updates the Registry with given config data.
     *
     * @param eventingConfigData eventing configuration data
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          thrown when updating the registry properties fails.
     */
    public void update(ServiceEventingConfigData eventingConfigData) throws RegistryException {

//        StatisticsType statisticsType = ServiceAgentUtil.findTheStatisticType(eventingConfigData);
        InternalEventingConfigData eventConfigNStreamDef = null;
        StreamDefinition streamDefinition = StreamDefinitionCreatorUtil.getStreamDefinition(eventingConfigData);
        eventConfigNStreamDef = fillEventingConfigData(eventingConfigData);
        eventConfigNStreamDef.setStreamDefinition(streamDefinition);

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<Integer, InternalEventingConfigData> tenantEventConfigData = TenantEventConfigData.getTenantSpecificEventingConfigData();
        tenantEventConfigData.put(tenantId, eventConfigNStreamDef);

        updateConfigurationProperty(WebappStatisticsPublisherConstants.ENABLE_WEBAPP_STATS_EVENTING,
                eventingConfigData.isWebappStatsEnabled(),
                WebappStatisticsPublisherConstants.WEBAPP_STATISTICS_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_URL, eventingConfigData.getUrl(),
                WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_USER_NAME, eventingConfigData.getUserName(),
                WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_PASSWORD, eventingConfigData.getPassword(),
                WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH);

        updateConfigurationProperty(BAMDataPublisherConstants.STREAM_NAME, eventingConfigData.getStreamName(),
                WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.VERSION, eventingConfigData.getVersion(),
                WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.NICK_NAME, eventingConfigData.getNickName(),
                WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.DESCRIPTION, eventingConfigData.getDescription(),
                WebappStatisticsPublisherConstants.WEBAPP_COMMON_REG_PATH);

        Property[] propertiesDTO = eventingConfigData.getProperties();
        if (propertiesDTO != null) {
            Properties properties = new Properties();
            for (int i = 0; i < propertiesDTO.length; i++) {
                Property property = propertiesDTO[i];
                List<String> valueList = new ArrayList<String>();
                valueList.add(property.getValue());
                properties.put(property.getKey(), valueList);
            }
            updateAllProperties(properties, WebappStatisticsPublisherConstants.WEBAPP_PROPERTIES_REG_PATH);
        } else {
            updateAllProperties(null, WebappStatisticsPublisherConstants.WEBAPP_PROPERTIES_REG_PATH);
        }
        WebappAgentUtil.removeExistingEventPublisherConfigValue(eventingConfigData.getUrl() + "_"
                + eventingConfigData.getUserName() + "_"
                + eventingConfigData.getPassword());
        WebappAgentUtil.removeExistingEventPublisherConfigValue(eventingConfigData.getUrl() + "_"
                + eventingConfigData.getUserName() + "_"
                + eventingConfigData.getPassword());

    }

    /**
     * Fetches the value of the property with propertyName from registry. Returns null if no property
     * exists with the given name.
     *
     * @param registryPath reg path
     * @param propertyName Name of the property to be fetched.
     * @return Property value
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     */
    public String getConfigurationProperty(String registryPath, String propertyName)
            throws RegistryException {
        String resourcePath = registryPath + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.
                getThreadLocalCarbonContext().getTenantId());
        String value = null;
        if (registry.resourceExists(resourcePath)) {
            Resource resource = registry.get(resourcePath);
            value = resource.getProperty(propertyName);
        }
        return value;
    }

    /**
     * Fetches all properties for any registry resource
     *
     * @param registryPath
     * @return properties
     * @throws RegistryException
     */
    public Properties getAllConfigProperties(String registryPath) throws RegistryException {
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.
                getThreadLocalCarbonContext().getTenantId());
        Properties properties = null;
        Properties filterProperties = null;
//        Properties reverseProperties = null;
        if (registry.resourceExists(registryPath)) {
            Resource resource = registry.get(registryPath);
            properties = resource.getProperties();
            if (properties != null) {
                filterProperties = new Properties();
                for (Map.Entry<Object, Object> keyValuePair : properties.entrySet()) {
                    //When using mounted registry it keeps some properties starting with "registry." we don't need it.
                    if (!keyValuePair.getKey().toString().startsWith(BAMDataPublisherConstants.PREFIX_FOR_REGISTRY_HIDDEN_PROPERTIES)) {
                        filterProperties.put(keyValuePair.getKey(), keyValuePair.getValue());
                    }
                }

            }
        }
        return filterProperties;
    }

    public ServiceEventingConfigData getEventingConfigData() {
        return load();
    }

}
