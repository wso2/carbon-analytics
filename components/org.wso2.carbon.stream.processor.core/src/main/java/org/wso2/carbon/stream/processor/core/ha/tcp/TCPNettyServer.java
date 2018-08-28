/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.stream.processor.core.ha.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.event.queue.EventTreeMapManager;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.ha.transport.handlers.MessageDecoder;
import org.wso2.carbon.stream.processor.core.internal.beans.TCPServerConfig;
import org.wso2.carbon.stream.processor.core.util.BinaryMessageConverterUtil;
import org.wso2.siddhi.core.event.Event;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * TCP Netty Server.
 */
public class TCPNettyServer {

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private String hostAndPort;
    private TCPServerConfig serverConfig;
    private static final Logger log = Logger.getLogger(TCPNettyServer.class);
    private EventTreeMapManager eventTreeMapManager = new EventTreeMapManager();
    private BlockingQueue<ByteBuffer> eventByteBufferQueue;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private EventBufferExtractor eventBufferExtractor = new EventBufferExtractor();
    private ScheduledFuture eventBufferExtractorScheduleFuture;

    public void start(TCPServerConfig serverConf, BlockingQueue<ByteBuffer> eventByteBufferQueue) {
        this.eventByteBufferQueue = eventByteBufferQueue;
        serverConfig = serverConf;
        bossGroup = new NioEventLoopGroup(serverConfig.getBossThreads());
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkerThreads());

        hostAndPort = serverConfig.getHost() + ":" + serverConfig.getPort();

        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {

                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new MessageDecoder(eventTreeMapManager, eventByteBufferQueue));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            // Bind and start to accept incoming connections.
            channelFuture = bootstrap.bind(serverConfig.getHost(), serverConfig.getPort()).sync();
            log.info("Tcp Server started in " + hostAndPort + "");
        } catch (InterruptedException e) {
            log.error("Error when booting up tcp server on '" + hostAndPort + "' " + e.getMessage(), e);
        }

        eventBufferExtractorScheduleFuture = scheduledExecutorService.
                scheduleAtFixedRate(eventBufferExtractor, 0, 5, TimeUnit.MILLISECONDS);
    }

    public void shutdownGracefully() {
        channelFuture.channel().close();
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error when shutdowning the tcp server " + e.getMessage(), e);
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        log.info("Tcp Server running on '" + hostAndPort + "' stopped.");
        workerGroup = null;
        bossGroup = null;

    }

    public void shutdownScheduler() {//todo check whether need to handle interrupted exception
        eventBufferExtractorScheduleFuture.cancel(true);
    }

    /**
     * This {@link Runnable} class is executed by the {@link ScheduledExecutorService}
     */
    private class EventBufferExtractor implements Runnable {

        @Override
        public void run() {
            if (eventByteBufferQueue.peek() != null) {
                ByteBuffer eventContent = eventByteBufferQueue.poll();
                try {
                    int noOfEvents = eventContent.getInt();
                    QueuedEvent queuedEvent;
                    // log.info("No events in the received batch :     " + noOfEvents);
                    Event[] events = new Event[noOfEvents];
                    for (int i = 0; i < noOfEvents; i++) {
                        String sourceHandlerElementId;
                        String siddhiAppName;
                        int sequenceID = eventContent.getInt();
                        int stringSize = eventContent.getInt();
                        if (stringSize == 0) {
                            sourceHandlerElementId = null;
                        } else {
                            sourceHandlerElementId = BinaryMessageConverterUtil.getString(eventContent, stringSize);
                        }

                        int appSize = eventContent.getInt();
                        if (appSize == 0) {
                            siddhiAppName = null;
                        } else {
                            siddhiAppName = BinaryMessageConverterUtil.getString(eventContent, appSize);
                        }

                        String attributes;
                        stringSize = eventContent.getInt();
                        if (stringSize == 0) {
                            attributes = null;
                        } else {
                            attributes = BinaryMessageConverterUtil.getString(eventContent, stringSize);
                        }
                        String[] attributeTypes = attributes.substring(1, attributes.length() - 1).split(", ");
                        events[i] = SiddhiEventConverter.getEvent(eventContent, attributeTypes);
                        queuedEvent = new QueuedEvent(siddhiAppName, sourceHandlerElementId, sequenceID, events[i]);
                        eventTreeMapManager.addToTreeMap(sequenceID, queuedEvent);
//                        log.info("RECEIVED EVENT - " + sourceHandlerElementId + "       ||      " +
//                                events[0].toString() + " " + "   |   COUNT " + count);
                    }
                } catch (UnsupportedEncodingException e) {

                }
            }
        }
    }
}
