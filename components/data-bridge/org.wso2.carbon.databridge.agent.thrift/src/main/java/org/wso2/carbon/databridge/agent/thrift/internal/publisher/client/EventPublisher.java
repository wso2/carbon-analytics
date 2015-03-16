/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.databridge.agent.thrift.internal.publisher.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.thrift.conf.DataPublisherConfiguration;
import org.wso2.carbon.databridge.agent.thrift.conf.ReceiverConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.exception.EventPublisherException;
import org.wso2.carbon.databridge.agent.thrift.internal.EventQueue;
import org.wso2.carbon.databridge.agent.thrift.internal.publisher.authenticator.AgentAuthenticator;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverStateObserver;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The publisher who sends all the arrived events to the Agent Server using a pool of threads
 */
public abstract class EventPublisher implements Runnable {
    private static Log log = LogFactory.getLog(EventPublisher.class);

    private EventQueue<Event> eventQueue;
    private GenericKeyedObjectPool transportPool;
    private Semaphore queueSemaphore;
    private int maxMessageBundleSize;
    private DataPublisherConfiguration dataPublisherConfiguration;
    private AgentAuthenticator agentAuthenticator;
    private ThreadPoolExecutor threadPool;

    private ReceiverStateObserver receiverStateObserver;

    public EventPublisher(EventQueue<Event> eventQueue,
                          GenericKeyedObjectPool transportPool,
                          Semaphore queueSemaphore,
                          int maxMessageBundleSize,
                          DataPublisherConfiguration dataPublisherConfiguration,
                          AgentAuthenticator agentAuthenticator, ThreadPoolExecutor threadPool) {

        this.eventQueue = eventQueue;
        this.transportPool = transportPool;
        this.queueSemaphore = queueSemaphore;
        this.maxMessageBundleSize = maxMessageBundleSize;
        this.dataPublisherConfiguration = dataPublisherConfiguration;
        this.agentAuthenticator = agentAuthenticator;
        this.threadPool = threadPool;
    }

    public void registerReceiverStateObserver(ReceiverStateObserver stateObserver) {
        if (null == receiverStateObserver) {
            receiverStateObserver = stateObserver;
        }
    }

    public void run() {
        ArrayList<Event> tempEventBundleHolder = null;
        Object eventBundle = null;
        if (null != receiverStateObserver) {
            tempEventBundleHolder = new ArrayList<Event>();
        }
        while (true) {
            Event event = eventQueue.poll();
            if (event != null) {
                queueSemaphore.release();

                if (null != tempEventBundleHolder) {
                    tempEventBundleHolder.add(event);
                }
                eventBundle = convertToEventBundle(eventBundle, event, dataPublisherConfiguration.getSessionId());

                //Sending the event bundle when maxMessageBundleSize is reached
                if (getNumberOfEvents(eventBundle) >= maxMessageBundleSize) {
                    publishEvent(eventBundle, tempEventBundleHolder);
                    if (null != receiverStateObserver) {
                        tempEventBundleHolder = new ArrayList<Event>();
                    }

                    //checking if all threads in the pool are NOT active
                    if (threadPool.getActiveCount() < threadPool.getCorePoolSize()) {
                        //starts building another event bundle with the same thread
                        eventBundle = null;
                    } else {
                        //submit the remaining task to the end of the taskQueue
                        // and exit
                        threadPool.submit(this);
                        break;
                    }
                }
            } else {
                //When the queue is empty
                if (eventBundle != null) {
                    publishEvent(eventBundle, tempEventBundleHolder);
                }
                //Since no more events are available to dispatch, exit
                break;
            }
        }
    }

