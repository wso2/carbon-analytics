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

package org.wso2.carbon.databridge.agent.thrift;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.conf.DataPublisherConfiguration;
import org.wso2.carbon.databridge.agent.thrift.conf.ReceiverConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.internal.EventQueue;
import org.wso2.carbon.databridge.agent.thrift.internal.publisher.client.EventPublisher;
import org.wso2.carbon.databridge.agent.thrift.internal.publisher.client.EventPublisherFactory;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentServerURL;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverStateObserver;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Publisher will maintain a single connection to a server,
 * and define/find event streams and send events.
 */
public class DataPublisher {

//    private static Log log = LogFactory.getLog(DataPublisher.class);

    private static Log log = LogFactory.getLog(DataPublisher.class);
    //    private Agent agent;
    private DataPublisherConfiguration dataPublisherConfiguration;
    private EventPublisher eventPublisher;
    private EventQueue<Event> eventQueue;
    private Gson gson = new Gson();

    private ThreadPoolExecutor threadPool;

    private ReceiverStateObserver receiverStateObserver;

    /**
     * To create the Data Publisher and the respective agent to publish events
     * For TCP protocol
     * For secure data transfer the Authenticator URL will be the same as the Receiver URL and
     * for normal data transfer the Authenticator ip will be the same as receiver ip but its port will be 100+<receiver port> for tcp
     * For HTTP protocol
     * For secure data transfer the Authenticator URL will be the same as the Receiver URL and
     * normal data transfer NOT supported via this constructor
     *
     * @param receiverUrl the event receiver url
     *                    use <tcp>://<HOST>:<PORT> for normal data transfer and
     *                    use <ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param userName    user name
     * @param password    password
     * @throws java.net.MalformedURLException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     *
     */
    public DataPublisher(String receiverUrl, String userName, String password)
            throws MalformedURLException, AgentException, AuthenticationException,
            TransportException {
        this(receiverUrl, userName, password, AgentHolder.getOrCreateAgent());
    }

    /**
     * To create the Data Publisher and the respective agent to publish events
     *
     * @param authenticationUrl the secure authentication url, use <ssl/https>://<HOST>:<PORT>
     * @param receiverUrl       the event receiver url
     *                          use <tcp/http></tcp/http>://<HOST>:<PORT> for normal data transfer and
     *                          use <ssl/https></ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param userName          user name
     * @param password          password
     * @throws java.net.MalformedURLException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     *
     */
    public DataPublisher(String authenticationUrl, String receiverUrl, String userName,
                         String password)
            throws MalformedURLException, AgentException, AuthenticationException,
            TransportException {
        this(authenticationUrl, receiverUrl, userName, password, AgentHolder.getOrCreateAgent());
    }

    /**
     * To create the Data Publisher to publish events
     * For TCP protocol
     * For secure data transfer the Authenticator URL will be the same as the Receiver URL and
     * for normal data transfer the Authenticator ip will be the same as receiver ip but its port will be 100+<receiver port> for tcp
     * For HTTP protocol
     * For secure data transfer the Authenticator URL will be the same as the Receiver URL and
     * normal data transfer NOT supported via this constructor
     *
     * @param receiverUrl the event receiver url
     *                    use <tcp>://<HOST>:<PORT> for normal data transfer and
     *                    use <ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param userName    user name
     * @param password    password
     * @param agent       the underlining agent
     * @throws java.net.MalformedURLException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     *
     */
    public DataPublisher(String receiverUrl, String userName,
                         String password, Agent agent)
            throws MalformedURLException, AgentException, AuthenticationException,
            TransportException {
        /**
         * Only if the agent is not null, then the agent will be set to agent holder.
         * Ie, only for the first datapublisher which is setting the agent,
         * will be actually used and the rest of the agents passed-in will be neglected.
         */
        if (null == agent) {
            agent = AgentHolder.getOrCreateAgent();
        } else if (AgentHolder.getAgent() == null) {
            AgentHolder.setAgent(agent);
        }
        AgentServerURL receiverURL = new AgentServerURL(receiverUrl);
        checkHostAddress(receiverURL.getHost());
        if (receiverURL.isSecured()) {
            this.start(new ReceiverConfiguration(userName, password, receiverURL.getProtocol(),
                    (receiverURL.getHost()), receiverURL.getPort(),
                    receiverURL.getProtocol(),
                    (receiverURL.getHost()), receiverURL.getPort(), receiverURL.isSecured()),
                    AgentHolder.getAgent());
        } else if (receiverURL.getProtocol() == ReceiverConfiguration.Protocol.TCP) {
            this.start(new ReceiverConfiguration(userName, password, receiverURL.getProtocol(),
                    (receiverURL.getHost()), receiverURL.getPort(),
                    receiverURL.getProtocol(),
                    (receiverURL.getHost()), receiverURL.getPort() + CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET, receiverURL.isSecured()),
                    AgentHolder.getAgent());
        } else {
            throw new AgentException("http not supported via this constructor use https, ssl or tcp ");
        }

    }

