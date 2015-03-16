package org.wso2.carbon.databridge.agent.thrift;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverStateObserver;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.agent.thrift.util.PublishData;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * AsyncDataPublisher creates the connection to with receiver asynchronously and will queue all the events
 */
public class AsyncDataPublisher {

    private static Log log = LogFactory.getLog(AsyncDataPublisher.class);

    private DataPublisher dataPublisher;

    private ConcurrentHashMap<String, String> streamIdCache = new ConcurrentHashMap<String, String>();

    private ConcurrentHashMap<String, String> streamDefnCache = new ConcurrentHashMap<String, String>();

    private LinkedBlockingQueue<PublishData> publishDataQueue;

    private AtomicBoolean isPublisherAlive = new AtomicBoolean(false);

    private AtomicBoolean isConnectorAlive;

    private ReceiverConnectionWorker receiverConnectionWorker;

    private ExecutorService connectorService = Executors.newSingleThreadExecutor();

    private ExecutorService publisherService = Executors.newSingleThreadExecutor();

    private ReceiverStateObserver receiverStateObserver;

    private Gson gson = new Gson();


    /**
     * To create the Data Publisher and creates the connection asynchronously
     *
     * @param authenticationURL the secure authentication url, use <ssl/https>://<HOST>:<PORT>
     * @param receiverURL       the event receiver url
     *                          use <tcp/http></tcp/http>://<HOST>:<PORT> for normal data transfer and
     *                          use <ssl/https></ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param username          user name
     * @param password          password
     */
    public AsyncDataPublisher(String authenticationURL, String receiverURL,
                              String username, String password) {
        isConnectorAlive = new AtomicBoolean(true);
        this.publishDataQueue = new LinkedBlockingQueue<PublishData>(AgentConstants
                                                                             .DEFAULT_ASYNC_CLIENT_BUFFERED_EVENTS_SIZE);
        receiverConnectionWorker = new ReceiverConnectionWorker(authenticationURL,
                                                                receiverURL, username, password, null);
        connectorService.submit(receiverConnectionWorker);
    }

    /**
     * To create the Data Publisher and creates the connection asynchronously
     *
     * @param receiverURL the event receiver url
     *                    use <tcp/http></tcp/http>://<HOST>:<PORT> for normal data transfer and
     *                    use <ssl/https></ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param username    user name
     * @param password    password
     */
    public AsyncDataPublisher(String receiverURL, String username,
                              String password) {
        isConnectorAlive = new AtomicBoolean(true);
        this.publishDataQueue = new LinkedBlockingQueue<PublishData>(AgentConstants
                                                                             .DEFAULT_ASYNC_CLIENT_BUFFERED_EVENTS_SIZE);
        receiverConnectionWorker = new ReceiverConnectionWorker(receiverURL,
                                                                username, password);
        connectorService.submit(receiverConnectionWorker);
    }

    /**
     * To create the Data Publisher and creates the connection asynchronously
     *
     * @param receiverURL the event receiver url
     *                    use <tcp/http></tcp/http>://<HOST>:<PORT> for normal data transfer and
     *                    use <ssl/https></ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param username    user name
     * @param password    password
     * @param agent       the underlining agent
     */
    public AsyncDataPublisher(String receiverURL, String username,
                              String password, Agent agent) {
        isConnectorAlive = new AtomicBoolean(true);
        this.publishDataQueue = new LinkedBlockingQueue<PublishData>
                (agent.getAgentConfiguration().getAsyncDataPublisherBufferedEventSize());
        receiverConnectionWorker = new ReceiverConnectionWorker(receiverURL,
                                                                username, password, agent);
        connectorService.submit(receiverConnectionWorker);
    }

    /**
     * To create the Data Publisher and creates the connection asynchronously
     *
     * @param authenticationUrl the secure authentication url, use <ssl/https>://<HOST>:<PORT>
     * @param receiverUrl       the event receiver url
     *                          use <tcp/http></tcp/http>://<HOST>:<PORT> for normal data transfer and
     *                          use <ssl/https></ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param userName          user name
     * @param password          password
     * @param agent             the underlining agent
     */
    public AsyncDataPublisher(String authenticationUrl, String receiverUrl, String userName,
                              String password, Agent agent) {
        isConnectorAlive = new AtomicBoolean(true);
        this.publishDataQueue = new LinkedBlockingQueue<PublishData>
                (agent.getAgentConfiguration().getAsyncDataPublisherBufferedEventSize());
        receiverConnectionWorker = new ReceiverConnectionWorker(authenticationUrl, receiverUrl,
                                                                userName, password, agent);
        connectorService.submit(receiverConnectionWorker);
    }

