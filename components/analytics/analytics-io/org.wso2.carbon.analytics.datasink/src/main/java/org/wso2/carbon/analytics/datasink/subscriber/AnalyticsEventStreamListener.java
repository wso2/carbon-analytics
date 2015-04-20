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
import org.wso2.carbon.analytics.datasink.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.stream.core.EventStreamListener;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class AnalyticsEventStreamListener implements EventStreamListener {

    private static final Log log = LogFactory.getLog(AnalyticsEventStreamListener.class);

    private static final ConcurrentHashMap<String, AnalyticsWSO2EventConsumer> consumerCache
            = new ConcurrentHashMap<String, AnalyticsWSO2EventConsumer>();

    @Override
    public void removedEventStream(int tenantId, String streamName, String version) {
        String cacheKey = generateConsumerCacheKey(streamName, version, tenantId);
        if (consumerCache.get(cacheKey) != null) {
//            ServiceHolder.getEventStreamService().unsubscribe(consumerCache.get(cacheKey), tenantId);
        }
    }

    @Override
    public void addedEventStream(int tenantId, String streamName, String version) {
        AnalyticsWSO2EventConsumer analyticsWSO2EventConsumer =
                new AnalyticsWSO2EventConsumer(DataBridgeCommonsUtils.generateStreamId(streamName, version),
                        tenantId);
        consumerCache.put(generateConsumerCacheKey(streamName, version, tenantId), analyticsWSO2EventConsumer);
//        try {
////            ServiceHolder.getEventStreamService().subscribe(analyticsWSO2EventConsumer, tenantId);
//        } catch (EventStreamConfigurationException e) {
//            log.error("Error while registering subscriber for stream name :" + streamName +
//                    " , version :" + version + " for tenant id " + tenantId + ". " + e.getMessage(), e);
//        }
    }

    private String generateConsumerCacheKey(String streamName, String version, int tenantId) {
        return streamName + ":" + version + ":" + tenantId;
    }

    public synchronized void loadEventStreams(int tenantId) {
        try {
            Collection<StreamDefinition> streams = ServiceHolder.getStreamDefinitionStoreService().
                    getAllStreamDefinitionsFromStore(tenantId);
            for (StreamDefinition streamDefinition : streams) {
                addedEventStream(tenantId, streamDefinition.getName(), streamDefinition.getVersion());
            }
        } catch (StreamDefinitionStoreException e) {
            log.error("Error while loading the stream definitions from store for tenant "
                    + tenantId + ". " + e.getMessage(), e);
        }
    }

    public synchronized void unLoadEventStreams(int tenantId) {
        try {
            Collection<StreamDefinition> streams = ServiceHolder.getStreamDefinitionStoreService().
                    getAllStreamDefinitionsFromStore(tenantId);
            for (StreamDefinition streamDefinition : streams) {
                removedEventStream(tenantId, streamDefinition.getName(), streamDefinition.getVersion());
            }
        } catch (StreamDefinitionStoreException e) {
            log.error("Error while loading the stream definitions from store for tenant "
                    + tenantId + ". " + e.getMessage(), e);
        }
    }
}
