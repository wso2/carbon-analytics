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


import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.util.DataAgentConstants;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Data bridge agent config file resolver
 */
public class DataAgentConfigurationFileResolver {

    public static DataAgentsConfiguration resolveAndSetDataAgentConfiguration(
            LinkedHashMap dataAgentConfigurationHashMap) throws DataEndpointAgentConfigurationException {

        DataAgentsConfiguration dataAgentsConfiguration = new DataAgentsConfiguration();
        List<Agent> agents = new ArrayList<>();
        dataAgentsConfiguration.setAgents(agents);

        List agentList = (ArrayList) dataAgentConfigurationHashMap.get("agents");

        if (agentList != null) {
            for (Object agentConfigurationWrapper : agentList) {
                LinkedHashMap agentConfigurationHashMap = (LinkedHashMap) ((LinkedHashMap) agentConfigurationWrapper).
                        get("agentConfiguration");
                Agent agent = new Agent();
                AgentConfiguration agentConfiguration = agent.getAgentConfiguration();

                Object endpointNameConfig = agentConfigurationHashMap.get(DataAgentConstants.NAME);
                if (endpointNameConfig != null && !endpointNameConfig.toString().trim().isEmpty()) {
                    agentConfiguration.setName(endpointNameConfig.toString().trim());
                } else {
                    throw new DataEndpointAgentConfigurationException("Endpoint name is not set in "
                            + DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME);
                }

                Object endpointClassConfig = agentConfigurationHashMap.get(DataAgentConstants.DATA_ENDPOINT_CLASS);
                if (endpointClassConfig != null && !endpointClassConfig.toString().trim().isEmpty()) {
                    agentConfiguration.setDataEndpointClass(endpointClassConfig.toString().trim());
                } else {
                    throw new DataEndpointAgentConfigurationException("Endpoint class name is not set in "
                            + DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME
                            + " for name: " + endpointNameConfig);
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.PUBLISHING_STRATEGY) != null) {
                    agentConfiguration.setPublishingStrategy(agentConfigurationHashMap.get(
                            DataAgentConstants.PUBLISHING_STRATEGY).toString().trim());
                }


                Object trustStorePathObject = agentConfigurationHashMap.get(DataAgentConstants.TRUST_STORE_PATH);
                if (trustStorePathObject != null) {
                    String trustStorePath = trustStorePathObject.toString().trim();
                    if (!trustStorePath.isEmpty()) {
                        agentConfiguration.setTrustStorePath(DataBridgeCommonsUtils.
                                replaceSystemProperty(trustStorePath));
                    }
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.TRUST_STORE_PASSWORD) != null) {
                    agentConfiguration.setTrustStorePassword(agentConfigurationHashMap.get(
                            DataAgentConstants.TRUST_STORE_PASSWORD).toString().trim());
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.QUEUE_SIZE) != null) {
                    agentConfiguration.setQueueSize(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.QUEUE_SIZE).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.BATCH_SIZE) != null) {
                    agentConfiguration.setBatchSize(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.BATCH_SIZE).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.CORE_POOL_SIZE) != null) {
                    agentConfiguration.setCorePoolSize(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.CORE_POOL_SIZE).toString().trim()));

                }

                if (agentConfigurationHashMap.get(DataAgentConstants.SOCKET_TIMEOUT_MS) != null) {
                    agentConfiguration.setSocketTimeoutMS(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.SOCKET_TIMEOUT_MS).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.MAX_POOL_SIZE) != null) {
                    agentConfiguration.setMaxPoolSize(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.MAX_POOL_SIZE).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.KEEP_ALIVE_TIME_INTERVAL_IN_POOL) != null) {
                    agentConfiguration.setKeepAliveTimeInPool(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.KEEP_ALIVE_TIME_INTERVAL_IN_POOL).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.RECONNETION_INTERVAL) != null) {
                    agentConfiguration.setReconnectionInterval(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.RECONNETION_INTERVAL).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.MAX_TRANSPORT_POOL_SIZE) != null) {
                    agentConfiguration.setMaxTransportPoolSize(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.MAX_TRANSPORT_POOL_SIZE).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.MAX_IDLE_CONNECTIONS) != null) {
                    agentConfiguration.setMaxIdleConnections(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.MAX_IDLE_CONNECTIONS).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.EVICTION_TIME_PERIOD) != null) {
                    agentConfiguration.setEvictionTimePeriod(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.EVICTION_TIME_PERIOD).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.MIN_IDLE_TIME_IN_POOL) != null) {
                    agentConfiguration.setMinIdleTimeInPool(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.MIN_IDLE_TIME_IN_POOL).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.SECURE_MAX_TRANSPORT_POOL_SIZE) != null) {
                    agentConfiguration.setSecureMaxTransportPoolSize(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.SECURE_MAX_TRANSPORT_POOL_SIZE).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.SECURE_MAX_IDLE_CONNECTIONS) != null) {
                    agentConfiguration.setSecureMaxIdleConnections(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.SECURE_MAX_IDLE_CONNECTIONS).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.SECURE_EVICTION_TIME_PERIOD) != null) {
                    agentConfiguration.setSecureEvictionTimePeriod(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.SECURE_EVICTION_TIME_PERIOD).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.SECURE_MIN_IDLE_TIME_IN_POOL) != null) {
                    agentConfiguration.setSecureMinIdleTimeInPool(Integer.parseInt(agentConfigurationHashMap.get(
                            DataAgentConstants.SECURE_MIN_IDLE_TIME_IN_POOL).toString().trim()));
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.SSL_ENABLED_PROTOCOLS) != null) {
                    agentConfiguration.setSslEnabledProtocols(agentConfigurationHashMap.get(
                            DataAgentConstants.SSL_ENABLED_PROTOCOLS).toString().trim());
                }

                if (agentConfigurationHashMap.get(DataAgentConstants.CIPHERS) != null) {
                    agentConfiguration.setCiphers(agentConfigurationHashMap.get(
                            DataAgentConstants.CIPHERS).toString().trim());
                }
                agents.add(agent);
            }
        } else {
            throw new DataEndpointAgentConfigurationException("Data Agents are not defined in " +
                    DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME);
        }

        return dataAgentsConfiguration;
    }

}
