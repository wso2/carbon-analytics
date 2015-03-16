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
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.conf.DataAgentsConfiguration;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The holder for all Agents created and this is singleton class.
 * The Agents will be loaded by reading a configuration file data-agent-conf.xml default.
 */

public class AgentHolder {

    private static Log log = LogFactory.getLog(AgentHolder.class);

    private static String configPath;

    private static AgentHolder instance;

    private Map<String, DataEndpointAgent> dataEndpointAgents;

    /**
     * If there is no data publisher type is passed from,then the default Agent/Publisher will be used.
     * The first element in the data-agent-conf.xml is taken as default data publisher type.
     */
    private String defaultDataEndpointAgentName;

    private AgentHolder() throws DataEndpointAgentConfigurationException {
        try {
            dataEndpointAgents = new HashMap<String, DataEndpointAgent>();
            DataAgentsConfiguration dataAgentsConfiguration = loadConfiguration();
            boolean isDefault = true;
            for (AgentConfiguration agentConfiguration : dataAgentsConfiguration.getAgentConfigurations()) {
                addAgentConfiguration(agentConfiguration, isDefault);
                if (isDefault) isDefault = false;
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

    public synchronized DataEndpointAgent getDataEndpointAgent(String type)
            throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = this.dataEndpointAgents.get(type);
        if (agent == null) {
            throw new DataEndpointAgentConfigurationException("No data agent configured for the type: " + type);
        }
        return agent;
    }

    /**
     * Loading by data-agent-conf.xml via JAXB, and validating the configurations.
     *
     * @return Loaded DataAgentsConfiguration from config file.
     * @throws DataEndpointAgentConfigurationException
     */
    private DataAgentsConfiguration loadConfiguration()
            throws DataEndpointAgentConfigurationException {
        if (configPath == null) configPath = CarbonUtils.getCarbonConfigDirPath()
                + DataEndpointConstants.DATA_AGENT_CONF_FILE_PATH;
        try {
            File file = new File(configPath);
            JAXBContext jaxbContext = JAXBContext.newInstance(DataAgentsConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            DataAgentsConfiguration dataAgentsConfiguration = (DataAgentsConfiguration)
                    jaxbUnmarshaller.unmarshal(file);
            dataAgentsConfiguration.validateConfigurations();
            return dataAgentsConfiguration;
        } catch (JAXBException e) {
            throw new DataEndpointAgentConfigurationException("Error while loading the configuration file "
                    + configPath, e);
        }
    }


    private void addAgentConfiguration(AgentConfiguration agentConfiguration, boolean defaultAgent)
            throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = new DataEndpointAgent(agentConfiguration);
        dataEndpointAgents.put(agent.getAgentConfiguration().getDataEndpointName(), agent);
        if (defaultAgent) {
            defaultDataEndpointAgentName = agent.getAgentConfiguration().getDataEndpointName();
        }
    }

    /**
     * Returns the default agent,and the first element in the data-agent-conf.xml
     * is taken as default data publisher type.
     *
     * @return DataEndpointAgent for the default endpoint name.
     * @throws DataEndpointAgentConfigurationException
     */
    public DataEndpointAgent getDefaultDataEndpointAgent() throws DataEndpointAgentConfigurationException {
        return getDataEndpointAgent(defaultDataEndpointAgentName);
    }

    /**
     * Set the data-agent-conf.xml path from which the Agents for all endpoint types will be loaded.
     * This is a one time operation, and if you are changing form default config path,
     * then it needs to be done as first step when the JVM started.
     *
     * @param configPath The path of the data-bridge-conf.xml
     */
    public static void setConfigPath(String configPath) {
        AgentHolder.configPath = configPath;
    }
}