    /**
     * To create the Data Publisher and the respective agent to publish events
     *
     * @param authenticationUrl the secure authentication url, use <ssl/https>://<HOST>:<PORT>
     * @param receiverUrl       the event receiver url
     *                          use <tcp/http></tcp/http>://<HOST>:<PORT> for normal data transfer and
     *                          use <ssl/https></ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param userName          user name
     * @param password          password
     * @param agent             the underlining agent
     * @throws java.net.MalformedURLException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     *
     */
    public DataPublisher(String authenticationUrl, String receiverUrl, String userName,
                         String password, Agent agent)
            throws MalformedURLException, AgentException, AuthenticationException,
            TransportException {
        /**
         * Only if the agent is not null, then the agent will be set to agent holder.
         * Ie, only for the first datapublisher which is setting the agent,
         * will be actually used and the rest of the agents passed-in will be neglected.
         */
        if (null == agent) {
            agent = AgentHolder.getOrCreateAgent();
        } else if (AgentHolder.getAgent() == null) {
            AgentHolder.setAgent(agent);
        }
        AgentServerURL authenticationURL = new AgentServerURL(authenticationUrl);
        if (!authenticationURL.isSecured()) {
            throw new MalformedURLException("Authentication url protocol is not ssl/https, expected = <ssl/https>://<HOST>:<PORT> but actual = " + authenticationUrl);
        }
        AgentServerURL receiverURL = new AgentServerURL(receiverUrl);
        checkHostAddress(receiverURL.getHost());
        checkHostAddress(authenticationURL.getHost());
        this.start(new ReceiverConfiguration(userName, password, receiverURL.getProtocol(),
                receiverURL.getHost(), receiverURL.getPort(),
                authenticationURL.getProtocol(),
                authenticationURL.getHost(), authenticationURL.getPort(), receiverURL.isSecured()),
                AgentHolder.getAgent());

    }

    private void checkHostAddress(String hostAddress) throws AgentException {
        try {
            HostAddressFinder.findAddress(hostAddress);
        } catch (SocketException e) {
            throw new AgentException(hostAddress + " is malformed ", e);
        }
    }

    /**
     * to set the underlining agent
     * which could be sheared by many data publishers
     *
     * @param agent the underlining agent
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.AuthenticationException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.TransportException
     *
     */
    public void setAgent(Agent agent)
            throws AgentException, AuthenticationException, TransportException {
        AgentHolder.getAgent().shutdown(this);// to shutdown the old agent
        AgentHolder.setAgent(agent);
        start(this.dataPublisherConfiguration.getReceiverConfiguration(), agent);
        if (null != this.receiverStateObserver) eventPublisher.registerReceiverStateObserver(receiverStateObserver);
    }

    /**
     * to get the underlining agent
     *
     * @return agent
     */
    public Agent getAgent() {
        return AgentHolder.getAgent();
    }

    public void registerReceiverStateObserver(ReceiverStateObserver stateObserver) {
        this.receiverStateObserver = stateObserver;
        this.eventPublisher.registerReceiverStateObserver(receiverStateObserver);
    }

