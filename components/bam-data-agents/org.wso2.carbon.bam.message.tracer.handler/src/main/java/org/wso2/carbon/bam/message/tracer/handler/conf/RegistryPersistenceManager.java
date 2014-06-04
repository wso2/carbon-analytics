/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.message.tracer.handler.conf;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.message.tracer.handler.publish.ClusterNotifier;
import org.wso2.carbon.bam.message.tracer.handler.util.MessageTracerConstants;
import org.wso2.carbon.bam.message.tracer.handler.util.ServiceHolder;
import org.wso2.carbon.bam.message.tracer.handler.util.TenantEventConfigData;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Map;

public class RegistryPersistenceManager {

    private static Log log = LogFactory.getLog(RegistryPersistenceManager.class);

    public EventingConfigData load(int tenantId) {
        if (log.isDebugEnabled()) {
            log.debug("Loading config info from registry.");
        }
        EventingConfigData eventingConfigData = new EventingConfigData();
        // First set it to defaults, but do not persist
        eventingConfigData.setMessageTracingEnable(false);
        eventingConfigData.setUrl(MessageTracerConstants.EMPTY_STRING);
        eventingConfigData.setPassword(MessageTracerConstants.EMPTY_STRING);
        eventingConfigData.setUserName(MessageTracerConstants.EMPTY_STRING);
        eventingConfigData.setDumpBodyEnable(false);
        eventingConfigData.setLoggingEnable(false);
        eventingConfigData.setPublishToBAMEnable(false);

        // then load it from registry
        try {
            String enableTrace = getConfigurationProperty(tenantId, MessageTracerConstants.ENABLE_TRACE);
            String enablePublishToBAM  = getConfigurationProperty(tenantId, MessageTracerConstants.ENABLE_PUBLISH_TO_BAM);
            String bamUrl = getConfigurationProperty(tenantId, BAMDataPublisherConstants.BAM_URL);
            String bamUserName = getConfigurationProperty(tenantId, BAMDataPublisherConstants.BAM_USER_NAME);
            String bamPassword = getConfigurationProperty(tenantId, BAMDataPublisherConstants.BAM_PASSWORD);
            String dumpBody = getConfigurationProperty(tenantId, MessageTracerConstants.ENABLE_DUMP_MESSAGE_BODY);
            String enableLogging = getConfigurationProperty(tenantId, MessageTracerConstants.ENABLE_LOGGING);

            if (enableTrace != null) {
                eventingConfigData.setMessageTracingEnable(Boolean.valueOf(enableTrace));
                if (dumpBody != null) {
                    eventingConfigData.setDumpBodyEnable(Boolean.valueOf(dumpBody));
                }
                if (enableLogging != null) {
                    eventingConfigData.setLoggingEnable(Boolean.valueOf(enableLogging));
                }
                if (enablePublishToBAM != null && bamUrl != null && bamUserName != null && bamPassword != null ) {
                    eventingConfigData.setPublishToBAMEnable(Boolean.valueOf(enablePublishToBAM));
                    eventingConfigData.setUrl(bamUrl);
                    eventingConfigData.setUserName(bamUserName);
                    eventingConfigData.setPassword(bamPassword);
                }
                Map<Integer, EventingConfigData> tenantEventConfigData = TenantEventConfigData.getTenantSpecificEventingConfigData();
                tenantEventConfigData.put(tenantId, eventingConfigData);

            } else { // Registry does not have eventing config. Set to defaults.
                if (log.isDebugEnabled()) {
                    log.debug("Registry values are empty. Setting default values.");
                }
                update(eventingConfigData);
            }
        } catch (Exception ignored) {
            if (log.isDebugEnabled()) {
                log.debug(ignored);
            }
            // If something went wrong, then we have the default, or whatever loaded so far
        }
        return eventingConfigData;
    }

    private void updateConfigurationProperty(int tenantId, String propertyName, Object value)
            throws RegistryException {
        String resourcePath = MessageTracerConstants.ACTIVITY_REG_PATH + propertyName;
        Registry registry = ServiceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
        Resource resource;
        if (!registry.resourceExists(resourcePath)) {
            if (log.isDebugEnabled()) {
                log.debug("Resource " + propertyName + "available.");
            }
            resource = registry.newResource();
            resource.addProperty(propertyName, String.valueOf(value));
            registry.put(resourcePath, resource);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Resource " + propertyName + "creating.");
            }
            resource = registry.get(resourcePath);
            resource.setProperty(propertyName, String.valueOf(value));
            registry.put(resourcePath, resource);
        }
    }

    public void update(EventingConfigData eventingConfigData) throws RegistryException {

        if (log.isDebugEnabled()) {
            log.debug("Updating config ino.");
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<Integer, EventingConfigData> tenantEventConfigData = TenantEventConfigData.getTenantSpecificEventingConfigData();
        tenantEventConfigData.put(tenantId, eventingConfigData);

        updateConfigurationProperty(tenantId, MessageTracerConstants.ENABLE_TRACE,
                                    eventingConfigData.isMessageTracingEnable());
        updateConfigurationProperty(tenantId, BAMDataPublisherConstants.BAM_URL,
                                    eventingConfigData.getUrl());
        updateConfigurationProperty(tenantId, BAMDataPublisherConstants.BAM_USER_NAME,
                                    eventingConfigData.getUserName());
        updateConfigurationProperty(tenantId, BAMDataPublisherConstants.BAM_PASSWORD,
                                    eventingConfigData.getPassword());
        updateConfigurationProperty(tenantId, MessageTracerConstants.ENABLE_DUMP_MESSAGE_BODY,
                                    eventingConfigData.isDumpBodyEnable());
        updateConfigurationProperty(tenantId, MessageTracerConstants.ENABLE_LOGGING,
                                    eventingConfigData.isLoggingEnable());
        updateConfigurationProperty(tenantId, MessageTracerConstants.ENABLE_PUBLISH_TO_BAM,
                                    eventingConfigData.isPublishToBAMEnable());

        ClusterNotifier notifier  = new ClusterNotifier();
        notifier.setTenantId(tenantId);
        notifier.notifyClusterMessageTraceChange();
    }

    private String getConfigurationProperty(int tenantId, String propertyName)
            throws RegistryException {
        String resourcePath = MessageTracerConstants.ACTIVITY_REG_PATH + propertyName;
        Registry registry = ServiceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
        String value = null;
        if (registry.resourceExists(resourcePath)) {
            Resource resource = registry.get(resourcePath);
            value = resource.getProperty(propertyName);
        }
        return value;
    }
    
    public EventingConfigData getEventingConfigData() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return load(tenantId);
    }
}
