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
import org.apache.thrift.transport.*;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.client.AbstractClientPoolFactory;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;

/**
 * This is a Thrift Transport implementation for AbstractClientPoolFactory for Thrift Endpoint.
 */
public class ThriftClientPoolFactory extends AbstractClientPoolFactory {

    @Override
    public Object createClient(String protocol, String hostName, int port) throws DataEndpointException,
            DataEndpointAgentConfigurationException {
        if (protocol.equalsIgnoreCase(DataEndpointConfiguration.Protocol.TCP.toString())) {
            int socketTimeout = AgentHolder.getInstance().getDataEndpointAgent(DataEndpointConstants.THRIFT_DATA_AGENT_TYPE).
                    getAgentConfiguration().getSocketTimeoutMS();
            TTransport receiverTransport = new TSocket(hostName, port, socketTimeout);
            TProtocol tProtocol = new TBinaryProtocol(receiverTransport);
            ThriftEventTransmissionService.Client client = new ThriftEventTransmissionService.Client(tProtocol);
            try {
                receiverTransport.open();
            } catch (TTransportException e) {
                throw new DataEndpointException("Error while making the connection." + e.getMessage(), e);
            }
            return client;
        }
        throw new DataEndpointException("Unsupported protocol :" + protocol
                + " used to authenticate the client, only " + DataEndpointConfiguration.Protocol.TCP.toString()
                + " is supported");
    }

    @Override
    public boolean validateClient(Object client) {
        ThriftEventTransmissionService.Client thriftClient = (ThriftEventTransmissionService.Client) client;
        return thriftClient.getOutputProtocol().getTransport().isOpen();
    }

    @Override
    public void terminateClient(Object client) {
        ThriftEventTransmissionService.Client thriftClient = (ThriftEventTransmissionService.Client) client;
        thriftClient.getOutputProtocol().getTransport().close();
    }
}
