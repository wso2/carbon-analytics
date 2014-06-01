/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.agent.thrift.internal.publisher.client;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.conf.DataPublisherConfiguration;
import org.wso2.carbon.databridge.agent.thrift.internal.EventQueue;
import org.wso2.carbon.databridge.commons.Event;

public class EventPublisherFactory {
    public static EventPublisher getEventPublisher(
            DataPublisherConfiguration dataPublisherConfiguration,
            EventQueue<Event> eventQueue, Agent agent, GenericKeyedObjectPool transportPool) {
        return new ThriftEventPublisher(eventQueue, transportPool,
                                        agent.getQueueSemaphore(),
                                        agent.getAgentConfiguration().getMaxMessageBundleSize(),
                                        dataPublisherConfiguration, agent.getAgentAuthenticator(),
                                        agent.getThreadPool());
    }

}
