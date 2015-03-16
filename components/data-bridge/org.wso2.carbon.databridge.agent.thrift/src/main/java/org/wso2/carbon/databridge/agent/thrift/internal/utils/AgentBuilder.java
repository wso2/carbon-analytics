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
package org.wso2.carbon.databridge.agent.thrift.internal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.internal.conf.ThriftAgentConfiguration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Helper class to build Agent's configurations
 */
public final class AgentBuilder {

    private static final Log log = LogFactory.getLog(AgentBuilder.class);

    private AgentBuilder() {

    }

    /**
     * Helper method to load the agent config
     *
     * @return the Agent configuration
     */
    public static AgentConfiguration loadAgentConfiguration() {

        ThriftAgentConfiguration agentConfig = loadConfigXML();
        if (agentConfig != null) {
            return buildAgentConfiguration(agentConfig);
        }
        return new AgentConfiguration();
    }

    /**
     * Helper method to load the agent config
     *
     * @return ThriftAgentConfiguration representation of the agent config in xml file
     */
    private static ThriftAgentConfiguration loadConfigXML() {

        String carbonHome = System.getProperty(AgentConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + AgentConstants.AGENT_CONF_DIR + File.separator
                      + AgentConstants.AGENT_CONF;

        // if the agent config file not exists then simply return null.
        File agentConfigFile = new File(path);
        if (!agentConfigFile.exists()) {
            return null;
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ThriftAgentConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return (ThriftAgentConfiguration)
                    jaxbUnmarshaller.unmarshal(agentConfigFile);
        } catch (JAXBException e) {
            String errorMessage = "Unable to unmarshal config xml.";
            log.error(errorMessage, e);
        }
        return null;
    }

    private static AgentConfiguration buildAgentConfiguration(
            ThriftAgentConfiguration agentServerConfig) {

        return buildReceiverConfiguration(agentServerConfig);
    }

    private static AgentConfiguration buildReceiverConfiguration(
            ThriftAgentConfiguration agentServerConfig) {

        AgentConfiguration agentConfiguration = new AgentConfiguration();

        agentConfiguration.setMaxTransportPoolSize(agentServerConfig.getMaxTransportPoolSize());
        agentConfiguration.setMaxIdleConnections(agentServerConfig.getMaxIdleConnections());
        agentConfiguration.setMaxMessageBundleSize(agentServerConfig.getMaxMessageBundleSize());
        agentConfiguration.setMinIdleTimeInPool(agentServerConfig.getMinIdleTimeInPool());
        agentConfiguration.setBufferedEventsSize(agentServerConfig.getBufferedEventsSize());
        agentConfiguration.setPoolSize(agentServerConfig.getPoolSize());
        agentConfiguration.setMaxPoolSize((agentServerConfig.getMaxPoolSize() != 0) ? agentServerConfig.getMaxPoolSize() : 50);
        agentConfiguration.setEvictionTimePeriod(agentServerConfig.getEvictionTimePeriod());
        agentConfiguration.setSecureEvictionTimePeriod(agentServerConfig.getSecureEvictionTimePeriod());
        agentConfiguration.setSecureMaxIdleConnections(agentServerConfig.getSecureMaxIdleConnections());
        agentConfiguration.setSecureMaxTransportPoolSize(agentServerConfig.getSecureMaxTransportPoolSize());
        agentConfiguration.setSecureMinIdleTimeInPool(agentServerConfig.getSecureMinIdleTimeInPool());
        agentConfiguration.setAsyncDataPublisherBufferedEventSize(agentServerConfig.getAsyncDataPublisherBufferedEventSize());
        agentConfiguration.setReconnectionInterval(agentServerConfig.getLoadBalancingReconnectionInterval());

        return agentConfiguration;
    }
}
