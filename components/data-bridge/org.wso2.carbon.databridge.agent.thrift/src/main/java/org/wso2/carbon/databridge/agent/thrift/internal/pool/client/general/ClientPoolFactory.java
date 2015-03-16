/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.databridge.agent.thrift.internal.pool.client.general;


import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.databridge.agent.thrift.conf.ReceiverConfiguration;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;

import java.net.SocketException;

public class ClientPoolFactory extends BaseKeyedPoolableObjectFactory {

    @Override
    public ThriftEventTransmissionService.Client makeObject(Object key) throws TTransportException {
        String[] keyElements = key.toString().split(AgentConstants.SEPARATOR);
        if (keyElements[0].equals(ReceiverConfiguration.Protocol.TCP.toString())) {

            String[] hostNameAndPort = keyElements[1].split(AgentConstants.HOSTNAME_AND_PORT_SEPARATOR);

            TTransport receiverTransport = null;
            try {
                receiverTransport = new TSocket(HostAddressFinder.findAddress(hostNameAndPort[0]),
                                                           Integer.parseInt(hostNameAndPort[1]));
            } catch (SocketException ignored) {
                //already checked
            }
            TProtocol protocol = new TBinaryProtocol(receiverTransport);
            ThriftEventTransmissionService.Client client = new ThriftEventTransmissionService.Client(protocol);
            receiverTransport.open();

            return client;
        } else {
            THttpClient client = new THttpClient("http://" + keyElements[1] + "/thriftReceiver");
            TProtocol protocol = new TCompactProtocol(client);
            ThriftEventTransmissionService.Client publisherClient = new ThriftEventTransmissionService.Client(protocol);
            client.open();
            return publisherClient;
        }
    }

    @Override
    public boolean validateObject(Object key, Object obj) {
        ThriftEventTransmissionService.Client client = (ThriftEventTransmissionService.Client) obj;
        return client.getOutputProtocol().getTransport().isOpen();
    }

    public void destroyObject(Object key, Object obj)  {
      ThriftEventTransmissionService.Client client = (ThriftEventTransmissionService.Client) obj;
      client.getOutputProtocol().getTransport().close();
    }


}