    private void publishEvent(Object eventBundle, ArrayList<Event> tempEventBundleHolder) {
        Object client = null;
        try {
            client = getClient(dataPublisherConfiguration.getPublisherKey());
            setSessionId(eventBundle, dataPublisherConfiguration.getSessionId());
            publish(client, eventBundle);
            returnClient(client);
        } catch (AgentException e) {
            discardClient();
            notifyConnectionFailure(tempEventBundleHolder);
            log.error("Cannot get a client to send events to " + dataPublisherConfiguration.getPublisherKey(), e);
        } catch (SessionTimeoutException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey() + "," + e.getMessage());
            setSessionId(eventBundle, reconnect(getSessionId(eventBundle)));
            republish(client, eventBundle, tempEventBundleHolder);
        } catch (UndefinedEventTypeException e) {
            log.error("Wrongly typed event " + eventBundle + " sent to " + dataPublisherConfiguration.getPublisherKey(), e);
            returnClient(client);
        } catch (EventPublisherException e) {
            discardClient();
            notifyConnectionFailure(tempEventBundleHolder);
            log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey(), e);
        }
    }


    private void republish(Object client, Object eventBundle, ArrayList<Event> tempEventBundleHolder) {
        try {
            publish(client, eventBundle);
            returnClient(client);
        } catch (EventPublisherException e) {
            discardClient();
            log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                    " even after reconnecting ", e);
            notifyConnectionFailure(tempEventBundleHolder);
        } catch (SessionTimeoutException e) {
            discardClient();
            log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                    + " even after reconnecting ", e);
        } catch (UndefinedEventTypeException e) {
            log.error("Wrongly typed event " + eventBundle.toString() + " sent  to " +
                    dataPublisherConfiguration.getPublisherKey(), e);
            returnClient(client);
        }
    }

    private void notifyConnectionFailure(ArrayList<Event> tempEventBundleHolder) {
        if (null != receiverStateObserver) {
            ReceiverConfiguration conf = dataPublisherConfiguration.getReceiverConfiguration();
            StringBuilder url = new StringBuilder(conf.getDataReceiverProtocol().toString());
            url.append("://");
            url.append(conf.getDataReceiverIp());
            url.append(":");
            url.append(conf.getDataReceiverPort());
            receiverStateObserver.notifyConnectionFailure(url.toString(), conf.getUserName(), conf.getPassword());
            if (null != eventQueue) {
                LinkedBlockingQueue<Event> events = eventQueue.getAndResetQueue();
                if (null != tempEventBundleHolder) {
                    for (Event event : tempEventBundleHolder) {
                        try {
                            events.put(event);
                        } catch (InterruptedException ignore) {
                            log.error("Error while populating resend events list", ignore);
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("resending events size: " + events.size() + "in thread id :" + Thread.currentThread().getId());
                }
                receiverStateObserver.resendEvents(events);
            }

        }
    }


    protected abstract int getNumberOfEvents(Object eventBundle);

    protected abstract ThriftEventBundle convertToEventBundle(Object eventBundle, Event event,
                                                              String sessionId);


    protected abstract void setSessionId(Object eventBundle, String reconnect);

    protected abstract String getSessionId(Object eventBundle);


    abstract void publish(Object client, Object eventBundle)
            throws UndefinedEventTypeException, SessionTimeoutException, EventPublisherException;

    public String defineStream(String sessionId, String streamDefinition)
            throws AgentException, DifferentStreamDefinitionAlreadyDefinedException,
            MalformedStreamDefinitionException, StreamDefinitionException {
        String currentSessionId = sessionId;
        String streamId = null;
        Object client = getClient(dataPublisherConfiguration.getPublisherKey());
        try {
            streamId = defineStream(client, currentSessionId, streamDefinition);
            returnClient(client);
        } catch (SessionTimeoutException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey() + "," + e.getMessage());
            currentSessionId = reconnect(currentSessionId);
            streamId = redefineStream(client, streamDefinition, currentSessionId);
        } catch (StreamDefinitionException e) {
            returnClient(client);
            throw new StreamDefinitionException("Invalid type definition for stream " + streamDefinition, e);
        } catch (EventPublisherException e) {
            discardClient();
            notifyConnectionFailure(null);
            throw new AgentException("Cannot define type " + streamDefinition, e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            returnClient(client);
            throw new DifferentStreamDefinitionAlreadyDefinedException("Same stream with different definition already defined before sending this stream definitions to " +
                    dataPublisherConfiguration.getPublisherKey(), e);
        } catch (MalformedStreamDefinitionException e) {
            returnClient(client);
            throw new MalformedStreamDefinitionException("Malformed event definition :" + streamDefinition + " send  to " +
                    dataPublisherConfiguration.getPublisherKey(), e);
        }
        return streamId;
    }

    private String redefineStream(Object client, String streamDefinition,
                                  String currentSessionId)
            throws DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException,
            MalformedStreamDefinitionException {
        String streamId = null;
        try {
            streamId = defineStream(client, currentSessionId, streamDefinition);
            returnClient(client);
            return streamId;
        } catch (SessionTimeoutException ex) {
            log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                    + " even after reconnecting ", ex);
            discardClient();
            return null;
        } catch (EventPublisherException ex) {
            log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                    " even after reconnecting ", ex);
            discardClient();
            notifyConnectionFailure(null);
            return null;
        } catch (DifferentStreamDefinitionAlreadyDefinedException ex) {
            returnClient(client);
            throw new DifferentStreamDefinitionAlreadyDefinedException("Type already defined when send event definitions to" +
                    dataPublisherConfiguration.getPublisherKey(), ex);
        } catch (StreamDefinitionException ex) {
            returnClient(client);
            throw new StreamDefinitionException("Wrongly defined event definition after reconnection  :" + streamDefinition + " sent to " +
                    dataPublisherConfiguration.getPublisherKey(), ex);
        } catch (MalformedStreamDefinitionException ex) {
            returnClient(client);
            throw new MalformedStreamDefinitionException("Malformed event definition after reconnection  :" + streamDefinition + " sent to " +
                    dataPublisherConfiguration.getPublisherKey(), ex);
        }
    }

    protected abstract String defineStream(Object client, String currentSessionId,
                                           String streamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
            MalformedStreamDefinitionException,
            EventPublisherException, SessionTimeoutException, StreamDefinitionException;


    public String findStreamId(String sessionId, String name, String version)
            throws AgentException {
        String currentSessionId = sessionId;
        String streamId = null;
        Object client = getClient(dataPublisherConfiguration.getPublisherKey());
        try {
            streamId = findStreamId(client, currentSessionId, name, version);
            returnClient(client);
        } catch (SessionTimeoutException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey() + "," + e.getMessage());
            currentSessionId = reconnect(currentSessionId);
            try {
                streamId = findStreamId(client, currentSessionId, name, version);
                returnClient(client);
            } catch (SessionTimeoutException ex) {
                discardClient();
                log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                        + " even after reconnecting ", ex);
            } catch (EventPublisherException ex) {
                discardClient();
                log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                        " even after reconnecting ", ex);
                notifyConnectionFailure(null);
            }
        } catch (EventPublisherException e) {
            discardClient();
            notifyConnectionFailure(null);
            throw new AgentException("Error when finding event stream definition for : " + name + " " + version, e);
        }
        return streamId;

    }

    protected abstract String findStreamId(Object client, String currentSessionId, String name,
                                           String version)
            throws SessionTimeoutException, EventPublisherException;

    public boolean deleteStream(String sessionId, String streamId) throws AgentException {
        String currentSessionId = sessionId;
        Object client = getClient(dataPublisherConfiguration.getPublisherKey());
        boolean status = false;
        try {
            status = deleteStream(client, currentSessionId, streamId);
            returnClient(client);
        } catch (SessionTimeoutException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey() + "," + e.getMessage());
            currentSessionId = reconnect(currentSessionId);
            try {
                status = deleteStream(client, currentSessionId, streamId);
                returnClient(client);
            } catch (EventPublisherException ex) {
                discardClient();
                log.error("Cannot delete stream " + streamId + " at " + dataPublisherConfiguration.getPublisherKey() +
                        " even after reconnecting ", ex);
                notifyConnectionFailure(null);
            } catch (SessionTimeoutException ex) {
                discardClient();
                log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                        + " even after reconnecting ", ex);
            }

        } catch (EventPublisherException e) {
            discardClient();
            notifyConnectionFailure(null);
            throw new AgentException("Error when deleting event stream of: " + streamId, e);
        }
        return status;
    }

    protected abstract boolean deleteStream(Object client, String currentSessionId, String streamId) throws EventPublisherException, SessionTimeoutException;

    public boolean deleteStream(String sessionId, String streamName, String streamVersion) throws AgentException {
        String currentSessionId = sessionId;
        Object client = getClient(dataPublisherConfiguration.getPublisherKey());
        boolean status = false;
        try {
            status = deleteStream(client, currentSessionId, streamName, streamVersion);
            returnClient(client);
        } catch (SessionTimeoutException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey() + "," + e.getMessage());
            currentSessionId = reconnect(currentSessionId);
            try {
                status = deleteStream(client, currentSessionId, streamName, streamVersion);
                returnClient(client);
            } catch (EventPublisherException ex) {
                discardClient();
                log.error("Cannot delete stream " + streamName + " " + streamVersion + " at " + dataPublisherConfiguration.getPublisherKey() +
                        " even after reconnecting ", ex);
                notifyConnectionFailure(null);
            } catch (SessionTimeoutException ex) {
                discardClient();
                log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                        + " even after reconnecting ", ex);
            }

        } catch (EventPublisherException e) {
            discardClient();
            notifyConnectionFailure(null);
            throw new AgentException("Error when deleting event stream of: " + streamName + " " + streamVersion, e);
        }
        return status;
    }

    protected abstract boolean deleteStream(Object client, String currentSessionId, String streamName, String streamVersion) throws EventPublisherException, SessionTimeoutException;

    private String reconnect(String currentSessionId) {
        attemptReconnection(AgentConstants.AGENT_RECONNECTION_TIMES, currentSessionId);
        return dataPublisherConfiguration.getSessionId();
    }

    public synchronized void attemptReconnection(
            int reconnectionTime, String sessionId) {
        if (!dataPublisherConfiguration.getSessionId().equals(sessionId)) {
            return;
        }
        if (reconnectionTime > 0) {
            try {
                dataPublisherConfiguration.setSessionId(
                        agentAuthenticator.connect(dataPublisherConfiguration));
            } catch (AuthenticationException e) {
                log.error(dataPublisherConfiguration.getReceiverConfiguration().getUserName() +
                        " not authorised to access server at " +
                        dataPublisherConfiguration.getPublisherKey());
            } catch (TransportException e) {
                attemptReconnection(reconnectionTime - 1, sessionId);
            } catch (AgentException e) {
                attemptReconnection(reconnectionTime - 1, sessionId);
            }
        }
    }

    private Object getClient(String publisherKey) throws AgentException {
        try {
            return transportPool.borrowObject(publisherKey);
        } catch (Exception e) {
            notifyConnectionFailure(null);
            throw new AgentException("Cannot borrow client for " + publisherKey, e);
        }
    }

    private void returnClient(Object client) {
        try {
            transportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
        } catch (Exception e) {
            notifyConnectionFailure(null);
            log.warn("Error occurred while returning object to connection pool", e);
            discardClient();
        }
    }

    private void discardClient() {
        transportPool.clear(dataPublisherConfiguration.getPublisherKey());
    }

}