    private void start(ReceiverConfiguration receiverConfiguration, Agent agent)
            throws AgentException, AuthenticationException, TransportException {
        agent.addDataPublisher(this);
        this.dataPublisherConfiguration = new DataPublisherConfiguration(receiverConfiguration);
        this.eventQueue = new EventQueue<Event>();
        this.threadPool = agent.getThreadPool();
        if (receiverConfiguration.isDataTransferSecured()) {
            this.eventPublisher = EventPublisherFactory.getEventPublisher(dataPublisherConfiguration, eventQueue, agent, agent.getSecureTransportPool());
        } else {
            this.eventPublisher = EventPublisherFactory.getEventPublisher(dataPublisherConfiguration, eventQueue, agent, agent.getTransportPool());
        }
        //Connect to the server
        dataPublisherConfiguration.setSessionId(agent.getAgentAuthenticator().connect(
                dataPublisherConfiguration));
    }

    /**
     * Defining stream on which events will be published by this DataPublisher
     *
     * @param streamDefinition on json format
     * @return the stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     *
     */
    public String defineStream(String streamDefinition)
            throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {
        String sessionId = dataPublisherConfiguration.getSessionId();
        return eventPublisher.defineStream(sessionId, streamDefinition);
    }

    /**
     * Defining stream on which events will be published by this DataPublisher
     *
     * @param streamDefinition on json format
     * @return the stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     *
     */
    public String defineStream(StreamDefinition streamDefinition)
            throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {
        String sessionId = dataPublisherConfiguration.getSessionId();
        String streamId = eventPublisher.defineStream(sessionId, gson.toJson(streamDefinition));
        try {
            Field field = StreamDefinition.class.getDeclaredField("streamId");
            field.setAccessible(true);
            field.set(streamDefinition, streamId);
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }
        return streamId;
    }

    /**
     * Finding already existing stream's Id to publish data
     *
     * @param name    the stream name
     * @param version the version of the stream
     * @return stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *          if client cannot publish the type definition
     */
    @Deprecated
    public String findStream(String name, String version)
            throws AgentException, StreamDefinitionException, NoStreamDefinitionExistException {
        String streamId= findStreamId(name,version);
        if(streamId==null){
            throw new NoStreamDefinitionExistException("Cannot find Stream Id for "+name+" "+version);
        }
        return streamId;
    }

 /**
     * Finding already existing stream's Id to publish data
     *
     * @param name    the stream name
     * @param version the version of the stream
     * @return stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *          if client cannot publish the type definition
     */
    public String findStreamId(String name, String version)
            throws AgentException {
        String sessionId = dataPublisherConfiguration.getSessionId();
        return eventPublisher.findStreamId(sessionId, name, version);
    }


    /**
     * Defining stream on which events will be published by this DataPublisher
     *
     * @param streamId of the Stream
     * @return the stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     *
     */
    public boolean deleteStream(String streamId) throws AgentException {
        String sessionId = dataPublisherConfiguration.getSessionId();
        return eventPublisher.deleteStream(sessionId, streamId);
    }

    /**
     * Defining stream on which events will be published by this DataPublisher
     *
     * @param streamName    of the Stream
     * @param streamVersion of the Stream
     * @return the stream id
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     *
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     *
     */
    public boolean deleteStream(String streamName, String streamVersion) throws AgentException {
        String sessionId = dataPublisherConfiguration.getSessionId();
        return eventPublisher.deleteStream(sessionId, streamName, streamVersion);
    }

    /**
     * Publishing the events to the server
     *
     * @param event the event to be published
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *          if client cannot publish the event
     */
    public void publish(Event event) throws AgentException {
        try {
            AgentHolder.getAgent().getQueueSemaphore().acquire();//control the total number of buffered events

            //Adds event to the queue and checks whether its scheduled for event dispatching
            if (!eventQueue.put(event)) {
                try {
                    //if not schedule for event dispatching
                    threadPool.execute(eventPublisher);
                } catch (RejectedExecutionException ignored) {
                }
            }
        } catch (InterruptedException e) {
            throw new AgentException("Cannot add " + event + " to event queue", e);
        }
    }

