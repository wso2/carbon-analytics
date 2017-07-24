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

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.spark.event.internal.ServiceHolder;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskLocationResolver;
import org.wso2.carbon.ntask.core.TaskServiceContext;

import com.hazelcast.core.HazelcastInstance;

/**
 * Task location resolver to always possibly schedule the task on the active receiver in
 * an H/A scenario.
 */
public class SparkEventingTaskLocationResolver implements TaskLocationResolver {

    private static final Log log = LogFactory.getLog(SparkEventingTaskLocationResolver.class);
    
    @Override
    public void init(Map<String, String> props) throws TaskException { }
    
    @Override
    public int getLocation(TaskServiceContext ctx, TaskInfo taskInfo) throws TaskException {
        int myNodeLocation = this.extractMyNodeLocation(ctx);
        if (log.isDebugEnabled()) {
            log.debug("My Location [" + myNodeLocation + "]: " + ctx.getServerAddress(myNodeLocation));
        }
        int result;
        if (ServiceHolder.getEventPublisherManagementService().isDrop()) {
            /* this will make sure, it will go to the node next to me (the locations wraps around) */
            result = myNodeLocation + 1;
        } else {
            /* I'm accepting events, so I can run the task */
            result = myNodeLocation; 
        }
        if (log.isDebugEnabled()) {
            log.debug("Resolved Location: " + result);
        }
        return result;
    }
    
    private int extractMyNodeLocation(TaskServiceContext ctx) {
        int count = ctx.getServerCount();
        for (int i = 0; i < count; i++) {
            if (this.isMyNode(ctx.getServerAddress(i))) {
                return i;
            }
        }
        return 0;
    }
    
    private boolean isMyNode(InetSocketAddress addr) {
        HazelcastInstance hz = AnalyticsServiceHolder.getHazelcastInstance();
        if (hz == null) {
            return false;
        }
        return hz.getCluster().getLocalMember().getSocketAddress().equals(addr);
    }
    
}