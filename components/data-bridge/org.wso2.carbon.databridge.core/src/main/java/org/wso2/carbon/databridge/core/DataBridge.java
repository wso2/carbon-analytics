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

package org.wso2.carbon.databridge.core;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.StreamAddRemoveListener;
import org.wso2.carbon.databridge.core.definitionstore.StreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.EventDispatcher;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.core.internal.authentication.Authenticator;

import java.util.ArrayList;
import java.util.List;

/**
 * this class represents as the interface between the agent server and agent
 * server implementations.
 */
public class DataBridge implements DataBridgeSubscriberService, DataBridgeReceiverService {

    private static final Log log = LogFactory.getLog(DataBridge.class);
    private StreamDefinitionStore streamDefinitionStore;
    private EventDispatcher eventDispatcher;
    private Authenticator authenticator;
    private AuthenticationHandler authenticatorHandler;
    private OMElement initialConfig;
    private List<StreamAddRemoveListener> streamAddRemoveListenerList = new ArrayList<StreamAddRemoveListener>();

    public DataBridge(AuthenticationHandler authenticationHandler,
                      AbstractStreamDefinitionStore streamDefinitionStore,
                      DataBridgeConfiguration dataBridgeConfiguration) {
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, dataBridgeConfiguration, authenticationHandler);
        this.streamDefinitionStore = streamDefinitionStore;
        authenticatorHandler = authenticationHandler;
        authenticator = new Authenticator(authenticationHandler, dataBridgeConfiguration);
    }

    public DataBridge(AuthenticationHandler authenticationHandler,
                      AbstractStreamDefinitionStore streamDefinitionStore) {
        DataBridgeConfiguration dataBridgeConfiguration = new DataBridgeConfiguration();
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, dataBridgeConfiguration, authenticationHandler);
        this.streamDefinitionStore = streamDefinitionStore;
        authenticatorHandler = authenticationHandler;
        authenticator = new Authenticator(authenticationHandler, dataBridgeConfiguration);
    }

    public String defineStream(String sessionId, String streamDefinition)
            throws

            DifferentStreamDefinitionAlreadyDefinedException,
            MalformedStreamDefinitionException, SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            String streamId = eventDispatcher.defineStream(streamDefinition, agentSession);
            if (streamId != null) {
                int tenantId = agentSession.getCredentials().getTenantId();
                for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                    streamAddRemoveListener.streamAdded(tenantId, streamId);
                }
            }
            return streamId;
        } catch (MalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new DifferentStreamDefinitionAlreadyDefinedException(e.getErrorMessage(), e);
        } catch (StreamDefinitionStoreException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    public String defineStream(String sessionId, String streamDefinition, String indexDefinition)
            throws

            DifferentStreamDefinitionAlreadyDefinedException,
            MalformedStreamDefinitionException, SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            String streamId = eventDispatcher.defineStream(streamDefinition, agentSession, indexDefinition);
            if (streamId != null) {
                int tenantId = agentSession.getCredentials().getTenantId();
                for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                    streamAddRemoveListener.streamAdded(tenantId, streamId);
                }
            }
            return streamId;
        } catch (MalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new DifferentStreamDefinitionAlreadyDefinedException(e.getErrorMessage(), e);
        } catch (StreamDefinitionStoreException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    public String findStreamId(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            return eventDispatcher.findStreamId(streamName,
                    streamVersion, agentSession);
        } catch (StreamDefinitionStoreException e) {
            log.warn("Cannot find streamId for " + streamName + " " + streamVersion, e);
            return null;
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }

    }

    public boolean deleteStream(String sessionId, String streamId)
            throws SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        boolean status;

        try {
            authenticatorHandler.initContext(agentSession);
            status = eventDispatcher.deleteStream(DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId), DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId), agentSession);
            if (status) {
                for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                    streamAddRemoveListener.streamRemoved(agentSession.getCredentials().getTenantId(), streamId);
                }
            }
        } finally {
            authenticatorHandler.destroyContext(agentSession);

        }
        return status;
    }

    public boolean deleteStream(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException {
        return deleteStream(sessionId, DataBridgeCommonsUtils.generateStreamId(streamName, streamVersion));
    }


    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            eventDispatcher.publish(eventBundle, agentSession, eventConverter);
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    public String login(String username, String password) throws AuthenticationException {
        log.info("user " + username + " connected");
        return authenticator.authenticate(username, password);
    }


    public void logout(String sessionId) throws Exception {
        AgentSession agentSession = authenticator.getSession(sessionId);
        authenticator.logout(sessionId);
        if (agentSession != null) {
            log.info("user " + agentSession.getUsername() + " disconnected");
        } else {
            log.info("session " + sessionId + " disconnected");
        }
    }

    public OMElement getInitialConfig() {
        return initialConfig;
    }

    public void setInitialConfig(OMElement initialConfig) {
        this.initialConfig = initialConfig;
    }

    /**
     * CEP/BAM can subscribe for Event Streams
     *
     * @param agentCallback callbacks of the subscribers
     */
    public void subscribe(AgentCallback agentCallback) {
        eventDispatcher.addCallback(agentCallback);
    }

    /**
     * CEP/BAM can subscribe for Raw Event Streams
     *
     * @param agentCallback callbacks of the subscribers
     */
    public void subscribe(RawDataAgentCallback agentCallback) {
        eventDispatcher.addCallback(agentCallback);
    }

    @Override
    public StreamDefinition getStreamDefinition(String sessionId, String streamName,
                                                String streamVersion)
            throws SessionTimeoutException, StreamDefinitionNotFoundException,
            StreamDefinitionStoreException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }

        try {
            authenticatorHandler.initContext(agentSession);
            return getStreamDefinition(streamName, streamVersion, agentSession.getCredentials().getTenantId());
        } finally {
            authenticatorHandler.destroyContext(agentSession);

        }
    }

    @Override
    public List<StreamDefinition> getAllStreamDefinitions(String sessionId)
            throws SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            return getAllStreamDefinitions(agentSession.getCredentials().getTenantId());
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    @Override
    public void saveStreamDefinition(String sessionId, StreamDefinition streamDefinition)
            throws SessionTimeoutException, StreamDefinitionStoreException,
            DifferentStreamDefinitionAlreadyDefinedException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            saveStreamDefinition(streamDefinition, agentSession.getCredentials().getTenantId());
            eventDispatcher.updateStreamDefinitionHolder(agentSession);
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    // Stream store operations
    @Override
    public StreamDefinition getStreamDefinition(String streamName,
                                                String streamVersion, int tenantId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return streamDefinitionStore.getStreamDefinition(streamName, streamVersion, tenantId);

    }

    @Override
    public StreamDefinition getStreamDefinition(String streamId, int tenantId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return streamDefinitionStore.getStreamDefinition(streamId, tenantId);

    }

    @Override
    public List<StreamDefinition> getAllStreamDefinitions(int tenantId) {
        return new ArrayList<StreamDefinition>(streamDefinitionStore.getAllStreamDefinitions(tenantId));
    }


    @Override
    public void saveStreamDefinition(StreamDefinition streamDefinition, int tenantId)
            throws DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException {
        streamDefinitionStore.saveStreamDefinition(streamDefinition, tenantId);
    }

    @Override
    public boolean deleteStreamDefinition(String streamName,
                                          String streamVersion, int tenantId) {
        return streamDefinitionStore.deleteStreamDefinition(streamName, streamVersion, tenantId);
    }

    public List<AgentCallback> getSubscribers() {
        return eventDispatcher.getSubscribers();
    }

    public List<RawDataAgentCallback> getRawDataSubscribers() {
        return eventDispatcher.getRawDataSubscribers();
    }


    @Override
    public void subscribe(StreamAddRemoveListener streamAddRemoveListener) {
        if (streamAddRemoveListener != null) {
            streamAddRemoveListenerList.add(streamAddRemoveListener);
        }
    }

    @Override
    public void unsubscribe(StreamAddRemoveListener streamAddRemoveListener) {
        if (streamAddRemoveListener != null) {
            streamAddRemoveListenerList.remove(streamAddRemoveListener);
        }
    }


}




