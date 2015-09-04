/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.analytics.tools.migration;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;

import org.apache.commons.cli.*;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This class represents the Migration functionality, from Cassandra to new DAL.
 */
public class AnalyticsDataMigrationTool {

    private static final String DEFAULT_CASSANDRA_CQL_PORT = "9042";
    private static final String DEFAULT_CASSANDRA_PORT = "9160";
    private static final String DEFAULT_CASSANDRA_SERVER_URL = "localhost" ;
    private static final String SERVER_URL = "serverUrl";
    private static final String SERVER_PORT = "serverPort";
    private static final String CASSANDRA_PORT = "cassandraPort";
    private static final String COLUMN_FAMILY = "columnFamily";
    private static final String ANALYTIC_TABLE = "analyticTable";
    private static final String TENANT_ID = "tenantId";
    private static final String SERVER_URL_ARG = "server url";
    private static final String SERVER_PORT_ARG = "serverPort";
    private static final String CASSANDRA_PORT_ARG = "cassandraPort";
    private static final String COLUMN_FAMILY_NAME_ARG = "column family name";
    private static final String ANALYTIC_TABLE_NAME_ARG = "analytic table name";
    private static final String TENANT_ID_ARG = "tenant id";
    private static final String CASSANDRA_USERNAME = "username";
    private static final String CASSANDRA_USERNAME_ARG = "username";
    private static final String CASSANDRA_PASSWORD = "password";
    private static final String CASSANDRA_PASSWORD_ARG = "password";
    private static final String RECORD_BATCH_SIZE = "1000";
    private static final String BATCH_SIZE = "batchSize";
    private static final String BATCH_SIZE_ARG = "number of rows per fetch";
    private static final String PAYLOAD_PREFIX = "payload_";
    private static final String OLD_VERSION_FIELD = "Version";
    private static final String NEW_VERSION_FIELD = "_version";
    private static final String CLUSTER_NAME = "cluster1";
    private static final String CASSANDRA_KEYSPACE = "EVENT_KS";

