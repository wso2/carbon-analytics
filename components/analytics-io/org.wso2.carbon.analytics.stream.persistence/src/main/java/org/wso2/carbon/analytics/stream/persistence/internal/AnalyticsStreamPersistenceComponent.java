/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.stream.persistence.internal;

import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventSinkService;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * This class represents the analytics stream persistence service declarative services component.
 *
 * @scr.component name="analytics.stream.persistence.component" immediate="true"
 * @scr.reference name="analytics.component" interface="AnalyticsDataService"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 * @scr.reference name="eventStreamManager.component"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="eventSink.component"
 * interface="org.wso2.carbon.analytics.eventsink.AnalyticsEventSinkService" cardinality="1..1"
 * policy="dynamic" bind="setEventSinkService" unbind="unsetEventSinkService"
 */
public class AnalyticsStreamPersistenceComponent {

    protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(analyticsDataService);
    }

    protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(null);
    }

    protected void setEventSinkService(AnalyticsEventSinkService analyticsEventSinkService) {
        ServiceHolder.setAnalyticsEventSinkService(analyticsEventSinkService);
    }

    protected void unsetEventSinkService(AnalyticsEventSinkService analyticsEventSinkService) {
        ServiceHolder.setAnalyticsEventSinkService(null);
    }
}
