package org.wso2.carbon.databridge.agent.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: suho
 * Date: 11/20/12
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentHolder {
    private static Agent agent;
    private static AgentConfiguration agentConfiguration;

    private static final Log log = LogFactory.getLog(AgentHolder.class);

    public static Agent getAgent() {
        return agent;
    }

    public static void setAgent(Agent agent) {
        AgentHolder.agent = agent;
    }

    public static AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    public static void setAgentConfiguration(AgentConfiguration agentConfiguration) {
        AgentHolder.agentConfiguration = agentConfiguration;
    }

    public static Agent getOrCreateAgent() {
        if (agent == null) {
            if (agentConfiguration == null) {
                agentConfiguration = AgentBuilder.loadAgentConfiguration();
                log.info("Agent created !");
            }
            agent = new Agent(agentConfiguration);
        }
        return agent;
    }
}

