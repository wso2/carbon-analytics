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
import org.wso2.carbon.event.processor.manager.commons.transport.common.EventServerUtils;
import org.wso2.carbon.event.processor.manager.commons.transport.common.StreamRuntimeInfo;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPEventServer {
    private static Logger log = Logger.getLogger(TCPEventServer.class);
    private TCPEventServerConfig tcpEventServerConfig = new TCPEventServerConfig(7211);
    private ExecutorService executorService;
    private StreamCallback streamCallback;
    private ConnectionCallback connectionCallback;
    private ServerWorker serverWorker;
    private Map<String, StreamRuntimeInfo> streamRuntimeInfoMap = new ConcurrentHashMap<String, StreamRuntimeInfo>();

    public TCPEventServer(TCPEventServerConfig TCPEventServerConfig, StreamCallback streamCallback, ConnectionCallback connectionCallback) {
        this.tcpEventServerConfig = TCPEventServerConfig;
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

    public void start() {
        if (!serverWorker.isRunning()) {
            new Thread(serverWorker).start();
        }
    }

    public void shutdown() {
        serverWorker.shutdownServerWorker();
    }

    private class ServerWorker implements Runnable {
        private ServerSocket receiverSocket;
        private boolean isRunning = false;

        public boolean isRunning() {
            return isRunning;
        }

        public void shutdownServerWorker() {
            isRunning = false;
            try {
                receiverSocket.close();
            } catch (IOException e) {
                log.error("Error occurred while trying to shutdown socket: " + e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            try {
                log.info("EventServer starting event listener on port " + tcpEventServerConfig.getPort());
                isRunning = true;
                receiverSocket = new ServerSocket(tcpEventServerConfig.getPort());
                while (isRunning) {
                    final Socket connectionSocket = receiverSocket.accept();
                    if(connectionCallback != null){
                        connectionCallback.onConnect();
                    }
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
                    BufferedInputStream in = new BufferedInputStream(connectionSocket.getInputStream());
                    while (true) {

                        byte[] streamNameByteSize = loadData(in, new byte[4]);
                        ByteBuffer sizeBuf = ByteBuffer.wrap(streamNameByteSize);
                        int streamNameSize = sizeBuf.getInt();
                        byte[] streamNameData = loadData(in, new byte[streamNameSize]);
                        String streamId = new String(streamNameData, 0, streamNameData.length);
                        StreamRuntimeInfo streamRuntimeInfo = streamRuntimeInfoMap.get(streamId);

                        Object[] eventData = new Object[streamRuntimeInfo.getNoOfAttributes()];
                        byte[] fixedMessageData = loadData(in, new byte[8 + streamRuntimeInfo.getFixedMessageSize()]);

                        ByteBuffer bbuf = ByteBuffer.wrap(fixedMessageData, 0, fixedMessageData.length);
                        long timestamp = bbuf.getLong();

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
                                byte[] stringData = loadData(in, new byte[stringValueSizes.get(stringSizePosition)]);
                                stringSizePosition++;
                                eventData[i] = new String(stringData, 0, stringData.length);
                            }
                        }
                        streamCallback.receive(streamId, timestamp, eventData);
                    }
                } catch (EOFException e) {
                    log.info("Closing listener socket. " + e.getMessage());
                } catch (IOException e) {
                    log.error("Error reading data from receiver socket:" + e.getMessage(), e);
                } catch (Throwable t) {
                    log.error("Error :" + t.getMessage(), t);
                } finally {
                    if(connectionCallback != null){
                        connectionCallback.onClose();
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