    /**
     * To create the Data Publisher and creates the connection asynchronously
     *
     * @param authenticationUrl the secure authentication url, use <ssl/https>://<HOST>:<PORT>
     * @param receiverUrl       the event receiver url
     *                          use <tcp/http></tcp/http>://<HOST>:<PORT> for normal data transfer and
     *                          use <ssl/https></ssl/https>://<HOST>:<PORT> for secure data transfer
     * @param userName          user name
     * @param password          password
     * @param agent             the underlining agent
     * @param streamDefnCache   the common Stream Definition cache used by AsyncDataPublishers
     */
    public AsyncDataPublisher(String authenticationUrl, String receiverUrl, String userName,
                              String password, Agent agent,
                              ConcurrentHashMap<String, String> streamDefnCache) {
        if (streamDefnCache != null) {
            this.streamDefnCache = streamDefnCache;
        }
        isConnectorAlive = new AtomicBoolean(true);
        if (agent != null) {
            this.publishDataQueue = new LinkedBlockingQueue<PublishData>
                    (agent.getAgentConfiguration().getAsyncDataPublisherBufferedEventSize());
            receiverConnectionWorker = new ReceiverConnectionWorker(authenticationUrl, receiverUrl,
                                                                    userName, password, agent);
        } else {
            this.publishDataQueue = new LinkedBlockingQueue<PublishData>(AgentConstants
                                                                                 .DEFAULT_ASYNC_CLIENT_BUFFERED_EVENTS_SIZE);
            receiverConnectionWorker = new ReceiverConnectionWorker(authenticationUrl, receiverUrl,
                                                                    userName, password, null);
        }
        connectorService.submit(receiverConnectionWorker);
    }


    /**
     * To create the Data Publisher and creates the connection asynchronously
     *
     * @param dataPublisher DataPublisher used by the Asynchronous client to publish the events
     */

    public AsyncDataPublisher(DataPublisher dataPublisher) {
        this.dataPublisher = dataPublisher;
        int bufferSize = dataPublisher.getAgent()
                .getAgentConfiguration().getAsyncDataPublisherBufferedEventSize();
        publishDataQueue = new LinkedBlockingQueue<PublishData>(bufferSize);
    }


    /**
     * Establish the connection of data publisher to receiver again
     */
    public void reconnect() {
        if (!isConnectorAlive.get()) {
            if (isConnectorAlive.compareAndSet(false, true)) {
                receiverConnectionWorker.isReconnecting = true;
                publisherService.submit(receiverConnectionWorker);
            }
        }
    }

    /**
     * Evaluates the publisher is ready and established the connection to publish the events
     *
     * @return boolean value about the status of the data publisher's publishing capability
     */
    public boolean canPublish() {
        return !(null == dataPublisher && !isConnectorAlive.get());
//         if (null == dataPublisher && !isConnectorAlive.get()) {
//            return false;
//        } else {
//            return true;
//        }
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
        dataPublisher.setAgent(agent);
    }

    /**
     * to get the underlining agent
     *
     * @return agent
     */
    public Agent getAgent() {
        if (null != dataPublisher) {
            return dataPublisher.getAgent();
        } else {
            return null;
        }
    }


    public void registerReceiverObserver(ReceiverStateObserver observer) {
        receiverStateObserver = observer;
        if (null != dataPublisher) {
            dataPublisher.registerReceiverStateObserver(receiverStateObserver);
        }
    }

