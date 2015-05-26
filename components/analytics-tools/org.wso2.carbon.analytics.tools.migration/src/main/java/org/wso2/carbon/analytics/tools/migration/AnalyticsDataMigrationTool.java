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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Migration functionality, from Cassandra to new DAL
 */
public class AnalyticsDataMigrationTool {

    private static final String DOUBLE = "DOUBLE";
    private static final String INTEGER = "INTEGER";
    private static final String LONG = "LONG";
    private static final String FLOAT = "FLOAT";
    private static final String BIGINTEGER = "BIGINTEGER";
    private static final String BIGDECIMAL = "BIGDECIMAL" ;
    private static final String DEFAULT_CASSANDRA_CQL_PORT = "9042";
    private static final String DEFAULT_CASSANDRA_SERVER_URL = "localhost" ;
    private static final String SERVER_URL = "serverUrl";
    private static final String PORT = "port";
    private static final String COLUMN_FAMILY = "columnFamily";
    private static final String ANALYTIC_TABLE = "analyticTable";
    private static final String TENANT_ID = "tenantId";
    private static final String SERVER_URL_ARG = "server url";
    private static final String PORT_ARG = "port";
    private static final String COLUMN_FAMILY_NAME_ARG = "column family name";
    private static final String ANALYTIC_TABLE_NAME_ARG = "analytic table name";
    private static final String TENANT_ID_ARG = "tenant id";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(new Option(SERVER_URL, SERVER_URL_ARG, true, "Cassandra server url '<default value: localhost>'"));
        options.addOption(new Option(PORT, PORT_ARG, true, "Cassandra server port '<default value: 9042'"));
        options.addOption(new Option(COLUMN_FAMILY, COLUMN_FAMILY_NAME_ARG, true, "Name of the columnFamily to be migrated"));
        options.addOption(new Option(ANALYTIC_TABLE, ANALYTIC_TABLE_NAME_ARG, true, "Destination name of the table which will have the migrated data"));
        options.addOption(new Option(TENANT_ID, TENANT_ID_ARG, true, "specify tenant id of the tenant considered '<default value: super tenant>'"));
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);
        if (args.length < 6 ) {
            new HelpFormatter().printHelp("analytics-migrate.sh|cmd", options);
            System.exit(1);
        }

        AnalyticsDataService service = null;
        Cluster cluster = null;
        Session session = null;
        try {
            service = AnalyticsServiceHolder.getAnalyticsDataService();
            int tenantId = 0;
            String serverUrl;
            int port = 0;
            String columnFamily;
            String analyticTable;
            if (line.hasOption(TENANT_ID)) {
                tenantId = Integer.parseInt(line.getOptionValue(TENANT_ID, "" + MultitenantConstants.SUPER_TENANT_ID));
            }
            if (line.hasOption(SERVER_URL)) {
                serverUrl = line.getOptionValue(SERVER_URL, DEFAULT_CASSANDRA_SERVER_URL);
            } else {
                throw new Exception("Server url is not provided!");
            }
            if (line.hasOption(PORT)) {
                port = Integer.parseInt(line.getOptionValue(PORT, DEFAULT_CASSANDRA_CQL_PORT));
            }
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

            System.out.println("Intializing [tenant=" + tenantId + "] [serverUrl='" + serverUrl + "'] " +
                               "[port='" + port + "'] [columnFamily='" + columnFamily + "'] " +
                               "[analyticTable='" + analyticTable + "']...");

            cluster = Cluster.builder().addContactPoint(serverUrl).withPort(port).build();
            final Metadata metadata = cluster.getMetadata();
            System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
            for (final Host host : metadata.getAllHosts())
            {
                System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n", host.getDatacenter(), host.getAddress(), host.getRack());
            }

            session = cluster.connect();
            final ResultSet results = session.execute(
                    "SELECT * from \"EVENT_KS\"." + columnFamily);
            System.out.println("Migrating data...");
            if (!service.tableExists(tenantId, analyticTable)) {
                service.createTable(tenantId, analyticTable);
                System.out.println("Creating the analytics table: " + analyticTable);
            } else {
                System.out.println("Analytics table: " + analyticTable + " already exists. ");
            }
            Iterator<Row> iterator = results.iterator();
            System.out.println("Inserting records to Analytic Table: " + analyticTable + " from column family: " + columnFamily);
            while (iterator.hasNext()) {
                Map<String, Object> values = getAnalyticsValues(iterator.next());
                List<Record> records = new ArrayList<Record>();
                Record record = new Record(tenantId, analyticTable, values);
                records.add(record);
                service.put(records);
            }
            System.out.println("Successfully migrated!.");
            System.exit(0);
        }catch(Exception e) {
            System.out.println("Error while migrating: " + e.getMessage());
            throw new Exception("Error while migrating: " + e.getMessage(), e);
        } finally {
            if (service != null) {
                service.destroy();
            }
            if (session != null) {
                session.close();
            }
            if (cluster != null) {
                cluster.close();
            }
        }
    }

    private static Map<String, Object> getAnalyticsValues(Row row) {
        ColumnDefinitions definitions = row.getColumnDefinitions();
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        for (ColumnDefinitions.Definition definition : definitions) {
            String dataType = definition.getType().asJavaClass().getSimpleName().toUpperCase();
            String columnName = definition.getName();
            if (dataType.equals(DOUBLE)) {
                values.put(columnName, row.getDouble(columnName));
            } else if (dataType.equals(INTEGER)) {
                values.put(columnName, row.getInt(columnName));
            } else if (dataType.equals(LONG)) {
                values.put(columnName, row.getLong(columnName));
            } else if (dataType.equals(FLOAT)) {
                values.put(columnName, row.getFloat(columnName));
            } else if (dataType.equals(BIGINTEGER)) {
                values.put(columnName, row.getVarint(columnName).longValue());
            } else if (dataType.equals(BIGDECIMAL)) {
                values.put(columnName, row.getDecimal(columnName).doubleValue());
            }else {
                values.put(columnName, row.getString(columnName));
            }
        }
        return values;
    }
}
