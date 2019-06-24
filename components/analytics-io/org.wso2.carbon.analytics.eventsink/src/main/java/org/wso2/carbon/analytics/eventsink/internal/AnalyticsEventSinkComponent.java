/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.eventsink.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventSinkService;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventSinkServiceImpl;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStoreCAppDeployer;
import org.wso2.carbon.analytics.eventsink.internal.jmx.QueueEventBufferSizeCalculator;
import org.wso2.carbon.analytics.eventsink.internal.jmx.EventReceiverCounter;
import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkConstants;
import org.wso2.carbon.analytics.eventsink.internal.util.ServiceHolder;
import org.wso2.carbon.analytics.eventsink.subscriber.AnalyticsEventStreamListener;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.utils.CarbonUtils;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.lang.management.ManagementFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * This is the declarative service component which registers the required OSGi
 * services for this component's operation.
 */
@Component(
         name = "analytics.eventsink.comp", 
         immediate = true)
public class AnalyticsEventSinkComponent {

    private static Log log = LogFactory.getLog(AnalyticsEventSinkComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Started the Analytics Event Sink component");
            }
            ServiceHolder.setAnalyticsEventSinkService(new AnalyticsEventSinkServiceImpl());
            ServiceHolder.setEventPublisherManagementService(new CarbonEventSinkManagementService());
            componentContext.getBundleContext().registerService(EventStreamListener.class.getName(), ServiceHolder.getAnalyticsEventStreamListener(), null);
            componentContext.getBundleContext().registerService(AnalyticsEventSinkService.class.getName(), ServiceHolder.getAnalyticsEventSinkService(), null);
            componentContext.getBundleContext().registerService(ServerStartupObserver.class.getName(), AnalyticsEventSinkServerStartupObserver.getInstance(), null);
            componentContext.getBundleContext().registerService(AppDeploymentHandler.class.getName(), new AnalyticsEventStoreCAppDeployer(), null);
            ServiceHolder.getEventManagementService().subscribe(ServiceHolder.getEventPublisherManagementService());
            this.loadAnalyticsEventSinkConfiguration();
            ServiceHolder.setAnalyticsDSConnector(new AnalyticsDSConnector());
        } catch (Exception e) {
            log.error("Error while activating the AnalyticsEventSinkComponent.", e);
        }
        try {
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            String eventCounterObject = "org.wso2.carbon:00=analytics,01=EVENT_PERSISTENCE_COUNTER";
            ObjectName eventCounterMbean = new ObjectName(eventCounterObject);
            if (!platformMBeanServer.isRegistered(eventCounterMbean)) {
                EventReceiverCounter counter = new EventReceiverCounter();
                platformMBeanServer.registerMBean(counter, eventCounterMbean);
            }
            String queueBufferSize = "org.wso2.carbon:00=analytics,01=RECEIVER_REMAINING_PERSISTENCE_QUEUE_BUFFER_SIZE_IN_BYTES";
            ObjectName queueBufferMbean = new ObjectName(queueBufferSize);
            if (!platformMBeanServer.isRegistered(queueBufferMbean)) {
                QueueEventBufferSizeCalculator counter = new QueueEventBufferSizeCalculator();
                platformMBeanServer.registerMBean(counter, queueBufferMbean);
            }
        } catch (Exception e) {
            log.error("Unable to create EventCounter stat MBean: " + e.getMessage(), e);
        }
    }

    private void loadAnalyticsEventSinkConfiguration() {
        File analyticsConfFile = new File(CarbonUtils.getCarbonConfigDirPath() + File.separator + AnalyticsEventSinkConstants.ANALYTICS_CONF_DIR + File.separator + AnalyticsEventSinkConstants.EVENT_SINK_CONFIGURATION_FILE_NAME);
        if (analyticsConfFile.exists()) {
            try {
                JAXBContext context = JAXBContext.newInstance(AnalyticsEventSinkConfiguration.class);
                Unmarshaller un = context.createUnmarshaller();
                ServiceHolder.setAnalyticsEventSinkConfiguration((AnalyticsEventSinkConfiguration) un.unmarshal(analyticsConfFile));
            } catch (JAXBException e) {
                log.error("Error while unmarshalling the file : " + analyticsConfFile.getName() + ". Therefore getting the " + "default configuration.", e);
                ServiceHolder.setAnalyticsEventSinkConfiguration(new AnalyticsEventSinkConfiguration());
            }
        } else {
            ServiceHolder.setAnalyticsEventSinkConfiguration(new AnalyticsEventSinkConfiguration());
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopped AnalyticsEventSink component");
        }
    }

    @Reference(
             name = "registry.streamdefn.comp", 
             service = org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetStreamDefinitionStoreService")
    protected void setStreamDefinitionStoreService(AbstractStreamDefinitionStore abstractStreamDefinitionStore) {
        ServiceHolder.setStreamDefinitionStoreService(abstractStreamDefinitionStore);
    }

    protected void unsetStreamDefinitionStoreService(AbstractStreamDefinitionStore abstractStreamDefinitionStore) {
        ServiceHolder.setStreamDefinitionStoreService(null);
    }

    @Reference(
             name = "event.stream.service", 
             service = org.wso2.carbon.event.stream.core.EventStreamService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setAnalyticsEventStreamListener(new AnalyticsEventStreamListener());
        ServiceHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(null);
    }

    @Reference(
             name = "analytics.component", 
             service = org.wso2.carbon.analytics.api.AnalyticsDataAPI.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAnalyticsDataAPI")
    protected void setAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        ServiceHolder.setAnalyticsDataAPI(analyticsDataAPI);
    }

    protected void unsetAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        ServiceHolder.setAnalyticsDataAPI(null);
    }

    @Reference(
             name = "eventManagement.service", 
             service = org.wso2.carbon.event.processor.manager.core.EventManagementService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetEventManagementService")
    protected void setEventManagementService(EventManagementService eventManagementService) {
        ServiceHolder.setEventManagementService(eventManagementService);
    }

    protected void unsetEventManagementService(EventManagementService eventManagementService) {
        ServiceHolder.setEventManagementService(null);
    }
}

