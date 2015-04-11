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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;


public class MQTTAdapterPublisher {

    private static final Log log = LogFactory.getLog(MQTTAdapterPublisher.class);

    private MqttClient mqttClient;
    private MqttConnectOptions connectionOptions;
    private boolean cleanSession;
    private int keepAlive;
    private MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration;
    private String mqttClientId;
    private String topic;

    public MQTTAdapterPublisher(MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration,
                                String topic, String mqttClientId) {

        this.mqttBrokerConnectionConfiguration = mqttBrokerConnectionConfiguration;
        this.mqttClientId = mqttClientId;
        this.cleanSession = mqttBrokerConnectionConfiguration.isCleanSession();
        this.keepAlive = mqttBrokerConnectionConfiguration.getKeepAlive();
        this.topic = topic;

        //SORTING messages until the server fetches them
        String temp_directory = System.getProperty(MQTTEventAdapterConstants.ADAPTER_TEMP_DIRECTORY_NAME);
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(temp_directory);


        try {
            // Construct the connection options object that contains connection parameters
            // such as cleanSession and LWT
            connectionOptions = new MqttConnectOptions();
            connectionOptions.setCleanSession(cleanSession);
            connectionOptions.setKeepAliveInterval(keepAlive);
            if (this.mqttBrokerConnectionConfiguration.getBrokerPassword() != null) {
                connectionOptions
                        .setPassword(this.mqttBrokerConnectionConfiguration.getBrokerPassword().toCharArray());
            }
            if (this.mqttBrokerConnectionConfiguration.getBrokerUsername() != null) {
                connectionOptions.setUserName(this.mqttBrokerConnectionConfiguration.getBrokerUsername());
            }

            // Construct an MQTT blocking mode client
            mqttClient = new MqttClient(this.mqttBrokerConnectionConfiguration.getBrokerUrl(), this.mqttClientId,
                    dataStore);
            mqttClient.connect(connectionOptions);

        } catch (MqttException e) {
            log.error("Error occurred when constructing MQTT client for broker url : "
                    + mqttBrokerConnectionConfiguration.getBrokerUrl());
            handleException(e);
        }

    }

    public void publish(int qos, String payload) {
        try {
            // Create and configure a message
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);

            // Send the message to the server, control is not returned until
            // it has been delivered to the server meeting the specified
            // quality of service.
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            log.error("Error occurred when publishing message for MQTT server : "
                    + mqttClient.getServerURI());
            handleException(e);
        }
    }

    public void publish(String payload) {
        try {
            // Create and configure a message
            MqttMessage message = new MqttMessage(payload.getBytes());
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            log.error("Error occurred when publishing message for MQTT server : "
                    + mqttClient.getServerURI());
            handleException(e);
        }
    }

    public void close() throws OutputEventAdapterException {
        try {
            mqttClient.disconnect(1000);
            mqttClient.close();
        } catch (MqttException e) {
            throw new OutputEventAdapterException(e);
        }
    }

    private void handleException(MqttException e) {
        //Check for Client not connected exception code and throw ConnectionUnavailableException
        if (e.getReasonCode() == 32104) {
            throw new ConnectionUnavailableException(e);
        } else {
            throw new OutputEventAdapterRuntimeException(e);
        }
    }

}
