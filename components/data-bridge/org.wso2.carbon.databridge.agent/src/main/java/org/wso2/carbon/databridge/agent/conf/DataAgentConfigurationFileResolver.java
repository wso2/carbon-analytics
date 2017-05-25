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


import org.wso2.carbon.databridge.agent.util.DataAgentConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DataAgentConfigurationFileResolver {

    public static DataAgentsConfiguration resolveAndSetDataAgentConfiguration(
            LinkedHashMap dataAgentConfigurationHashMap) {

        DataAgentsConfiguration dataAgentsConfiguration = new DataAgentsConfiguration();
        List<Agent> agents = new ArrayList<>();
        dataAgentsConfiguration.setAgents(agents);

        List<LinkedHashMap> agentList = (ArrayList) dataAgentConfigurationHashMap.get("agents");

        for (LinkedHashMap agentConfigurationWrapper : agentList) {
            LinkedHashMap agentConfigurationHashMap = (LinkedHashMap) agentConfigurationWrapper.get("agentConfiguration");
            Agent agent = new Agent();
            AgentConfiguration agentConfiguration = agent.getAgentConfiguration();
            agentConfiguration.setName(agentConfigurationHashMap.get(DataAgentConstants.NAME).toString());
            agentConfiguration.setDataEndpointClass(agentConfigurationHashMap.get(DataAgentConstants.DATA_ENDPOINT_CLASS).toString());
            agentConfiguration.setPublishingStrategy(agentConfigurationHashMap.get(DataAgentConstants.PUBLISHING_STRATEGY).toString());
            agentConfiguration.setTrustStorePath(agentConfigurationHashMap.get(DataAgentConstants.TRUST_STORE_PATH).toString());
            agentConfiguration.setTrustStorePassword(agentConfigurationHashMap.get(DataAgentConstants.TRUST_STORE_PASSWORD).toString());
            agentConfiguration.setQueueSize(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.QUEUE_SIZE).toString()));
            agentConfiguration.setBatchSize(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.BATCH_SIZE).toString()));
            agentConfiguration.setCorePoolSize(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.CORE_POOL_SIZE).toString()));
            agentConfiguration.setSocketTimeoutMS(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.SOCKET_TIMEOUT_MS).toString()));
            agentConfiguration.setMaxPoolSize(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.MAX_POOL_SIZE).toString()));
            agentConfiguration.setKeepAliveTimeInPool(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.KEEP_ALIVE_TIME_INTERVAL_IN_POOL).toString()));
            agentConfiguration.setReconnectionInterval(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.RECONNETION_INTERVAL).toString()));
            agentConfiguration.setMaxTransportPoolSize(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.MAX_TRANSPORT_POOL_SIZE).toString()));
            agentConfiguration.setMaxIdleConnections(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.MAX_IDLE_CONNECTIONS).toString()));
            agentConfiguration.setEvictionTimePeriod(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.EVICTION_TIME_PERIOD).toString()));
            agentConfiguration.setMinIdleTimeInPool(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.MIN_IDLE_TIME_IN_POOL).toString()));
            agentConfiguration.setSecureMaxTransportPoolSize(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.SECURE_MAX_TRANSPORT_POOL_SIZE).toString()));
            agentConfiguration.setSecureMaxIdleConnections(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.SECURE_MAX_IDLE_CONNECTIONS).toString()));
            agentConfiguration.setSecureEvictionTimePeriod(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.SECURE_EVICTION_TIME_PERIOD).toString()));
            agentConfiguration.setSecureMinIdleTimeInPool(Integer.parseInt(agentConfigurationHashMap.get(DataAgentConstants.SECURE_MIN_IDLE_TIME_IN_POOL).toString()));
            agentConfiguration.setSslEnabledProtocols(agentConfigurationHashMap.get(DataAgentConstants.SSL_ENABLED_PROTOCOLS).toString());
            agentConfiguration.setCiphers(agentConfigurationHashMap.get(DataAgentConstants.CIPHERS).toString());

            agents.add(agent);
        }

        return dataAgentsConfiguration;
    }

}
