/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core.ha.transport;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

/**
 * The abstract class that needs to be implemented when supporting a new non-secure transport
 * to mainly create, validate and terminate  the client to the endpoint.
 */

public class ClientPoolFactory extends BaseKeyedPoolableObjectFactory {
    private String hostname;
    private int port;

    public ClientPoolFactory(String host, int port) {
        this.hostname = host;
        this.port = port;
    }

    @Override
    public Object makeObject(Object key) throws ConnectionUnavailableException {
        TCPNettyClient tcpNettyClient = new TCPNettyClient();
        tcpNettyClient.connect(hostname, port);
        return tcpNettyClient;
    }

    /**
     * Make a connection to the receiver and return the client.
     *
     * @param protocol protocol that is used to connect to the endpoint
     * @param hostName hostname of the endpoint
     * @param port     port of the endpoint that is listening to
     * @return A valid client which has connected to the receiver and can be used
     * for rest of the operations regarding the endpoint.
     */
    public Object createClient(String protocol, String hostName, int port) {
        return null;
    }

    @Override
    public boolean validateObject(Object key, Object obj) {
        return ((TCPNettyClient) obj).isActive();
    }

//    /**
//     * Check the validity of the client whether it's in the position to make the
//     * communication with endpoint.
//     *
//     * @param client Client object which needs to be validated.
//     * @return Returns true/false based on the client is valid or invalid.
//     */
//    public boolean validateClient(Object client) {
//        return false;
//    }

    public void destroyObject(Object key, Object obj) {
        TCPNettyClient tcpNettyClient = ((TCPNettyClient) obj);
        tcpNettyClient.disconnect();
        tcpNettyClient.shutdown();
    }

//    /**
//     * Terminates the connection between the client and the endpoint.
//     *
//     * @param client The client which needs to be terminated.
//     */
//    public void terminateClient(Object client){
//
//    }

}
