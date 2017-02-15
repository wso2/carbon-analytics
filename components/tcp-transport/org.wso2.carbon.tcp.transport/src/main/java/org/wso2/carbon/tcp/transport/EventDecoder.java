/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.tcp.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.wso2.carbon.tcp.transport.dto.StreamTypeHolder;
import org.wso2.carbon.tcp.transport.util.SiddhiEventConverter;

import java.util.List;


public class EventDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 5){
            return;
        }
        int protocol = in.readByte();
        int messageSize = in.readInt();
        if(protocol != 2 || messageSize > in.readableBytes()) {
            in.resetReaderIndex();
            return;
        }
        StreamTypeHolder streamTypeHolder = TransportStreamManager.getInstance().getStreamTypeHolder(-1234);
        List<SiddhiEventComposite> testList = SiddhiEventConverter.getConverter().toEventList(in, streamTypeHolder);
        out.add(testList);
    }
}
