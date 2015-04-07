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
import org.w3c.dom.Document;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsCategoryPath;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordReader;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceConstants;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.ndatasource.core.DataSourceRepository;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ndatasource.core.SystemDataSourcesConfiguration;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;

/**
 * Generic utility methods for analytics data source implementations.
 */
public class GenericUtils {

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

    private static final byte DATA_TYPE_CATEGORY = 0x10;

    private static final String DEFAULT_CHARSET = "UTF8";
    
    private static final String WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP = "wso2-analytics-conf-dir";
    
    private static DataSourceRepository globalCustomRepo;

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
            } else if (value instanceof AnalyticsCategoryPath) {
                buffer.put(DATA_TYPE_CATEGORY);
                AnalyticsCategoryPath analyticsCategoryPath = (AnalyticsCategoryPath) value;
                buffer.putFloat(analyticsCategoryPath.getWeight());
                buffer.putInt(AnalyticsCategoryPath.getCombinedPath(analyticsCategoryPath.getPath())
                                      .getBytes(DEFAULT_CHARSET).length);
                buffer.put(AnalyticsCategoryPath.getCombinedPath(analyticsCategoryPath.getPath())
                                   .getBytes(DEFAULT_CHARSET));
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
            } else if (value instanceof AnalyticsCategoryPath) {
                count += Float.SIZE / 8;
                count += Integer.SIZE / 8;
                AnalyticsCategoryPath analyticsCategoryPath = (AnalyticsCategoryPath) value;
                count += AnalyticsCategoryPath.getCombinedPath(analyticsCategoryPath.getPath())
                        .getBytes(DEFAULT_CHARSET).length;
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
                    case DATA_TYPE_CATEGORY:
                        float weight = buffer.getFloat();
                        size = buffer.getInt();
                        buff = new byte[size];
                        buffer.get(buff, 0, size);
                        value = new String(buff, DEFAULT_CHARSET);
                        value = new AnalyticsCategoryPath(weight, AnalyticsCategoryPath
                                .getPathAsArray((String) value));
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
            throw new AnalyticsException("Error in decoding record values: " + e.getMessage());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<Record> listRecords(AnalyticsRecordReader rs,
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
        return tenantId + "_" + normalizeTableName(tableName);
    }

    public static String calculateRecordIdentity(Record record) {
        return calculateTableIdentity(record.getTenantId(), record.getTableName());
    }

    public static Collection<List<Record>> generateRecordBatches(List<Record> records) {
        /* if the records have identities (unique table category and name) as the following
         * "ABABABCCAACBDABCABCDBAC", the job of this method is to make it like the following,
         * {"AAAAAAAA", "BBBBBBB", "CCCCCC", "DD" } */
        Map<String, List<Record>> recordBatches = new HashMap<String, List<Record>>();
        List<Record> recordBatch;
        for (Record record : records) {
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
        builder.append(Math.random());
        return builder.toString();
    }

    public static byte[] serializeObject(Object obj) throws AnalyticsException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        byte[] result;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            result = baos.toByteArray();
        } catch (IOException e) {
            throw new AnalyticsException("Error serializing object: " + e.getMessage(), e);
        } finally {
            GenericUtils.closeQuietly(oos);
            GenericUtils.closeQuietly(baos);
        }
        return result;
    }

    public static Object deserializeObject(byte[] source) throws AnalyticsException {
        ByteArrayInputStream bais = new ByteArrayInputStream(source);
        ObjectInputStream ois = null;
        Object result;
        try {
            ois = new ObjectInputStream(bais);
            result = ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new AnalyticsException("Error de-serializing object: " + e.getMessage(), e);
        } finally {
            GenericUtils.closeQuietly(ois);
            GenericUtils.closeQuietly(bais);
        }
        return result;
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
    
    private static void populateSystemDataSource(DataSourceRepository dsRepo, File sysDSFile) throws DataSourceException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(SystemDataSourcesConfiguration.class);
            Document doc = DataSourceUtils.convertToDocument(sysDSFile);
            DataSourceUtils.secureResolveDocument(doc, true);
            SystemDataSourcesConfiguration sysDS = (SystemDataSourcesConfiguration) ctx.createUnmarshaller().
                    unmarshal(doc);
            addDataSourceProviders(sysDS.getProviders());
            for (DataSourceMetaInfo dsmInfo : sysDS.getDataSources()) {
                dsmInfo.setSystem(true);
                dsRepo.addDataSource(dsmInfo);
            }
        } catch (Exception e) {
            throw new DataSourceException("Error in initializing system data sources at '" +
                    sysDSFile.getAbsolutePath() + "' - " + e.getMessage(), e);
        }
    }
    
    private static DataSourceRepository createGlobalCustomDataSourceRepo() throws DataSourceException {
        String confDir = System.getProperty(WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP);
        if (confDir == null || confDir.isEmpty()) {
            throw new IllegalStateException("The Java system property '" + WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP + 
                    "' must be set to initialize non-Carbon env analytics");
        }
        String dataSourcesDir = confDir + File.separator + DataSourceConstants.DATASOURCES_DIRECTORY_NAME;
        DataSourceRepository repo = new DataSourceRepository(MultitenantConstants.SUPER_TENANT_ID);
        File masterDSFile = new File(dataSourcesDir + File.separator + 
                DataSourceConstants.MASTER_DS_FILE_NAME);
        /* initialize the master data sources first */
        if (masterDSFile.exists()) {
            populateSystemDataSource(repo, masterDSFile);
        }
        /* then rest of the system data sources */
        File dataSourcesFolder = new File(dataSourcesDir);
        for (File sysDSFile : dataSourcesFolder.listFiles()) {
            if (sysDSFile.getName().endsWith(DataSourceConstants.SYS_DS_FILE_NAME_SUFFIX)
                    && !sysDSFile.getName().equals(DataSourceConstants.MASTER_DS_FILE_NAME)) {
                populateSystemDataSource(repo, sysDSFile);
            }
        }
        return null;
    }

    public static Object loadGlobalDataSource(String dsName) throws DataSourceException {
        DataSourceService service = ServiceHolder.getDataSourceService();
        if (service != null) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                CarbonDataSource ds = service.getDataSource(dsName);
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
            return globalCustomRepo.getDataSource(dsName).getDSObject();
        }
    }
    
}
