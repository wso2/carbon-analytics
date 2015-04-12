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
package org.wso2.carbon.event.output.adapter.http;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HTTPEventAdapter implements OutputEventAdapter {
    private static final Log log = LogFactory.getLog(OutputEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private ExecutorService executorService;
    private HttpClient httpClient;

    public HTTPEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }


    @Override
    public void init() throws OutputEventAdapterException {

        //ExecutorService will be assigned  if it is null
        if (this.executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(
                        HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(
                        HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer.parseInt(globalProperties.get(
                        HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            this.executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(jobQueSize));
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

        //Load dynamic properties
        this.checkHTTPClientInit(dynamicProperties);
        String url = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL);
        String username = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_USERNAME);
        String password = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_PASSWORD);
        Map<String, String> headers = this.extractHeaders(dynamicProperties.get(
                HTTPEventAdapterConstants.ADAPTER_HEADERS));
        String payload = message.toString();

        this.executorService.submit(new HTTPSender(url, payload, username, password, headers));
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    private void checkHTTPClientInit(
            Map<String, String> dynamicProperties) {
        if (this.httpClient != null) {
            return;
        }
        synchronized (HTTPEventAdapter.class) {
            if (this.httpClient != null) {
                return;
            }
            /* this needs to be created as late as possible, for the SSL trust store properties
             * to be set by Carbon in Java system properties */
            this.httpClient = new SystemDefaultHttpClient();
            String proxyHost = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_PROXY_HOST);
            String proxyPort = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_PROXY_PORT);
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
            String[] entries = headers.split(HTTPEventAdapterConstants.HEADER_SEPARATOR);
            String[] keyValue;
            Map<String, String> result = new HashMap<String, String>();
            for (String entry : entries) {
                keyValue = entry.split(HTTPEventAdapterConstants.ENTRY_SEPARATOR);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
            return result;
        } catch (Exception e) {
            log.error("Invalid headers format: \"" + headers + "\", ignoring HTTP headers...");
            return null;
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
                        (this.getUsername() + HTTPEventAdapterConstants.ENTRY_SEPARATOR
                                + this.getPassword()).getBytes()));
            }
        }

        private void processHeaders(HttpPost method) {
            if (this.getHeaders() != null) {
                for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
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
            } catch (UnknownHostException e) {
                throw new ConnectionUnavailableException("Exception while connecting adapter "
                        + eventAdapterConfiguration.getName() + " HTTP endpoint to " + this.getUrl(),
                        e);
            } catch (Exception e) {
                throw new OutputEventAdapterRuntimeException("Error executing HTTP output event adapter "
                        + eventAdapterConfiguration.getName() +" sender: ", e);
            }
        }

    }

}
