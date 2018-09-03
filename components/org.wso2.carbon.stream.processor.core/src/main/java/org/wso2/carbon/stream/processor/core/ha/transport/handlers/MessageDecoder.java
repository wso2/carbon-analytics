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
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.util.BinaryMessageConverterUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Byte to message decoder.
 */
public class MessageDecoder extends ByteToMessageDecoder {
    private BlockingQueue<ByteBuf> byteBufferQueue;
    private static long startTime;
    private static long endTime;
    static int count = 0;
    static final Logger log = Logger.getLogger(MessageDecoder.class);
    public MessageDecoder(BlockingQueue<ByteBuf> byteBufferQueue){
        this.byteBufferQueue = byteBufferQueue;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 5) {
            return;
        }
        int protocol = in.readByte();
        int messageSize = in.readInt();
        if (protocol != 2 || messageSize > in.readableBytes()) {
            in.resetReaderIndex();
            return;
        }
        byteBufferQueue.offer(in);
//        try {
//
//            int channelIdSize = in.readInt();
//            String channelId = BinaryMessageConverterUtil.getString(in, channelIdSize);
//
//            int dataLength = in.readInt();
//            byte[] bytes = new byte[dataLength];
//            in.readBytes(bytes);
//            //LOG.info("Message Received : " + new String(bytes));
//
//            if (channelId.equals(HAConstants.CHANNEL_ID_MESSAGE)) {
//                ByteBuffer messageBuffer = ByteBuffer.wrap(bytes);
//                count++;
//                if (count % 10000 == 0) {
//                    log.info("COUNT " + count);
//                    endTime = new Date().getTime();
//                    //LOG.info("TIME START    " + time + "    TIMES   " + timeE);
//                    log.info("Server TPS:   " + (((10000 * 1000) / (endTime - startTime))));
//                    startTime = new Date().getTime();
//                }
//
//            } else if (channelId.equals(HAConstants.CHANNEL_ID_CONTROL_MESSAGE)) {
//
//                log.info("Control Message Received : " + new String(bytes));
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } finally {
//
//        }
        in.markReaderIndex();

    }
}
