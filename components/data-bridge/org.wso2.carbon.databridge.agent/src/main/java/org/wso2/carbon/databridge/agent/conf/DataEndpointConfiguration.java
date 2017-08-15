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
package org.wso2.carbon.databridge.agent.conf;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;

/**
 * Data Endpoint Configuration
 */
public class DataEndpointConfiguration {

    private String receiverURL;

    private String authURL;

    private String username;

    private String password;

    private GenericKeyedObjectPool transportPool;

    private GenericKeyedObjectPool securedTransportPool;

    private int batchSize;

    private String publisherKey;

    private String authKey;

    private String sessionId;

    private int corePoolSize;

    private int maxPoolSize;

    private int keepAliveTimeInPool;

    public enum Protocol {
        TCP, SSL;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public DataEndpointConfiguration(String receiverURL, String authURL, String username, String password,
                                     GenericKeyedObjectPool transportPool,
                                     GenericKeyedObjectPool securedTransportPool,
                                     int batchSize, int corePoolSize, int maxPoolSize, int keepAliveTimeInPool) {
        this.receiverURL = receiverURL;
        this.authURL = authURL;
        this.username = username;
        this.password = password;
        this.transportPool = transportPool;
        this.securedTransportPool = securedTransportPool;
        this.publisherKey = this.receiverURL + DataEndpointConstants.SEPARATOR + username;
        this.authKey = this.authURL + DataEndpointConstants.SEPARATOR + username;
        this.batchSize = batchSize;
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTimeInPool = keepAliveTimeInPool;
    }

    public String getReceiverURL() {
        return receiverURL;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthURL() {
        return authURL;
    }

    public String getPassword() {
        return password;
    }

    public String toString() {
        return "ReceiverURL: " + receiverURL + "," +
                "Authentication URL: " + authURL + "," +
                "Username: " + username;
    }

    public String getPublisherKey() {
        return publisherKey;
    }

    public String getAuthKey() {
        return authKey;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public GenericKeyedObjectPool getTransportPool() {
        return transportPool;
    }

    public GenericKeyedObjectPool getSecuredTransportPool() {
        return securedTransportPool;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getKeepAliveTimeInPool() {
        return keepAliveTimeInPool;
    }

    public int getBatchSize() {
        return batchSize;
    }
}