    /**
     * Publish the event asynchronously.
     * <p/>
     * If the data publisher hasn't been initialized properly, the events will be pushed in the queue
     * If the stream id is not in the cache the event will be pushed in the queue
     * else the stream id from the cache will be used to publish the event
     *
     * @param streamName           Name of the stream which the events is for
     * @param streamVersion        Version of the stream which the events is for
     * @param timeStamp            Timestamp of the event
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation array of the event
     * @param payloadDataArray     payload array of the event
     * @throws AgentException
     */
    public void publish(String streamName, String streamVersion,
                        long timeStamp,
                        Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray) throws AgentException {
        publish(streamName, streamVersion, timeStamp, metaDataArray, correlationDataArray, payloadDataArray, null);
    }

    /**
     * Publish the event asynchronously.
     * <p/>
     * If the data publisher hasn't been initialized properly, the events will be pushed in the queue
     * If the stream id is not in the cache the event will be pushed in the queue
     * else the stream id from the cache will be used to publish the event
     *
     * @param streamName           Name of the stream which the events is for
     * @param streamVersion        Version of the stream which the events is for
     * @param timeStamp            Timestamp of the event
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation array of the event
     * @param payloadDataArray     payload array of the event
     * @param arbitraryDataMap     arbitrary mata (as meta.<key_name>),correlation (as correlation.<key_name>) & payload (as <key_name>) data as key-value pairs
     * @throws AgentException
     */
    public void publish(String streamName, String streamVersion,
                        long timeStamp,
                        Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray, Map<String, String> arbitraryDataMap) throws AgentException {
        if (canPublish()) {
            if (null != dataPublisher) {
                String streamKey = DataPublisherUtil.getStreamCacheKey(streamName, streamVersion);
                String streamId = streamIdCache.get(streamKey);
                if (null != streamId) {
                    boolean publishSuccessful = dataPublisher.tryPublish(streamId, timeStamp, metaDataArray,
                                          correlationDataArray, payloadDataArray, arbitraryDataMap);
                    if (!publishSuccessful) {
                        boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                streamVersion,
                                timeStamp,
                                metaDataArray,
                                correlationDataArray,
                                payloadDataArray,
                                arbitraryDataMap));

                        if (isPublisherAlive.compareAndSet(false, true)) {
                            publisherService.submit(new DataPublishWorker());
                        }

                        if (!isAdded) {
                            log.error("Event queue is full, and Event is not added to the queue to publish");
                        }
                    }
                } else {
                    boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                             streamVersion,
                                                                             timeStamp,
                                                                             metaDataArray,
                                                                             correlationDataArray,
                                                                             payloadDataArray,
                                                                             arbitraryDataMap));

                    if (isPublisherAlive.compareAndSet(false, true)) {
                        publisherService.submit(new DataPublishWorker());
                    }

