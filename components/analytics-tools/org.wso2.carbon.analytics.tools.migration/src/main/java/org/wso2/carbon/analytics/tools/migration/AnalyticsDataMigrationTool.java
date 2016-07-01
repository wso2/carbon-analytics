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

import me.prettyprint.cassandra.serializers.AsciiSerializer;
import me.prettyprint.cassandra.serializers.BooleanSerializer;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.FloatSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
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
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Migration functionality, from Cassandra to new DAL.
 */
public class AnalyticsDataMigrationTool {

    private static final String DEFAULT_CASSANDRA_PORT = "9160";
    private static final String DEFAULT_CASSANDRA_SERVER_URL = "localhost";
    private static final String CASSANDRA_URL = "cassandraUrl";
    private static final String CASSANDRA_PORT = "cassandraPort";
    private static final String COLUMN_FAMILY = "columnFamily";
    private static final String ANALYTIC_TABLE = "analyticTable";
    private static final String TENANT_ID = "tenantId";
    private static final String CASSANDRA_URL_ARG = "cassandra host url";
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
    private static final String DEFAULT_CLUSTER_NAME = "cluster1";
    private static final String CLUSTER_NAME_ARG = "clusterName";
    private static final String CLUSTER_NAME = "clusterName";
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
        System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
        Options options = getOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);
        if (args.length < 4) {
            new HelpFormatter().printHelp("analytics-migrate.sh|cmd", options);
            System.exit(1);
        }
        AnalyticsDataService service = null;
        try {
            service = AnalyticsServiceHolder.getAnalyticsDataService();
            int batchSize = Integer.parseInt(line.getOptionValue(BATCH_SIZE, RECORD_BATCH_SIZE));
            int tenantId = Integer.parseInt(line.getOptionValue(TENANT_ID, "" + MultitenantConstants.SUPER_TENANT_ID));
            String analyticTable;
            String columnFamily;
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
            System.out.println("Migrating data...");
            if (!service.tableExists(tenantId, analyticTable)) {
                service.createTable(tenantId, analyticTable);
                System.out.println("Creating the analytics table: " + analyticTable);
            } else {
                System.out.println("Analytics table: " + analyticTable + " already exists. ");
            }
            System.out.println(
                    "Inserting records to Analytic Table: " + analyticTable + " from column family: " + columnFamily);
            migrate(service, batchSize, tenantId, analyticTable, columnFamily, line);
            System.out.println("Successfully migrated!.");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Error while migrating: " + e.getMessage());
        } finally {
            if (service != null) {
                service.destroy();
            }
        }
    }

    private static void migrate(AnalyticsDataService service, int batchSize, int tenantId, String analyticTable,
            String columnFamily, CommandLine line) throws Exception {
        Cluster cluster = getCluster(line);
        Keyspace keyspace = HFactory.createKeyspace(CASSANDRA_KEYSPACE, cluster);
        Map<String, ColumnDefinition> columnDefinitionMap = getStringColumnDefinitionMap(cluster, columnFamily);
        RangeSlicesQuery<String, String, byte[]> rangeQuery = getStringStringRangeSlicesQuery(columnFamily,
                batchSize, keyspace);
        long totalRecordCount = 0;
        migrateRecords(service, analyticTable, tenantId, batchSize, columnDefinitionMap, rangeQuery,
                totalRecordCount);
    }

    private static Map<String, ColumnDefinition> getStringColumnDefinitionMap(Cluster cluster, String columnFamily) {
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
        if (columnFamilyDefinition != null) {
            for (ColumnDefinition columnDefinition : columnFamilyDefinition.getColumnMetadata()) {
                columnDefinitionMap
                        .put(Charset.defaultCharset().decode(columnDefinition.getName()).toString(), columnDefinition);
            }
        }
        if (columnDefinitionMap.isEmpty()) {
            System.out.println("The column Family could not be found in the keyspace...");
            System.exit(0);
        }
        return columnDefinitionMap;
    }

    private static Cluster getCluster(CommandLine line) throws Exception {
        String cassandraUrl;
        String cassandraUser = null, cassandraPassword = null, clusterName = null;
        //if no cassandra URL is provided use the server URL
        cassandraUrl = line.getOptionValue(CASSANDRA_URL, DEFAULT_CASSANDRA_SERVER_URL);
        clusterName = line.getOptionValue(CLUSTER_NAME, DEFAULT_CLUSTER_NAME);
        int cassandraPort = Integer.parseInt(line.getOptionValue(CASSANDRA_PORT, DEFAULT_CASSANDRA_PORT));
        if (line.hasOption(CASSANDRA_USERNAME)) {
            cassandraUser = line.getOptionValue(CASSANDRA_USERNAME);
        }
        if (line.hasOption(CASSANDRA_PASSWORD)) {
            cassandraPassword = line.getOptionValue(CASSANDRA_PASSWORD);
        }
        Cluster cluster;//configuring the cassandra cluster and the keyspace
        if (cassandraUser != null && cassandraPassword != null) {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", cassandraUser);
            credentials.put("password", cassandraPassword);
            cluster = HFactory.getOrCreateCluster(clusterName,
                    new CassandraHostConfigurator(cassandraUrl + ":" + cassandraPort), credentials);
        } else {
            throw new Exception("Username and Password is not provided!");
        }
        return cluster;
    }

    private static void migrateRecords(AnalyticsDataService service, String analyticTable, int tenantId, int batchSize,
            Map<String, ColumnDefinition> columnDefinitionMap, RangeSlicesQuery<String, String, byte[]> rangeQuery,
            long totalRecordCount) throws AnalyticsException {
        QueryResult<OrderedRows<String, String, byte[]>> result;
        OrderedRows<String, String, byte[]> orderedRows;
        Record record;
        String lastKey;//exectuting the query batch by batch ( using the specified batch size) and publishing to the server
        List<Record> records = new ArrayList<Record>();
        while (true) {
            result = rangeQuery.execute();
            orderedRows = result.get();
            for (Row<String, String, byte[]> row : orderedRows) {
                Map<String, Object> values = getRowValues(row, columnDefinitionMap);
                record = new Record(tenantId, analyticTable, values);
                records.add(record);
            }
            //setting the next set of records to be fetched
            lastKey = orderedRows.peekLast().getKey();
            rangeQuery.setKeys(lastKey, "");
            //putting the records to the Data Analtycs Server
            totalRecordCount += records.size();
            service.put(records);
            records.clear();
            System.out.println("Number of records migrated: " + totalRecordCount);
            // check if all rows are read
            if (orderedRows.getCount() != batchSize)
                break;
        }
    }

    private static RangeSlicesQuery<String, String, byte[]> getStringStringRangeSlicesQuery(String columnFamily,
            int batchSize, Keyspace keyspace) {
        //setting up the query
        RangeSlicesQuery<String, String, byte[]> rangeQuery = HFactory
                .createRangeSlicesQuery(keyspace, StringSerializer.get(), StringSerializer.get(),
                        BytesArraySerializer.get());
        rangeQuery.setColumnFamily(columnFamily);
        rangeQuery.setRange(null, null, false, Integer.MAX_VALUE);
        rangeQuery.setKeys(null, null);
        rangeQuery.setRowCount(batchSize);
        return rangeQuery;
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName(CASSANDRA_URL_ARG).hasArg()
                .withDescription("Cassandra server url '<default value: localhost>'").create(CASSANDRA_URL));
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
                .withDescription("specify the cassandra username").create(CASSANDRA_USERNAME));
        options.addOption(OptionBuilder.withArgName(CASSANDRA_PASSWORD_ARG).hasArg()
                .withDescription("specify the cassandra username").create(CASSANDRA_PASSWORD));
        options.addOption(
                OptionBuilder.withArgName(CASSANDRA_PORT_ARG).hasArg().withDescription("specify the cassandra port")
                        .create(CASSANDRA_PORT_ARG));
        options.addOption(
                OptionBuilder.withArgName(CLUSTER_NAME_ARG).hasArg().withDescription("specify the cassandra cluster")
                        .create(CLUSTER_NAME));
        return options;
    }

    private static Map<String, Object> getRowValues(Row row, Map<String, ColumnDefinition> columnDefinitionMap) {
        Map<String, Object> valuesMap = new HashMap<>();
        List<HColumn<String, byte[]>> columns = row.getColumnSlice().getColumns();
        String columnName;

        for (HColumn column : columns) {
            columnName = column.getName().toString();
            ColumnDefinition columnDefinition = columnDefinitionMap.get(columnName);
            String validationClass = columnDefinition.getValidationClass();

            //stripping off the payload prefix
            if (columnName.contains(PAYLOAD_PREFIX)) {
                columnName = columnName.substring(PAYLOAD_PREFIX.length());
            } else if (columnName.equals(OLD_VERSION_FIELD)) {
                columnName = NEW_VERSION_FIELD;
            }
            Object value = unMarshalValues(validationClass, column.getValueBytes());
            valuesMap.put(columnName, value);
        }
        return valuesMap;
    }

    private static Object unMarshalValues(String marshalType, ByteBuffer bytes) {
        switch (marshalType) {
        case UTF8_TYPE:
            return StringSerializer.get().fromByteBuffer(bytes);
        case LONG_TYPE:
            return LongSerializer.get().fromByteBuffer(bytes);
        case INTEGER_TYPE:
            return IntegerSerializer.get().fromByteBuffer(bytes);
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
            return "";
        }
    }
}
