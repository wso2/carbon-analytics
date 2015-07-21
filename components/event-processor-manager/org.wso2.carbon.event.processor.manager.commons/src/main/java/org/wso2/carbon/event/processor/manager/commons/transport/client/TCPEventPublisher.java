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
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class TCPEventPublisher {

    public static final String DEFAULT_CHARSET = "UTF-8";
    private static Logger log = Logger.getLogger(TCPEventPublisher.class);
    private final String hostUrl;
    private Disruptor<ByteArrayHolder> disruptor;
    private RingBuffer<ByteArrayHolder> ringBuffer;
    private Map<String, StreamRuntimeInfo> streamRuntimeInfoMap;
    private OutputStream outputStream;
    private Socket clientSocket;
    private TCPEventPublisherConfig publisherConfig;
    /**
     * Indicate synchronous or asynchronous mode. In asynchronous mode Disruptor pattern is used and in synchronous mode sendEvent call
     * returns only after writing the event to the socket.
     */
    private boolean isSynchronous;

    public TCPEventPublisher(String hostUrl, TCPEventPublisherConfig publisherConfig, boolean isSynchronous) throws IOException {
        this.hostUrl = hostUrl;
        this.publisherConfig = publisherConfig;
        this.streamRuntimeInfoMap = new ConcurrentHashMap<String, StreamRuntimeInfo>();
        this.isSynchronous = isSynchronous;

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

    private void connect(String hostUrl) throws IOException {
        String[] hp = hostUrl.split(":");
        String host = hp[0];
        int port = Integer.parseInt(hp[1]);

        this.clientSocket = new Socket(host, port);
        this.outputStream = new BufferedOutputStream(this.clientSocket.getOutputStream());
        log.info("Connecting to " + hostUrl);
    }

    public TCPEventPublisher(String hostUrl, boolean isSynchronous) throws IOException {
        this(hostUrl, new TCPEventPublisherConfig(), isSynchronous);
    }

    public void addStreamDefinition(StreamDefinition streamDefinition) {
        streamRuntimeInfoMap.put(streamDefinition.getId(), EventServerUtils.createStreamRuntimeInfo(streamDefinition));
    }

    public void removeStreamDefinition(StreamDefinition streamDefinition) {
        streamRuntimeInfoMap.remove(streamDefinition.getId());
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
    public void sendEvent(String streamId, long timestamp, Object[] eventData, boolean flush) throws IOException {
        StreamRuntimeInfo streamRuntimeInfo = streamRuntimeInfoMap.get(streamId);

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

        int streamIdSize = (streamRuntimeInfo.getStreamId()).getBytes(DEFAULT_CHARSET).length;
        ByteBuffer buf = ByteBuffer.allocate(streamRuntimeInfo.getFixedMessageSize() + streamIdSize + 12);
        buf.putInt(streamIdSize);
        buf.put((streamRuntimeInfo.getStreamId()).getBytes(DEFAULT_CHARSET));
        buf.putLong(timestamp);

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
                    int length = ((String) eventData[i]).getBytes(DEFAULT_CHARSET).length;
                    buf.putInt(length);
                    stringDataIndex[stringIndex] = i;
                    stringIndex++;
                    stringSize += length;
            }
        }
        arrayOutputStream.write(buf.array());

        buf = ByteBuffer.allocate(stringSize);
        for (int aStringIndex : stringDataIndex) {
            buf.put(((String) eventData[aStringIndex]).getBytes(DEFAULT_CHARSET));
        }
        arrayOutputStream.write(buf.array());

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

    private void publishEvent(byte[] data, boolean flush) throws IOException {
        outputStream.write(data);
        if (flush) {
            outputStream.flush();
        }
    }

    private void publishEventAsync(byte[] data, boolean flush) throws IOException {
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
        }, publisherConfig.getBufferSize(), Executors.newSingleThreadExecutor());

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
     * Gracefully shutdown the TCPEventPublisher.
     * When this method is used already consumer threads of distruptor will try to publishToDisruptor the queued messages in the RingBuffer.
     */
    public void shutdown() {
        try {
            if (!isSynchronous) {
                disruptor.shutdown();
            }
            outputStream.flush();
        } catch (IOException e) {
            log.warn("Error while closing stream to " + hostUrl + " : " + e.getMessage(), e);
        } finally {
            disconnect();
        }

    }

    private void disconnect() {
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
        }
    }

    /**
     * Discard all buffered messages and terminate the connection
     */
    public void terminate() {
        if (!isSynchronous) {
            disruptor.halt();
        }
        disconnect();
    }

    class ByteArrayHolder {
        byte[] bytes;
    }

    public String getHostUrl() {
        return hostUrl;
    }
}
