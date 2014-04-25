package org.wso2.carbon.databridge.persistence.cassandra.Utils;

import me.prettyprint.cassandra.serializers.BooleanSerializer;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.FloatSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.CassandraConnector;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.DataType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CassandraSDSUtils {
    static Log log = LogFactory.getLog(CassandraSDSUtils.class);

    public static String convertStreamNameToCFName(String streamName) {
        if (streamName == null) {
            return null;
        }
        int keySpaceLength = StreamDefinitionUtils.getKeySpaceName().length();
        if ((streamName.length() + keySpaceLength) > 48) {
            throw new RuntimeException("The stream name you provided is too long. This has caused the" +
                                       " generated key (\"" + streamName + "\") to go " +
                                       "beyond the allowed characters. of " + (48 - keySpaceLength));
        }
        return streamName.replace(":", "_").replace(".", "_");
    }

    public static long getLong(ByteBuffer byteBuffer) throws IOException {
        return longSerializer.fromByteBuffer(byteBuffer);
    }

    public static String getString(ByteBuffer byteBuffer) throws IOException {
        return stringSerializer.fromByteBuffer(byteBuffer);
    }

    private final static StringSerializer stringSerializer = StringSerializer.get();
    private final static IntegerSerializer integerSerializer = IntegerSerializer.get();
    private final static LongSerializer longSerializer = LongSerializer.get();
    private final static BooleanSerializer booleanSerializer = BooleanSerializer.get();
    private final static FloatSerializer floatSerializer = FloatSerializer.get();
    private final static DoubleSerializer doubleSerializer = DoubleSerializer.get();

    public static Object getOriginalValueFromColumnValue(ByteBuffer byteBuffer,
                                                         AttributeType attributeType)
            throws IOException {
        switch (attributeType) {
            case BOOL: {
                return booleanSerializer.fromByteBuffer(byteBuffer);
            }
            case INT: {
                return integerSerializer.fromByteBuffer(byteBuffer);
            }
            case DOUBLE: {
                return doubleSerializer.fromByteBuffer(byteBuffer);
            }
            case FLOAT: {
                return floatSerializer.fromByteBuffer(byteBuffer);
            }
            case LONG: {
                return longSerializer.fromByteBuffer(byteBuffer);
            }
            case STRING: {
                return stringSerializer.fromByteBuffer(byteBuffer);
            }
        }
        return null;
    }


    public static String getColumnName(DataType dataType, Attribute attribute) {
        return dataType.name() + "_" + attribute.getName();
    }

    public static String createRowKey(long timestamp, String ip, int port, int count) {
        return timestamp + "::" + ip + "::" + port + "::" + count;
    }

    public static long getIndexCFRowKey(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();

    }

    public static String getIndexColumnFamilyName(String eventCFName) {
        return CassandraConnector.EVENT_INDEX_CF_PREFIX + eventCFName;
    }

    public static String getSecondaryIndexColumnName(String columnName) {
        return columnName + CassandraConnector.SEC_INDEX_COLUMN_SUFFIX;
    }

    //Todo: name of column family should have less than 48 chars. handle it properly
    public static String getCustomIndexCFName(String primaryCFName, String indexColumnName) {
        String indexCFName = String.valueOf(Math.abs((primaryCFName + (indexColumnName.indexOf("_") > 0 ?
                                                                       indexColumnName.substring(indexColumnName.indexOf("_") + 1) : indexColumnName)).hashCode()));

        log.debug("Index CF Name for " + indexColumnName + "(" + primaryCFName + ") -->" + indexCFName);
        return indexCFName;
    }

    //Todo: name of column family should have less than 48 chars. handle it properly
    public static String getCustomIndexCFNameForInsert(String primaryCFName,
                                                       String indexColumnName) {
        return String.valueOf(Math.abs((primaryCFName + indexColumnName).hashCode()));
    }

    public static Object getParsedArbitraryFieldValue(String val, AttributeType attributeType) {
        try {
            switch (attributeType) {
                case BOOL: {
                    return Boolean.parseBoolean(val);
                }
                case FLOAT: {
                    return Float.parseFloat(val);
                }
                case DOUBLE: {
                    return Double.parseDouble(val);
                }
                case INT: {
                    return Integer.parseInt(val);
                }
                case LONG: {
                    return Long.parseLong(val);
                }
                case STRING: {
                    return val;
                }

            }
        } catch (NumberFormatException e) {
            log.debug("Error while parsing attribute field value from String ", e);
            return null;
        }
        return null;
    }
}
