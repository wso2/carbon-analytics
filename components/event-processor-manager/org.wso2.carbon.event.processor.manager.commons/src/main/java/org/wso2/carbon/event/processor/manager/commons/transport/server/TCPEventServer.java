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

package org.wso2.carbon.event.processor.manager.commons.transport.server;

import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.transport.common.EventServerUtils;
import org.wso2.carbon.event.processor.manager.commons.transport.common.StreamRuntimeInfo;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPEventServer {
    private static Logger log = Logger.getLogger(TCPEventServer.class);
    private TCPEventServerConfig tcpEventServerConfig = new TCPEventServerConfig("0.0.0.0", 7211);
    private ExecutorService executorService;
    private StreamCallback streamCallback;
    private ConnectionCallback connectionCallback;
    private ServerWorker serverWorker;
    private Map<String, StreamRuntimeInfo> streamRuntimeInfoMap = new ConcurrentHashMap<>();

    public TCPEventServer(TCPEventServerConfig tcpeventserverconfig, StreamCallback streamCallback, ConnectionCallback connectionCallback) {
        this.tcpEventServerConfig = tcpeventserverconfig;
        this.streamCallback = streamCallback;
        this.connectionCallback = connectionCallback;
        this.serverWorker = new ServerWorker();
        this.executorService = Executors.newCachedThreadPool();
    }

    public void addStreamDefinition(StreamDefinition streamDefinition) {
        String streamId = streamDefinition.getId();
        this.streamRuntimeInfoMap.put(streamId, EventServerUtils.createStreamRuntimeInfo(streamDefinition));
    }

    public void removeStreamDefinition(String streamId) {
        this.streamRuntimeInfoMap.remove(streamId);
    }

    public synchronized void start() throws IOException {
        if (!serverWorker.isRunning()) {
            serverWorker.startServerWorker();
            new Thread(serverWorker).start();
        }
    }

    public synchronized void shutdown() {
        serverWorker.shutdownServerWorker();
    }

    private class ServerWorker implements Runnable {
        private ServerSocket receiverSocket;
        private boolean isRunning = false;

        public boolean isRunning() {
            return isRunning;
        }

        public void startServerWorker() throws IOException {
            InetAddress inetAddress = InetAddress.getByName(tcpEventServerConfig.getHostName());
            log.info("EventServer starting event listener on " + inetAddress.getHostAddress() + ":" + tcpEventServerConfig.getPort());
            receiverSocket = new ServerSocket(tcpEventServerConfig.getPort(), 50, inetAddress);
            isRunning = true;
            receiverSocket.setReuseAddress(true);
        }

        public void shutdownServerWorker() {
            isRunning = false;
            try {
                if (receiverSocket != null) {
                    receiverSocket.close();
                }
            } catch (IOException e) {
                log.error("Error occurred while trying to shutdown socket: " + e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            try {

                while (isRunning) {
                    final Socket connectionSocket = receiverSocket.accept();
                    connectionSocket.setKeepAlive(true);
                    executorService.execute(new ListenerProcessor(connectionSocket));
                }
            } catch (Throwable e) {
                if (isRunning) {
                    log.error("Error while the server was listening for events: " + e.getMessage(), e);
                } else {
                    // Server shutdown has been triggered with a call to socket.close().
                    // socket.accept() is expected to throw SocketException
                    log.info("EventServer stopped listening for socket connections, " + e.getMessage());
                    if (log.isDebugEnabled()) {
                        log.debug("EventServer stopped listening for socket connections", e);
                    }
                }
            } finally {
                isRunning = false;
            }
        }

        private class ListenerProcessor implements Runnable {

            private final Socket connectionSocket;

            public ListenerProcessor(Socket connectionSocket) {
                this.connectionSocket = connectionSocket;
            }

            @Override
            public void run() {
                try {
                    if (connectionCallback != null) {
                        connectionCallback.onPublisherBoltConnect();
                    }
                    BufferedInputStream in = new BufferedInputStream(connectionSocket.getInputStream());
                    while (true) {

                        byte[] streamNameByteSize = loadData(in, new byte[4]);
                        ByteBuffer sizeBuf = ByteBuffer.wrap(streamNameByteSize);
                        int streamNameSize = sizeBuf.getInt();
                        if (streamNameSize == TCPEventPublisher.PING_HEADER_VALUE) {
                            continue;
                        }
                        byte[] streamNameData = loadData(in, new byte[streamNameSize]);
                        String streamId = new String(streamNameData, 0, streamNameData.length);
                        StreamRuntimeInfo streamRuntimeInfo = streamRuntimeInfoMap.get(streamId);
                        while (streamRuntimeInfo == null) {
                            Thread.sleep(1000);
                            log.warn("TCP server on port :'" + tcpEventServerConfig.getPort() + "' waiting for streamId:'" + streamId + "' to process incoming events");
                            streamRuntimeInfo = streamRuntimeInfoMap.get(streamId);
                        }
                        Object[] eventData = new Object[streamRuntimeInfo.getNoOfAttributes()];
                        byte[] fixedMessageData = loadData(in, new byte[12 + streamRuntimeInfo.getFixedMessageSize()]);

                        ByteBuffer bbuf = ByteBuffer.wrap(fixedMessageData, 0, fixedMessageData.length);
                        long timestamp = bbuf.getLong();
                        int arbitraryMapSize = bbuf.getInt();

                        List<Integer> stringValueSizes = new ArrayList<>();
                        Attribute.Type[] attributeTypes = streamRuntimeInfo.getAttributeTypes();
                        for (int i = 0; i < attributeTypes.length; i++) {
                            Attribute.Type type = attributeTypes[i];
                            switch (type) {
                                case INT:
                                    eventData[i] = bbuf.getInt();
                                    continue;
                                case LONG:
                                    eventData[i] = bbuf.getLong();
                                    continue;
                                case BOOL:
                                    eventData[i] = bbuf.get() == 1;
                                    continue;
                                case FLOAT:
                                    eventData[i] = bbuf.getFloat();
                                    continue;
                                case DOUBLE:
                                    eventData[i] = bbuf.getDouble();
                                    continue;
                                case STRING:
                                    int size = bbuf.getInt();
                                    stringValueSizes.add(size);
                            }
                        }

                        int stringSizePosition = 0;
                        for (int i = 0; i < attributeTypes.length; i++) {
                            Attribute.Type type = attributeTypes[i];
                            if (Attribute.Type.STRING == type) {
                                int size = stringValueSizes.get(stringSizePosition);
                                if (size == -1) {
                                    eventData[i] = null;
                                } else {
                                    byte[] stringData = loadData(in, new byte[size]);
                                    eventData[i] = new String(stringData, 0, stringData.length);
                                }
                                stringSizePosition++;
                            }
                        }

                        Map<String, String> arbitraryMap = null;
                        if (arbitraryMapSize > 0) {
                            arbitraryMap = new HashMap<>();
                            int arbitraryMapBytesRead = 0;
                            int keyStringSize, valueStringSize;
                            //TODO Consider loading all the attributes at once for better performance
                            byte[] arbitraryMapAttributeSizeData;
                            ByteBuffer arbitraryMapAttributeSizeByteBuf;
                            while (arbitraryMapBytesRead < arbitraryMapSize) {
                                arbitraryMapAttributeSizeData = loadData(in, new byte[4]);
                                arbitraryMapAttributeSizeByteBuf = ByteBuffer.wrap(arbitraryMapAttributeSizeData);
                                keyStringSize = arbitraryMapAttributeSizeByteBuf.getInt();
                                byte[] keyStringData = loadData(in, new byte[keyStringSize]);
                                arbitraryMapBytesRead += 4 + keyStringSize;
                                arbitraryMapAttributeSizeData = loadData(in, new byte[4]);
                                arbitraryMapAttributeSizeByteBuf = ByteBuffer.wrap(arbitraryMapAttributeSizeData);
                                valueStringSize = arbitraryMapAttributeSizeByteBuf.getInt();
                                byte[] valueStringData = loadData(in, new byte[valueStringSize]);
                                arbitraryMapBytesRead += 4 + valueStringSize;
                                arbitraryMap.put(new String(keyStringData), new String(valueStringData));
                            }
                        }

                        streamCallback.receive(streamId, timestamp, eventData, arbitraryMap);
                    }
                } catch (EOFException e) {
                    log.info("Closing listener socket. " + e.getMessage());
                } catch (IOException e) {
                    log.error("Error reading data from receiver socket:" + e.getMessage(), e);
                } catch (Throwable t) {
                    log.error("Error :" + t.getMessage(), t);
                } finally {
                    if (connectionCallback != null) {
                        connectionCallback.onPublisherBoltDisconnect();
                    }
                }
            }

            /**
             * Returns the number of bytes that were read by the stream.
             * This method does not return if the stream is closed from remote end.
             *
             * @param in the input stream to be read
             * @return the number of bytes read
             * @throws java.io.IOException
             */
            private int loadData(BufferedInputStream in) throws IOException {
                int byteData = in.read();
                if (byteData != -1) {
                    return byteData;
                } else {
                    throw new EOFException("Connection closed from remote end.");
                }
            }

            private byte[] loadData(BufferedInputStream in, byte[] dataArray) throws IOException {

                int start = 0;
                while (true) {
                    int readCount = in.read(dataArray, start, dataArray.length - start);
                    if (readCount != -1) {
                        start += readCount;
                        if (start == dataArray.length) {
                            return dataArray;
                        }
                    } else {
                        throw new EOFException("Connection closed from remote end.");
                    }
                }
            }
        }
    }
}
