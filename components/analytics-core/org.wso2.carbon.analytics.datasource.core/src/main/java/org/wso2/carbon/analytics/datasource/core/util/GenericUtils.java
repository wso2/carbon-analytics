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

import org.apache.axiom.om.util.Base64;
import org.apache.commons.collections.IteratorUtils;
import org.w3c.dom.Document;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceConstants;
import org.wso2.carbon.ndatasource.common.DataSourceConstants.DataSourceStatusModes;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.ndatasource.core.DataSourceRepository;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ndatasource.core.DataSourceStatus;
import org.wso2.carbon.ndatasource.core.SystemDataSourcesConfiguration;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.bind.JAXBContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generic utility methods for analytics data source implementations.
 */
public class GenericUtils {

    private static final String CUSTOM_WSO2_CONF_DIR_NAME = "conf";

    private static final String DATA_SOURCES_FIELD = "dataSources";

    private static final String CREATE_DATA_SOURCE_OBJECT_METHOD = "createDataSourceObject";

    private static final String ADD_DATA_SOURCE_PROVIDERS_METHOD = "addDataSourceProviders";

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

    private static final String DEFAULT_CHARSET = "UTF8";

    public static final String WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP = "wso2_custom_conf_dir";
    
    private static final String ANALYTICS_USER_TABLE_PREFIX = "ANX";

    private static DataSourceRepository globalCustomRepo;

//    private static ThreadLocal<Kryo> kryoTL = new ThreadLocal<Kryo>() {
//        protected Kryo initialValue() {
//            return new Kryo();
//        };
//    };

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
     *
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
            buffer.putInt(name.getBytes(DEFAULT_CHARSET).length);
            buffer.put(name.getBytes(DEFAULT_CHARSET));
            if (value instanceof String) {
                buffer.put(DATA_TYPE_STRING);
                strVal = (String) value;
                buffer.putInt(strVal.getBytes(DEFAULT_CHARSET).length);
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
                buffer.put(DATA_TYPE_OBJECT);
                binData = GenericUtils.serializeObject(value);
                buffer.putInt(binData.length);
                buffer.put(binData);
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
        try {
            /* column name */
            count += name.getBytes(DEFAULT_CHARSET).length;
            if (value instanceof String) {
                /* string length + value */
                count += Integer.SIZE / 8;
                count += ((String) value).getBytes(DEFAULT_CHARSET).length;
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
            } else if (value instanceof Object) {
                count += Integer.SIZE / 8;
                count += GenericUtils.serializeObject(value).length;
            } else if (value != null) {
                throw new AnalyticsException("Invalid column value type in calculating column "
                                             + "values length: " + value.getClass());
            }
        } catch (UnsupportedEncodingException e) {
            throw new AnalyticsException("Default CharSet : " + DEFAULT_CHARSET + " is not supported");
        }
        return count;
    }

