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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Util helping to convert from Siddhi Event to byte message.
 */
public final class BinaryMessageConverterUtil {

    private BinaryMessageConverterUtil() {

    }

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
}
