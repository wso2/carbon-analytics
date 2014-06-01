/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.receiver.thrift.internal.utils;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;

/**
 * Helper class to build Agent Server Initial Configurations
 */
public final class ThriftDataReceiverBuilder {

    private static final Log log = LogFactory.getLog(ThriftDataReceiverBuilder.class);

    private ThriftDataReceiverBuilder() {
    }


    private static void populatePorts(OMElement config,
                                      int portOffset,
                                      ThriftDataReceiverConfiguration thriftDataReceiverConfiguration) {
        OMElement secureDataReceiverPort = config.getFirstChildWithName(
                new QName(DataBridgeConstants.DATA_BRIDGE_NAMESPACE,
                          ThriftDataReceiverConstants.SECURE_PORT_ELEMENT));
        if (secureDataReceiverPort != null) {
            try {
                thriftDataReceiverConfiguration.setSecureDataReceiverPort(Integer
                                                                                  .parseInt(secureDataReceiverPort.getText()) + portOffset);
            } catch (NumberFormatException ignored) {

            }
        }
        OMElement receiverPort = config.getFirstChildWithName(
                new QName(DataBridgeConstants.DATA_BRIDGE_NAMESPACE,
                          ThriftDataReceiverConstants.PORT_ELEMENT));
        if (receiverPort != null) {
            try {
                thriftDataReceiverConfiguration.setDataReceiverPort(Integer
                                                                            .parseInt(receiverPort.getText()) + portOffset);
            } catch (NumberFormatException ignored) {

            }
        }
    }

    private static void populateHostName(OMElement config,
                                         ThriftDataReceiverConfiguration thriftDataReceiverConfiguration) {
        OMElement receiverHostName = config.getFirstChildWithName(
                new QName(DataBridgeConstants.DATA_BRIDGE_NAMESPACE,
                          ThriftDataReceiverConstants.RECEIVER_HOST_NAME));
        if (receiverHostName != null && receiverHostName.getText() != null
            && !receiverHostName.getText().trim().equals("")) {
            thriftDataReceiverConfiguration.setReceiverHostName(receiverHostName.getText());
        }
    }


    public static int readPortOffset() {
        return CarbonUtils.
                getPortFromServerConfig(ThriftDataReceiverConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }


    public static void populateConfigurations(int portOffset,
                                              ThriftDataReceiverConfiguration thriftDataReceiverConfiguration,
                                              OMElement initialConfig) {
        if (initialConfig != null) {
            OMElement thriftReceiverConfig = initialConfig.getFirstChildWithName(
                    new QName(DataBridgeConstants.DATA_BRIDGE_NAMESPACE,
                              ThriftDataReceiverConstants.THRIFT_EVENT_RECEIVER_ELEMENT));
            if (thriftReceiverConfig != null) {
                populatePorts(thriftReceiverConfig, portOffset, thriftDataReceiverConfiguration);
                populateHostName(thriftReceiverConfig, thriftDataReceiverConfiguration);
            }
        }
    }
}
