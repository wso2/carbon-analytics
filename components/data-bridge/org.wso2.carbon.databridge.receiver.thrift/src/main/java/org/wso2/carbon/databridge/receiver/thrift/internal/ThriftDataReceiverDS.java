/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.receiver.thrift.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiverFactory;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.internal.utils.ThriftDataReceiverBuilder;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftEventTransmissionServiceImpl;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftEventTransmissionServlet;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftSecureEventTransmissionServiceImpl;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftSecureEventTransmissionServlet;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

/**
 * @scr.component name="thriftdatareceiver.component" immediate="true"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="configuration.context"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="databridge.core"
 * interface="org.wso2.carbon.databridge.core.DataBridgeReceiverService"
 * cardinality="1..1" policy="dynamic" bind="setDataBridgeReceiverService" unbind="unsetDatabridgeReceiverService"
 * @scr.reference name="http.service"
 * interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic"  bind="setHttpService" unbind="unsetHttpService"
 */
public class ThriftDataReceiverDS {
    private static final Log log = LogFactory.getLog(ThriftDataReceiverDS.class);
    private DataBridgeReceiverService dataBridgeReceiverService;
    private ServerConfigurationService serverConfiguration;
    private ConfigurationContextService configurationContext;
    private ThriftDataReceiver dataReceiver;
    private HttpService httpServiceInstance;

    private static final String DISABLE_RECEIVER = "disable.receiver";


    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        String disableReceiver = System.getProperty(DISABLE_RECEIVER);
        if (disableReceiver != null && Boolean.parseBoolean(disableReceiver)) {
            log.info("Receiver disabled.");
            return;
        }

        try {
            int portOffset = ThriftDataReceiverBuilder.readPortOffset();
            ThriftDataReceiverConfiguration thriftDataReceiverConfiguration = new ThriftDataReceiverConfiguration(CommonThriftConstants.DEFAULT_RECEIVER_PORT + CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET + portOffset, CommonThriftConstants.DEFAULT_RECEIVER_PORT + portOffset);
            ThriftDataReceiverBuilder.populateConfigurations(portOffset, thriftDataReceiverConfiguration, dataBridgeReceiverService.getInitialConfig());

            if (dataReceiver == null) {

                dataReceiver = new ThriftDataReceiverFactory().createAgentServer(thriftDataReceiverConfiguration, dataBridgeReceiverService);
                String serverUrl = CarbonUtils.getServerURL(serverConfiguration, configurationContext.getServerConfigContext());
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
                dataReceiver.start(hostName);


                ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl> processor = new ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl>(
                        new ThriftEventTransmissionServiceImpl(dataBridgeReceiverService));
                TCompactProtocol.Factory inProtFactory = new TCompactProtocol.Factory();
                TCompactProtocol.Factory outProtFactory = new TCompactProtocol.Factory();

                httpServiceInstance.registerServlet("/thriftReceiver",
                                                    new ThriftEventTransmissionServlet(processor, inProtFactory,
                                                                                       outProtFactory),
                                                    new Hashtable(),
                                                    httpServiceInstance.createDefaultHttpContext());

                ThriftSecureEventTransmissionService.Processor<ThriftSecureEventTransmissionServiceImpl> authProcessor = new ThriftSecureEventTransmissionService.Processor<ThriftSecureEventTransmissionServiceImpl>(
                        new ThriftSecureEventTransmissionServiceImpl(dataBridgeReceiverService));
                httpServiceInstance.registerServlet("/securedThriftReceiver",
                                                    new ThriftSecureEventTransmissionServlet(authProcessor, inProtFactory,
                                                                                             outProtFactory),
                                                    new Hashtable(),
                                                    httpServiceInstance.createDefaultHttpContext());

            }
        } catch (DataBridgeException e) {
            log.error("Can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        } catch (Throwable e) {
            log.error("Error in starting Agent Server ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        log.info("Thrift server shutting down...");
        dataReceiver.stop();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent server");
        }
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        this.serverConfiguration = null;
    }

    protected void setConfigurationContext(ConfigurationContextService configurationContext) {
        this.configurationContext = configurationContext;
    }

    protected void unsetConfigurationContext(ConfigurationContextService configurationContext) {
        this.configurationContext = null;
    }

    protected void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = dataBridgeReceiverService;
    }

    protected void unsetDatabridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = null;
    }

    protected void setHttpService(HttpService httpService) {
        httpServiceInstance = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        httpServiceInstance = null;
    }

}
