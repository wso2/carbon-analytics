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
import org.wso2.carbon.stream.processor.core.event.queue.EventListMapManager;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.ha.transport.handlers.MessageDecoder;
import org.wso2.carbon.stream.processor.core.internal.beans.TCPServerConfig;
import org.wso2.carbon.stream.processor.core.util.BinaryMessageConverterUtil;
import org.wso2.siddhi.core.event.Event;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

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
    private EventListMapManager eventListMapManager = new EventListMapManager();
    private BlockingQueue<ByteBuffer> eventByteBufferQueue;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);//todo constants
    private EventBufferExtractor eventBufferExtractor = new EventBufferExtractor();
    private Future eventBufferExtractorFuture;

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
                        p.addLast(new MessageDecoder(eventListMapManager, eventByteBufferQueue));
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
        eventBufferExtractorFuture = executorService.submit(eventBufferExtractor);
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

    public void clearResources() {//todo check whether need to handle interrupted exception
        eventBufferExtractorFuture.cancel(true);
        eventByteBufferQueue.clear();
    }

    /**
     * This {@link Runnable} class is executed by the {@link ScheduledExecutorService}
     */
    private class EventBufferExtractor implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    ByteBuffer eventContent = eventByteBufferQueue.take();
                    log.info("took from queue '" + eventByteBufferQueue.size());
                    int noOfEvents = eventContent.getInt();
                    QueuedEvent queuedEvent;
                    Event[] events = new Event[noOfEvents];
                    for (int i = 0; i < noOfEvents; i++) {
                        String sourceHandlerElementId;
                        String siddhiAppName;
                        long sequenceID = eventContent.getLong();
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
                        eventListMapManager.addToEventListMap(sequenceID, queuedEvent);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                //todo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Job is interrupted", e);
            }
        }
    }
}
