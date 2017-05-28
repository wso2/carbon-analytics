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

package org.wso2.carbon.databridge.receiver.thrift.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiverFactory;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;

/**
 * Thrift Server Startup Implementation
 */
public class ThriftServerStartupImpl implements ThriftServerStartup {

    private static final Log log = LogFactory.getLog(ThriftServerStartupImpl.class);

    public void completingServerStartup() {
        try {
            ThriftDataReceiverConfiguration thriftDataReceiverConfiguration = new ThriftDataReceiverConfiguration(
                    ServiceHolder.getDataBridgeReceiverService().getInitialConfig(),
                    ServiceHolder.getCarbonRuntime().getConfiguration().getPortsConfig().getOffset());

            if (ServiceHolder.getDataReceiver() == null) {
                ServiceHolder.setDataReceiver(new ThriftDataReceiverFactory().createAgentServer(
                        thriftDataReceiverConfiguration, ServiceHolder.getDataBridgeReceiverService()));
                // TODO: 1/27/17 Hack to get host name. Change later
                /*String serverUrl = CarbonUtils.getServerURL(ServiceHolder.getServerConfiguration(),
                        ServiceHolder.getConfigurationContext().getServerConfigContext());*/
                String hostName = thriftDataReceiverConfiguration.getReceiverHostName();
                if (null == hostName) {
                    hostName = HostAddressFinder.findAddress("localhost");
                    /*try {
                        hostName = new URL(serverUrl).getHost();
                    } catch (MalformedURLException e) {
                        hostName = HostAddressFinder.findAddress("localhost");
                        if (!serverUrl.matches("local:/.services/")) {
                            log.info("The server url :" + serverUrl + " is using local, hence hostname is assigned as '"
                                    + hostName + "'");
                        }
                    }*/
                }
                ServiceHolder.getDataReceiver().start(hostName);
            }
        } catch (DataBridgeException e) {
            log.error("Can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        } catch (Throwable e) {
            log.error("Unexpected Error in starting Agent Server ", e);
        }
    }
}
