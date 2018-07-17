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
import io.netty.handler.codec.MessageToByteEncoder;
import org.wso2.carbon.stream.processor.core.ha.transport.EventComposite;

/**
 * message to bite converter.
 */
public class MessageEncoder extends MessageToByteEncoder<EventComposite> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, EventComposite eventComposite,
                          ByteBuf byteBuf) throws Exception {

        String sessionId = eventComposite.getSessionId();
        String streamId = eventComposite.getChannelId();
        int dataLength = eventComposite.getMessage().length;

        int messageSize = 4 + sessionId.length() + 4 + streamId.length() + 4 + dataLength;

        byteBuf.writeByte((byte) 2);  //1
        byteBuf.writeInt(messageSize); //4
        byteBuf.writeInt(sessionId.length()); //4
        byteBuf.writeBytes(sessionId.getBytes("UTF-8"));
        byteBuf.writeInt(streamId.length()); //4
        byteBuf.writeBytes(streamId.getBytes("UTF-8"));
        byteBuf.writeInt(dataLength); //4
        byteBuf.writeBytes(eventComposite.getMessage());
    }

}
