/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.processor.manager.commons.transport.client;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
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
    private Disruptor<BiteArrayHolder> disruptor;
    private RingBuffer<BiteArrayHolder> ringBuffer;
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

        String[] hp = hostUrl.split(":");
        String host = hp[0];
        int port = Integer.parseInt(hp[1]);

        this.clientSocket = new Socket(host, port);
        this.outputStream = new BufferedOutputStream(this.clientSocket.getOutputStream());

        if (!isSynchronous){
            initializeDisruptor(publisherConfig);
        }
        log.info("Client configured to send events to " + hostUrl);
    }

    public TCPEventPublisher(String hostUrl, boolean isSynchronous) throws IOException {
        this(hostUrl, new TCPEventPublisherConfig(), isSynchronous);
    }

    public String getHostUrl(){
        return hostUrl;
    }

    public void addStreamDefinition(StreamDefinition streamDefinition) {
        streamRuntimeInfoMap.put(streamDefinition.getId(), EventServerUtils.createStreamRuntimeInfo(streamDefinition));
        log.info("Stream definition added for stream: " + streamDefinition.getId());
    }

    public void removeStreamDefinition(StreamDefinition streamDefinition) {
        streamRuntimeInfoMap.remove(streamDefinition.getId());
        log.info("Stream definition removed for stream: " + streamDefinition.getId());
    }

    /**
     * Send Events to the remote server. In synchronous mode this method call returns only after writing data to the socket
     * in asynchronous mode disruptor pattern is used
     * @param streamId ID of the stream
     * @param event event to send
     * @throws java.io.IOException
     */
    public void sendEvent(String streamId, Object[] event, boolean flush) throws IOException {
        StreamRuntimeInfo streamRuntimeInfo = streamRuntimeInfoMap.get(streamId);

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

        byte streamIdSize = streamRuntimeInfo.getStreamIdSize();
        ByteBuffer buf = ByteBuffer.allocate(streamRuntimeInfo.getFixedMessageSize() + streamIdSize + 1);
        buf.put(streamIdSize);
        buf.put((streamRuntimeInfo.getStreamId()).getBytes(DEFAULT_CHARSET));

        int[] stringDataIndex = new int[streamRuntimeInfo.getNoOfStringAttributes()];
        int stringIndex = 0;
        int stringSize = 0;
        Attribute.Type[] types = streamRuntimeInfo.getAttributeTypes();
        for (int i = 0, typesLength = types.length; i < typesLength; i++) {
            Attribute.Type type = types[i];
            switch (type) {
                case INT:
                    buf.putInt((Integer) event[i]);
                    continue;
                case LONG:
                    buf.putLong((Long) event[i]);
                    continue;
                case BOOL:
                    buf.put((byte) (((Boolean) event[i]) ? 1 : 0));
                    continue;
                case FLOAT:
                    buf.putFloat((Float) event[i]);
                    continue;
                case DOUBLE:
                    buf.putDouble((Double) event[i]);
                    continue;
                case STRING:
                    int length = ((String) event[i]).length();
                    buf.putInt(length);
                    stringDataIndex[stringIndex] = i;
                    stringIndex++;
                    stringSize += length;
            }
        }
        arrayOutputStream.write(buf.array());

        buf = ByteBuffer.allocate(stringSize);
        for (int aStringIndex : stringDataIndex) {
            buf.put(((String) event[aStringIndex]).getBytes(DEFAULT_CHARSET));
        }
        arrayOutputStream.write(buf.array());

        if (!isSynchronous){
            publishToDisruptor(arrayOutputStream.toByteArray());
        }else {
            publishEvent(arrayOutputStream.toByteArray(), flush);
        }
    }

    private void publishToDisruptor(byte[] byteArray){
        long sequenceNo = ringBuffer.next();
        try {
            BiteArrayHolder existingHolder = ringBuffer.get(sequenceNo);
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

    private void initializeDisruptor(TCPEventPublisherConfig publisherConfig) {
        this.disruptor = new Disruptor<BiteArrayHolder>(new EventFactory<BiteArrayHolder>() {
            @Override
            public BiteArrayHolder newInstance() {
                return new BiteArrayHolder();
            }
        }, publisherConfig.getBufferSize(), Executors.newSingleThreadExecutor());

        this.ringBuffer = disruptor.getRingBuffer();

        this.disruptor.handleExceptionsWith(new ExceptionHandler() {
            @Override
            public void handleEventException(Throwable throwable, long l, Object o) {}
            @Override
            public void handleOnStartException(Throwable throwable) {}
            @Override
            public void handleOnShutdownException(Throwable throwable) {}
        });

        this.disruptor.handleEventsWith(new EventHandler<BiteArrayHolder>() {
            @Override
            public void onEvent(BiteArrayHolder biteArrayHolder, long sequence, boolean endOfBatch) throws IOException {
                publishEvent(biteArrayHolder.bytes, endOfBatch);
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
            if (!isSynchronous){
                disruptor.shutdown();
            }
            outputStream.flush();
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            log.warn("Error while closing stream to " + hostUrl + " : " + e.getMessage(), e);
        }
    }

    /**
     * Discard all buffered messages and terminate the connection
     */
    public void terminate(){
        try {
            if (!isSynchronous){
                disruptor.halt();
            }
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            log.warn("Error while closing stream to " + hostUrl + " : " + e.getMessage(), e);
        }
    }

    class BiteArrayHolder {
        byte[] bytes;
    }
}
