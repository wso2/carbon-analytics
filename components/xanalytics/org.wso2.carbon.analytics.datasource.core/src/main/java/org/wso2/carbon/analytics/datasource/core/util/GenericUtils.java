/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.datasource.core.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;

/**
 * Generic utility methods for analytics data source implementations.
 */
public class GenericUtils {
    
    private static final byte BOOLEAN_TRUE = 1;

    private static final byte BOOLEAN_FALSE = 0;

    private static final byte DATA_TYPE_NULL = 0x00;
    
    private static final byte DATA_TYPE_STRING = 0x01;
    
    private static final byte DATA_TYPE_INTEGER = 0x02;
    
    private static final byte DATA_TYPE_LONG = 0x03;
    
    private static final byte DATA_TYPE_FLOAT = 0x4;
    
    private static final byte DATA_TYPE_DOUBLE = 0x5;
    
    private static final byte DATA_TYPE_BOOLEAN = 0x6;
    
    private static final String DEFAULT_CHARSET = "UTF8";

    public static String getParentPath(String path) {
        if (path.equals("/")) {
            return null;
        }
        String parent = path.substring(0, path.lastIndexOf('/'));
        if (parent.length() == 0) {
            parent = "/";
        }
        return parent;
    }
    
    public static byte[] encodeColumnObject(Object obj) throws AnalyticsDataSourceException {
        ByteBuffer byteBuffer;
        if (obj instanceof String) {
            try {
                byte[] strData = ((String) obj).getBytes(DEFAULT_CHARSET);
                byteBuffer = ByteBuffer.allocate(strData.length + 1);
                byteBuffer.put(DATA_TYPE_STRING);
                byteBuffer.put(strData);
            } catch (UnsupportedEncodingException e) {
                throw new AnalyticsDataSourceException("Error in decoding String: " + e.getMessage(), e);
            }
        } else if (obj instanceof Integer) {
            byteBuffer = ByteBuffer.allocate(Integer.SIZE / 8 + 1);
            byteBuffer.put(DATA_TYPE_INTEGER);
            byteBuffer.putInt((Integer) obj);
        } else if (obj instanceof Long) {
            byteBuffer = ByteBuffer.allocate(Long.SIZE / 8 + 1);
            byteBuffer.put(DATA_TYPE_LONG);
            byteBuffer.putLong((Long) obj);
        } else if (obj instanceof Double) {
            byteBuffer = ByteBuffer.allocate(Double.SIZE / 8 + 1);
            byteBuffer.put(DATA_TYPE_DOUBLE);
            byteBuffer.putDouble((Double) obj);
        } else if (obj instanceof Float) {
            byteBuffer = ByteBuffer.allocate(Float.SIZE / 8 + 1);
            byteBuffer.put(DATA_TYPE_FLOAT);
            byteBuffer.putFloat((Float) obj);
        } else if (obj instanceof Boolean) {
            byteBuffer = ByteBuffer.allocate(2);
            byteBuffer.put(DATA_TYPE_BOOLEAN);
            boolean boolValue = (Boolean) obj;
            if (boolValue) {
                byteBuffer.put((byte) BOOLEAN_TRUE);
            } else {
                byteBuffer.put((byte) BOOLEAN_FALSE);
            }
        } else if (obj == null) {
            byteBuffer = ByteBuffer.allocate(1);
            byteBuffer.put(DATA_TYPE_NULL);
        } else {
            throw new AnalyticsDataSourceException("Unsupported object type to be encoded: " + obj.getClass());
        }
        return byteBuffer.array();
    }
    
    public static Object decodeColumnObject(byte[] data) throws AnalyticsDataSourceException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byte type = byteBuffer.get();
        switch (type) {
        case DATA_TYPE_STRING:
            byte[] strData = new byte[data.length - 1];
            byteBuffer.get(strData);
            try {
                return new String(strData, DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException e) {
                throw new AnalyticsDataSourceException("Error in decoding string data to object: " + e.getMessage(), e);
            }
        case DATA_TYPE_INTEGER:
            return byteBuffer.getInt();
        case DATA_TYPE_LONG:
            return byteBuffer.getLong();
        case DATA_TYPE_DOUBLE:
            return byteBuffer.getDouble();
        case DATA_TYPE_FLOAT:
            return byteBuffer.getFloat();
        case DATA_TYPE_BOOLEAN:
            byte boolValue = byteBuffer.get();
            if (boolValue == BOOLEAN_TRUE) {
                return true;
            } else if (boolValue == BOOLEAN_FALSE) {
                return false;
            } else {
                throw new AnalyticsDataSourceException("Invalid boolean data while decoding: " + boolValue);
            }
        case DATA_TYPE_NULL:
            return null;
        default:
            throw new AnalyticsDataSourceException("Invalid object data type while decoding: " + type);
        }
    }
    
    public static void main(String[] args) throws Exception {
        Object obj = 34.53;
        int c = 100000;
        byte[] data = encodeColumnObject(obj);
        long start = System.currentTimeMillis();
        for (int i = 0; i < c; i++) {
            decodeColumnObject(data);
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + " ms.");
    }
    
}
