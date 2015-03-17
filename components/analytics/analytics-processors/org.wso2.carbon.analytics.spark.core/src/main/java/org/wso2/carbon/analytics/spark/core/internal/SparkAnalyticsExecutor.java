/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.deploy.master.Master;
import org.apache.spark.deploy.worker.Worker;
import org.apache.spark.sql.api.java.JavaSQLContext;
import org.apache.spark.sql.api.java.JavaSchemaRDD;
import org.apache.spark.sql.api.java.Row;
import org.apache.spark.sql.api.java.StructField;
import org.wso2.carbon.analytics.dataservice.AnalyticsDSUtils;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.spark.core.internal.SparkDataListener;
import scala.Option;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsRelation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class represents the analytics query execution context.
 */
public class SparkAnalyticsExecutor {

    private static final String CARBON_ANALYTICS_SPARK_APP_NAME = "CarbonAnalytics";

    private static final Log log = LogFactory.getLog(SparkAnalyticsExecutor.class);
    
    private static JavaSparkContext sparkCtx;

    private static JavaSQLContext sqlCtx;

    public static void init() {

        initSparkDataListener();

        SparkConf sparkConf = new SparkConf();

        //master
        startMaster("localhost",7077,8081, sparkConf);

        //workers
        Worker.startSystemAndActor("localhost", 4501, 8090, 2, 1000000,
                                   new String[]{"spark://localhost:7077"}, null, new Option<Object>() {
                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public Object get() {
                        return new Integer(1);
                    }

                    @Override
                    public Object productElement(int n) {
                        return null;
                    }

                    @Override
                    public int productArity() {
                        return 0;
                    }

                    @Override
                    public boolean canEqual(Object that) {
                        return false;
                    }

                    @Override
                    public boolean equals(Object that) {
                        return false;
                    }
                });

//        Worker.startSystemAndActor("localhost", 4502, 8091, 2, 1000000,
//                                   new String[]{"spark://localhost:7077"}, null, new Option<Object>() {
//                    @Override
//                    public boolean isEmpty() {
//                        return false;
//                    }
//
//                    @Override
//                    public Object get() {
//                        return new Integer(2);
//                    }
//
//                    @Override
//                    public Object productElement(int n) {
//                        return null;
//                    }
//
//                    @Override
//                    public int productArity() {
//                        return 0;
//                    }
//
//                    @Override
//                    public boolean canEqual(Object that) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean equals(Object that) {
//                        return false;
//                    }
//                });

        sparkConf.setMaster("spark://localhost:7077").setAppName(CARBON_ANALYTICS_SPARK_APP_NAME);
        sparkCtx = new JavaSparkContext(sparkConf);
        sqlCtx = new JavaSQLContext(sparkCtx);
    }

    public static void initUsingLocal() {
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local").setAppName(CARBON_ANALYTICS_SPARK_APP_NAME);
        sparkCtx = new JavaSparkContext(sparkConf);
        sqlCtx = new JavaSQLContext(sparkCtx);
    }

    private static void startMaster(String host, int port, int webUIport, SparkConf sConf){
        Master.startSystemAndActor(host, port, webUIport, sConf);
    }

    private static void initSparkDataListener() {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        SparkDataListener listener = new SparkDataListener();
        executor.execute(listener);
    }

    public static void stop() {
        sqlCtx.sqlContext().sparkContext().stop();
        sparkCtx.close();
    }
    
    private static void processDefineTable(int tenantId, String query, 
            String[] tokens) throws AnalyticsExecutionException {
        String tableName = tokens[2].trim();
        String alias = tableName;
        if (tokens[tokens.length - 2].equalsIgnoreCase(AnalyticsConstants.TERM_AS)) {
            alias = tokens[tokens.length - 1];
            query = query.substring(0, query.lastIndexOf(tokens[tokens.length - 2]));
        }
        String schemaString = query.substring(query.indexOf(tableName) + tableName.length()).trim();
        try {
            registerTable(tenantId, tableName, alias, schemaString);
        } catch (AnalyticsException e) {
            throw new AnalyticsExecutionException("Error in registering analytics table: " + e.getMessage(), e);
        }
    }
    
    public static int getNumPartitionsHint() {
        return 4;
    }
    
    private static void processInsertInto(int tenantId, String query, 
            String[] tokens) throws AnalyticsExecutionException {
        String tableName = tokens[2].trim();
        String selectQuery = query.substring(query.indexOf(tableName) + tableName.length()).trim();
        try {
            insertIntoTable(tenantId, tableName, toResult(sqlCtx.sql(selectQuery)));
        } catch (AnalyticsException e) {
            throw new AnalyticsExecutionException("Error in executing insert into query: " + e.getMessage(), e);
        }
    }
    
