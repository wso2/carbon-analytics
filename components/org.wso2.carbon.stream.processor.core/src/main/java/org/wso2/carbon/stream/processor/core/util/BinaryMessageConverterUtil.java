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

package org.wso2.carbon.stream.processor.core.util;

import io.netty.buffer.ByteBuf;
import org.wso2.carbon.stream.processor.core.event.queue.EventDataMetaInfo;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Util helping to convert from Siddhi Event to byte message.
 */
public final class BinaryMessageConverterUtil {

    public static int getSize(Object data) {
        if (data instanceof String) {
            return 4 + ((String) data).length();
        } else if (data instanceof Integer) {
            return 4;
        } else if (data instanceof Long) {
            return 8;
        } else if (data instanceof Float) {
            return 4;
        } else if (data instanceof Double) {
            return 8;
        } else if (data instanceof Boolean) {
            return 1;
        } else {
            return 4;
        }
    }

    public static EventDataMetaInfo getEventMetaInfo(Object data) {
        int eventSize;
        Attribute.Type attributeType;
        if (data instanceof String) {
            attributeType = Attribute.Type.STRING;
            eventSize = 4 + ((String) data).length();
        } else if (data instanceof Integer) {
            attributeType = Attribute.Type.INT;
            eventSize = 4;
        } else if (data instanceof Long) {
            attributeType = Attribute.Type.LONG;
            eventSize = 8;
        } else if (data instanceof Float) {
            attributeType = Attribute.Type.FLOAT;
            eventSize = 4;
        } else if (data instanceof Double) {
            attributeType = Attribute.Type.DOUBLE;
            eventSize = 8;
        } else if (data instanceof Boolean) {
            attributeType = Attribute.Type.BOOL;
            eventSize = 1;
        } else {
            attributeType = Attribute.Type.OBJECT;
            eventSize = 1;
        }
        return new EventDataMetaInfo(eventSize, attributeType);
    }

    public static void assignData(Object data, ByteBuffer eventDataBuffer) throws IOException {
        if (data instanceof String) {
            eventDataBuffer.putInt(((String) data).length());
            eventDataBuffer.put((((String) data).getBytes(Charset.defaultCharset())));
        } else if (data instanceof Integer) {
            eventDataBuffer.putInt((Integer) data);
        } else if (data instanceof Long) {
            eventDataBuffer.putLong((Long) data);
        } else if (data instanceof Float) {
            eventDataBuffer.putFloat((Float) data);
        } else if (data instanceof Double) {
            eventDataBuffer.putDouble((Double) data);
        } else if (data instanceof Boolean) {
            eventDataBuffer.put((byte) (((Boolean) data) ? 1 : 0));
        } else {
            eventDataBuffer.putInt(0);
        }
    }

    public static String getString(ByteBuf byteBuf, int size) throws UnsupportedEncodingException {
        byte[] bytes = new byte[size];
        byteBuf.readBytes(bytes);
        return new String(bytes, Charset.defaultCharset());
    }

    public static String getString(ByteBuffer byteBuf, int size) throws UnsupportedEncodingException {
        byte[] bytes = new byte[size];
        byteBuf.get(bytes);
        return new String(bytes, Charset.defaultCharset());
    }
}
