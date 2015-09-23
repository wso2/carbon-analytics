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
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiverFactory;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
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
        context.getBundleContext().registerService(ServerStartupObserver.class.getName(),
                new ThriftServerStartupObserver(), null);
    }


    protected void deactivate(ComponentContext context) {
        log.info("Thrift server shutting down...");
        ServiceHolder.getDataReceiver().stop();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent server");
        }
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        ServiceHolder.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
       ServiceHolder.setServerConfiguration(null);
    }

    protected void setConfigurationContext(ConfigurationContextService configurationContext) {
        ServiceHolder.setConfigurationContext(configurationContext);
    }

    protected void unsetConfigurationContext(ConfigurationContextService configurationContext) {
       ServiceHolder.setConfigurationContext(null);
    }

    protected void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void unsetDatabridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void setHttpService(HttpService httpService) {
        ServiceHolder.setHttpServiceInstance(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {
        ServiceHolder.setHttpServiceInstance(httpService);
    }

}
