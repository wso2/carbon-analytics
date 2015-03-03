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

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.core.rs.RecordGroup;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;

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
    
    private static final byte DATA_TYPE_FLOAT = 0x04;
    
    private static final byte DATA_TYPE_DOUBLE = 0x05;
    
    private static final byte DATA_TYPE_BOOLEAN = 0x06;
    
    private static final byte DATA_TYPE_BINARY = 0x07;
    
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
    
    /**
     * Normalizes the path to make every path not end with "/".
     * @param path The path
     * @return The normalized path string
     */
    public static String normalizePath(String path) {
        if (path == null || path.equals("/")) {
            return path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    
    private static int calculateRecordValuesBufferSize(Map<String, Object> values) throws AnalyticsException {
        int count = 0;
        String name;
        Object value;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            name = entry.getKey();
            value = entry.getValue();
            count += calculateBufferSizePerElement(name, value);
        }
        return count;
    }
    
    public static byte[] encodeRecordValues(Map<String, Object> values) throws AnalyticsException {
        ByteBuffer secondaryBuffer = ByteBuffer.allocate(calculateRecordValuesBufferSize(values));
        String name;
        Object value;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            name = entry.getKey();
            value = entry.getValue();
            secondaryBuffer.put(encodeElement(name, value));
        }
        return secondaryBuffer.array();
    }

    public static byte[] encodeElement(String name, Object value) throws AnalyticsException {
        ByteBuffer buffer = ByteBuffer.allocate(calculateBufferSizePerElement(name, value));
        String strVal;
        boolean boolVal;
        byte[] binData;
        try {
            buffer.putInt(name.length());
            buffer.put(name.getBytes(DEFAULT_CHARSET));
            if (value instanceof String) {
                buffer.put(DATA_TYPE_STRING);
                strVal = (String) value;
                buffer.putInt(strVal.length());
                buffer.put(strVal.getBytes(DEFAULT_CHARSET));
            } else if (value instanceof Long) {
                buffer.put(DATA_TYPE_LONG);
                buffer.putLong((Long) value);
            } else if (value instanceof Double) {
                buffer.put(DATA_TYPE_DOUBLE);
                buffer.putDouble((Double) value);
            } else if (value instanceof Boolean) {
                buffer.put(DATA_TYPE_BOOLEAN);
                boolVal = (Boolean) value;
                if (boolVal) {
                    buffer.put(BOOLEAN_TRUE);
                } else {
                    buffer.put(BOOLEAN_FALSE);
                }
            } else if (value instanceof Integer) {
                buffer.put(DATA_TYPE_INTEGER);
                buffer.putInt((Integer) value);
            } else if (value instanceof Float) {
                buffer.put(DATA_TYPE_FLOAT);
                buffer.putFloat((Float) value);
            } else if (value instanceof byte[]) {
                buffer.put(DATA_TYPE_BINARY);
                binData = (byte[]) value;
                buffer.putInt(binData.length);
                buffer.put(binData);
            } else if (value == null) {
                buffer.put(DATA_TYPE_NULL);
            } else {
                throw new AnalyticsException("Invalid column value type in encoding "
                        + "column value: " + value.getClass());
            }
        } catch (UnsupportedEncodingException e) {
            throw new AnalyticsException("Error in encoding record values: " + e.getMessage());
        }
        return buffer.array();
    }

    private static int calculateBufferSizePerElement(String name, Object value) throws AnalyticsException {
        int count = 0;
         /* column name length value + data type (including null) */
        count += Integer.SIZE / 8 + 1;
            /* column name */
        count += name.getBytes().length;
        if (value instanceof String) {
                /* string length + value */
            count += Integer.SIZE / 8;
            count += ((String) value).getBytes().length;
        } else if (value instanceof Long) {
            count += Long.SIZE / 8;
        } else if (value instanceof Double) {
            count += Double.SIZE / 8;
        } else if (value instanceof Boolean) {
            count += Byte.SIZE / 8;
        } else if (value instanceof Integer) {
            count += Integer.SIZE / 8;
        } else if (value instanceof Float) {
            count += Float.SIZE / 8;
        } else if (value instanceof byte[]) {
            count += Integer.SIZE / 8;
            count += ((byte[]) value).length;
        } else if (value != null) {
            throw new AnalyticsException("Invalid column value type in calculating column "
                    + "values length: " + value.getClass());
        }
        return count;
    }
    
    public static Map<String, Object> decodeRecordValues(byte[] data, 
            Set<String> columns) throws AnalyticsException {
        /* using LinkedHashMap to retain the column order */
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int type, size;
        String colName;
        Object value;
        byte[] buff;
        byte boolVal;
        byte[] binData;
        while (buffer.remaining() > 0) {
            try {
                size = buffer.getInt();
                buff = new byte[size];
                buffer.get(buff, 0, size);
                colName = new String(buff, DEFAULT_CHARSET);
                type = buffer.get();
                switch (type) {
                case DATA_TYPE_STRING:
                    size = buffer.getInt();
                    buff = new byte[size];
                    buffer.get(buff, 0, size);
                    value = new String(buff, DEFAULT_CHARSET);
                    break;
                case DATA_TYPE_LONG:
                    value = buffer.getLong();
                    break;
                case DATA_TYPE_DOUBLE:
                    value = buffer.getDouble();
                    break;
                case DATA_TYPE_BOOLEAN:
                    boolVal = buffer.get();
                    if (boolVal == BOOLEAN_TRUE) {
                        value = true;
                    } else if (boolVal == BOOLEAN_FALSE) {
                        value = false;
                    } else {
                        throw new AnalyticsException("Invalid encoded boolean value: " + boolVal);
                    }
                    break;
                case DATA_TYPE_INTEGER:
                    value = buffer.getInt();
                    break;
                case DATA_TYPE_FLOAT:
                    value = buffer.getFloat();
                    break;
                case DATA_TYPE_BINARY:
                    size = buffer.getInt();
                    binData = new byte[size];
                    buffer.get(binData);
                    value = binData;
                    break;
                case DATA_TYPE_NULL:
                    value = null;
                    break;
                default:
                    throw new AnalyticsException("Unknown encoded data source type : " + type);
                }
                if (columns == null || columns.contains(colName)) {
                    result.put(colName, value);
                }
            } catch (Exception e) {
                throw new AnalyticsException("Error in decoding record values: " + e.getMessage());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Record> listRecords(AnalyticsRecordStore rs, 
            RecordGroup[] rgs) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (RecordGroup rg : rgs) {
            result.addAll(IteratorUtils.toList(rs.readRecords(rg)));
        }
        return result;
    }    
    
}
