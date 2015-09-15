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

package org.wso2.carbon.analytics.eventsink.internal.util;

import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventSinkService;
import org.wso2.carbon.analytics.eventsink.internal.AnalyticsDSConnector;
import org.wso2.carbon.analytics.eventsink.internal.AnalyticsEventSinkConfiguration;
import org.wso2.carbon.analytics.eventsink.subscriber.AnalyticsEventStreamListener;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * This class holds all the osgi services registered within the component.
 */
public class ServiceHolder {
    private static EventStreamService eventStreamService;
    private static AnalyticsEventStreamListener analyticsEventStreamListener;
    private static AnalyticsDSConnector analyticsDSConnector;
    private static AnalyticsDataAPI analyticsDataAPI;
    private static AbstractStreamDefinitionStore streamDefinitionStoreService;
    private static AnalyticsEventSinkService analyticsEventSinkService;
    private static EventManagementService eventManagementService;
    private static EventPublisherManagementService eventPublisherManagementService;
    private static AnalyticsEventSinkConfiguration analyticsEventSinkConfiguration;

    private ServiceHolder(){}

    public static AbstractStreamDefinitionStore getStreamDefinitionStoreService() {
        return streamDefinitionStoreService;
    }

    public static void setStreamDefinitionStoreService(AbstractStreamDefinitionStore streamDefinitionStoreService) {
        ServiceHolder.streamDefinitionStoreService = streamDefinitionStoreService;
    }

    public static EventStreamService getEventStreamService() {
        return eventStreamService;
    }

    public static void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.eventStreamService = eventStreamService;
    }

    public static AnalyticsEventStreamListener getAnalyticsEventStreamListener() {
        return analyticsEventStreamListener;
    }

    public static void setAnalyticsEventStreamListener(AnalyticsEventStreamListener analyticsEventStreamListener) {
        ServiceHolder.analyticsEventStreamListener = analyticsEventStreamListener;
    }

    public static AnalyticsDSConnector getAnalyticsDSConnector() {
        return analyticsDSConnector;
    }

    public static void setAnalyticsDSConnector(AnalyticsDSConnector analyticsDSConnector) {
        ServiceHolder.analyticsDSConnector = analyticsDSConnector;
    }

    public static AnalyticsDataAPI getAnalyticsDataAPI() {
        return analyticsDataAPI;
    }

    public static void setAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI1) {
        ServiceHolder.analyticsDataAPI = analyticsDataAPI1;
    }

    public static AnalyticsEventSinkService getAnalyticsEventSinkService() {
        return analyticsEventSinkService;
    }

    public static void setAnalyticsEventSinkService(AnalyticsEventSinkService analyticsEventSinkService) {
        ServiceHolder.analyticsEventSinkService = analyticsEventSinkService;
    }

    public static EventManagementService getEventManagementService() {
        return eventManagementService;
    }

    public static void setEventManagementService(EventManagementService eventManagementService) {
        ServiceHolder.eventManagementService = eventManagementService;
    }

    public static EventPublisherManagementService getEventPublisherManagementService() {
        return eventPublisherManagementService;
    }

    public static void setEventPublisherManagementService(EventPublisherManagementService eventPublisherManagementService) {
        ServiceHolder.eventPublisherManagementService = eventPublisherManagementService;
    }

    public static AnalyticsEventSinkConfiguration getAnalyticsEventSinkConfiguration() {
        return analyticsEventSinkConfiguration;
    }

    public static void setAnalyticsEventSinkConfiguration(AnalyticsEventSinkConfiguration analyticsEventSinkConfiguration) {
        ServiceHolder.analyticsEventSinkConfiguration = analyticsEventSinkConfiguration;
    }
}
