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

package org.wso2.carbon.stream.processor.core.ha.transport.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.event.queue.EventQueueManager;
import org.wso2.carbon.stream.processor.core.ha.tcp.SiddhiEventConverter;
import org.wso2.carbon.stream.processor.core.util.BinaryMessageConverterUtil;
import org.wso2.siddhi.core.event.Event;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Byte to message decoder.
 */
public class MessageDecoder extends ByteToMessageDecoder {
    static final Logger LOG = Logger.getLogger(MessageDecoder.class);
    private EventQueueManager eventQueueManager;

    public MessageDecoder(EventQueueManager eventQueueManager){
        this.eventQueueManager = eventQueueManager;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 5) {
            return;
        }
        try {
            int protocol = in.readByte();
            int messageSize = in.readInt();
            if (protocol != 2 || messageSize > in.readableBytes()) {
                in.resetReaderIndex();
                return;
            }

            int sessionIdSize = in.readInt();
            BinaryMessageConverterUtil.getString(in, sessionIdSize);

            int channelIdSize = in.readInt();
            String channelId = BinaryMessageConverterUtil.getString(in, channelIdSize);

            int dataLength = in.readInt();
            byte[] bytes = new byte[dataLength];
            in.readBytes(bytes);
            LOG.info("Message Received : " + new String(bytes));
            if (channelId.equals("eventMessage")) {
                Event[] events = SiddhiEventConverter.toConvertAndEnqueue(ByteBuffer.wrap(bytes),eventQueueManager);


                LOG.info("Event Received : " + events.toString());
            } else if (channelId.equals("controlMessage")) {
                LOG.info("Control Message Received : " + new String(bytes));
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            in.markReaderIndex();
        }

    }

}
