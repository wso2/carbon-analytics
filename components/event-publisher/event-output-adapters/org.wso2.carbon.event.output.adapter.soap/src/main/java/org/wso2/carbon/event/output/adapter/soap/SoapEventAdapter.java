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
package org.wso2.carbon.event.output.adapter.soap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.soap.internal.util.SoapEventAdapterConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SoapEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(SoapEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    ExecutorService executorService;

    public SoapEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        //executorService will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(SoapEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(
                        SoapEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = SoapEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(SoapEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(
                        SoapEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = SoapEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(SoapEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        SoapEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = SoapEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME;
            }

            if (globalProperties.get(SoapEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueueSize = Integer.parseInt(globalProperties.get(
                        SoapEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueueSize = SoapEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(jobQueueSize));
        }

    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-available");
    }

    @Override
    public void connect() {
        //not required
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        String url = dynamicProperties.get(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_URL);
        String userName = dynamicProperties.get(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_USERNAME);
        String password = dynamicProperties.get(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_PASSWORD);
        Map<String, String> headers = this.extractHeaders(dynamicProperties.get(
                SoapEventAdapterConstants.ADAPTER_CONF_SOAP_HEADERS));

        this.executorService.submit(new SoapSender(url, message, userName, password, headers));
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    private Map<String, String> extractHeaders(String headers) {
        if (headers == null || headers.trim().length() == 0) {
            return null;
        }
        try {
            String[] entries = headers.split(SoapEventAdapterConstants.HEADER_SEPARATOR);
            String[] keyValue;
            Map<String, String> result = new HashMap<String, String>();
            for (String entry : entries) {
                keyValue = entry.split(SoapEventAdapterConstants.ENTRY_SEPARATOR);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
            return result;
        } catch (Exception e) {
            log.error("Invalid headers format: \"" + headers + "\", ignoring headers...");
            return null;
        }
    }

    public class SoapSender implements Runnable {

        private String url;
        private Object payload;
        private String username;
        private String password;
        private Map<String, String> headers;

        public SoapSender(String url, Object payload, String username, String password,
                          Map<String, String> headers) {
            this.url = url;
            this.payload = payload;
            this.username = username;
            this.password = password;
            this.headers = headers;
        }

        @Override
        public void run() {
            ConfigurationContext configContext;
            try {
                configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        System.getProperty(ServerConstants.CARBON_HOME)
                                + SoapEventAdapterConstants.SERVER_CLIENT_DEPLOYMENT_DIR,
                        System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH)
                                + SoapEventAdapterConstants.AXIS2_CLIENT_CONF_FILE);

                ServiceClient serviceClient;
                try {
                    serviceClient = new ServiceClient(configContext, null);
                    Options options = new Options();
                    options.setTo(new EndpointReference(url));
                    try {
                        if (headers != null) {
                            for (Map.Entry<String, String> headerValue : headers.entrySet()) {
                                options.setProperty(headerValue.getKey(), headerValue.getValue());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Invalid headers : \"" + headers + "\", ignoring headers...");
                    }

                    if (username != null || password != null) {
                        options.setUserName(username);
                        options.setPassword(password);
                        serviceClient.engageModule("rampart");
                        options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, loadPolicy());
                    }

                    serviceClient.setOptions(options);
                    serviceClient.fireAndForget(AXIOMUtil.stringToOM(payload.toString()));

                } catch (AxisFault e) {
                    throw new ConnectionUnavailableException("Exception in adapter "
                            + eventAdapterConfiguration.getName() + " while sending events to soap endpoint "
                            + this.url, e);
                } catch (XMLStreamException e) {
                    throw new OutputEventAdapterRuntimeException(
                            "Exception occurred in adapter " + eventAdapterConfiguration.getName()
                                    + " while converting the event to xml object ", e);
                } catch (Exception e) {
                    throw new OutputEventAdapterRuntimeException("Exception occurred in adapter "
                            + eventAdapterConfiguration.getName(), e);
                }
            } catch (AxisFault axisFault) {
                throw new OutputEventAdapterRuntimeException("Exception occurred in adapter "
                        + eventAdapterConfiguration.getName(), axisFault);
            }

        }

        private Policy loadPolicy() throws Exception {
            OMElement omElement = AXIOMUtil.stringToOM(
                    "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
                            "            xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"\n" +
                            "            wsu:Id=\"UTOverTransport\">\n" +
                            "    <wsp:ExactlyOne>\n" +
                            "        <wsp:All>\n" +
                            "            <sp:TransportBinding xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">\n" +
                            "                <wsp:Policy>\n" +
                            "                    <sp:TransportToken>\n" +
                            "                        <wsp:Policy>\n" +
                            "                            <sp:HttpsToken RequireClientCertificate=\"false\"></sp:HttpsToken>\n" +
                            "                        </wsp:Policy>\n" +
                            "                    </sp:TransportToken>\n" +
                            "                    <sp:AlgorithmSuite>\n" +
                            "                        <wsp:Policy>\n" +
                            "                            <sp:Basic256></sp:Basic256>\n" +
                            "                        </wsp:Policy>\n" +
                            "                    </sp:AlgorithmSuite>\n" +
                            "                    <sp:Layout>\n" +
                            "                        <wsp:Policy>\n" +
                            "                            <sp:Lax></sp:Lax>\n" +
                            "                        </wsp:Policy>\n" +
                            "                    </sp:Layout>\n" +
                            "                    <sp:IncludeTimestamp></sp:IncludeTimestamp>\n" +
                            "                </wsp:Policy>\n" +
                            "            </sp:TransportBinding>\n" +
                            "            <sp:SignedSupportingTokens\n" +
                            "                    xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">\n" +
                            "                <wsp:Policy>\n" +
                            "                    <sp:UsernameToken\n" +
                            "                            sp:IncludeToken=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient\"></sp:UsernameToken>\n" +
                            "                </wsp:Policy>\n" +
                            "            </sp:SignedSupportingTokens>\n" +
                            "        </wsp:All>\n" +
                            "    </wsp:ExactlyOne>\n" +
                            "</wsp:Policy>");
            return PolicyEngine.getPolicy(omElement);
        }
    }
}


