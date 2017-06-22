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
package org.wso2.carbon.databridge.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.conf.Agent;
import org.wso2.carbon.databridge.agent.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.conf.DataAgentConfigurationFileResolver;
import org.wso2.carbon.databridge.agent.conf.DataAgentsConfiguration;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.internal.DataAgentServiceValueHolder;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The holder for all Agents created and this is singleton class.
 * The Agents will be loaded by reading a configuration file data.agent.config.yaml default.
 */

public class AgentHolder {

    private static Log log = LogFactory.getLog(AgentHolder.class);
    private static String configPath;
    private static AgentHolder instance;
    private Map<String, DataEndpointAgent> dataEndpointAgents;
    /**
     * If there is no data publisher type is passed from,then the default Agent/Publisher will be used.
     * The first element in the data.agent.config.yaml is taken as default data publisher type.
     */
    private String defaultDataEndpointAgentName;

    private AgentHolder() throws DataEndpointAgentConfigurationException {
        try {
            dataEndpointAgents = new HashMap<String, DataEndpointAgent>();
            DataAgentsConfiguration dataAgentsConfiguration = loadConfiguration();
            boolean isDefault = true;
            for (Agent agent : dataAgentsConfiguration.getAgents()) {
                addAgentConfiguration(agent.getAgentConfiguration(), isDefault);
                if (isDefault) {
                    isDefault = false;
                }
            }
        } catch (DataEndpointAgentConfigurationException e) {
            log.error("Unable to complete initialization of agents." + e.getMessage(), e);
            throw e;
        }
    }

    public synchronized static AgentHolder getInstance() throws DataEndpointAgentConfigurationException {
        if (instance == null) {
            instance = new AgentHolder();
        }
        return instance;
    }

    /**
     * Set the data-agent-config.xml path from which the Agents for all endpoint types will be loaded.
     * This is a one time operation, and if you are changing form default config path,
     * then it needs to be done as first step when the JVM started.
     *
     * @param configPath The path of the data-bridge-conf.xml
     */
    public static void setConfigPath(String configPath) {
        AgentHolder.configPath = configPath;
    }

    public synchronized static void shutdown() throws DataEndpointException {
        if (instance != null) {
            for (DataEndpointAgent dataEndpointAgent : instance.dataEndpointAgents.values()) {
                dataEndpointAgent.shutDown();
            }
            instance = null;
        }
    }

    public synchronized DataEndpointAgent getDataEndpointAgent(String type)
            throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = this.dataEndpointAgents.get(type.toLowerCase());
        if (agent == null) {
            throw new DataEndpointAgentConfigurationException("No data agent configured for the type: " + type.toLowerCase());
        }
        return agent;
    }

    /**
     * Loading by data.agent.config.yaml, and validating the configurations.
     *
     * @return Loaded DataAgentsConfiguration from config file.
     * @throws DataEndpointAgentConfigurationException Exception to be thrown for DataEndpointAgentConfiguration which
     *                                                 was specified in the data.agent.config.yaml.
     */
    private DataAgentsConfiguration loadConfiguration()
            throws DataEndpointAgentConfigurationException {

        try {
            DataAgentsConfiguration dataAgentsConfiguration = null;
            if (configPath == null) {
                ConfigProvider configProvider = DataAgentServiceValueHolder.getConfigProvider();
                if (configProvider != null) {
                    dataAgentsConfiguration = DataAgentConfigurationFileResolver.
                            resolveAndSetDataAgentConfiguration
                                    ((LinkedHashMap) configProvider.
                                            getConfigurationMap(DataEndpointConstants.DATA_AGENT_CONFIG_NAMESPACE));
                }
            } else {
                File file = new File(configPath);
                if (file.exists()) {
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        Yaml yaml = new Yaml();
                        dataAgentsConfiguration = DataAgentConfigurationFileResolver.
                                resolveAndSetDataAgentConfiguration
                                        ((LinkedHashMap) ((LinkedHashMap) yaml.load(fileInputStream)).get(
                                                DataEndpointConstants.DATA_AGENT_CONFIG_NAMESPACE));

                    } catch (IOException e) {
                        throw new DataEndpointAgentConfigurationException("Exception when loading databridge " +
                                "agent configuration.", e);
                    }
                }
            }

            if (dataAgentsConfiguration != null) {
                for (Agent agent : dataAgentsConfiguration.getAgents()) {
                    AgentConfiguration agentConfiguration = agent.getAgentConfiguration();

                    if (agentConfiguration.getTrustStorePath() == null ||
                            agentConfiguration.getTrustStorePath().isEmpty()) {
                        agentConfiguration.setTrustStorePath(System.getProperty("javax.net.ssl.trustStore"));
                        if (agentConfiguration.getTrustStorePath() == null) {
                            throw new DataEndpointAgentConfigurationException("No trustStore found");
                        }
                    }

                    if (agentConfiguration.getTrustStorePassword() == null ||
                            agentConfiguration.getTrustStorePassword().isEmpty()) {
                        agentConfiguration.setTrustStorePassword(System.getProperty(
                                "javax.net.ssl.trustStorePassword"));
                        if (agentConfiguration.getTrustStorePassword() == null) {
                            throw new DataEndpointAgentConfigurationException("No trustStore password found");
                        }
                    }

                }
            }
            return dataAgentsConfiguration;

        } catch (Exception e) {
            throw new DataEndpointAgentConfigurationException("Error while loading the configuration file "
                    + configPath, e);
        }
    }

    private void addAgentConfiguration(AgentConfiguration agentConfiguration, boolean defaultAgent)
            throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = new DataEndpointAgent(agentConfiguration);
        dataEndpointAgents.put(agent.getAgentConfiguration().getName().toLowerCase(), agent);
        if (defaultAgent) {
            defaultDataEndpointAgentName = agent.getAgentConfiguration().getName();
        }
    }

    /**
     * Returns the default agent,and the first element in the data.agent.config.yaml
     * is taken as default data publisher type.
     *
     * @return DataEndpointAgent for the default endpoint name.
     * @throws DataEndpointAgentConfigurationException Exception to be thrown for DataEndpointAgentConfiguration
     *                                                 which was specified in the data.agent.config.yaml.
     */
    public DataEndpointAgent getDefaultDataEndpointAgent() throws DataEndpointAgentConfigurationException {
        return getDataEndpointAgent(defaultDataEndpointAgentName);
    }
}
