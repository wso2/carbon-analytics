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

package org.wso2.carbon.streaming.integrator.core.ha.tcp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.streaming.integrator.core.event.queue.EventListMapManager;
import org.wso2.carbon.streaming.integrator.core.ha.transport.handlers.MessageDecoder;
import org.wso2.carbon.streaming.integrator.core.ha.util.HAConstants;
import org.wso2.carbon.streaming.integrator.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.streaming.integrator.core.internal.beans.EventSyncServerConfig;
import org.wso2.carbon.streaming.integrator.core.util.BinaryMessageConverterUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Event Sync Server.
 */
public class EventSyncServer {

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private String hostAndPort;
    private EventSyncServerConfig serverConfig;
    private static final Logger log = LoggerFactory.getLogger(EventSyncServer.class);
    private EventListMapManager eventListMapManager = new EventListMapManager();
    private BlockingQueue<ByteBuffer> eventByteBufferQueue;
    private ExecutorService eventBufferExtractorExecutorService;
    private EventBufferExtractor eventBufferExtractor = new EventBufferExtractor();

    public void start(DeploymentConfig deploymentConfig) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("EventSyncServer-%d").build();
        eventBufferExtractorExecutorService = Executors.newFixedThreadPool(
                HAConstants.EVENT_BUFFER_EXTRACTOR_THREAD_POOL_SIZE, namedThreadFactory);
        eventByteBufferQueue = new LinkedBlockingQueue<>(deploymentConfig.
                getEventByteBufferQueueCapacity());
        serverConfig = deploymentConfig.eventSyncServerConfigs();
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
                        p.addLast(new MessageDecoder(eventByteBufferQueue));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            // Bind and start to accept incoming connections.
            channelFuture = bootstrap.bind(serverConfig.getHost(), serverConfig.getPort()).sync();
            for (int i = 0; i < HAConstants.EVENT_BUFFER_EXTRACTOR_THREAD_POOL_SIZE; i++) {
                eventBufferExtractorExecutorService.submit(eventBufferExtractor);
            }
            log.info("EventSyncServer started in " + hostAndPort + "");
        } catch (InterruptedException e) {
            log.error("Error when booting up EventSyncServer on '" + hostAndPort + "' " + e.getMessage(), e);
        }
    }

    public void shutdownGracefully() {
        channelFuture.channel().close();
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error when shutdowning the EventSyncServer " + e.getMessage(), e);
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        log.info("EventSyncServer running on '" + hostAndPort + "' stopped.");
        workerGroup = null;
        bossGroup = null;

    }

    public void clearResources() {
        eventBufferExtractor.run = false;
        eventBufferExtractorExecutorService.shutdownNow();
        eventByteBufferQueue.clear();
    }

    /**
     * This {@link Runnable} class is executed by the {@link ScheduledExecutorService}
     */
    private class EventBufferExtractor implements Runnable {
        volatile boolean run = true;
        @Override
        public void run() {
            try {
                while (run) {
                    ByteBuffer in = eventByteBufferQueue.take();
                    try {
                        int channelIdSize = in.getInt();
                        String channelId = BinaryMessageConverterUtil.getString(in, channelIdSize);
                        int dataLength = in.getInt();
                        byte[] bytes = new byte[dataLength];
                        in.get(bytes);
                        if (channelId.equals(HAConstants.CHANNEL_ID_CONTROL_MESSAGE)) {
                            eventListMapManager.parseControlMessage(bytes);
                        } else if (channelId.equals(HAConstants.CHANNEL_ID_MESSAGE)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Received a event message");
                            }
                            eventListMapManager.parseMessage(bytes);
                        }
                    } catch (UnsupportedEncodingException e) {
                        log.warn("Error when converting bytes " + e.getMessage(), e);
                    } catch (Throwable t) {
                        log.error("Error occurred while processing eventByteBufferQueue " + t.getMessage(), t);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!run) {
                    log.error("EventSyncServer EventBufferExtractor Job is interrupted");
                }

            }
        }
    }

    public BlockingQueue<ByteBuffer> getEventByteBufferQueue() {
        return eventByteBufferQueue;
    }
}
