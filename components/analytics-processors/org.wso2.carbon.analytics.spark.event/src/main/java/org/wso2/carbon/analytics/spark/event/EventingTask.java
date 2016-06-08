/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.spark.event;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.event.internal.ServiceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.ntask.core.Task;

/**
 * Spark eventing scheduled task to read from the eventing data store and to dispatch the events to 
 * its respective streams.
 */
public class EventingTask implements Task {

    private static final Log log = LogFactory.getLog(EventingTask.class);
    
    @Override
    public void init() { }

    @Override
    public void setProperties(Map<String, String> props) { }
    
    @Override
    public void execute() {
        Map<Integer, List<Event>> eventBatches;
        do {
            eventBatches = EventStreamDataStore.extractNextEventBatch();
            this.processEventBatches(eventBatches);
        } while (eventBatches.size() > 0);        
    }
    
    private void processEventBatches(Map<Integer, List<Event>> eventBatches ) {
        for (Map.Entry<Integer, List<Event>> entry : eventBatches.entrySet()) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(entry.getKey());
                for (Event event : entry.getValue()) {
                    ServiceHolder.getEventStreamService().publish(event);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Dispatched " + entry.getValue().size() + " events for tenant: " + entry.getKey());
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }     
    }
    
}