                    if (!isAdded) {
                        log.error("Event queue is full, and Event is not added to the queue to publish");
                    }
                }
            } else {
                //receiverConnector worker is started to connect, but not yet dataPublisher is setted
                boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                         streamVersion,
                                                                         timeStamp,
                                                                         metaDataArray,
                                                                         correlationDataArray,
                                                                         payloadDataArray,
                                                                         arbitraryDataMap));
                if (!isAdded && log.isDebugEnabled()) {
                    log.debug("Event queue is full, and Event is not added to the queue to publish");
                }
            }

        } else {
            boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                     streamVersion,
                                                                     timeStamp,
                                                                     metaDataArray,
                                                                     correlationDataArray,
                                                                     payloadDataArray,
                                                                     arbitraryDataMap));
            reconnect();
            if (!isAdded && log.isDebugEnabled()) {
                log.debug("Event queue is full, and Event is not added to the queue to publish");
            }
        }
    }


    /**
     * Publish the event asynchronously.
     * <p/>
     * If the data publisher hasn't been initialized properly, the events will be pushed in the queue
     * If the stream generatorsid is not in the cache the event will be pushed in the queue
     * else the stream id from the cache will be used to publish the event
     *
     * @param streamName           Name of the stream which the events is for
     * @param streamVersion        Version of the stream which the events is for
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation array of the event
     * @param payloadDataArray     payload array of the event
     * @throws AgentException
     */
    public void publish(String streamName, String streamVersion,
                        Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray) throws AgentException {
        publish(streamName, streamVersion, metaDataArray, correlationDataArray, payloadDataArray, null);
    }

    /**
     * Publish the event asynchronously.
     * <p/>
     * If the data publisher hasn't been initialized properly, the events will be pushed in the queue
     * If the stream generatorsid is not in the cache the event will be pushed in the queue
     * else the stream id from the cache will be used to publish the event
     *
     * @param streamName           Name of the stream which the events is for
     * @param streamVersion        Version of the stream which the events is for
     * @param metaDataArray        metadata array of the event
     * @param correlationDataArray correlation array of the event
     * @param payloadDataArray     payload array of the event
     * @throws AgentException
     */
    public void publish(String streamName, String streamVersion,
                        Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray, Map<String, String> arbitraryDataMap) throws AgentException {
        if (canPublish()) {
            if (null != dataPublisher) {
                String streamKey = DataPublisherUtil.getStreamCacheKey(streamName, streamVersion);
                String streamId = streamIdCache.get(streamKey);
                if (null != streamId) {
                    boolean publishSuccessful = dataPublisher.tryPublish(streamId, metaDataArray,
                                          correlationDataArray, payloadDataArray, arbitraryDataMap);
                    if (!publishSuccessful) {
                        boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                streamVersion,
                                metaDataArray,
                                correlationDataArray,
                                payloadDataArray,
                                arbitraryDataMap));

                        if (isPublisherAlive.compareAndSet(false, true)) {
                            publisherService.submit(new DataPublishWorker());
                        }

                        if (!isAdded) {
                            log.error("Event queue is full, and Event is not added to the queue to publish");
                        }
                    }
                } else {
                    boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                             streamVersion,
                                                                             metaDataArray,
                                                                             correlationDataArray,
                                                                             payloadDataArray,
                                                                             arbitraryDataMap));

                    if (isPublisherAlive.compareAndSet(false, true)) {
                        publisherService.submit(new DataPublishWorker());
                    }

                    if (!isAdded) {
                        log.error("Event queue is full, and Event is not added to the queue to publish");
                    }
                }
            } else {
                //receiverConnector worker is started to connect, but not yet dataPublisher is setted
                boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                         streamVersion,
                                                                         metaDataArray,
                                                                         correlationDataArray,
                                                                         payloadDataArray,
                                                                         arbitraryDataMap));
                if (!isAdded && log.isDebugEnabled()) {
                    log.debug("Event queue is full, and Event is not added to the queue to publish");
                }
            }
        } else {
            boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                     streamVersion,
                                                                     metaDataArray,
                                                                     correlationDataArray,
                                                                     payloadDataArray,
                                                                     arbitraryDataMap));
            reconnect();
            if (!isAdded && log.isDebugEnabled()) {
                log.debug("Event queue is full, and Event is not added to the queue to publish");
            }
        }
    }


    /**
     * Publish the event asynchronously.
     * <p/>
     * If the data publisher hasn't been initialized properly, the events will be pushed in the queue
     * If the stream id is not in the cache the event will be pushed in the queue
     * else the stream id from the cache will be used to publish the event
     *
     * @param streamName    Name of the stream which the events is for
     * @param streamVersion Version of the stream which the events is for
     * @param event         Event which should be published
     * @throws AgentException
     */
    public void publish(String streamName, String streamVersion, Event event)
            throws AgentException {
        if (canPublish()) {
            if (null != dataPublisher) {
                String streamKey = DataPublisherUtil.getStreamCacheKey(streamName, streamVersion);
                String streamId = streamIdCache.get(streamKey);
                if (null != streamId) {
                    event.setStreamId(streamId);
                    boolean publishSuccessful = dataPublisher.tryPublish(event);
                    if (!publishSuccessful) {
                        boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                streamVersion,
                                event));

                        if (isPublisherAlive.compareAndSet(false, true)) {
                            publisherService.submit(new DataPublishWorker());
                        }

                        if (!isAdded) {
                            log.error("Event queue is full, and Event is not added to the queue to publish");
                        }
                    }
                } else {
                    boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                             streamVersion,
                                                                             event));

                    if (isPublisherAlive.compareAndSet(false, true)) {
                        publisherService.submit(new DataPublishWorker());
                    }

                    if (!isAdded) {
                        log.error("Event queue is full, and Event is not added to the queue to publish");
                    }
                }
            } else {
                //receiverConnector worker is started to connect, but not yet dataPublisher is setted
                boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                         streamVersion,
                                                                         event));
                if (!isAdded && log.isDebugEnabled()) {
                    log.debug("Event queue is full, and Event is not added to the queue to publish");
                }
            }
        } else {
            boolean isAdded = publishDataQueue.offer(new PublishData(streamName,
                                                                     streamVersion,
                                                                     event));
            reconnect();
            if (!isAdded && log.isDebugEnabled()) {
                log.debug("Event queue is full, and Event is not added to the queue to publish");
            }
        }
    }

    /**
     * Publish the event asynchronously.
     * <p/>
     * If the data publisher hasn't been initialized properly, the events will be pushed in the queue
     * If the stream id is not in the cache the event will be pushed in the queue
     * else the stream id from the cache will be used to publish the event
     *
     * @param event Event which should be published
     * @throws AgentException
     */
    public void publish(Event event) throws AgentException {
        if (canPublish()) {
            if (null != dataPublisher) {
                boolean publishSuccessful = dataPublisher.tryPublish(event);
                if (!publishSuccessful) {
                    //receiverConnector worker is started to connect, but not yet dataPublisher is setted
                    boolean isAdded = publishDataQueue.offer(new PublishData(null,
                            null,
                            event));

                    if (!isAdded && log.isDebugEnabled()) {
                        log.debug("Event queue is full, and Event is not added to the queue to publish");
                    }
                }
            } else {
                //receiverConnector worker is started to connect, but not yet dataPublisher is setted
                boolean isAdded = publishDataQueue.offer(new PublishData(null,
                                                                         null,
                                                                         event));

                if (!isAdded && log.isDebugEnabled()) {
                    log.debug("Event queue is full, and Event is not added to the queue to publish");
                }
            }
        } else {
            boolean isAdded = publishDataQueue.offer(new PublishData(null,
                                                                     null,
                                                                     event));
            reconnect();

            if (!isAdded && log.isDebugEnabled()) {
                log.debug("Event queue is full, and Event is not added to the queue to publish");
            }
        }
    }

    /**
     * resets the publishDataQueue and returns all elements
     *
     * @return all elements of queue
     */
    public synchronized LinkedBlockingQueue<PublishData> getQueuedEventsAndReset() {
        LinkedBlockingQueue<PublishData> temp = publishDataQueue;
        publishDataQueue = null;
        return temp;
    }

    /**
     * add the stream definition which can be published by
     * the AsyncDataPublisher
     *
     * @param streamDefn json string format of the stream definition
     * @param streamName Name of stream which should be added
     * @param version    Version of stream which should be added
     */
    public void addStreamDefinition(String streamDefn, String streamName, String version) {
        String key = DataPublisherUtil.getStreamCacheKey(streamName, version);
        streamDefnCache.put(key, streamDefn);
    }

    /**
     * add the stream definition which can be published by
     * the AsyncDataPublisher
     *
     * @param definition Stream definition object which should be added
     */

    public void addStreamDefinition(StreamDefinition definition) {
        String key = DataPublisherUtil.getStreamCacheKey(definition.getName(), definition.getVersion());
        streamDefnCache.put(key, gson.toJson(definition));
    }

    /**
     * Returns a boolean whether the stream definition is already
     * added to cache in async data publisher
     *
     * @param streamName Name of the stream needed to check
     * @param version    version of the stream needed to check
     * @return whether the stream definition is exists or not
     */
    public boolean isStreamDefinitionAdded(String streamName, String version) {
        String key = DataPublisherUtil.getStreamCacheKey(streamName, version);
        return null != streamDefnCache.get(key);
    }


    /**
     * Returns a boolean whether the stream definition is already
     * added to cache in async data publisher
     *
     * @param streamDefinition Object of the stream definition needed to check in the cache
     * @return whether the stream definition is exists or not
     */
    public boolean isStreamDefinitionAdded(StreamDefinition streamDefinition) {
        String key = DataPublisherUtil.getStreamCacheKey(streamDefinition.getName(),
                                                         streamDefinition.getVersion());
        return null != streamDefnCache.get(key);
    }


    /**
     * Returns the streamId if the already stream name and version is defined in the receiver
     *
     * @param name    Name of the event stream
     * @param version Version of the stream
     * @return
     * @throws AgentException
     * @throws StreamDefinitionException
     * @throws NoStreamDefinitionExistException
     *
     */
    @Deprecated
    public String findStream(String name, String version)
            throws AgentException, StreamDefinitionException, NoStreamDefinitionExistException {
        if (null != dataPublisher) {
            String key = DataPublisherUtil.getStreamCacheKey(name, version);
            String streamId = streamIdCache.get(key);
            if (null == streamId) {
                streamId = dataPublisher.findStream(name, version);
                streamIdCache.put(key, streamId);
            }
            return streamId;
        }
        return null;
    }

    /**
     * Returns the streamId if the already stream name and version is defined in the receiver
     *
     * @param name    Name of the event stream
     * @param version Version of the stream
     * @return
     * @throws AgentException
     */
    public String findStreamId(String name, String version)
            throws AgentException {
        if (null != dataPublisher) {
            String key = DataPublisherUtil.getStreamCacheKey(name, version);
            String streamId = streamIdCache.get(key);
            if (null == streamId) {
                streamId = dataPublisher.findStreamId(name, version);
                streamIdCache.put(key, streamId);
            }
            return streamId;
        }
        return null;
    }

    /**
     * Disconnecting from the server
     */

    public void stop() {
        publisherService.shutdown();
        connectorService.shutdown();
        if (null != dataPublisher) {
            dataPublisher.stop();
        }
    }


    /**
     * Worker thread which is responsible to get the events from the queue
     * and publish
     */
    private class DataPublishWorker implements Runnable {

        @Override
        public void run() {
            try {
                PublishData data = publishDataQueue.poll();
                while (null != data) {
                    String streamIdKey = null;
                    String streamId = null;
                    if (data.getStreamName() != null && data.getStreamVersion() != null) {
                        streamIdKey = DataPublisherUtil.getStreamCacheKey(data.getStreamName(),
                                                                          data.getStreamVersion());
                        streamId = streamIdCache.get(streamIdKey);
                    } else {
                        streamId = data.getEvent().getStreamId();
                    }
                    if (null == streamId) {
                        try {
                            Object defn = streamDefnCache.get(streamIdKey);
                            if (defn instanceof StreamDefinition) {
                                streamId = dataPublisher.defineStream((StreamDefinition) defn);
                            } else if (defn instanceof String) {
                                streamId = dataPublisher.defineStream(defn.toString());
                            } else {
                                log.error("Not Supported stream definition type");
                            }

                            if (null != streamId) {
                                streamIdCache.put(streamIdKey, streamId);
                                data.getEvent().setStreamId(streamId);
                                dataPublisher.publish(data.getEvent());
                            } else {
                                log.error("Stream Id is null for stream definition :" + defn.toString());
                            }

                        } catch (AgentException e) {
                            log.error("Error occurred while finding | defining the event", e);
                        } catch (StreamDefinitionException e) {
                            log.error("Error occurred while defining the event", e);
                        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                            log.error("Stream definition already exist", e);
                        } catch (MalformedStreamDefinitionException e) {
                            log.error("Malformed stream definition", e);
                        }
                    } else {
                        data.getEvent().setStreamId(streamId);
                        try {
                            dataPublisher.publish(data.getEvent());
                        } catch (AgentException e) {
                            log.error("Error occurred while publishing the event", e);
                        }
                    }
                    data = publishDataQueue.poll();
                }
                isPublisherAlive.set(false);
                if (null != publishDataQueue.peek() && !isPublisherAlive.get()) {
                    if (isPublisherAlive.compareAndSet(false, true)) {
                        publisherService.submit(new DataPublishWorker());
                    }
                }
            } catch (Throwable extremeCaseEx) {
                log.error(extremeCaseEx.getMessage(), extremeCaseEx);
                isPublisherAlive.set(false);
                if (null != publishDataQueue.peek() && !isPublisherAlive.get()) {
                    if (isPublisherAlive.compareAndSet(false, true)) {
                        publisherService.submit(new DataPublishWorker());
                    }
                }
            }
        }
    }

    /**
     * Worker thread which is responsible for
     * making connection to the server
     */
    private class ReceiverConnectionWorker implements Runnable {

        private String authenticationUrl;
        private String receiverUrl;
        private String username;
        private String password;
        private Agent agent;
        private boolean isReconnecting;

        private ReceiverConnectionWorker(String authenticationUrl, String receiverUrl, String username, String password, Agent agent) {
            this.authenticationUrl = authenticationUrl;
            this.receiverUrl = receiverUrl;
            this.username = username;
            this.password = password;
            this.agent = agent;
        }

        private ReceiverConnectionWorker(String receiverUrl, String username, String password) {
            this.receiverUrl = receiverUrl;
            this.username = username;
            this.password = password;
        }


        private ReceiverConnectionWorker(String receiverUrl, String username, String password, Agent agent) {
            this.receiverUrl = receiverUrl;
            this.username = username;
            this.password = password;
            this.agent = agent;
        }


        @Override
        public void run() {
            try {
                try {
                    if (null != authenticationUrl) {
                        if (null != agent) {
                            dataPublisher = new DataPublisher(authenticationUrl, receiverUrl, username, password, agent);
                        } else {
                            dataPublisher = new DataPublisher(authenticationUrl, receiverUrl, username, password);
                        }
                    } else if (null != agent) {
                        dataPublisher = new DataPublisher(receiverUrl, username, password, agent);
                    } else {
                        dataPublisher = new DataPublisher(receiverUrl, username, password);
                    }

                    if (null != receiverStateObserver) {
                        dataPublisher.registerReceiverStateObserver(receiverStateObserver);
                        receiverStateObserver.notifyConnectionSuccess(receiverUrl, username, password);
                    }

                    isPublisherAlive.compareAndSet(false, true);
                    Thread triggerDataPublisherWorker = new Thread(new DataPublishWorker());
                    triggerDataPublisherWorker.start();

                } catch (MalformedURLException e) {
                    dataPublisher = null;
                    if (!isReconnecting) {
                        log.error("Malformed url error when connecting to receiver", e);
                    } else {
                        log.error("Reconnection failed for " + receiverUrl);
                    }
                    if (null != receiverStateObserver) {
                        receiverStateObserver.notifyConnectionFailure(receiverUrl, username, password);
                        receiverStateObserver.
                                resendPublishedData(publishDataQueue);
                    }
                } catch (AgentException e) {
                    dataPublisher = null;
                    if (!isReconnecting) {
                        log.error("Error while connection to event receiver", e);
                    } else {
                        log.error("Reconnection failed for for " + receiverUrl);
                    }
                    if (null != receiverStateObserver) {
                        receiverStateObserver.notifyConnectionFailure(receiverUrl, username, password);
                        receiverStateObserver.
                                resendPublishedData(publishDataQueue);
                    }
                } catch (AuthenticationException e) {
                    dataPublisher = null;
                    if (!isReconnecting) {
                        log.error("Error while connection to event receiver", e);
                    } else {
                        log.error("Reconnection failed for" + receiverUrl);
                    }
                    if (null != receiverStateObserver) {
                        receiverStateObserver.notifyConnectionFailure(receiverUrl, username, password);
                        receiverStateObserver.
                                resendPublishedData(publishDataQueue);
                    }
                } catch (TransportException e) {
                    dataPublisher = null;
                    if (!isReconnecting) {
                        log.error("Error while connection to event receiver", e);
                    } else {
                        log.error("Reconnection failed for " + receiverUrl);
                    }
                    if (null != receiverStateObserver) {
                        receiverStateObserver.notifyConnectionFailure(receiverUrl, username, password);
                        receiverStateObserver.
                                resendPublishedData(publishDataQueue);
                    }
                }
                isReconnecting = false;
                isConnectorAlive.set(false);

            } catch (Throwable extremeCaseEx) {
                log.error(extremeCaseEx.getMessage(), extremeCaseEx);
                isReconnecting = false;
                isConnectorAlive.set(false);
            }
        }

    }
}