    public boolean tryPublish(Event event) throws AgentException {
        try {
            if (AgentHolder.getAgent().getQueueSemaphore().tryAcquire()) {//control the total number of buffered events
                //Adds event to the queue and checks whether its scheduled for event dispatching
                short status = eventQueue.tryPut(event);
                switch (status) {
                    case 0:
                        try {
                            //if not schedule for event dispatching
                            threadPool.execute(eventPublisher);
                        } catch (RejectedExecutionException ignored) {
                        }
                        return true;
                    case 1:
                        return true;
                    case 2:
                        return false;
                    default:
                        return false;
                }
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            throw new AgentException("Cannot add " + event + " to event queue", e);
        }
    }

    /**
     * Publishing events to the server
     *
     * @param streamId             of the stream on which the events are published
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation data array of the event
     * @param payloadDataArray     payload data array of the event
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     */
    public void publish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray)
            throws AgentException {
        publish(new Event(streamId, System.currentTimeMillis(), metaDataArray, correlationDataArray, payloadDataArray));
    }

    public boolean tryPublish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray, Map<String, String> arbitraryDataMap)
            throws AgentException {
        return tryPublish(new Event(streamId, System.currentTimeMillis(), metaDataArray, correlationDataArray, payloadDataArray, arbitraryDataMap));
    }

    public boolean tryPublish(String streamId, long timestamp, Object[] metaDataArray, Object[] correlationDataArray,
                              Object[] payloadDataArray, Map<String, String> arbitraryDataMap)
            throws AgentException {
        return tryPublish(new Event(streamId, timestamp, metaDataArray, correlationDataArray, payloadDataArray, arbitraryDataMap));
    }


    /**
     * Publishing events to the server
     *
     * @param streamId             of the stream on which the events are published
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation data array of the event
     * @param payloadDataArray     payload data array of the event
     * @param arbitraryDataMap     arbitrary mata (as meta.<key_name>),correlation (as correlation.<key_name>) & payload (as <key_name>) data as key-value pairs
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     */
    public void publish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray, Map<String, String> arbitraryDataMap)
            throws AgentException {
        publish(new Event(streamId, System.currentTimeMillis(), metaDataArray, correlationDataArray, payloadDataArray, arbitraryDataMap));
    }

    /**
     * Publishing events to the server
     *
     * @param streamId             of the stream on which the events are published
     * @param timeStamp            time stamp of the event
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation data array of the event
     * @param payloadDataArray     payload data array of the event
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     */
    public void publish(String streamId, long timeStamp, Object[] metaDataArray,
                        Object[] correlationDataArray, Object[] payloadDataArray)
            throws AgentException {
        publish(new Event(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray));
    }

    /**
     * Publishing events to the server
     *
     * @param streamId             of the stream on which the events are published
     * @param timeStamp            time stamp of the event
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation data array of the event
     * @param payloadDataArray     payload data array of the event
     * @param arbitraryDataMap     arbitrary mata (as meta.<key_name>),correlation (as correlation.<key_name>) & payload (as <key_name>) data as key-value pairs
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     *
     */
    public void publish(String streamId, long timeStamp, Object[] metaDataArray,
                        Object[] correlationDataArray, Object[] payloadDataArray, Map<String, String> arbitraryDataMap)
            throws AgentException {
        publish(new Event(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray, arbitraryDataMap));
    }

    /**
     * Disconnecting from the server
     */
    public void stop() {
        AgentHolder.getAgent().getAgentAuthenticator().disconnect(dataPublisherConfiguration);
        AgentHolder.getAgent().shutdown(this);
    }


    /**
     * Disconnecting from the server
     */
    public void stopNow() {
        AgentHolder.getAgent().getAgentAuthenticator().disconnect(dataPublisherConfiguration);
        AgentHolder.getAgent().shutdownNow(this);
    }


}
