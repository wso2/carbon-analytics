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

package org.wso2.carbon.analytics.spark.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;

import java.util.List;

public class DataPublisherClient {
    private static String THRIFT_AGENT_TYPE = "Thrift";
    private DataPublisher dataPublisher;

    private static final Log log = LogFactory.getLog(EventIteratorFunction.class);

    public DataPublisherClient(String receiverURLSet, String authURLSet, String username, String password) throws Exception {
        AgentHolder.setConfigPath(System.getProperty("Agent.Config.Path"));
        this.dataPublisher = new DataPublisher(THRIFT_AGENT_TYPE, receiverURLSet, authURLSet, username, password);
    }

    public void shutdown() throws DataEndpointException {
        dataPublisher.shutdown();
    }

    public void publish(List<Event> events) throws DataEndpointException {
        for (Event event : events) {
            dataPublisher.tryPublish(event);
        }
    }

    public void publish(Event event) throws DataEndpointException {
        dataPublisher.tryPublish(event);
    }
}
