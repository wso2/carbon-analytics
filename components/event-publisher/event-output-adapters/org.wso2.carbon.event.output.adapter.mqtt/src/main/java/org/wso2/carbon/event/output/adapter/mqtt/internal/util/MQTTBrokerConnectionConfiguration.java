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
package org.wso2.carbon.event.output.adapter.mqtt.internal.util;

public class MQTTBrokerConnectionConfiguration {

    private String brokerUsername;
    private String brokerPassword;
    private String brokerUrl;
    private boolean cleanSession = true;
    private int keepAlive;

    public String getBrokerPassword() {
        return brokerPassword;
    }

    public String getBrokerUsername() {
        return brokerUsername;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public String getBrokerUrl() {
        return brokerUrl;

    }
    public int getKeepAlive() {
        return keepAlive;
    }

    public MQTTBrokerConnectionConfiguration(String brokerUrl, String brokerUsername,
                                             String brokerPassword, int keepAlive, String cleanSession) {
        this.brokerUsername = brokerUsername;
        this.brokerPassword = brokerPassword;
        this.brokerUrl = brokerUrl;
        this.keepAlive = keepAlive;

        if (cleanSession != null) {
            this.cleanSession = Boolean.parseBoolean(cleanSession);
        }

    }
}
