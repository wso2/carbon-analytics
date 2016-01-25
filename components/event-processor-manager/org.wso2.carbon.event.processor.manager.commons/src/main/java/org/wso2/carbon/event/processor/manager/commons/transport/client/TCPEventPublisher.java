/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.processor.manager.commons.transport.client;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.manager.commons.transport.common.EventServerUtils;
import org.wso2.carbon.event.processor.manager.commons.transport.common.StreamRuntimeInfo;
import org.wso2.carbon.event.processor.manager.commons.transport.server.ConnectionCallback;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class TCPEventPublisher {
    public static final int PING_HEADER_VALUE = -99;

    private static Logger log = Logger.getLogger(TCPEventPublisher.class);
    private final String hostUrl;
    private Disruptor<ByteArrayHolder> disruptor;
    private RingBuffer<ByteArrayHolder> ringBuffer;
    private Map<String, StreamRuntimeInfo> streamRuntimeInfoMap;
    private OutputStream outputStream;
    private Socket clientSocket;
    private TCPEventPublisherConfig publisherConfig;
    public String defaultCharset;
    private Timer connectionStatusCheckTimer;

    /**
     * Indicate synchronous or asynchronous mode. In asynchronous mode Disruptor pattern is used and in synchronous mode sendEvent call
     * returns only after writing the event to the socket.
     */
    private boolean isSynchronous;
    private ConnectionCallback connectionCallback;
    /**
     * Callback to handle when the connection fails in middle
     */
    private ConnectionFailureHandler failureHandler = null;

    /**
     * @param hostUrl
     * @param publisherConfig
     * @param isSynchronous
     * @param connectionCallback is a callback, invoked on connect() and disconnect() methods of TCPEventPublisher. Set this to null if a callback is not needed.
     * @throws IOException
     */
    public TCPEventPublisher(String hostUrl, TCPEventPublisherConfig publisherConfig, boolean isSynchronous, ConnectionCallback connectionCallback)
            throws IOException {
        this.hostUrl = hostUrl;
        this.publisherConfig = publisherConfig;
        this.defaultCharset = publisherConfig.getCharset();
        this.streamRuntimeInfoMap = new ConcurrentHashMap<String, StreamRuntimeInfo>();
        this.isSynchronous = isSynchronous;
        this.connectionCallback = connectionCallback;

        if (!isSynchronous) {
            try {
                connect(hostUrl);
            } catch (IOException e) {
                log.error("Error connection to " + hostUrl, e);
            }
            initializeDisruptor(publisherConfig);
        } else {
            connect(hostUrl);
        }
    }

    private synchronized void connect(String hostUrl) throws IOException {
        String[] hp = hostUrl.split(":");
        String host = hp[0];
        int port = Integer.parseInt(hp[1]);

        this.clientSocket = new Socket(host, port);
        this.clientSocket.setKeepAlive(true);
        this.clientSocket.setTcpNoDelay(true);
        this.clientSocket.setSendBufferSize(publisherConfig.getTcpSendBufferSize());
        this.outputStream = new BufferedOutputStream(this.clientSocket.getOutputStream());
        log.info("Connecting to " + hostUrl);
        if (connectionCallback != null) {
            connectionCallback.onCepReceiverConnect();
        }

        connectionStatusCheckTimer = new Timer();
        connectionStatusCheckTimer.schedule(new ConnectionStatusCheckTask(), publisherConfig.getConnectionStatusCheckInterval(), publisherConfig.getConnectionStatusCheckInterval());
    }

    public TCPEventPublisher(String hostUrl, boolean isSynchronous, ConnectionCallback connectionCallback) throws IOException {
        this(hostUrl, new TCPEventPublisherConfig(), isSynchronous, connectionCallback);
    }

    public void addStreamDefinition(StreamDefinition streamDefinition) {
        streamRuntimeInfoMap.put(streamDefinition.getId(), EventServerUtils.createStreamRuntimeInfo(streamDefinition));
    }

    public void removeStreamDefinition(StreamDefinition streamDefinition) {
        streamRuntimeInfoMap.remove(streamDefinition.getId());
    }

    public void registerConnectionFailureHandler(ConnectionFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    /**
     * Send Events to the remote server. In synchronous mode this method call returns only after writing data to the socket
     * in asynchronous mode disruptor pattern is used
     *
     * @param streamId  ID of the stream
     * @param timestamp timestamp of the event
     * @param eventData data to send
     * @param flush     whether to flush the output stream in synchronous mode
     * @throws IOException
     */
    public void sendEvent(String streamId, long timestamp, Object[] eventData, boolean flush) throws IOException {
        sendEvent(streamId, timestamp, eventData, null, flush);
    }

    /**
     * Send Events to the remote server. In synchronous mode this method call returns only after writing data to the socket
     * in asynchronous mode disruptor pattern is used
     *
     * @param streamId  ID of the stream
     * @param timestamp timestamp of the event
     * @param eventData data to send
     * @throws java.io.IOException
     */
    public void sendEvent(String streamId, long timestamp, Object[] eventData, Map<String, String> arbitraryMap, boolean flush) throws IOException {
        StreamRuntimeInfo streamRuntimeInfo = streamRuntimeInfoMap.get(streamId);

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        boolean hasArbitraryAttributes = arbitraryMap != null;

        int streamIdSize = (streamRuntimeInfo.getStreamId()).getBytes(defaultCharset).length;
        int arbitraryMapSize = 0;
        if (hasArbitraryAttributes) {
            for (Map.Entry<String, String> entry : arbitraryMap.entrySet()) {
                arbitraryMapSize += 4 + entry.getKey().length();
                arbitraryMapSize += 4 + entry.getValue().length();
            }
        }
        ByteBuffer buf = ByteBuffer.allocate(streamRuntimeInfo.getFixedMessageSize() + streamIdSize + 16);
        buf.putInt(streamIdSize);
        buf.put((streamRuntimeInfo.getStreamId()).getBytes(defaultCharset));
        buf.putLong(timestamp);
        buf.putInt(arbitraryMapSize);

        int[] stringDataIndex = new int[streamRuntimeInfo.getNoOfStringAttributes()];
        int stringIndex = 0;
        int stringSize = 0;
        Attribute.Type[] types = streamRuntimeInfo.getAttributeTypes();
        for (int i = 0, typesLength = types.length; i < typesLength; i++) {
            Attribute.Type type = types[i];
            switch (type) {
                case INT:
                    buf.putInt((Integer) eventData[i]);
                    continue;
                case LONG:
                    buf.putLong((Long) eventData[i]);
                    continue;
                case BOOL:
                    buf.put((byte) (((Boolean) eventData[i]) ? 1 : 0));
                    continue;
                case FLOAT:
                    buf.putFloat((Float) eventData[i]);
                    continue;
                case DOUBLE:
                    buf.putDouble((Double) eventData[i]);
                    continue;
                case STRING:
                    if (eventData[i] == null) {
                        buf.putInt(-1);
                    } else {
                        int length = ((String) eventData[i]).getBytes(defaultCharset).length;
                        buf.putInt(length);
                        stringSize += length;
                    }
                    stringDataIndex[stringIndex] = i;
                    stringIndex++;
            }
        }
        arrayOutputStream.write(buf.array());

        buf = ByteBuffer.allocate(stringSize);
        for (int aStringIndex : stringDataIndex) {
            Object data = eventData[aStringIndex];
            if (data != null) {
                buf.put(((String) eventData[aStringIndex]).getBytes(defaultCharset));
            }
        }
        arrayOutputStream.write(buf.array());

        if (arbitraryMapSize > 0) {
            buf = ByteBuffer.allocate(arbitraryMapSize);
            for (Map.Entry<String, String> entry : arbitraryMap.entrySet()) {
                buf.putInt(entry.getKey().length());
                buf.put(entry.getKey().getBytes(defaultCharset));
                buf.putInt(entry.getValue().length());
                buf.put(entry.getValue().getBytes(defaultCharset));
            }
            arrayOutputStream.write(buf.array());
        }


        if (!isSynchronous) {
            publishToDisruptor(arrayOutputStream.toByteArray());
        } else {
            publishEvent(arrayOutputStream.toByteArray(), flush);
        }
    }

    private void publishToDisruptor(byte[] byteArray) {
        long sequenceNo = ringBuffer.next();
        try {
            ByteArrayHolder existingHolder = ringBuffer.get(sequenceNo);
            existingHolder.bytes = byteArray;
        } finally {
            ringBuffer.publish(sequenceNo);
        }
    }

    private synchronized void publishEvent(byte[] data, boolean flush) throws IOException {
        outputStream.write(data);
        if (flush) {
            outputStream.flush();
        }
    }

    private synchronized void publishEventAsync(byte[] data, boolean flush) throws IOException {
        if (outputStream != null) {
            try {
                outputStream.write(data);
                if (flush) {
                    outputStream.flush();
                }

            } catch (IOException e) {
                try {
                    log.error("Error on sending to " + hostUrl, e);
                    log.info("Reconnecting to " + hostUrl);
                    disconnect();
                    connect(hostUrl);
                    outputStream.write(data);
                    if (flush) {
                        outputStream.flush();
                    }
                } catch (IOException ex) {
                    log.error("Error on reconnection to " + hostUrl, ex);
                }
            }

        } else {
            try {
                log.info("Reconnecting to " + hostUrl);
                disconnect();
                connect(hostUrl);
                outputStream.write(data);
                if (flush) {
                    outputStream.flush();
                }
            } catch (IOException ex) {
                log.error("Error on reconnection to " + hostUrl, ex);
            }

        }

    }

    private void initializeDisruptor(TCPEventPublisherConfig publisherConfig) {
        this.disruptor = new Disruptor<ByteArrayHolder>(new EventFactory<ByteArrayHolder>() {
            @Override
            public ByteArrayHolder newInstance() {
                return new ByteArrayHolder();
            }
        }, publisherConfig.getBufferSize(), Executors.newCachedThreadPool());

        this.ringBuffer = disruptor.getRingBuffer();

        this.disruptor.handleEventsWith(new EventHandler<ByteArrayHolder>() {
            @Override
            public void onEvent(ByteArrayHolder byteArrayHolder, long sequence, boolean endOfBatch) throws IOException {
                publishEventAsync(byteArrayHolder.bytes, endOfBatch);
            }
        });

        disruptor.start();
    }


    /**
     * Gracefully shutdown the TCPEventPublisher and flush the data in output buffer.
     * When this method is used already consumer threads of disruptor will try to publishToDisruptor the queued messages in the RingBuffer.
     */
    public void shutdown() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            log.warn("Error while flushing output stream to " + hostUrl + " : " + e.getMessage(), e);
        } finally {
            terminate();
        }
    }

    /**
     * Immediately shutdown the TCPEventPublisher and discard the data in output buffer.
     */
    public void terminate() {
        connectionStatusCheckTimer.cancel();
        if (!isSynchronous) {
            disruptor.shutdown();
        }
        disconnect();
    }

    private void disconnect() {
        if (connectionStatusCheckTimer != null) {
            connectionStatusCheckTimer.cancel();
        }
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            log.debug("Error while disconnecting to " + hostUrl + " : " + e.getMessage(), e);
        }
        try {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
        } catch (IOException e) {
            log.debug("Error while closing socket to " + hostUrl + " : " + e.getMessage(), e);
        } finally {
            if (connectionCallback != null) {
                connectionCallback.onCepReceiverDisconnect();
            }
        }
    }

    class ByteArrayHolder {
        byte[] bytes;
    }

    public String getHostUrl() {
        return hostUrl;
    }


    class ConnectionStatusCheckTask extends TimerTask {

        private byte[] createPing() throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(TCPEventPublisher.PING_HEADER_VALUE);
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            arrayOutputStream.write(buffer.array());
            return arrayOutputStream.toByteArray();
        }

        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            try {
                publishEvent(createPing(), true);
            } catch (IOException e) {
                log.warn("Ping failed to " + getHostUrl() + " with error: " + e.getMessage());
                connectionStatusCheckTimer.cancel();
                if (failureHandler != null) {
                    failureHandler.onConnectionFail(e);
                }

            }
        }
    }
}