    private static final String UTF8_TYPE = "org.apache.cassandra.db.marshal.UTF8Type";
    private static final String LONG_TYPE = "org.apache.cassandra.db.marshal.LongType";
    private static final String INTEGER_TYPE = "org.apache.cassandra.db.marshal.IntegerType";
    private static final String TIME_UUID_TYPE = "org.apache.cassandra.db.marshal.TimeUUIDType";
    private static final String ASCII_TYPE = "org.apache.cassandra.db.marshal.AsciiType";
    private static final String BYTE_TYPE = "org.apache.cassandra.db.marshal.ByteType";
    private static final String BOOLEAN_TYPE = "org.apache.cassandra.db.marshal.BooleanType";
    private static final String FLOAT_TYPE = "org.apache.cassandra.db.marshal.FloatType";
    private static final String DOUBLE_TYPE = "org.apache.cassandra.db.marshal.DoubleType";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName(SERVER_URL_ARG).hasArg()
                .withDescription("Cassandra server url '<default value: localhost>'")
                .create(SERVER_URL));
        options.addOption(OptionBuilder.withArgName(SERVER_PORT_ARG).hasArg()
                .withDescription("Cassandra server port '<default value: 9042'").create(SERVER_PORT));
        options.addOption(OptionBuilder.withArgName(COLUMN_FAMILY_NAME_ARG).hasArg()
                .withDescription("Name of the columnFamily to be migrated").create(COLUMN_FAMILY));
        options.addOption(OptionBuilder.withArgName(ANALYTIC_TABLE_NAME_ARG).hasArg()
                .withDescription("Destination name of the table which will have the migrated data")
                .create(ANALYTIC_TABLE));
        options.addOption(OptionBuilder.withArgName(BATCH_SIZE_ARG).hasArg()
                .withDescription("specify the batch size of rows of column family '<default value: 1000>'")
                .create(BATCH_SIZE));
        options.addOption(OptionBuilder.withArgName(TENANT_ID_ARG).hasArg()
                .withDescription("specify tenant id of the tenant considered '<default value: super tenant>'")
                .create(TENANT_ID));
        options.addOption(OptionBuilder.withArgName(CASSANDRA_USERNAME_ARG).hasArg()
                .withDescription("specify the cassandra username")
                .create(CASSANDRA_USERNAME));
        options.addOption(OptionBuilder.withArgName(CASSANDRA_PASSWORD_ARG).hasArg()
                .withDescription("specify the cassandra username")
                .create(CASSANDRA_PASSWORD));
        options.addOption(OptionBuilder.withArgName(CASSANDRA_PORT_ARG).hasArg()
                .withDescription("specify the cassandra port")
                .create(CASSANDRA_PORT_ARG));
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);
        if (args.length < 4 ) {
            new HelpFormatter().printHelp("analytics-migrate.sh|cmd", options);
            System.exit(1);
        }

        AnalyticsDataService service = null;
        Cluster cluster = null;
        try {
            service = AnalyticsServiceHolder.getAnalyticsDataService();
            String serverUrl;
            String columnFamily;
            String analyticTable;
            String cassandraUser = null, cassandraPassword = null;
            int tenantId = Integer.parseInt(line.getOptionValue(TENANT_ID, "" + MultitenantConstants.SUPER_TENANT_ID));
            serverUrl = line.getOptionValue(SERVER_URL, DEFAULT_CASSANDRA_SERVER_URL);
            int port = Integer.parseInt(line.getOptionValue(SERVER_PORT, DEFAULT_CASSANDRA_CQL_PORT));
            int cassandraPort = Integer.parseInt(line.getOptionValue(CASSANDRA_PORT, DEFAULT_CASSANDRA_PORT));
            int batchSize = Integer.parseInt(line.getOptionValue(BATCH_SIZE, RECORD_BATCH_SIZE));
            if (line.hasOption(COLUMN_FAMILY)) {
                columnFamily = line.getOptionValue(COLUMN_FAMILY);
            } else {
                throw new Exception("Column Family Name is not provided!");
            }
            if (line.hasOption(ANALYTIC_TABLE)) {
                analyticTable = line.getOptionValue(ANALYTIC_TABLE);
            } else {
                throw new Exception("Analytic Table is not provided!");
            }
            if (line.hasOption(CASSANDRA_USERNAME)) {
                cassandraUser = line.getOptionValue(CASSANDRA_USERNAME);
            }
            if (line.hasOption(CASSANDRA_PASSWORD)) {
                cassandraPassword = line.getOptionValue(CASSANDRA_PASSWORD);
            }
            System.out.println("Intializing [tenant=" + tenantId + "] [serverUrl='" + serverUrl + "'] " +
                               "[port='" + port + "'] [columnFamily='" + columnFamily + "'] " +
                               "[analyticTable='" + analyticTable + "']...");

            //configuring the cassandra cluster and the keyspace
            if (cassandraUser != null && cassandraPassword != null) {
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", cassandraUser);
                credentials.put("password", cassandraPassword);
                cluster = HFactory.getOrCreateCluster(CLUSTER_NAME,new CassandraHostConfigurator(serverUrl+":"+cassandraPort),credentials);
            } else {
                cluster = HFactory.getOrCreateCluster(CLUSTER_NAME, new CassandraHostConfigurator(serverUrl + ":" + port));
            }
            Keyspace keyspace = HFactory.createKeyspace(CASSANDRA_KEYSPACE, cluster);

            //getting the column definitions
            KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(CASSANDRA_KEYSPACE);
            List<ColumnFamilyDefinition> cfDefs = keyspaceDefinition.getCfDefs();
            ColumnFamilyDefinition columnFamilyDefinition = null;

            //getting the column family definition
            for (ColumnFamilyDefinition cfDef : cfDefs) {
                if (cfDef.getName().equals(columnFamily)) {
                    columnFamilyDefinition = cfDef;
                    break;
                }
            }

            //adding the column definitions into a map <columnName,columnDefinition>
            Map<String, ColumnDefinition> columnDefinitionMap = new HashMap<>();
            for (ColumnDefinition columnDefinition : columnFamilyDefinition.getColumnMetadata()) {
                columnDefinitionMap.put(Charset.defaultCharset().decode(columnDefinition.getName()).toString(), columnDefinition);
            }

            if (columnDefinitionMap == null) {
                System.out.println("The column Family could not be found in the keyspace...");
                System.exit(0);
            }

            System.out.println("Migrating data...");
            if (!service.tableExists(tenantId, analyticTable)) {
                service.createTable(tenantId, analyticTable);
                System.out.println("Creating the analytics table: " + analyticTable);
            } else {
                System.out.println("Analytics table: " + analyticTable + " already exists. ");
            }
            System.out.println("Inserting records to Analytic Table: " + analyticTable + " from column family: " + columnFamily);

            //setting up the query
            RangeSlicesQuery<String, String, byte[]> rangeQuery = HFactory.createRangeSlicesQuery(keyspace, StringSerializer.get(),
                    StringSerializer.get(), BytesArraySerializer.get());
            rangeQuery.setColumnFamily(columnFamily);
            rangeQuery.setRange(null, null, false, Integer.MAX_VALUE);
            rangeQuery.setKeys(null, null);
            rangeQuery.setRowCount(batchSize);

            QueryResult<OrderedRows<String, String, byte[]>> result;
            OrderedRows<String, String, byte[]> orderedRows;
            Record record;
            String lastKey;

            //exectuting the query batch by batch ( using the specified batch size) and publishing to the server
            List<Record> records = new ArrayList<Record>();
            while (true) {
                result = rangeQuery.execute();
                orderedRows = result.get();
                for (Row<String, String, byte[]> row : orderedRows) {
                    Map<String, Object> values = getRowValues(row,columnDefinitionMap);
                    record = new Record(tenantId, analyticTable, values);
                    records.add(record);
                }

                //setting the next set of records to be fetched
                lastKey = orderedRows.peekLast().getKey();
                rangeQuery.setKeys(lastKey, "");

                //putting the records to the Data Analtycs Server
                service.put(records);
                records.clear();

                // check if all rows are read
                if (orderedRows.getCount() != batchSize)
                    break;
            }

            System.out.println("Successfully migrated!.");
            System.exit(0);
        }catch(Exception e) {
            System.out.println("Error while migrating: " + e.getMessage());
        } finally {
            if (service != null) {
                service.destroy();
            }
        }
    }

    /**
     * Provides the columns and values for the record
     * @param row
     * @param columnDefinitionMap
     * @return a map of columns and the corresponding values.
     */
    private static Map<String, Object> getRowValues(Row row,Map<String,ColumnDefinition> columnDefinitionMap) {
        Map<String, Object> valuesMap = new HashMap<>();
        List<HColumn<String,byte[]>> columns = row.getColumnSlice().getColumns();
        String columnName;

        for (HColumn column : columns) {
            columnName = column.getName().toString();
            ColumnDefinition columnDefinition = columnDefinitionMap.get(columnName);
            String validationClass = columnDefinition.getValidationClass();

            //stripping off the payload prefix
            if (columnName.contains(PAYLOAD_PREFIX)) {
                columnName = columnName.substring(PAYLOAD_PREFIX.length());
            }
            Object value = unMarshalValues(validationClass, column.getValueBytes());
            valuesMap.put(columnName, value);
        }
        return valuesMap;
    }

    /**
     * Un-marshal the cassandra returned byte array into Object.
     * @param marshalType
     * @param bytes
     * @return an Object of the corresponding type of the validation class.
     */
    private static Object unMarshalValues(String marshalType, ByteBuffer bytes) {
        switch (marshalType) {
            case UTF8_TYPE:
                return StringSerializer.get().fromByteBuffer(bytes);
            case LONG_TYPE:
                return LongSerializer.get().fromByteBuffer(bytes);
            case INTEGER_TYPE:
                return IntegerSerializer.get().fromByteBuffer(bytes);
            case TIME_UUID_TYPE:
                return TimeUUIDSerializer.get().fromByteBuffer(bytes);
            case ASCII_TYPE:
                return AsciiSerializer.get().fromByteBuffer(bytes);
            case BYTE_TYPE:
                return ByteBufferSerializer.get().fromByteBuffer(bytes);
            case BOOLEAN_TYPE:
                return BooleanSerializer.get().fromByteBuffer(bytes);
            case FLOAT_TYPE:
                return FloatSerializer.get().fromByteBuffer(bytes);
            case DOUBLE_TYPE:
                return DoubleSerializer.get().fromByteBuffer(bytes);
            default:
                return StringSerializer.get().fromByteBuffer(bytes);
        }
    }
}
