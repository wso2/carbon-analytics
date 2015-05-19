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
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.soap.internal.util.SoapEventAdapterConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SoapEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(SoapEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private ExecutorService executorService;
    private ConfigurationContext configContext;

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

            if (globalProperties.get(SoapEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        SoapEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = SoapEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
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
        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    System.getProperty(ServerConstants.CARBON_HOME)
                            + SoapEventAdapterConstants.SERVER_CLIENT_DEPLOYMENT_DIR,
                    System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH)
                            + SoapEventAdapterConstants.AXIS2_CLIENT_CONF_FILE);

            int axis2ClientTimeOutInMillis = SoapEventAdapterConstants.DEFAULT_AXIS2_CLIENT_CONNECTION_TIMEOUT;
            boolean isReuseHTTPClient = SoapEventAdapterConstants.IS_DEFAULT_AXIS2_REUSE_HTTP_CLIENT;
            boolean isAutoReleaseConnection = SoapEventAdapterConstants.IS_DEFAULT_AXIS2_AUTO_RELEASE_CONNECTION;
            int maxConnectionPerHostValue = SoapEventAdapterConstants.DEFAULT_AXIS2_MAX_CONNECTION_PER_HOST;

            String axi2ClientTimeOut = globalProperties.get(SoapEventAdapterConstants.AXIS2_CLIENT_CONNECTION_TIMEOUT);
            try {
                if (axi2ClientTimeOut != null) {
                    axis2ClientTimeOutInMillis = Integer.parseInt(axi2ClientTimeOut);
                }
            } catch (NumberFormatException e) {
                log.error("Invalid axis2 client timeout value " + axi2ClientTimeOut + " ignoring the configuration and using default value " + axis2ClientTimeOutInMillis);
            }

            String reuseHTTPClient = globalProperties.get(SoapEventAdapterConstants.AXIS2_REUSE_HTTP_CLIENT);
            try {
                if (reuseHTTPClient != null) {
                    isReuseHTTPClient = Boolean.parseBoolean(reuseHTTPClient);
                }
            } catch (NumberFormatException e) {
                log.error("Invalid Reuse HTTP Client value " + reuseHTTPClient + " ignoring the configuration and using default value " + isReuseHTTPClient);
            }

            String autoReleaseConnection = globalProperties.get(SoapEventAdapterConstants.AXIS2_AUTO_RELEASE_CONNECTION);
            try {
                if (autoReleaseConnection != null) {
                    isAutoReleaseConnection = Boolean.parseBoolean(autoReleaseConnection);
                }
            } catch (NumberFormatException e) {
                log.error("Invalid Auto release connection value " + autoReleaseConnection + " ignoring the configuration and using default value " + isAutoReleaseConnection);
            }

            String maxConnectionPerHost = globalProperties.get(SoapEventAdapterConstants.AXIS2_MAX_CONNECTION_PER_HOST);
            try {
                if (maxConnectionPerHost != null) {
                    maxConnectionPerHostValue = Integer.parseInt(maxConnectionPerHost);
                }
            } catch (NumberFormatException e) {
                log.error("Invalid Max connection per host value " + maxConnectionPerHost + " ignoring the configuration and using default value " + maxConnectionPerHostValue);
            }

            configContext.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, isReuseHTTPClient);
            configContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, createMultiThreadedHttpConnectionManager(axis2ClientTimeOutInMillis, maxConnectionPerHostValue));
            configContext.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION, isAutoReleaseConnection);

        } catch (AxisFault axisFault) {
            throw new OutputEventAdapterRuntimeException("Error while creating configuration context from filesystem ", axisFault);
        }
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        String url = dynamicProperties.get(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_URL);
        String userName = dynamicProperties.get(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_USERNAME);
        String password = dynamicProperties.get(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_PASSWORD);
        Map<String, String> soapHeaders = this.extractHeaders(dynamicProperties.get(
                SoapEventAdapterConstants.ADAPTER_CONF_SOAP_HEADERS));
        Map<String, String> httpHeaders = this.extractHeaders(dynamicProperties.get(
                SoapEventAdapterConstants.ADAPTER_CONF_HTTP_HEADERS));

        this.executorService.submit(new SoapSender(url, message, userName, password, soapHeaders, httpHeaders));
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
        String[] entries = headers.split(SoapEventAdapterConstants.HEADER_SEPARATOR);
        String[] keyValue;
        Map<String, String> result = new HashMap<String, String>();
        for (String header : entries) {
            try {
                keyValue = header.split(SoapEventAdapterConstants.ENTRY_SEPARATOR, 2);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            } catch (Throwable e) {
                log.warn("Header property \"" + header + "\" is not defined in the correct format.", e);
            }
        }
        return result;

    }

    public class SoapSender implements Runnable {

        private String url;
        private Object payload;
        private String username;
        private String password;
        private Map<String, String> soapHeaders;
        private Map<String, String> httpHeaders;

        public SoapSender(String url, Object payload, String username, String password,
                          Map<String, String> soapHeaders, Map<String, String> httpHeaders) {
            this.url = url;
            this.payload = payload;
            this.username = username;
            this.password = password;
            this.soapHeaders = soapHeaders;
            this.httpHeaders = httpHeaders;
        }

        @Override
        public void run() {

            ServiceClient serviceClient = null;
            try {
                serviceClient = new ServiceClient(configContext, null);
                Options options = new Options();
                options.setTo(new EndpointReference(url));

                if (soapHeaders != null) {
                    serviceClient.engageModule("addressing");
                    setSoapHeaders(soapHeaders, options);
                }

                if (httpHeaders != null) {
                    setHttpHeaders(httpHeaders, options);
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
                log.error("Exception in adapter " + eventAdapterConfiguration.getName()
                        + " while sending events to soap endpoint " + this.url + ": " + e.getMessage(), e);
            } catch (XMLStreamException e) {
                log.error("Exception occurred in adapter " + eventAdapterConfiguration.getName()
                        + " while converting the event to xml object :" + e.getMessage(), e);
            } catch (Exception e) {
                log.error("Exception occurred in adapter "
                        + eventAdapterConfiguration.getName() + ": " + e.getMessage(), e);
            } finally {
                if (serviceClient != null) {
                    try {
                        serviceClient.cleanup();
                    } catch (AxisFault axisFault) {
                        log.error("Error while cleaning-up service client resources : " + axisFault.getMessage(), axisFault);
                    }
                }
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

        private void setSoapHeaders(Map<String, String> headers, Options options) {

            for (Map.Entry<String, String> headerValue : headers.entrySet()) {
                try {
                    if (headerValue.getKey().equalsIgnoreCase("SOAPAction")) {
                        options.setAction(headerValue.getValue());
                    } else if (headerValue.getKey().equalsIgnoreCase("From")) {
                        options.setFrom(new EndpointReference(headerValue.getValue()));
                    } else if (headerValue.getKey().equalsIgnoreCase("FaultTo")) {
                        options.setFaultTo(new EndpointReference(headerValue.getValue()));
                    } else if (headerValue.getKey().equalsIgnoreCase("TransportIn")) {
                        options.setTransportIn(new TransportInDescription(headerValue.getValue()));
                    } else if (headerValue.getKey().equalsIgnoreCase("TransportInProtocol")) {
                        options.setTransportInProtocol(headerValue.getValue());
                    } else if (headerValue.getKey().equalsIgnoreCase("MessageID")) {
                        options.setMessageId(headerValue.getValue());
                    } else if (headerValue.getKey().equalsIgnoreCase("RelatesTo")) {
                        options.addRelatesTo(new RelatesTo(headerValue.getValue()));
                    } else if (headerValue.getKey().equalsIgnoreCase("ReplyTo")) {
                        options.setReplyTo(new EndpointReference(headerValue.getValue()));
                    } else if (headerValue.getKey().equalsIgnoreCase("TransportOut")) {
                        options.setTransportOut(new TransportOutDescription(headerValue.getValue()));
                    } else if (headerValue.getKey().equalsIgnoreCase("SoapVersionURI")) {
                        options.setSoapVersionURI(headerValue.getValue());
                    } else if (headerValue.getKey().equalsIgnoreCase("To")) {
                        options.setTo(new EndpointReference(headerValue.getValue()));
                    } else if (headerValue.getKey().equalsIgnoreCase("ManageSession")) {
                        options.setManageSession(Boolean.parseBoolean(headerValue.getValue()));
                    } else {
                        try {
                            int headerParameterValue = Integer.parseInt(headerValue.getValue());
                            options.setProperty(headerValue.getKey(), headerParameterValue);
                        } catch (NumberFormatException e) {
                            options.setProperty(headerValue.getKey(), headerValue.getValue());
                        }
                    }

                } catch (Throwable e) {
                    //Catching the exception because there can be several exception thrown from axis2 level and we cannot drop the message because of a header issue
                    log.warn("Invalid soap header : \"" + headerValue + "\", ignoring corresponding header..." + e.getMessage());
                }
            }
        }

        private void setHttpHeaders(Map<String, String> headers, Options options) {
            List<Header> list = new ArrayList<>();

            for (Map.Entry<String, String> headerValue : headers.entrySet()) {
                try {
                    Header header = new Header();
                    header.setName(headerValue.getKey());
                    header.setValue(headerValue.getValue());
                    list.add(header);
                } catch (Throwable e) {
                    //Catching the exception because there can be several exception thrown from axis2 level and we cannot drop the message because of a header issue
                    log.warn("Invalid http header : \"" + headerValue + "\", ignoring corresponding header..." + e.getMessage());
                }
            }
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, list);
        }

    }

    private HttpClient createMultiThreadedHttpConnectionManager(int connectionTimeOut, int maxConnectionPerHost) {

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
        params.setConnectionTimeout(connectionTimeOut);
        MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        httpConnectionManager.setParams(params);
        return new HttpClient(httpConnectionManager);
    }
}


