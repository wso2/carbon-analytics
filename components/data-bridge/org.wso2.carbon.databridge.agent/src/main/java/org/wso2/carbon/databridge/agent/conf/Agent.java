/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.agent.conf;


import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.LinkedHashMap;

/**
 * Class which wrap data receiver constructs
 */
@Configuration(description = "Agent configuration")
public class Agent {

    @Element(description = "Agent configuration")
    private AgentConfiguration agentConfiguration;

    public AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    public Agent(String name, String dataEndpointClass) {
        agentConfiguration = new AgentConfiguration(name, dataEndpointClass);
    }

    public Agent() {
        agentConfiguration = new AgentConfiguration();
    }


}
