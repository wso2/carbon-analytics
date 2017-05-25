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
import org.wso2.carbon.databridge.agent.exception.DataEndpointSecurityException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.client.AbstractSecureClientPoolFactory;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

/**
 * This is a Binary Transport secure implementation for AbstractSecureClientPoolFactory to be used by BinaryEndpoint.
 */
public class BinarySecureClientPoolFactory extends AbstractSecureClientPoolFactory {
    private static Log log = LogFactory.getLog(BinarySecureClientPoolFactory.class);

    public BinarySecureClientPoolFactory(String trustStore, String trustStorePassword) {
        super(trustStore, trustStorePassword);
    }

    @Override
    public Object createClient(String protocol, String hostName, int port) throws DataEndpointException,
            DataEndpointSecurityException, DataEndpointAgentConfigurationException {
        if (protocol.equalsIgnoreCase(DataEndpointConfiguration.Protocol.SSL.toString())) {
            int timeout = AgentHolder.getInstance().getDataEndpointAgent(DataEndpointConstants.BINARY_DATA_AGENT_TYPE)
                    .getAgentConfiguration().getSocketTimeoutMS();
            String sslProtocols = AgentHolder.getInstance().getDataEndpointAgent(DataEndpointConstants.BINARY_DATA_AGENT_TYPE).
                    getAgentConfiguration().getSslEnabledProtocols();
            String ciphers = AgentHolder.getInstance().getDataEndpointAgent(DataEndpointConstants.BINARY_DATA_AGENT_TYPE).
                    getAgentConfiguration().getCiphers();

            try {
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) sslsocketfactory.createSocket(hostName, port);
                sslSocket.setSoTimeout(timeout);

                if (sslProtocols != null && sslProtocols.length() != 0) {
                    String [] sslProtocolsArray = sslProtocols.split(",");
                    sslSocket.setEnabledProtocols(sslProtocolsArray);
                }

                if (ciphers != null && ciphers.length() != 0) {
                    String [] ciphersArray = ciphers.split(",");
                    sslSocket.setEnabledCipherSuites(ciphersArray);
                } else {
                    sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
                }
                return sslSocket;
            } catch (IOException e) {
                throw new DataEndpointException("Error while opening socket to " + hostName + ":" + port + ". " +
                        e.getMessage(), e);
            }
        } else {
            throw new DataEndpointException("Unsupported protocol: " + protocol + ". Currently only " +
                    DataEndpointConfiguration.Protocol.SSL.toString() + " supported.");
        }
    }

    @Override
    public boolean validateClient(Object client) {
        Socket socket = (Socket) client;
        return socket.isConnected();
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
