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
package org.wso2.carbon.databridge.core.Utils;

import org.wso2.carbon.databridge.core.EventConverter;
import org.wso2.carbon.databridge.core.StreamTypeHolder;

/**
 * Event composite that's passed to the Queue Worker
 */
public class EventComposite {
    private StreamTypeHolder streamTypeHolder;
    private Object eventBundle;
    private AgentSession agentSession;
    private EventConverter eventConverter;

    public EventComposite(Object eventBundle,
                          StreamTypeHolder streamTypeHolder, AgentSession agentSession,
                          EventConverter eventConverter) {
        this.streamTypeHolder = streamTypeHolder;
        this.eventBundle = eventBundle;
        this.agentSession = agentSession;
        this.eventConverter = eventConverter;
    }

    public StreamTypeHolder getStreamTypeHolder() {
        return streamTypeHolder;
    }

    public Object getEventBundle() {
        return eventBundle;
    }

    public AgentSession getAgentSession() {
        return agentSession;
    }

    public EventConverter getEventConverter() {
        return eventConverter;
    }
}
