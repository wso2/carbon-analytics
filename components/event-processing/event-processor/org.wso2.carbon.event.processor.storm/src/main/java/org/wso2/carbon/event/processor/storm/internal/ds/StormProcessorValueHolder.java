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

package org.wso2.carbon.event.processor.storm.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughSenderConfigurator;
import org.wso2.carbon.event.processor.api.receive.EventReceiver;
import org.wso2.carbon.event.processor.api.send.EventProducerStreamNotificationListener;
import org.wso2.carbon.event.processor.storm.internal.CarbonStormProcessorService;
import org.wso2.carbon.event.processor.storm.internal.CarbonStormProcessorService;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.siddhi.core.persistence.PersistenceStore;

import java.util.ArrayList;
import java.util.List;

public class StormProcessorValueHolder {
    private static CarbonStormProcessorService stormProcessorService;
    private static List<EventReceiver> eventReceiverList = new ArrayList<EventReceiver>();
    private static EventProducerStreamNotificationListener eventProducerStreamNotificationListener;
    private static EventStatisticsService eventStatisticsService;
    private static DataAccessService dataAccessService;
    private static ClusterInformation clusterInformation;
    private static UserRealm userRealm;
    private static DataSourceService dataSourceService;

    public static void registerStormProcessorService(CarbonStormProcessorService service) {
        stormProcessorService = service;
    }

    public static CarbonStormProcessorService getStormProcessorService() {
        return stormProcessorService;
    }

    public static void addEventReceiver(EventReceiver eventReceiver) {
        eventReceiverList.add(eventReceiver);
    }

    public static void removeEventReceiver(EventReceiver eventReceiver) {
        eventReceiverList.remove(eventReceiver);
    }

    public static List<EventReceiver> getEventReceiverList() {
        return eventReceiverList;
    }

    public static void registerNotificationListener(EventProducerStreamNotificationListener eventProducerStreamNotificationListener) {
        StormProcessorValueHolder.eventProducerStreamNotificationListener = eventProducerStreamNotificationListener;
    }

    public static EventProducerStreamNotificationListener getNotificationListener() {
        return eventProducerStreamNotificationListener;
    }

    public static void registerEventStatisticsService(EventStatisticsService eventStatisticsService) {
        StormProcessorValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }

    public static void setDataAccessService(DataAccessService dataAccessService) {
        StormProcessorValueHolder.dataAccessService = dataAccessService;
    }

    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public static void setClusterInformation(ClusterInformation clusterInformation) {
        StormProcessorValueHolder.clusterInformation = clusterInformation;
    }

    public static ClusterInformation getClusterInformation() {
        return clusterInformation;
    }

    public static void setUserRealm(UserRealm userRealm) {
        StormProcessorValueHolder.userRealm = userRealm;
    }

    public static UserRealm getUserRealm() {
        return userRealm;
    }

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        StormProcessorValueHolder.dataSourceService = dataSourceService;
    }

}