    public static AnalyticsQueryResult executeQuery(int tenantId, String query) throws AnalyticsExecutionException {
        query = query.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        String[] tokens = query.split(" ");
        if (tokens.length >= 3) {
            if (tokens[0].trim().equalsIgnoreCase(AnalyticsConstants.TERM_DEFINE) &&
                    tokens[1].trim().equalsIgnoreCase(AnalyticsConstants.TERM_TABLE)) {
                processDefineTable(tenantId, query, tokens);
                return null;
            } else if (tokens[0].trim().equalsIgnoreCase(AnalyticsConstants.TERM_INSERT) &&
                    tokens[1].trim().equalsIgnoreCase(AnalyticsConstants.TERM_INTO)) {
                processInsertInto(tenantId, query, tokens);
                return null;
            }
        }
        return toResult(sqlCtx.sql(query));
    }
    
    private static void insertIntoTable(int tenantId, String tableName, 
            AnalyticsQueryResult data) throws AnalyticsTableNotAvailableException, AnalyticsException {
        AnalyticsDataService ads = ServiceHolder.getAnalyticsDataService();
        List<Record> records = generateInsertRecordsForTable(tenantId, tableName, data);
        ads.put(records);
    }
    
    private static Integer[] generateTableKeyIndices(String[] keys, StructField[] columns) {
        List<Integer> result = new ArrayList<Integer>();
        for (String key : keys) {
            for (int i = 0; i < columns.length; i++) {
                if (key.equals(columns[i].getName())) {
                    result.add(i);
                    break;
                }
            }
        }
        return result.toArray(new Integer[result.size()]);
    }
    
    private static String generateInsertRecordId(List<Object> row, Integer[] keyIndices) {
        StringBuilder builder = new StringBuilder();
        Object obj;
        for (int index : keyIndices) {
            obj = row.get(index);
            if (obj != null) {
                builder.append(obj.toString());
            }
        }
        /* to make sure, we don't have an empty string */
        builder.append("X");
        try {
            byte[] data = builder.toString().getBytes(AnalyticsConstants.DEFAULT_CHARSET);
            return UUID.nameUUIDFromBytes(data).toString();
        } catch (UnsupportedEncodingException e) {
            /* this wouldn't happen */
            throw new RuntimeException(e);
        }
    }
    
    private static List<Record> generateInsertRecordsForTable(int tenantId, String tableName, 
            AnalyticsQueryResult data) throws AnalyticsException {
        String[] keys = loadTableKeys(tenantId, tableName);
        boolean primaryKeysExists = keys.length > 0;
        List<List<Object>> rows = data.getRows();
        StructField[] columns = data.getColumns();
        Integer[] keyIndices = generateTableKeyIndices(keys, columns);
        List<Record> result = new ArrayList<Record>(rows.size());
        Record record;
        for (List<Object> row : rows) {
            if (primaryKeysExists) {
                record = new Record(generateInsertRecordId(row, keyIndices), tenantId, tableName, 
                        extractValuesFromRow(row, columns), System.currentTimeMillis());
            } else {
                record = new Record(tenantId, tableName, extractValuesFromRow(row, columns), 
                        System.currentTimeMillis());
            }
            result.add(record);
        }
        return result;
    }
    
    private static Map<String, Object> extractValuesFromRow(List<Object> row, StructField[] columns) {
        Map<String, Object> result = new HashMap<String, Object>(row.size());
        for (int i = 0; i < row.size(); i++) {
            result.put(columns[i].getName(), row.get(i));
        }
        return result;
    }
    
    private static AnalyticsQueryResult toResult(JavaSchemaRDD schemaRDD) throws AnalyticsExecutionException {
        return new AnalyticsQueryResult(schemaRDD.schema().getFields(), convertRowsToObjects(schemaRDD.collect()));
    }
    
    private static List<List<Object>> convertRowsToObjects(List<Row> rows) {
        List<List<Object>> result = new ArrayList<List<Object>>();
        List<Object> objects;
        for (Row row : rows) {
            objects = new ArrayList<Object>();
            for (int i = 0; i < row.length(); i++) {
                objects.add(row.get(i));
            }
            result.add(objects);
        }
        return result;
    }
    
    private static void throwInvalidDefineTableQueryException() throws AnalyticsException {
        throw new AnalyticsException("Invalid define table query, must be in the format of "
                + "'define table <table> (name1 type1, name2 type2, name3 type3,... primary key(name1, name2..))'");
    }
    
    private static String generateTableKeysId(int tenantId, String tableName) {
        return tenantId + "_" + tableName;
    }
    
