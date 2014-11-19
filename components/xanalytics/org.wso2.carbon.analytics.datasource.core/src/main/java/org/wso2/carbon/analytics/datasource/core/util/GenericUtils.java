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
import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.Record.Column;

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
    
    private static int calculateRecordValuesBufferSize(List<Column> values) throws AnalyticsDataSourceException {
        int count = 0;
        String name;
        Object value;
        for (Column column : values) {
            name = column.getName();
            value = column.getValue();
            /* column name length value + data type (including null) */
            count += Integer.SIZE / 8 + 1;
            /* column name */
            count += name.length();
            if (value instanceof String) {
                /* string length + value */
                count += Integer.SIZE / 8;
                count += ((String) value).length();
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
            } else if (value != null) {
                throw new AnalyticsDataSourceException("Invalid column value type in calculating column "
                        + "values length: " + value.getClass());
            }
        }
        return count;
    }
    
    public static byte[] encodeRecordValues(List<Column> values) throws AnalyticsDataSourceException {
        ByteBuffer buffer = ByteBuffer.allocate(calculateRecordValuesBufferSize(values));
        String name, strVal;
        boolean boolVal;
        Object value;
        for (Column column : values) {
            try {
                name = column.getName();
                value = column.getValue();
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
                } else if (value == null) {
                    buffer.put(DATA_TYPE_NULL);
                } else {
                    throw new AnalyticsDataSourceException("Invalid column value type in encoding "
                            + "column value: " + value.getClass());
                }
            } catch (UnsupportedEncodingException e) {
                throw new AnalyticsDataSourceException("Error in encoding record values: " + e.getMessage());
            }
        }
        return buffer.array();
    }
    
    public static List<Column> decodeRecordValues(byte[] data) throws AnalyticsDataSourceException {
        List<Column> result = new ArrayList<Column>();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int type, size;
        String colName;
        byte[] buff;
        byte boolVal;
        Column column;
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
                    column = new Column(colName, new String(buff, DEFAULT_CHARSET));
                    break;
                case DATA_TYPE_LONG:
                    column = new Column(colName, buffer.getLong());
                    break;
                case DATA_TYPE_DOUBLE:
                    column = new Column(colName, buffer.getDouble());
                    break;
                case DATA_TYPE_BOOLEAN:
                    boolVal = buffer.get();
                    if (boolVal == BOOLEAN_TRUE) {
                        column = new Column(colName, true);
                    } else if (boolVal == BOOLEAN_FALSE) {
                        column = new Column(colName, false);
                    } else {
                        throw new AnalyticsDataSourceException("Invalid encoded boolean value: " + boolVal);
                    }
                    break;
                case DATA_TYPE_INTEGER:
                    column = new Column(colName, buffer.getInt());
                    break;
                case DATA_TYPE_FLOAT:
                    column = new Column(colName, buffer.getFloat());
                    break;
                case DATA_TYPE_NULL:
                    column = new Column(colName, null);
                    break;
                default:
                    throw new AnalyticsDataSourceException("Unknown encoded data source type : " + type);
                }
                result.add(column);
            } catch (Exception e) {
                throw new AnalyticsDataSourceException("Error in decoding record values: " + e.getMessage());
            }
        }
        return result;
    }
    
}
