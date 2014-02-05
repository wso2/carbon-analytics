/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughSenderConfigurator;
import org.wso2.carbon.event.processor.api.receive.EventReceiver;
import org.wso2.carbon.event.processor.api.send.EventProducer;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.internal.CEPEventProducer;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.listener.EventReceiverStreamNotificationListenerImpl;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;

/**
 * @scr.component name="eventProcessorService.component" immediate="true"
 * @scr.reference name="eventReceiver.service"
 * interface="org.wso2.carbon.event.processor.api.receive.EventReceiver" cardinality="1..1"
 * policy="dynamic" bind="notifyEventReceiver" unbind="notifyRemovalEventReceiver"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamManagerService" unbind="unsetEventStreamManagerService"
 * @scr.reference name="hazelcast.instance.service"
 * interface="com.hazelcast.core.HazelcastInstance" cardinality="0..1"
 * policy="dynamic" bind="setHazelcastInstance" unbind="unsetHazelcastInstance"
 * @scr.reference name="dataaccess.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="user.realm.delegating" interface="org.wso2.carbon.user.core.UserRealm"
 * cardinality="1..1" policy="dynamic" bind="setUserRealm" unbind="unsetUserRealm"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 */
public class EventProcessorServiceDS {
    private static final Log log = LogFactory.getLog(EventProcessorServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            CarbonEventProcessorService carbonEventProcessorService = new CarbonEventProcessorService();
            EventProcessorValueHolder.registerEventProcessorService(carbonEventProcessorService);

            CEPEventProducer eventSender = new CEPEventProducer(carbonEventProcessorService);

            context.getBundleContext().registerService(EventProcessorService.class.getName(), carbonEventProcessorService, null);
            context.getBundleContext().registerService(EventProducer.class.getName(), eventSender, null);
            log.info("Successfully deployed EventProcessorService");

        } catch (RuntimeException e) {
            log.error("Could not create EventProcessorService");
        }

    }

    public void notifyEventReceiver(EventReceiver eventReceiver) {
        EventProcessorValueHolder.addEventReceiver(eventReceiver);
        eventReceiver.subscribeNotificationListener(new EventReceiverStreamNotificationListenerImpl(eventReceiver));
    }

    public void notifyRemovalEventReceiver(EventReceiver eventReceiver) {
        EventProcessorValueHolder.removeEventReceiver(eventReceiver);
    }

    public void addPassthroughSenderConfigurator(PassthroughSenderConfigurator passthroughSenderConfigurator) {
        EventProcessorValueHolder.addPassthroughSenderConfigurator(passthroughSenderConfigurator);
    }

    public void removePassthroughSenderConfigurator(PassthroughSenderConfigurator passthroughSenderConfigurator) {
        EventProcessorValueHolder.removePassthroughSenderConfigurator(passthroughSenderConfigurator);
    }

    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.registerEventStatisticsService(null);
    }

    public void setEventStreamManagerService(EventStreamService eventStreamService) {
        EventProcessorValueHolder.registerEventStreamManagerService(eventStreamService);
    }

    public void unsetEventStreamManagerService(EventStreamService eventStreamService) {
        EventProcessorValueHolder.registerEventStreamManagerService(null);
    }

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorValueHolder.registerHazelcastInstance(hazelcastInstance);
        EventProcessorValueHolder.getEventProcessorService().notifyServiceAvailability(EventProcessorConstants.HAZELCAST_INSTANCE);
    }

    public void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorValueHolder.registerHazelcastInstance(null);
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        EventProcessorValueHolder.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        EventProcessorValueHolder.setDataAccessService(null);
    }

    protected void setUserRealm(UserRealm userRealm) {
        EventProcessorValueHolder.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm userRealm) {
        EventProcessorValueHolder.setUserRealm(null);

    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        EventProcessorValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        EventProcessorValueHolder.setDataSourceService(null);
    }
}