    private static byte[] tableKeysToBinary(String[] keys) throws AnalyticsException {
        ByteArrayOutputStream byteOut = null;
        ObjectOutputStream objOut = null;
        try {
            byteOut = new ByteArrayOutputStream();
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(keys);
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new AnalyticsException("Error in converting table keys to binary: " + e.getMessage(), e);
        } finally {
            try {
                objOut.close();
            } catch (IOException e) {
                log.error(e);
            }
            try {
                byteOut.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
    
    private static String[] binaryToTableKeys(byte[] data) throws AnalyticsException {
        ByteArrayInputStream byteIn = null;
        ObjectInputStream objIn = null;
        try {
            byteIn = new ByteArrayInputStream(data);
            objIn = new ObjectInputStream(byteIn);
            return (String[]) objIn.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new AnalyticsException("Error in converting binary data to table info: " + e.getMessage(), e);
        } finally {
            try {
                objIn.close();
            } catch (IOException e) {
                log.error(e);
            }
            try {
                byteIn.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
    
    private static String[] loadTableKeys(int tenantId, String tableName) throws AnalyticsException {
        AnalyticsDataService ads = ServiceHolder.getAnalyticsDataService();
        List<String> ids = new ArrayList<String>(1);
        ids.add(generateTableKeysId(tenantId, tableName));
        List<Record> records = AnalyticsDSUtils.listRecords(ads, ads.get(
                AnalyticsConstants.TABLE_INFO_TENANT_ID,
                AnalyticsConstants.TABLE_INFO_TABLE_NAME, 1, null, ids));
        if (records.size() == 0) {
            throw new AnalyticsException("Table keys cannot be found for tenant: " + tenantId + " table: " + tableName);
        }
        Record record = records.get(0);
        byte[] data = (byte[]) record.getValue(AnalyticsConstants.OBJECT);
        if (data == null) {
            throw new AnalyticsException("Corrupted table keys for tenant: " + tenantId + " table: " + tableName);
        }
        return binaryToTableKeys(data);
    }
    
    private static void registerTableKeys(int tenantId, String tableName, 
            String[] keys) throws AnalyticsException {
        AnalyticsDataService ads = ServiceHolder.getAnalyticsDataService();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(AnalyticsConstants.OBJECT, tableKeysToBinary(keys));
        Record record = new Record(generateTableKeysId(tenantId, tableName), 
                AnalyticsConstants.TABLE_INFO_TENANT_ID, AnalyticsConstants.TABLE_INFO_TABLE_NAME,
                values, System.currentTimeMillis());
        List<Record> records = new ArrayList<Record>(1);
        records.add(record);
        try {
            ads.put(records);
        } catch (AnalyticsTableNotAvailableException e) {
            ads.createTable(AnalyticsConstants.TABLE_INFO_TENANT_ID, AnalyticsConstants.TABLE_INFO_TABLE_NAME);
            ads.put(records);
        }
    }
    
    private static String processPrimaryKeyAndReturnSchema(int tenantId, String tableName, 
            String schemaString) throws AnalyticsException {
        int index = schemaString.toLowerCase().lastIndexOf(AnalyticsConstants.TERM_PRIMARY);
        String lastSection = "";
        if (index != -1) {
            index = schemaString.lastIndexOf(',', index);
            lastSection = schemaString.substring(index + 1).trim();
        }
        String[] lastTokens = lastSection.split(" ");
        if (lastTokens.length >= 2 && lastTokens[1].trim().toLowerCase().startsWith(AnalyticsConstants.TERM_KEY)) {
            String keysSection = lastSection.substring(lastSection.toLowerCase().indexOf(
                    AnalyticsConstants.TERM_KEY) + 3).trim();
            if (!(keysSection.startsWith("(") && keysSection.endsWith(")"))) {
                throwInvalidDefineTableQueryException();
            }
            keysSection = keysSection.substring(1, keysSection.length() - 1).trim();
            String keys[] = keysSection.split(",");
            for (int i = 0; i < keys.length; i++) {
                keys[i] = keys[i].trim();
            }
            registerTableKeys(tenantId, tableName, keys);
            return schemaString.substring(0, index).trim();
        } else {
            registerTableKeys(tenantId, tableName, new String[0]);
            return schemaString;
        }
    }
    
    private static void registerTable(int tenantId, String tableName, String alias,
            String schemaString) throws AnalyticsException {
        if (!(schemaString.startsWith("(") && schemaString.endsWith(")"))) {
            throwInvalidDefineTableQueryException();
        }
        schemaString = schemaString.substring(1, schemaString.length() - 1).trim();
        schemaString = processPrimaryKeyAndReturnSchema(tenantId, tableName, schemaString);
        AnalyticsDataService ads = ServiceHolder.getAnalyticsDataService();
        if (!ads.tableExists(tenantId, tableName)) {
            ads.createTable(tenantId, tableName);
        }
        AnalyticsRelation table = new AnalyticsRelation(tenantId, tableName, sqlCtx, schemaString);
        JavaSchemaRDD schemaRDD = sqlCtx.baseRelationToSchemaRDD(table);
        schemaRDD.registerTempTable(alias);
    }
}
