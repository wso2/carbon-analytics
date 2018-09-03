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
import io.netty.buffer.ByteBuf;
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
import org.wso2.carbon.stream.processor.core.ha.transport.handlers.MessageDecoder;
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.internal.beans.TCPServerConfig;
import org.wso2.carbon.stream.processor.core.util.BinaryMessageConverterUtil;

import java.io.UnsupportedEncodingException;
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
    private BlockingQueue<ByteBuf> eventByteBufferQueue;
    private ExecutorService eventBufferExtractorExecutorService = Executors.newFixedThreadPool(
            HAConstants.EVENT_BUFFER_EXTRACTOR_THREAD_POOL_SIZE);
    private EventBufferExtractor eventBufferExtractor = new EventBufferExtractor();
    private Future eventBufferExtractorFuture;

    public void start(TCPServerConfig serverConf, BlockingQueue<ByteBuf> eventByteBufferQueue) {
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
                        p.addLast(new MessageDecoder(eventByteBufferQueue));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            // Bind and start to accept incoming connections.
            channelFuture = bootstrap.bind(serverConfig.getHost(), serverConfig.getPort()).sync();
            eventBufferExtractorFuture = eventBufferExtractorExecutorService.submit(eventBufferExtractor);
            log.info("Tcp Server started in " + hostAndPort + "");
        } catch (InterruptedException e) {
            log.error("Error when booting up tcp server on '" + hostAndPort + "' " + e.getMessage(), e);
        }
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

    public void clearResources() {
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
                    ByteBuf in = eventByteBufferQueue.take();
                    int channelIdSize = in.readInt();
                    String channelId = BinaryMessageConverterUtil.getString(in, channelIdSize);
                    int dataLength = in.readInt();
                    byte[] bytes = new byte[dataLength];
                    in.readBytes(bytes);
                    if (channelId.equals(HAConstants.CHANNEL_ID_CONTROL_MESSAGE)) {
                        eventListMapManager.parseControlMessage(bytes);
                    } else if (channelId.equals(HAConstants.CHANNEL_ID_MESSAGE)) {
                        eventListMapManager.parseMessage(bytes);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                log.error("Error when converting bytes " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("EventBufferExtractor Job is interrupted");
            }
        }
    }
}