    public static Map<String, Object> decodeRecordValues(byte[] data, Set<String> columns) throws AnalyticsException {
        /* using LinkedHashMap to retain the column order */
        Map<String, Object> result = new LinkedHashMap<String, Object>();
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
                    case DATA_TYPE_OBJECT:
                        size = buffer.getInt();
                        binData = new byte[size];
                        buffer.get(binData);
                        value = GenericUtils.deserializeObject(binData);
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

    @SuppressWarnings("unchecked")
    public static List<Record> listRecords(AnalyticsRecordStore rs,
                                           RecordGroup[] rgs) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (RecordGroup rg : rgs) {
            result.addAll(IteratorUtils.toList(rs.readRecords(rg)));
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

    public static String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }
    
    public static String calculateTableIdentity(int tenantId, String tableName) {
        return tenantId + "_" + tableName;
    }

    public static String calculateRecordIdentity(Record record) {
        return calculateTableIdentity(record.getTenantId(), record.getTableName());
    }

    public static Collection<List<Record>> generateRecordBatches(List<Record> records) {
        return generateRecordBatches(records, false);
    }
    
    public static Collection<List<Record>> generateRecordBatches(List<Record> records, boolean normalizeTableName) {
        /* if the records have identities (unique table category and name) as the following
         * "ABABABCCAACBDABCABCDBAC", the job of this method is to make it like the following,
         * {"AAAAAAAA", "BBBBBBB", "CCCCCC", "DD" } */
        Map<String, List<Record>> recordBatches = new HashMap<String, List<Record>>();
        List<Record> recordBatch;
        for (Record record : records) {
            if (normalizeTableName) {
                record.setTableName(normalizeTableName(record.getTableName()));
            }
            recordBatch = recordBatches.get(calculateRecordIdentity(record));
            if (recordBatch == null) {
                recordBatch = new ArrayList<Record>();
                recordBatches.put(calculateRecordIdentity(record), recordBatch);
            }
            recordBatch.add(record);
        }
        return recordBatches.values();
    }

    public static String generateRecordID() {
        StringBuilder builder = new StringBuilder();
        builder.append(System.currentTimeMillis());
        builder.append(Math.random() * Math.random());
        return builder.toString();
    }

    /* do not touch if you do not know what you're doing, critical for serialize/deserialize
     * implementation to be stable to retain backward compatibility */
    public static byte[] serializeObject(Object obj) {
//        Kryo kryo = kryoTL.get();
//        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//        Output out = new Output(byteOut);
//        try {
//            kryo.writeClassAndObject(out, obj);
//            out.flush();
//            byte[] data = byteOut.toByteArray();
//            ByteBuffer result = ByteBuffer.allocate(data.length + Integer.SIZE / 8);
//            result.putInt(data.length);
//            result.put(data);
//            return result.array();
//        } finally {
//            out.close();
//        }
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(obj);
            objOut.close();
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
//        /* skip the object size integer */
//        Input input = new Input(Arrays.copyOfRange(source, Integer.SIZE / 8, source.length));
//        try {
//            Kryo kryo = kryoTL.get();
//            return kryo.readClassAndObject(input);
//        } finally {
//            input.close();
//        }
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(source);
            ObjectInputStream objIn = new ObjectInputStream(byteIn);
            return objIn.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* do not touch, @see serializeObject(Object) */
    public static Object deserializeObject(InputStream in) throws IOException, EOFException {
        if (in == null) {
            return null;
        }
//        if (in.available() == 0) {
//            throw new EOFException();
//        }
//        DataInputStream dataIn = new DataInputStream(in);
//        int size = dataIn.readInt();
//        byte[] buff = new byte[size];
//        dataIn.readFully(buff);
//        Input input = new Input(buff);
//        Kryo kryo = kryoTL.get();
//        try {
//            return kryo.readClassAndObject(input);
//        } finally {
//            input.close();
//        }
        try {
            ObjectInputStream objIn = new ObjectInputStream(in);
            return objIn.readObject();
        } catch (EOFException ex){
           throw ex;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addDataSourceProviders(List<String> providers) throws DataSourceException {
        DataSourceManager dsm = DataSourceManager.getInstance();
        try {
            Method method = DataSourceManager.class.getDeclaredMethod(ADD_DATA_SOURCE_PROVIDERS_METHOD, List.class);
            method.setAccessible(true);
            method.invoke(dsm, providers);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            throw new DataSourceException("Error in adding data source providers: " + e.getMessage(), e);
        }
    }

    private static Object createDataSourceObject(DataSourceRepository dsRepo,
            DataSourceMetaInfo dsmInfo) throws DataSourceException {
        try {
            Method method = DataSourceRepository.class.getDeclaredMethod(CREATE_DATA_SOURCE_OBJECT_METHOD, DataSourceMetaInfo.class, boolean.class);
            method.setAccessible(true);
            return method.invoke(dsRepo, dsmInfo, false);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            throw new DataSourceException("Error in creating data source object: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addDataSource(DataSourceRepository dsRepo, CarbonDataSource cds) throws DataSourceException {
        Field field;
        try {
            field = DataSourceRepository.class.getDeclaredField(DATA_SOURCES_FIELD);
            field.setAccessible(true);
            Map<String, CarbonDataSource> dataSources = (Map<String, CarbonDataSource>) field.get(dsRepo);
            dataSources.put(cds.getDSMInfo().getName(), cds);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new DataSourceException("Error in accessing data source map: " + e.getMessage(), e);
        }
    }

    private static void populateSystemDataSource(DataSourceRepository dsRepo, File sysDSFile) throws DataSourceException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(SystemDataSourcesConfiguration.class);
            Document doc = DataSourceUtils.convertToDocument(sysDSFile);
            SystemDataSourcesConfiguration sysDS = (SystemDataSourcesConfiguration) ctx.createUnmarshaller().
                    unmarshal(doc);
            addDataSourceProviders(sysDS.getProviders());
            CarbonDataSource cds;
            for (DataSourceMetaInfo dsmInfo : sysDS.getDataSources()) {
                dsmInfo.setSystem(true);
                cds = new CarbonDataSource(dsmInfo, new DataSourceStatus(DataSourceStatusModes.ACTIVE, null),
                        createDataSourceObject(dsRepo, dsmInfo));
                addDataSource(dsRepo, cds);
            }
        } catch (Exception e) {
            throw new DataSourceException("Error in initializing system data sources at '" +
                    sysDSFile.getAbsolutePath() + "' - " + e.getMessage(), e);
        }
    }

    public static String getAnalyticsConfDirectory() throws AnalyticsException {
        File confDir = null;
        try {
            confDir = new File(CarbonUtils.getCarbonConfigDirPath());
        } catch (Throwable ignore) {
            /* some kind of an exception can be thrown if we are in a non-Carbon env */
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

    private static DataSourceRepository createGlobalCustomDataSourceRepo() throws DataSourceException {
        String confDir;
        try {
            confDir = getCustomAnalyticsConfDirectory();
        } catch (AnalyticsException e) {
            throw new DataSourceException("Error creating global custom data source repo: " + e.getMessage(), e);
        }
        String dataSourcesDir = confDir + File.separator + DataSourceConstants.DATASOURCES_DIRECTORY_NAME;
        File dataSourcesFolder = new File(dataSourcesDir);
        if (!dataSourcesFolder.isDirectory()) {
            throw new IllegalStateException("Invalid directory: " + dataSourcesFolder.getAbsolutePath());
        }
        DataSourceRepository repo = new DataSourceRepository(MultitenantConstants.SUPER_TENANT_ID);
        File masterDSFile = new File(dataSourcesDir + File.separator +
                DataSourceConstants.MASTER_DS_FILE_NAME);
        /* initialize the master data sources first */
        if (masterDSFile.exists()) {
            populateSystemDataSource(repo, masterDSFile);
        }
        /* then rest of the system data sources */
        for (File sysDSFile : dataSourcesFolder.listFiles()) {
            if (sysDSFile.getName().endsWith(DataSourceConstants.SYS_DS_FILE_NAME_SUFFIX)
                    && !sysDSFile.getName().equals(DataSourceConstants.MASTER_DS_FILE_NAME)) {
                populateSystemDataSource(repo, sysDSFile);
            }
        }
        return repo;
    }

    public static Object loadGlobalDataSource(String dsName) throws DataSourceException {
        DataSourceService service = ServiceHolder.getDataSourceService();
        if (service != null) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                CarbonDataSource ds = service.getDataSource(dsName);
                if (ds == null) {
                    return null;
                }
                return ds.getDSObject();
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            if (globalCustomRepo == null) {
                synchronized (GenericUtils.class) {
                    if (globalCustomRepo == null) {
                        globalCustomRepo = createGlobalCustomDataSourceRepo();
                    }
                }
            }
            CarbonDataSource cds = globalCustomRepo.getDataSource(dsName);
            if (cds == null) {
                return null;
            }
            return cds.getDSObject();
        }
    }

    public static void clearGlobalCustomDataSourceRepo() {
        globalCustomRepo = null;
    }

    public static boolean isCarbonServer() {
        try {
            return CarbonUtils.getCarbonHome() != null;
        } catch (Throwable e) {
            return false;
        }
    }

    public static String streamToTableName(String streamName) {
        return streamName.replace('.', '_');
    }

    public static Iterator<Record> recordGroupsToIterator(AnalyticsRecordStore reader,
                                                      RecordGroup[] rgs) throws AnalyticsException {
        return new RecordGroupIterator(reader, rgs);
    }

    /**
     * This class exposes an array of RecordGroup objects as an Iterator.
     */
    public static class RecordGroupIterator implements Iterator<Record> {

        private AnalyticsRecordStore reader;

        private RecordGroup[] rgs;

        private Iterator<Record> itr;

        private int index = -1;

        public RecordGroupIterator(AnalyticsRecordStore reader, RecordGroup[] rgs)
                throws AnalyticsException {
            this.reader = reader;
            this.rgs = rgs;
        }

        @Override
        public boolean hasNext() {
            boolean result;
            if (this.itr == null) {
                result = false;
            } else {
                result = this.itr.hasNext();
            }
            if (result) {
                return true;
            } else {
                if (rgs.length > this.index + 1) {
                    try {
                        this.index++;
                        this.itr = this.reader.readRecords(rgs[index]);
                    } catch (AnalyticsException e) {
                        throw new IllegalStateException("Error in traversing record group: " + e.getMessage(), e);
                    }
                    return this.hasNext();
                } else {
                    return false;
                }
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return this.itr.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            /* ignored */
        }
    }
    
    /**
     * This method is used to generate an UUID from the target table name, to make sure, it is a compact
     * name that can be fitted in all the supported RDBMSs. For example, Oracle has a table name
     * length of 30. So we must translate source table names to hashed strings, which here will have
     * a very low probability of clashing.
     */
    public static String generateTableUUID(int tenantId, String tableName) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(byteOut);
            dout.writeInt(tenantId);
            /* we've to limit it to 64 bits */
            dout.writeInt(tableName.hashCode());
            dout.close();
            byteOut.close();
            String result = Base64.encode(byteOut.toByteArray());
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

}
