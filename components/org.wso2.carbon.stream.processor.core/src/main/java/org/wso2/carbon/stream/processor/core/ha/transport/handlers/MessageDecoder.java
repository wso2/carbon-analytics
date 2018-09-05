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

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Byte to message decoder.
 */
public class MessageDecoder extends ByteToMessageDecoder {
    private BlockingQueue<ByteBuffer> byteBufferQueue;
    private static long startTime;
    private static long endTime;
    private static int count = 0;
    private static final int TPS_EVENT_BATCH_THRESHOLD = 10000;
    private static final Logger log = Logger.getLogger(MessageDecoder.class);

    public MessageDecoder(BlockingQueue<ByteBuffer> byteBufferQueue) {
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
        byte[] bytes = new byte[messageSize - HAConstants.PROTOCOL_AND_MESSAGE_BYTE_LENGTH];
        in.readBytes(bytes);
        in.markReaderIndex();
        in.resetReaderIndex();
        try {
            byteBufferQueue.put(ByteBuffer.wrap(bytes));
        } catch (InterruptedException e) {
            log.error("Error while waiting for the insertion of ByteBufferQueue " + e.getMessage(), e);
        }
        if (log.isDebugEnabled()) {
            synchronized (this) {
                if (startTime == 0L) {
                    startTime = new Date().getTime();
                }
                count++;
                if (count % TPS_EVENT_BATCH_THRESHOLD == 0) {
                    endTime = new Date().getTime();
                    log.info("Server Event Batch TPS: " +
                            (((TPS_EVENT_BATCH_THRESHOLD * 1000) / (endTime - startTime))));
                }
            }
        }
        in.markReaderIndex();
    }
}
