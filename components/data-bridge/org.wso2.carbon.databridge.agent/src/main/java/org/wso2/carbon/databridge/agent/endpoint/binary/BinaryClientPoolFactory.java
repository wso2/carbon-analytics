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
package org.wso2.carbon.databridge.agent.endpoint.binary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.client.AbstractClientPoolFactory;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;

import java.io.IOException;
import java.net.Socket;

/**
 * This class implements AbstractClientPoolFactory to handle the Binary transport related connections.
 *
 */
public class BinaryClientPoolFactory extends AbstractClientPoolFactory {
    private static Log log = LogFactory.getLog(BinaryClientPoolFactory.class);

    @Override
    public Object createClient(String protocol, String hostName, int port) throws DataEndpointException,
            DataEndpointAgentConfigurationException {
        if (protocol.equalsIgnoreCase(DataEndpointConfiguration.Protocol.TCP.toString())) {
            int timeout = AgentHolder.getInstance().getDataEndpointAgent(DataEndpointConstants.BINARY_DATA_AGENT_TYPE)
                    .getAgentConfiguration().getSocketTimeoutMS();
            try {
                Socket socket =  new Socket(hostName, port);
                socket.setSoTimeout(timeout);
                return socket;
            } catch (IOException e) {
                throw new DataEndpointException("Error while opening socket to " + hostName + ":" + port + ". " +
                        e.getMessage(), e);
            }
        } else {
            throw new DataEndpointException("Unsupported protocol: " + protocol + ". Currently only " +
                    DataEndpointConfiguration.Protocol.TCP.toString() + " supported.");
        }
    }

    @Override
    public boolean validateClient(Object client) {
        return ((Socket) client).isConnected();
    }

    @Override
    public void terminateClient(Object client) {
        Socket socket = null;
        try {
            socket = (Socket) client;
            socket.close();
        } catch (IOException e) {
            log.warn("Cannot close the socket successfully from " + socket.getLocalAddress().getHostAddress()
                    + ":" + socket.getPort());
        }
    }
}
