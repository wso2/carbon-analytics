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

import org.wso2.carbon.analytics.eventsink.internal.jmx.EventCounter;
import org.wso2.carbon.analytics.eventsink.internal.queue.AnalyticsEventQueueManager;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.WSO2EventConsumer;

/**
 * This is the actual consumer which is getting registered for the list of
 * wso2 events received in stream junction for a specific stream.
 */
public class AnalyticsWSO2EventConsumer implements WSO2EventConsumer {
    
    private String streamId;
    
    private int tenantId;

    public AnalyticsWSO2EventConsumer(String streamId, int tenantId) {
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
    public void onAddDefinition(StreamDefinition streamDefinition) {
       //Nothing to do, create table, and setting the schema has been handled above.
    }

    @Override
    public void onRemoveDefinition(StreamDefinition streamDefinition) {
        //Nothing we need to do here.
    }

    @Override
    public boolean equals(Object object){
        if (object != null && object instanceof AnalyticsWSO2EventConsumer){
            AnalyticsWSO2EventConsumer anotherObj = (AnalyticsWSO2EventConsumer) object;
            if (anotherObj.getStreamId().equals(this.streamId) && anotherObj.tenantId == this.tenantId){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.streamId.hashCode() + this.tenantId << 10;
    }
    
}
