/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.databridge.agent.conf;


import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for data-bridge-config.yaml file.
 */
@Configuration(namespace = "data.agent.config", description = "Configuration of the Data Agents - to publish events through databridge")
public class DataAgentsConfiguration {


    @Element(description = "Data agent configurations", required = true)
    public List<Agent> agents = new ArrayList<>();

    public DataAgentsConfiguration() {
        agents.add(new Agent("Thrift", "org.wso2.carbon.databridge.agent.endpoint.thrift.ThriftDataEndpoint"));
        agents.add(new Agent("Binary", "org.wso2.carbon.databridge.agent.endpoint.binary.BinaryDataEndpoint"));
    }

    @Override
    public String toString() {
        return "Data Agents - " + agents.toString();
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

}
