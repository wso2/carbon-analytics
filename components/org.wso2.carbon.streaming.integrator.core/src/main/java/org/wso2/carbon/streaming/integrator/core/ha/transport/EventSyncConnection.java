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

package org.wso2.carbon.streaming.integrator.core.ha.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.wso2.carbon.streaming.integrator.core.ha.transport.handlers.MessageEncoder;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

import java.util.UUID;

/**
 * Tcp Netty Client.
 */
public class EventSyncConnection {
    private static final Logger log = Logger.getLogger(EventSyncConnection.class);
    private EventLoopGroup group;
    private Bootstrap bootstrap;

    public EventSyncConnection() {
        this(0, true, true);
    }

    public EventSyncConnection(int numberOfThreads, boolean keepAlive, boolean noDelay) {
        group = new NioEventLoopGroup(numberOfThreads);
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, keepAlive)
                .option(ChannelOption.TCP_NODELAY, noDelay)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addFirst(new MessageEncoder());
                    }
                });
    }

    public Connection connect(String host, int port) throws ConnectionUnavailableException {
        // Start the connection attempt.
        String hostAndPort = host + ":" + port;
        try {
            return new Connection(hostAndPort, bootstrap.connect(host, port).sync().channel(),
                    UUID.randomUUID() + "-" + hostAndPort);
        } catch (Throwable e) {
            throw new ConnectionUnavailableException("Error connecting to '" + hostAndPort + "', " + e.getMessage(), e);
        }
    }

    public static class Connection {
        private String hostAndPort;
        private Channel channel;
        private String sessionId;

        public Connection (String hostAndPort, Channel channel, String sessionId) {
            this.hostAndPort = hostAndPort;
            this.channel = channel;
            this.sessionId = sessionId;
        }

        public ChannelFuture send(final String channelId, final byte[] message) throws ConnectionUnavailableException {
            EventComposite eventComposite = new EventComposite(sessionId, channelId, message);
            ChannelFuture future = channel.writeAndFlush(eventComposite);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        log.error("Error sending events to '" + hostAndPort + "' on channel '" +
                                channelId + "', " + future.cause() + ", dropping events ", future.cause());
                    }
                }
            });
            if (future.isDone() && !future.isSuccess()) {
                throw new ConnectionUnavailableException("Error sending events to '" + hostAndPort +
                        "' on channel '" + channelId + "', " + hostAndPort + ", " + future.cause().getMessage(),
                        future.cause());
            }
            return future;
        }

        public boolean isActive() {
            return channel != null && channel.isActive();
        }

        public void shutdown() {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    log.error("Error closing connection to '" + hostAndPort + "' from client '" + sessionId +
                            "', " + e);
                } finally {
                    channel.disconnect();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Disconnecting client to '" + hostAndPort + "' with sessionId:" + sessionId);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Stopping client to '" + hostAndPort + "' with sessionId:" + sessionId);
            }
            hostAndPort = null;
            sessionId = null;
        }
    }

}



