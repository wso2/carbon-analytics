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
import org.apache.thrift.protocol.TCompactProtocol;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiverFactory;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftEventTransmissionServiceImpl;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftEventTransmissionServlet;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftSecureEventTransmissionServiceImpl;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftSecureEventTransmissionServlet;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

public class ThriftServerStartupObserver implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(ThriftServerStartupObserver.class);
    @Override
    public void completingServerStartup() {
        try {
            ThriftDataReceiverConfiguration thriftDataReceiverConfiguration = new
                    ThriftDataReceiverConfiguration(ServiceHolder.getDataBridgeReceiverService().getInitialConfig());

            if (ServiceHolder.getDataReceiver() == null) {
                ServiceHolder.setDataReceiver(new ThriftDataReceiverFactory().createAgentServer(thriftDataReceiverConfiguration, ServiceHolder.getDataBridgeReceiverService()));
                String serverUrl = CarbonUtils.getServerURL(ServiceHolder.getServerConfiguration(), ServiceHolder.getConfigurationContext().getServerConfigContext());
                String hostName = thriftDataReceiverConfiguration.getReceiverHostName();
                if (null == hostName) {
                    try {
                        hostName = new URL(serverUrl).getHost();
                    } catch (MalformedURLException e) {
                        hostName = HostAddressFinder.findAddress("localhost");
                        if (!serverUrl.matches("local:/.*/services/")) {
                            log.info("The server url :" + serverUrl + " is using local, hence hostname is assigned as '" + hostName + "'");
                        }
                    }
                }
                ServiceHolder.getDataReceiver().start(hostName);
                ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl> processor = new ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl>(
                        new ThriftEventTransmissionServiceImpl(ServiceHolder.getDataBridgeReceiverService()));
                TCompactProtocol.Factory inProtFactory = new TCompactProtocol.Factory();
                TCompactProtocol.Factory outProtFactory = new TCompactProtocol.Factory();

                ServiceHolder.getHttpServiceInstance().registerServlet("/thriftReceiver",
                        new ThriftEventTransmissionServlet(processor, inProtFactory,
                                outProtFactory),
                        new Hashtable(),
                        ServiceHolder.getHttpServiceInstance().createDefaultHttpContext());

                ThriftSecureEventTransmissionService.Processor<ThriftSecureEventTransmissionServiceImpl> authProcessor = new ThriftSecureEventTransmissionService.Processor<ThriftSecureEventTransmissionServiceImpl>(
                        new ThriftSecureEventTransmissionServiceImpl(ServiceHolder.getDataBridgeReceiverService()));
                ServiceHolder.getHttpServiceInstance().registerServlet("/securedThriftReceiver",
                        new ThriftSecureEventTransmissionServlet(authProcessor, inProtFactory,
                                outProtFactory),
                        new Hashtable(),
                        ServiceHolder.getHttpServiceInstance().createDefaultHttpContext());

            }
        } catch (DataBridgeException e) {
            log.error("Can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        } catch (Throwable e) {
            log.error("Error in starting Agent Server ", e);
        }
    }

    @Override
    public void completedServerStartup() {

    }
}
