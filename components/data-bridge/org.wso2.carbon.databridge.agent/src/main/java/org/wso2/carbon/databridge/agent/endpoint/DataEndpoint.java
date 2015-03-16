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
package org.wso2.carbon.databridge.agent.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract class for DataEndpoint, and this is a main class that needs to be implemented
 * for supporting different transports to DataPublisher. This abstraction provides the additional
 * functionality to handle failover, asynchronous connection to the endpoint, etc.
 */

public abstract class DataEndpoint {

    private static Log log = LogFactory.getLog(DataEndpoint.class);

    private DataEndpointConnectionWorker connectionWorker;

    private GenericKeyedObjectPool transportPool;

    private int batchSize;

    private AtomicBoolean isPublishing;

    private EventPublisher eventPublisher;

    private DataEndpointFailureCallback dataEndpointFailureCallback;

    private ExecutorService connectionService;

    private List<Event> events;

    private State state;

    public enum State {
        ACTIVE, UNAVAILABLE, BUSY
    }

    public DataEndpoint() {
        this.batchSize = DataEndpointConstants.DEFAULT_DATA_AGENT_BATCH_SIZE;
        this.state = State.UNAVAILABLE;
        this.isPublishing = new AtomicBoolean(false);
        eventPublisher = new EventPublisher();
        connectionService = Executors.newSingleThreadExecutor();
        events = new ArrayList<Event>();
    }

    void collectAndSend(Event event) {
        events.add(event);
        if (events.size() == batchSize) {
            this.state = State.BUSY;
            Thread thread = new Thread(eventPublisher);
            thread.start();
        }
    }

    void flushEvents() {
        if (events.size() != 0) {
            if (isPublishing.compareAndSet(false, true)) {
                state = State.BUSY;
                Thread thread = new Thread(eventPublisher);
                thread.start();
            }
        }
    }


    void connect()
            throws TransportException,
            DataEndpointAuthenticationException, DataEndpointException {
        if (connectionWorker != null) {
            connectionService.submit(connectionWorker);
        } else {
            throw new DataEndpointException("Data Endpoint is not initialized");
        }
    }

    public void initialize(DataEndpointConfiguration dataEndpointConfiguration)
            throws DataEndpointException, DataEndpointAuthenticationException,
            TransportException {
        this.transportPool = dataEndpointConfiguration.getTransportPool();
        this.batchSize = dataEndpointConfiguration.getBatchSize();
        connectionWorker = new DataEndpointConnectionWorker();
        connectionWorker.initialize(this, dataEndpointConfiguration);
        connect();
    }

    /**
     * Login to the endpoint and return the sessionId.
     *
     * @param client   The client which can be used to connect to the endpoint.
     * @param userName The username which is used to login,
     * @param password The password which is required for the login operation.
     * @return returns the sessionId
     * @throws DataEndpointAuthenticationException
     */
    protected abstract String login(Object client, String userName, String password)
            throws DataEndpointAuthenticationException;

    /**
     * Logout from the endpoint.
     *
     * @param client    The client that is used to logout operation.
     * @param sessionId The current session Id.
     * @throws DataEndpointAuthenticationException
     */
    protected abstract void logout(Object client, String sessionId)
            throws DataEndpointAuthenticationException;


    public State getState() {
        return state;
    }

    void activate() {
        state = State.ACTIVE;
    }

    void deactivate() {
        state = State.UNAVAILABLE;
    }

    /**
     * Send the list of events to the actual endpoint.
     *
     * @param client The client that can be used to send the events.
     * @param events List of events that needs to be sent.
     * @throws DataEndpointException
     * @throws SessionTimeoutException
     * @throws UndefinedEventTypeException
     */
    protected abstract void send(Object client, List<Event> events) throws
            DataEndpointException, SessionTimeoutException, UndefinedEventTypeException;


    protected DataEndpointConfiguration getDataEndpointConfiguration() {
        return this.connectionWorker.getDataEndpointConfiguration();
    }

    private Object getClient() throws DataEndpointException {
        try {
            return transportPool.borrowObject(getDataEndpointConfiguration().getPublisherKey());
        } catch (Exception e) {
            throw new DataEndpointException("Cannot borrow client for " +
                    getDataEndpointConfiguration().getPublisherKey(), e);
        }
    }

    private void returnClient(Object client) {
        try {
            transportPool.returnObject(getDataEndpointConfiguration().getPublisherKey(), client);
        } catch (Exception e) {
            log.warn("Error occurred while returning object to connection pool", e);
            discardClient();
        }
    }

    private void discardClient() {
        transportPool.clear(getDataEndpointConfiguration().getPublisherKey());
    }

    void registerDataEndpointFailureCallback(DataEndpointFailureCallback callback) {
        dataEndpointFailureCallback = callback;
    }

    /**
     * Event Publisher worker thread to actually sends the events to the endpoint.
     */
    class EventPublisher implements Runnable {
        @Override
        public void run() {
            try {
                publish();
            } catch (SessionTimeoutException e) {
                try {
                    connect();
                    publish();
                } catch (UndefinedEventTypeException ex) {
                    log.error("Unable to process this event.", ex);
                } catch (Exception ex) {
                    handleFailedEvents();
                }
            } catch (DataEndpointException e) {
                handleFailedEvents();
            } catch (UndefinedEventTypeException e) {
                log.error("Unable to process this event.", e);
            }
        }

        private void handleFailedEvents() {
            deactivate();
            dataEndpointFailureCallback.tryResendEvents(events);
            isPublishing.set(false);
        }

        private void publish() throws DataEndpointException,
                SessionTimeoutException,
                UndefinedEventTypeException {
            Object client = getClient();
            send(client, events);
            events.clear();
            state = State.ACTIVE;
            isPublishing.set(false);
            returnClient(client);
        }
    }

    boolean isConnected() {
        return !state.equals(State.UNAVAILABLE);
    }

    public String toString() {
        return "( Receiver URL : " + getDataEndpointConfiguration().getReceiverURL() +
                ", Authentication URL : " + getDataEndpointConfiguration().getAuthURL() + ")";
    }

    /**
     * Graceful shutdown until publish all the events given to the endpoint.
     */
    public void shutdown() {
        while (state.equals(State.BUSY)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        connectionWorker.disconnect(getDataEndpointConfiguration());
        connectionService.shutdown();
    }

    /**
     * Get the class name of implementation for
     * org.wso2.carbon.databridge.agent.client.AbstractClientPoolFactory class.
     *
     * @return Canonical name of the implementing class.
     */
    public abstract String getClientPoolFactoryClass();

    /**
     * Get the class name of implementation for
     * org.wso2.carbon.databridge.agent.client.AbstractSecureClientPoolFactory class.
     *
     * @return Canonical name of the implementing class.
     */
    public abstract String getSecureClientPoolFactoryClass();
}
