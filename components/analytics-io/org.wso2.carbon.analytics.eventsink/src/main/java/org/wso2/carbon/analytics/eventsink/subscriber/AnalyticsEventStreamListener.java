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

package org.wso2.carbon.analytics.eventsink.subscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;
import org.wso2.carbon.analytics.eventsink.internal.AnalyticsEventStoreManager;
import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkUtil;
import org.wso2.carbon.analytics.eventsink.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the Stream listener implementation to react upon new stream getting added and removed.
 */
public class AnalyticsEventStreamListener implements EventStreamListener {

    private static final Log log = LogFactory.getLog(AnalyticsEventStreamListener.class);

    private static final ConcurrentHashMap<Integer, List<AnalyticsWSO2EventConsumer>> consumerCache
            = new ConcurrentHashMap<>();

    @Override
    public void removedEventStream(int tenantId, String streamName, String version) {
        List<AnalyticsWSO2EventConsumer> tenantConsumers = consumerCache.get(tenantId);
        if (tenantConsumers != null) {
            String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, version);
            for (AnalyticsWSO2EventConsumer consumer : tenantConsumers) {
                if (consumer.getStreamId().equals(streamId)) {
                    try {
                        ServiceHolder.getAnalyticsEventSinkService().removeEventSink(tenantId, streamName, version);
                    } catch (AnalyticsEventStoreException e) {
                        log.error("Error while removing analytics event store configuration for stream Id :" + streamId
                                , e);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void addedEventStream(int tenantId, String streamName, String version) {
        AnalyticsEventStore analyticsEventStore = AnalyticsEventStoreManager.getInstance().
                getAnalyticsEventStore(tenantId, AnalyticsEventSinkUtil.generateAnalyticsTableName(streamName));
        if (analyticsEventStore != null) {
            if (analyticsEventStore.getEventSource().contains(DataBridgeCommonsUtils.generateStreamId(streamName, version))) {
                subscribeForStream(tenantId, DataBridgeCommonsUtils.generateStreamId(streamName, version));
            }
        }
    }

    public void subscribeForStream(int tenantId, String streamId) {
        List<AnalyticsWSO2EventConsumer> consumers = consumerCache.get(tenantId);
        if (consumers == null) {
            synchronized (this) {
                consumers = consumerCache.get(tenantId);
                if (consumers == null) {
                    consumers = new ArrayList<>();
                    consumerCache.put(tenantId, consumers);
                }
            }
        }
        AnalyticsWSO2EventConsumer analyticsWSO2EventConsumer =
                new AnalyticsWSO2EventConsumer(streamId, tenantId);
        if (!consumers.contains(analyticsWSO2EventConsumer)) {
            try {
                ServiceHolder.getEventStreamService().subscribe(analyticsWSO2EventConsumer);
                consumers.add(analyticsWSO2EventConsumer);
            } catch (EventStreamConfigurationException e) {
                log.error("Error while registering subscriber for stream id " + streamId
                        + " for tenant id " + tenantId + ". " + e.getMessage(), e);
            }
        }
    }

    public void unsubscribeFromStream(int tenantId, String streamId) {
        List<AnalyticsWSO2EventConsumer> consumers = consumerCache.get(tenantId);
        if (consumers != null) {
            AnalyticsWSO2EventConsumer analyticsWSO2EventConsumer =
                    new AnalyticsWSO2EventConsumer(streamId, tenantId);
            int index = consumers.indexOf(analyticsWSO2EventConsumer);
            if (index != -1) {
                AnalyticsWSO2EventConsumer consumer = consumers.remove(index);
                ServiceHolder.getEventStreamService().unsubscribe(consumer);
            }
        }
    }
}
