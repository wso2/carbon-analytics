/*
 * Copyright 2013 The Apache Software Foundation.
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
package org.wso2.carbon.event.output.adaptor.http;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.http.internal.util.HTTPEventAdaptorConstants;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the HTTP output event adaptor implementation.
 */
public class HTTPEventAdaptor extends AbstractOutputEventAdaptor {

    private static HTTPEventAdaptor instance = new HTTPEventAdaptor();

    private List<Property> outputAdapterProps;

    private List<Property> outputMessageProps;

    private List<String> supportOutputMessageTypes;

    private HttpClient httpClient;

    private ExecutorService executorService;

    private Log log = LogFactory.getLog(HTTPEventAdaptor.class);

    private HTTPEventAdaptor() {
    }

    public static HTTPEventAdaptor getInstance() {
        return instance;
    }

    @Override
    protected String getName() {
        return HTTPEventAdaptorConstants.ADAPTER_TYPE_HTTP;
    }

    @Override
    protected List<Property> getOutputAdaptorProperties() {
        return outputAdapterProps;
    }

    @Override
    protected List<Property> getOutputMessageProperties() {
        return outputMessageProps;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        return supportOutputMessageTypes;
    }

    private void populateAdapterMessageProps() {
        this.outputAdapterProps = new ArrayList<Property>();
        this.outputMessageProps = new ArrayList<Property>();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "org.wso2.carbon.event.output.adaptor.http.i18n.Resources", Locale.getDefault());
        Property urlProp = new Property(HTTPEventAdaptorConstants.ADAPTER_MESSAGE_URL);
        urlProp.setDisplayName(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_MESSAGE_URL));
        urlProp.setHint(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_MESSAGE_URL_HINT));
        urlProp.setRequired(true);
        Property usernameProp = new Property(HTTPEventAdaptorConstants.ADAPTER_USERNAME);
        usernameProp.setDisplayName(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_USERNAME));
        usernameProp.setHint(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_USERNAME_HINT));
        usernameProp.setRequired(false);
        Property passwordProp = new Property(HTTPEventAdaptorConstants.ADAPTER_PASSWORD);
        passwordProp.setDisplayName(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_PASSWORD));
        passwordProp.setHint(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_PASSWORD_HINT));
        passwordProp.setRequired(false);
        passwordProp.setSecured(true);
        Property headersProp = new Property(HTTPEventAdaptorConstants.ADAPTER_HEADERS);
        headersProp.setDisplayName(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_HEADERS));
        headersProp.setHint(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_HEADERS_HINT));
        headersProp.setRequired(false);
        Property proxyHostProp = new Property(HTTPEventAdaptorConstants.ADAPTER_PROXY_HOST);
        proxyHostProp.setDisplayName(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_PROXY_HOST));
        proxyHostProp.setHint(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_PROXY_HOST_HINT));
        proxyHostProp.setRequired(false);
        Property proxyPortProp = new Property(HTTPEventAdaptorConstants.ADAPTER_PROXY_PORT);
        proxyPortProp.setDisplayName(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_PROXY_PORT));
        proxyPortProp.setHint(resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTER_PROXY_PORT_HINT));
        proxyPortProp.setRequired(false);
        this.outputMessageProps.add(urlProp);
        this.outputMessageProps.add(usernameProp);
        this.outputMessageProps.add(passwordProp);
        this.outputMessageProps.add(headersProp);
        this.outputAdapterProps.add(proxyHostProp);
        this.outputAdapterProps.add(proxyPortProp);
    }

    @Override
    protected void init() {
        this.populateAdapterMessageProps();
        this.supportOutputMessageTypes = new ArrayList<String>();
        this.supportOutputMessageTypes.add(MessageType.XML);
        this.supportOutputMessageTypes.add(MessageType.JSON);
        this.supportOutputMessageTypes.add(MessageType.TEXT);
        this.executorService = new ThreadPoolExecutor(HTTPEventAdaptorConstants.ADAPTER_MIN_THREAD_POOL_SIZE,
                                                      HTTPEventAdaptorConstants.ADAPTER_MAX_THREAD_POOL_SIZE, AgentConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                                                      new LinkedBlockingQueue<Runnable>(HTTPEventAdaptorConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE));
    }

    private void checkHTTPClientInit(
            OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration) {
        if (this.httpClient != null) {
            return;
        }
        synchronized (HTTPEventAdaptor.class) {
            if (this.httpClient != null) {
                return;
            }
            /* this needs to be created as late as possible, for the SSL truststore properties 
             * to be set by Carbon in Java system properties */
            this.httpClient = new SystemDefaultHttpClient();
            Map<String, String> props = outputEventMessageConfiguration
                    .getOutputMessageProperties();
            String proxyHost = props.get(HTTPEventAdaptorConstants.ADAPTER_PROXY_HOST);
            String proxyPort = props.get(HTTPEventAdaptorConstants.ADAPTER_PROXY_PORT);
            if (proxyHost != null && proxyHost.trim().length() > 0) {
                try {
                    HttpHost host = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
                    this.httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
                } catch (NumberFormatException e) {
                    log.error("Invalid proxy port: " + proxyPort + ", "
                              + "ignoring proxy settings for HTTP output event adaptor...");
                }
            }
        }
    }

    private Map<String, String> extractHeaders(String headers) {
        if (headers == null || headers.trim().length() == 0) {
            return null;
        }
        try {
            String[] entries = headers.split(",");
            String[] keyValue;
            Map<String, String> result = new HashMap<String, String>();
            for (String entry : entries) {
                keyValue = entry.split(":");
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
            return result;
        } catch (Exception e) {
            log.error("Invalid headers format: \"" + headers + "\", ignoring HTTP headers...");
            return null;
        }
    }

    @Override
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        /* outputEventAdaptorConfiguration should come to init()? */
        this.checkHTTPClientInit(outputEventMessageConfiguration);
        Map<String, String> messageProps = outputEventMessageConfiguration.getOutputMessageProperties();
        String url = messageProps.get(HTTPEventAdaptorConstants.ADAPTER_MESSAGE_URL);
        String username = messageProps.get(HTTPEventAdaptorConstants.ADAPTER_USERNAME);
        String password = messageProps.get(HTTPEventAdaptorConstants.ADAPTER_PASSWORD);
        Map<String, String> headers = this.extractHeaders(messageProps.get(HTTPEventAdaptorConstants.ADAPTER_HEADERS));
        String payload = message.toString();
        this.executorService.submit(new HTTPSender(url, payload, username, password, headers));
    }

    @Override
    public void testConnection(OutputEventAdaptorConfiguration
                                       outputEventAdaptorConfiguration, int tenantId) {

        Map<String, String> adaptorProps = outputEventAdaptorConfiguration.getOutputProperties();

        if (adaptorProps != null) {
            if (adaptorProps.get(HTTPEventAdaptorConstants.ADAPTER_PROXY_HOST) != null && adaptorProps.get(HTTPEventAdaptorConstants.ADAPTER_PROXY_PORT) != null) {
                String host = adaptorProps.get(HTTPEventAdaptorConstants.ADAPTER_PROXY_HOST);
                int port = Integer.parseInt(adaptorProps.get(HTTPEventAdaptorConstants.ADAPTER_PROXY_PORT));
                try {
                    new Socket(host, port);
                } catch (IOException e) {
                    throw new OutputEventAdaptorEventProcessingException(e);
                } catch (NumberFormatException e) {
                    throw new OutputEventAdaptorEventProcessingException(e);
                }
            }else{
                throw new OutputEventAdaptorEventProcessingException("Necessary host and/or port values are not found to check the connection");
            }
        }
    }

    /**
     * This class represents a job to send an HTTP request to a target URL.
     */
    public class HTTPSender implements Runnable {

        private String url;

        private String payload;

        private String username;

        private String password;

        private Map<String, String> headers;

        public HTTPSender(String url, String payload, String username, String password,
                          Map<String, String> headers) {
            this.url = url;
            this.payload = payload;
            this.username = username;
            this.password = password;
            this.headers = headers;
        }

        public String getUrl() {
            return url;
        }

        public String getPayload() {
            return payload;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        private void processAuthentication(HttpPost method) {
            if (this.getUsername() != null && this.getUsername().trim().length() > 0) {
                method.setHeader("Authorization", "Basic " + Base64.encode(
                        (this.getUsername() + ":" + this.getPassword()).getBytes()));
            }
        }

        private void processHeaders(HttpPost method) {
            if (this.getHeaders() != null) {
                for (Entry<String, String> header : this.getHeaders().entrySet()) {
                    method.setHeader(header.getKey(), header.getValue());
                }
            }
        }

        public void run() {
            HttpPost method = new HttpPost(this.getUrl());
            StringEntity entity;
            try {
                entity = new StringEntity(this.getPayload());
                method.setEntity(entity);
                this.processAuthentication(method);
                this.processHeaders(method);
                httpClient.execute(method).getEntity().getContent().close();
            } catch (Exception e) {
                log.error("Error executing HTTP output event adapter sender: " + e.getMessage(), e);
            }
        }

    }

}
