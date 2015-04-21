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

package org.wso2.carbon.analytics.datasink.subscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasink.internal.queue.AnalyticsEventQueueManager;
import org.wso2.carbon.analytics.datasink.internal.util.ServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.WSO2EventListConsumer;

import java.util.List;

public class AnalyticsWSO2EventListConsumer implements WSO2EventListConsumer {
    private static final Log log = LogFactory.getLog(AnalyticsWSO2EventListConsumer.class);
    private String streamId;
    private int tenantId;

    public AnalyticsWSO2EventListConsumer(String streamId, int tenantId) {
        this.streamId = streamId;
        this.tenantId = tenantId;
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    @Override
    public void onEvent(Event event) {
        AnalyticsEventQueueManager.getInstance().put(tenantId, event);
    }

    @Override
    public void onEventList(List<Event> eventList) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            ServiceHolder.getAnalyticsDSConnector().insertEvents(tenantId, eventList);
            PrivilegedCarbonContext.endTenantFlow();
        } catch (Exception e) {
            String errorMsg = "Error processing event. ";
            log.error(errorMsg, e);
        }
    }

    @Override
    public void onAddDefinition(StreamDefinition streamDefinition) {
        try {
            ServiceHolder.getAnalyticsDSConnector().addStream(tenantId, streamDefinition);
        } catch (AnalyticsException e) {
            log.error("Error while creating the table for the stream id: " + streamDefinition.getStreamId()
                    + "for tenant " + tenantId + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void onRemoveDefinition(StreamDefinition streamDefinition) {
        try {
            ServiceHolder.getAnalyticsDSConnector().deleteStream(tenantId, streamDefinition);
        } catch (AnalyticsException e) {
            log.error("Error while deleting the stream id : " + streamDefinition.getStreamId()
                    + "for tenant " + tenantId + ". " + e.getMessage(), e);
        }
    }
}
