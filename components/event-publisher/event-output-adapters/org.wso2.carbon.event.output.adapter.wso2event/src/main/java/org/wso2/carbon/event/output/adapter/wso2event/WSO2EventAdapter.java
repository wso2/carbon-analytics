/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.output.adapter.wso2event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.wso2event.internal.util.WSO2EventAdapterConstants;

import java.util.Map;


public final class WSO2EventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(WSO2EventAdapter.class);
    private final OutputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private DataPublisher dataPublisher = null;
    private boolean isBlockingMode = false;
    private long timeout = 0;
    private int tenantId;
    private String authUrl;
    private String receiverUrl;
    private String protocol;

    public WSO2EventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {

        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;

    }

    /**
     * Initialises the resource bundle
     */
    @Override
    public void init() throws OutputEventAdapterException {
        validateOutputEventAdapterConfigurations();
        tenantId= PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String configPath = globalProperties.get(WSO2EventAdapterConstants.ADAPTER_CONF_PATH);
        if (configPath != null) {
            AgentHolder.setConfigPath(configPath);
        }

        authUrl = eventAdapterConfiguration.getStaticProperties().get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL);
        receiverUrl = eventAdapterConfiguration.getStaticProperties().get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_RECEIVER_URL);
        protocol = eventAdapterConfiguration.getStaticProperties().get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PROTOCOL);

        if (receiverUrl == null) {
            if (protocol.equals(WSO2EventAdapterConstants.ADAPTER_PROTOCOL_THRIFT)) {
                String defaultTCPUrl = globalProperties.get(WSO2EventAdapterConstants.DEFAULT_THRIFT_TCP_URL);
                String defaultSSLUrl = globalProperties.get(WSO2EventAdapterConstants.DEFAULT_THRIFT_SSL_URL);
                if (defaultTCPUrl != null) {
                    receiverUrl = defaultTCPUrl;

                    if (authUrl == null && defaultSSLUrl != null) {
                        authUrl = defaultSSLUrl;
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Thirft TCP url is not specified for WSO2Event Publisher " +
                                  eventAdapterConfiguration.getName() + " ,hence using default thrift url " + defaultTCPUrl);
                    }
                } else {
                    throw new OutputEventAdapterException("Cannot deploy WSO2Event Publisher " +
                                                                 eventAdapterConfiguration.getName() + " , since there is no any thrift url specified in " +
                                                                 "global/event publisher configuration");
                }
            } else {
                String defaultTCPUrl = globalProperties.get(WSO2EventAdapterConstants.DEFAULT_BINARY_TCP_URL);
                String defaultSSLUrl = globalProperties.get(WSO2EventAdapterConstants.DEFAULT_BINARY_SSL_URL);
                if (defaultTCPUrl != null) {
                    receiverUrl = defaultTCPUrl;
                    if (authUrl == null && defaultSSLUrl != null) {
                        authUrl = defaultSSLUrl;
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Thirft TCP url is not specified for WSO2Event Publisher " +
                                  eventAdapterConfiguration.getName() + " ,hence using default binary url " + defaultTCPUrl);
                    }
                } else {
                    throw new OutputEventAdapterException("Cannot deploy WSO2Event Publisher " +
                                                                 eventAdapterConfiguration.getName() + " , since there is no any binary url specified in " +
                                                                 "global/event publisher configuration");
                }
            }
        }

    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        connect();
    }

    @Override
    public synchronized void connect() {

        String userName = eventAdapterConfiguration.getStaticProperties().get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_USER_NAME);
        String password = eventAdapterConfiguration.getStaticProperties().get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PASSWORD);
        String publishingMode = eventAdapterConfiguration.getStaticProperties()
                .get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PUBLISHING_MODE);
        String timeoutString = eventAdapterConfiguration.getStaticProperties()
                .get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PUBLISH_TIMEOUT_MS);

        if (publishingMode.equalsIgnoreCase(WSO2EventAdapterConstants.ADAPTER_PUBLISHING_MODE_BLOCKING)) {
            isBlockingMode = true;
        } else {
            timeout = Long.parseLong(timeoutString);
        }

        try {
            if (authUrl != null && authUrl.length() > 0) {
                dataPublisher = new DataPublisher(protocol, receiverUrl, authUrl, userName, password);
            } else {
                dataPublisher = new DataPublisher(protocol, receiverUrl, null, userName, password);
            }
        } catch (DataEndpointAgentConfigurationException e) {
            throwRuntimeException(receiverUrl, authUrl, protocol, userName, e);
        } catch (DataEndpointException e) {
            throwConnectionException(receiverUrl, authUrl, protocol, userName, e);
        } catch (DataEndpointConfigurationException e) {
            throwRuntimeException(receiverUrl, authUrl, protocol, userName, e);
        } catch (DataEndpointAuthenticationException e) {
            throwConnectionException(receiverUrl, authUrl, protocol, userName, e);
        } catch (TransportException e) {
            throwConnectionException(receiverUrl, authUrl, protocol, userName, e);
        }

    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        Event event = (Event) (message);

        if (isBlockingMode) {
            dataPublisher.publish(event);
        } else {
            if (!dataPublisher.tryPublish(event, timeout)) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Cannot send event", log, tenantId);
            }
        }
    }

    @Override
    public void disconnect() {
        if (dataPublisher != null) {
            try {
                dataPublisher.shutdown();
            } catch (DataEndpointException e) {
                String userName = eventAdapterConfiguration.getStaticProperties()
                        .get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_USER_NAME);
                String authUrl = eventAdapterConfiguration.getStaticProperties()
                        .get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL);
                String receiverUrl = eventAdapterConfiguration.getStaticProperties()
                        .get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_RECEIVER_URL);
                String protocol = eventAdapterConfiguration.getStaticProperties()
                        .get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PROTOCOL);
                logException("Error in shutting down the data publisher", receiverUrl, authUrl, protocol, userName, e);
            }
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isPolled() {
        return false;
    }

    private void validateOutputEventAdapterConfigurations() throws OutputEventAdapterException {
        String timeoutProperty = eventAdapterConfiguration.getStaticProperties().get(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PUBLISH_TIMEOUT_MS);
        if(timeoutProperty != null){
            try{
                Long.parseLong(timeoutProperty);
            } catch (NumberFormatException e){
                throw new OutputEventAdapterException("Invalid value set for property 'Publishing Timeout': " + timeoutProperty, e);
            }
        }
    }

    private void throwRuntimeException(String receiverUrl, String authUrl, String protocol, String userName,
                                       Exception e) {
        throw new OutputEventAdapterRuntimeException(
                "Error in data-bridge config for adapter " + eventAdapterConfiguration.getName()
                        + " with the receiverUrl:" + receiverUrl + " authUrl:" + authUrl + " protocol:" + protocol
                        + " and userName:" + userName + "," + e.getMessage(), e);
    }

    private void logException(String message, String receiverUrl, String authUrl, String protocol, String userName,
                              Exception e) {
        log.error(message + " for adapter " + eventAdapterConfiguration.getName()
                + " with the receiverUrl:" + receiverUrl + " authUrl:" + authUrl + " protocol:" + protocol
                + " and userName:" + userName + "," + e.getMessage(), e);
    }

    private void throwConnectionException(String receiverUrl, String authUrl, String protocol, String userName,
                                          Exception e) {
        throw new ConnectionUnavailableException(
                "Connection not available for adapter " + eventAdapterConfiguration.getName()
                        + " with the receiverUrl:" + receiverUrl + " authUrl:" + authUrl + " protocol:" + protocol
                        + " and userName:" + userName + "," + e.getMessage(), e);
    }

}
