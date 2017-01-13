/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.analytics.data.commons.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.analytics.data.commons.AnalyticsDataService;
import org.wso2.analytics.data.commons.AnalyticsRecordStore;
import org.wso2.analytics.data.commons.service.AnalyticsDataResponse;
import org.wso2.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.analytics.data.commons.sources.AnalyticsCommonConstants;
import org.wso2.analytics.data.commons.sources.Record;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.sources.RecordGroup;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;

public class AnalyticsCommonUtils {

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
    private static final byte DATA_TYPE_OBJECT = 0x10;
    private static final String ANALYTICS_USER_TABLE_PREFIX = "ANX";

    private static final String CUSTOM_WSO2_CONF_DIR_NAME = "conf";
    public static final String WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP = "wso2_custom_conf_dir";

    private static final Log LOG = LogFactory.getLog(AnalyticsCommonUtils.class);

    private static ThreadLocal<Kryo> kryoTL = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            return new Kryo();
        }
    };

    public static String generateRecordID() {
        byte[] data = new byte[16];
        secureRandom.get().nextBytes(data);
        ByteBuffer buff = ByteBuffer.wrap(data);
        return new UUID(buff.getLong(), buff.getLong()).toString();
    }

    private static ThreadLocal<SecureRandom> secureRandom = new ThreadLocal<SecureRandom>() {
        protected SecureRandom initialValue() {
            return new SecureRandom();
        }
    };

    /* do not touch, @see serializeObject(Object) */
    public static void serializeObject(Object obj, OutputStream out) throws IOException {
        byte[] data = serializeObject(obj);
        out.write(data, 0, data.length);
    }

    /* do not touch, @see serializeObject(Object) */
    public static Object deserializeObject(byte[] source) {
        if (source == null) {
            return null;
        }
        /* skip the object size integer */
        try (Input input = new Input(Arrays.copyOfRange(source, Integer.SIZE / 8, source.length))) {
            Kryo kryo = kryoTL.get();
            return kryo.readClassAndObject(input);
        }
    }

    /* do not touch if you do not know what you're doing, critical for serialize/deserialize
     * implementation to be stable to retain backward compatibility */
    public static byte[] serializeObject(Object obj) {
        Kryo kryo = kryoTL.get();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (Output out = new Output(byteOut)) {
            kryo.writeClassAndObject(out, obj);
            out.flush();
            byte[] data = byteOut.toByteArray();
            ByteBuffer result = ByteBuffer.allocate(data.length + Integer.SIZE / 8);
            result.putInt(data.length);
            result.put(data);
            return result.array();
        }
    }

    public static Collection<List<Record>> generateRecordBatches(List<Record> records) {
        return generateRecordBatches(records, false);
    }

    public static Collection<List<Record>> generateRecordBatches(List<Record> records, boolean normalizeTableName) {
        /* if the records have identities (unique table category and name) as the following
         * "ABABABCCAACBDABCABCDBAC", the job of this method is to make it like the following,
         * {"AAAAAAAA", "BBBBBBB", "CCCCCC", "DD" } */
        Map<String, List<Record>> recordBatches = new HashMap<>();
        List<Record> recordBatch;
        for (Record record : records) {
            if (normalizeTableName) {
                record.setTableName(normalizeTableName(record.getTableName()));
            }
            recordBatch = recordBatches.get(calculateRecordIdentity(record));
            if (recordBatch == null) {
                recordBatch = new ArrayList<>();
                recordBatches.put(calculateRecordIdentity(record), recordBatch);
            }
            recordBatch.add(record);
        }
        return recordBatches.values();
    }

    public static String calculateRecordIdentity(Record record) {
        return normalizeTableName(record.getTableName());
    }

    public static String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }


    /**
     * This method is used to generate an UUID from the target table name to make sure that it is a compact
     * name that can be fitted in all the supported RDBMSs. For example, Oracle has a table name
     * length of 30. So we must translate source table names to hashed strings, which here will have
     * a very low probability of clashing.
     */
    public static String generateTableUUID(String tableName) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(byteOut);
            /* we've to limit it to 64 bits */
            dout.writeInt(tableName.hashCode());
            dout.close();
            byteOut.close();
            String result = Base64.getEncoder().encodeToString(byteOut.toByteArray());
            result = result.replace('=', '_');
            result = result.replace('+', '_');
            result = result.replace('/', '_');
            /* a table name must start with a letter */
            return ANALYTICS_USER_TABLE_PREFIX + result;
        } catch (IOException e) {
            /* this will never happen */
            throw new RuntimeException(e);
        }
    }

    public static byte[] encodeRecordValues(Map<String, Object> values) throws AnalyticsException {
        ByteArrayDataOutput byteOut = ByteStreams.newDataOutput();
        String name;
        Object value;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            name = entry.getKey();
            value = entry.getValue();
            byteOut.write(encodeElement(name, value));
        }
        return byteOut.toByteArray();
    }

    public static byte[] encodeElement(String name, Object value) throws AnalyticsException {
        ByteArrayDataOutput buffer = ByteStreams.newDataOutput();
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(nameBytes.length);
        buffer.write(nameBytes);
        if (value instanceof String) {
            buffer.write(DATA_TYPE_STRING);
            String strVal = (String) value;
            byte[] strBytes = strVal.getBytes(StandardCharsets.UTF_8);
            buffer.writeInt(strBytes.length);
            buffer.write(strBytes);
        } else if (value instanceof Long) {
            buffer.write(DATA_TYPE_LONG);
            buffer.writeLong((Long) value);
        } else if (value instanceof Double) {
            buffer.write(DATA_TYPE_DOUBLE);
            buffer.writeDouble((Double) value);
        } else if (value instanceof Boolean) {
            buffer.write(DATA_TYPE_BOOLEAN);
            boolean boolVal = (Boolean) value;
            if (boolVal) {
                buffer.write(BOOLEAN_TRUE);
            } else {
                buffer.write(BOOLEAN_FALSE);
            }
        } else if (value instanceof Integer) {
            buffer.write(DATA_TYPE_INTEGER);
            buffer.writeInt((Integer) value);
        } else if (value instanceof Float) {
            buffer.write(DATA_TYPE_FLOAT);
            buffer.writeFloat((Float) value);
        } else if (value instanceof byte[]) {
            buffer.write(DATA_TYPE_BINARY);
            byte[] binData = (byte[]) value;
            buffer.writeInt(binData.length);
            buffer.write(binData);
        } else if (value == null) {
            buffer.write(DATA_TYPE_NULL);
        } else {
            buffer.write(DATA_TYPE_OBJECT);
            byte[] binData = serializeObject(value);
            buffer.writeInt(binData.length);
            buffer.write(binData);
        }
        return buffer.toByteArray();
    }

    public static Map<String, Object> decodeRecordValues(byte[] data, Set<String> columns) throws AnalyticsException {
        /* using LinkedHashMap to retain the column order */
        Map<String, Object> result = new LinkedHashMap<>();
        int type, size;
        String colName;
        Object value;
        byte[] buff;
        byte boolVal;
        byte[] binData;
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.remaining() > 0) {
                size = buffer.getInt();
                if (size == 0) {
                    break;
                }
                buff = new byte[size];
                buffer.get(buff, 0, size);
                colName = new String(buff, StandardCharsets.UTF_8);
                type = buffer.get();
                switch (type) {
                    case DATA_TYPE_STRING:
                        size = buffer.getInt();
                        buff = new byte[size];
                        buffer.get(buff, 0, size);
                        value = new String(buff, StandardCharsets.UTF_8);
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
                    case DATA_TYPE_OBJECT:
                        size = buffer.getInt();
                        binData = new byte[size];
                        buffer.get(binData);
                        value = deserializeObject(binData);
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
            }
        } catch (Exception e) {
            throw new AnalyticsException("Error in decoding record values: " + e.getMessage(), e);
        }
        return result;
    }

    public static List<Integer[]> splitNumberRange(int count, int nsplit) {
        List<Integer[]> result = new ArrayList<>(nsplit);
        int range = Math.max(1, count / nsplit);
        int current = 0;
        for (int i = 0; i < nsplit; i++) {
            if (current >= count) {
                break;
            }
            if (i + 1 >= nsplit) {
                result.add(new Integer[]{current, count - current});
            } else {
                result.add(new Integer[]{current, current + range > count ? count - current : range});
                current += range;
            }
        }
        return result;
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignore) {
            /* ignore */
        }
    }

    public static List<Record> listRecords(AnalyticsRecordStore rs, RecordGroup[] rgs) throws AnalyticsException {
        List<Record> result = new ArrayList<>();
        for (RecordGroup rg : rgs) {
            result.addAll(IteratorUtils.toList(rs.readRecords(rg)));
        }
        return result;
    }

    public static List<Record> listRecords(AnalyticsDataService ads,
                                           AnalyticsDataResponse response) throws AnalyticsException {
        List<Record> result = new ArrayList<>();
        for (AnalyticsDataResponse.Entry entry : response.getEntries()) {
            result.addAll(IteratorUtils.toList(ads.readRecords(entry.getRecordStoreName(), entry.getRecordGroup())));
        }
        return result;
    }

    public static String getAnalyticsConfDirectory() throws AnalyticsException {
        File confDir = null;
        try {
            confDir = new File(getConfDirectoryPath());
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error in getting the config path: " + e.getMessage(), e);
            }
        }
        if (confDir == null || !confDir.exists()) {
            return getCustomAnalyticsConfDirectory();
        } else {
            return confDir.getAbsolutePath();
        }
    }

    private static String getCustomAnalyticsConfDirectory() throws AnalyticsException {
        String path = System.getProperty(WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP);
        if (path == null) {
            path = Paths.get("").toAbsolutePath().toString() + File.separator + CUSTOM_WSO2_CONF_DIR_NAME;
        }
        File confDir = new File(path);
        if (!confDir.exists()) {
            throw new AnalyticsException("The custom WSO2 configuration directory does not exist at '" + path + "'. "
                    + "This can be given by correctly pointing to a valid configuration directory by setting the "
                    + "Java system property '" + WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP + "'.");
        }
        return confDir.getAbsolutePath();
    }

    public static String getConfDirectoryPath() {
        String carbonConfigDirPath = System.getProperty("carbon.config.dir.path");
        if (carbonConfigDirPath == null) {
            carbonConfigDirPath = System.getenv("CARBON_CONFIG_DIR_PATH");
            if (carbonConfigDirPath == null) {
                return getBaseDirectoryPath() + File.separator + "conf";
            }
        }
        return carbonConfigDirPath;
    }

    public static String getBaseDirectoryPath() {
        String baseDir = System.getProperty("analytics.home");
        if (baseDir == null) {
            baseDir = System.getenv("ANALYTICS_HOME");
            System.setProperty("analytics.home", baseDir);
        }
        return baseDir;
    }

    public static String convertStreamNameToTableName(String stream) {
        return stream.replaceAll("\\.", "_");
    }

    public static File getFileFromSystemResources(String fileName) throws URISyntaxException {
        File file = null;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader != null) {
            URL url = classLoader.getResource(fileName);
            if (url == null) {
                url = classLoader.getResource(File.separator + fileName);
            }
            file = new File(url.toURI());
        }
        return file;
    }

    /**
     * This method preprocesses the records before adding to the record store,
     * e.g. update the record ids if its not already set by using the table
     * schema's primary keys.
     *
     * @param recordBatches batch of records
     */
    public static void preProcessRecords(Collection<List<Record>> recordBatches, AnalyticsDataService analyticsDataService) throws AnalyticsException {
        for (List<Record> recordBatch : recordBatches) {
            preProcessRecordBatch(recordBatch, analyticsDataService);
        }
    }

    private static void preProcessRecordBatch(List<Record> recordBatch, AnalyticsDataService service) throws AnalyticsException {
        Record firstRecord = recordBatch.get(0);
        AnalyticsSchema schema = service.getTableSchema(firstRecord.getTableName());
        List<String> primaryKeys = schema.getPrimaryKeys();
        if (primaryKeys != null && primaryKeys.size() > 0) {
            populateRecordsWithPrimaryKeyAwareIds(recordBatch, primaryKeys);
        } else {
            populateWithGenerateIds(recordBatch);
        }
    }

    private static void populateWithGenerateIds(List<Record> records) {
        records.stream().filter(record -> record.getId() == null).forEach(
                record -> record.setId(AnalyticsCommonUtils.generateRecordID()));
    }

    private static void populateRecordWithPrimaryKeyAwareId(Record record, List<String> primaryKeys) {
        record.setId(generateRecordIdFromPrimaryKeyValues(record.getValues(), primaryKeys));
    }

    private static String generateRecordIdFromPrimaryKeyValues(Map<String, Object> values, List<String> primaryKeys) {
        StringBuilder builder = new StringBuilder();
        Object obj;
        for (String key : primaryKeys) {
            obj = values.get(key);
            if (obj != null) {
                builder.append(obj.toString());
            }
        }
        // to make sure, we don't have an empty string
        builder.append("");
        try {
            byte[] data = builder.toString().getBytes(AnalyticsCommonConstants.DEFAULT_CHARSET);
            return UUID.nameUUIDFromBytes(data).toString();
        } catch (UnsupportedEncodingException e) {
            // This wouldn't happen
            throw new RuntimeException(e);
        }
    }

    private static void populateRecordsWithPrimaryKeyAwareIds(List<Record> records, List<String> primaryKeys) {
        /* users have the ability to explicitly provide a record id,
         * in-spite of having primary keys defined to auto generate the id */
        records.stream().filter(record -> record.getId() == null).forEach(record -> populateRecordWithPrimaryKeyAwareId(record, primaryKeys));
    }
}
