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
package org.wso2.carbon.analytics.message.tracer.handler.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.message.tracer.handler.conf.EventingConfigData;
import org.wso2.carbon.analytics.message.tracer.handler.conf.MessageTracerConfiguration;
import org.wso2.carbon.analytics.message.tracer.handler.conf.MessageTracerConfigurationManager;
import org.wso2.carbon.analytics.message.tracer.handler.conf.RegistryPersistenceManager;
import org.wso2.carbon.analytics.message.tracer.handler.util.MessageTracerConstants;
import org.wso2.carbon.analytics.message.tracer.handler.util.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "org.wso2.carbon.analytics.message.tracer.handler",
        immediate = true)
public class MessageTracerServiceComponent {

    private static final Log LOG = LogFactory.getLog(MessageTracerServiceComponent.class);

    private ConfigurationContext configurationContext;

    private static MessageTracerConfiguration msgTracerConfiguration;

    @Activate
    protected void activate(ComponentContext context) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BAM message tracer handler bundle is activated");
        }
        try {
            //initialize the messageTracer configuration
            msgTracerConfiguration = MessageTracerConfigurationManager.getMessageTracerConfiguration();
            // Engaging MessageTracerModule as a global module
            configurationContext.getAxisConfiguration().engageModule(
                    MessageTracerConstants.ANALYTICS_SERVICE_MESSAGE_TRACER_MODULE_NAME);
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new MessageTracerAxis2ConfigurationContextObserver(), null);

            new RegistryPersistenceManager().load(CarbonContext.getThreadLocalCarbonContext().getTenantId());

            String messageTracingEnabled = msgTracerConfiguration.getMessageTracingEnabled();
            if (StringUtils.isNotEmpty(messageTracingEnabled)) {
                EventingConfigData eventingConfigData = new RegistryPersistenceManager().getEventingConfigData();
                eventingConfigData.setMessageTracingEnable(Boolean.parseBoolean(messageTracingEnabled));
                new RegistryPersistenceManager().update(eventingConfigData);
            }

        } catch (Exception e) {
            LOG.error("Failed to activate BAM message tracer handler bundle", e);
        }
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            bind = "setConfigurationContextService",
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        configurationContext = configurationContextService.getServerConfigContext();
        ServiceHolder.setConfigurationContextService(configurationContextService);

    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        configurationContext = null;
        ServiceHolder.setConfigurationContextService(null);
    }

    @Reference(
            name = "org.wso2.carbon.registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            bind = "setRegistryService",
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }

    @Reference(
            name = "org.wso2.carbon.event.stream.core.EventStreamService",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            bind = "setEventStreamService",
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(null);
    }

    public static MessageTracerConfiguration getMessageTracerConfiguration() {
        return msgTracerConfiguration;
    }
}