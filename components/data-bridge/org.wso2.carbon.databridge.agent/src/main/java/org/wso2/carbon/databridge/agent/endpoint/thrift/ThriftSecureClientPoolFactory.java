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
package org.wso2.carbon.databridge.agent.endpoint.thrift;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointSecurityException;
import org.wso2.carbon.databridge.agent.client.AbstractSecureClientPoolFactory;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;

import javax.net.ssl.SSLSocket;


/**
 * This is a Thrift secure transport implementation for AbstractSecureClientPoolFactory
 * to be used by the Thrift Endpoint.
 */

public class ThriftSecureClientPoolFactory extends AbstractSecureClientPoolFactory {

    private TSSLTransportFactory.TSSLTransportParameters params;

    public ThriftSecureClientPoolFactory(String trustStore, String trustStorePassword) {
        super(trustStore, trustStorePassword);
        params = new TSSLTransportFactory.TSSLTransportParameters();
        params.setTrustStore(getTrustStore(), getTrustStorePassword());
    }

    @Override
    public Object createClient(String protocol, String hostName, int port) throws
            DataEndpointSecurityException, DataEndpointAgentConfigurationException {
        if (protocol.equalsIgnoreCase(DataEndpointConfiguration.Protocol.SSL.toString())) {
            int timeout = AgentHolder.getInstance().getDataEndpointAgent(DataEndpointConstants.THRIFT_DATA_AGENT_TYPE).
                    getAgentConfiguration().getSocketTimeoutMS();
            String sslProtocols = AgentHolder.getInstance().getDataEndpointAgent(DataEndpointConstants.THRIFT_DATA_AGENT_TYPE).
                    getAgentConfiguration().getSslEnabledProtocols();
            String ciphers = AgentHolder.getInstance().getDataEndpointAgent(DataEndpointConstants.THRIFT_DATA_AGENT_TYPE).
                    getAgentConfiguration().getCiphers();
            try {
                TTransport receiverTransport = TSSLTransportFactory.
                        getClientSocket(hostName, port, timeout, params );

                TSocket tSocket = (TSocket) receiverTransport;
                SSLSocket sslSocket = (SSLSocket) tSocket.getSocket();
                if (sslProtocols != null && sslProtocols.length() != 0) {
                    String [] sslProtocolsArray = sslProtocols.split(",");
                    sslSocket.setEnabledProtocols(sslProtocolsArray);
                }

                if (ciphers != null && ciphers.length() != 0) {
                    String [] ciphersArray = ciphers.split(",");
                    sslSocket.setEnabledCipherSuites(ciphersArray);
                }

                TProtocol tProtocol = new TBinaryProtocol(receiverTransport);
                return new ThriftSecureEventTransmissionService.Client(tProtocol);
            } catch (TTransportException e) {
                throw new DataEndpointSecurityException("Error while trying to connect to " +
                        protocol + "://" + hostName + ":" + port, e);
            }
        }
        throw new DataEndpointSecurityException("Unsupported protocol :" + protocol
                + " used to authenticate the client, only " + DataEndpointConfiguration.Protocol.SSL.toString()
                + " is supported");
    }

    @Override
    public boolean validateClient(Object client) {
        ThriftSecureEventTransmissionService.Client thriftClient = (ThriftSecureEventTransmissionService.Client) client;
        return thriftClient.getOutputProtocol().getTransport().isOpen();
    }

    @Override
    public void terminateClient(Object client) {
        ThriftSecureEventTransmissionService.Client thriftClient = (ThriftSecureEventTransmissionService.Client) client;
        thriftClient.getOutputProtocol().getTransport().close();
    }
}
