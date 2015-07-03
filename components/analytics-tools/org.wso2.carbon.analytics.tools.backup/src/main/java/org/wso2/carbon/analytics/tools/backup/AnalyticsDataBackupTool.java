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
package org.wso2.carbon.analytics.tools.backup;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a tool to backup and restore and re-index analytics data.
 */
public class AnalyticsDataBackupTool {

    private static final String TABLE_SCHEMA_FILE_NAME = "__TABLE_SCHEMA__";
    private static final int INDEX_PROCESS_WAIT_TIME = 5;
    private static final int RECORD_BATCH_SIZE = 200;
    
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        String timePattern = "yy-mm-dd hh:mm:ss";
        Options options = new Options();
        options.addOption(new Option("backup", false, "backup analytics data"));
        options.addOption(new Option("restore", false, "restores analytics data"));
        options.addOption(OptionBuilder.withArgName("directory").hasArg().withDescription(
                "source/target directory").create("dir"));
        options.addOption(OptionBuilder.withArgName("table list").hasArg().withDescription(
                "analytics tables (comma separated) to backup/restore").create("tables"));
        options.addOption(OptionBuilder.withArgName(timePattern).hasArg().withDescription(
                "consider records from this time (inclusive)").create("timefrom"));
        options.addOption(OptionBuilder.withArgName(timePattern).hasArg().withDescription(
                "consider records to this time (non-inclusive)").create("timeto"));
        options.addOption(OptionBuilder.withArgName("tenant id (default is super tenant)").hasArg().withDescription(
                "specify tenant id of the tenant considered").create("tenant_id"));
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);
        if (args.length < 2) {
            new HelpFormatter().printHelp("analytics-backup.sh|cmd", options);
            System.exit(1);
        }
        if (line.hasOption("restore")) {
            System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
        }
        AnalyticsDataService service = null;
        try {
            service = AnalyticsServiceHolder.getAnalyticsDataService();
            int tenantId;
            if (line.hasOption("tenant_id")) {
                tenantId = Integer.parseInt(args[2]);
            } else {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat(timePattern);
            long timeFrom = Long.MIN_VALUE;
            String tfStr = "-~";
            if (line.hasOption("timefrom")) {
                tfStr = line.getOptionValue("timefrom");
                timeFrom = dateFormat.parse(tfStr).getTime();
            }
            long timeTo = Long.MAX_VALUE;
            String ttStr = "+~";
            if (line.hasOption("timeto")) {
                ttStr = line.getOptionValue("timeto");
                timeTo = dateFormat.parse(ttStr).getTime();
            }
            String[] specificTables = null;
            if (line.hasOption("tables")) {
                specificTables = line.getOptionValue("tables").split(",");
            }
            File baseDir = new File(line.getOptionValue("dir"));
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            System.out.println("Intializing [tenant=" + tenantId + "] [timefrom='" + tfStr + "'] [timeto='" + ttStr + "'] [dir='" + baseDir.getAbsolutePath() + "']" + 
                    (specificTables != null ? (" [table=" + Arrays.toString(specificTables) + "]") : "") + "...");
            if (line.hasOption("backup")) {
                backup(service, tenantId, baseDir, timeFrom, timeTo, specificTables);
            } else if (line.hasOption("restore")) {
                restore(service, tenantId, baseDir, timeFrom, timeTo, specificTables);
            }
            System.out.println("Done.");
        } finally {
            if (service != null) {
                service.destroy();
            }
        }
    }
    
    private static void backup(AnalyticsDataService service, int tenantId, File baseDir, long timeFrom, 
            long timeTo, String[] specificTables) throws AnalyticsException {
        if (specificTables != null) {
            for (String specificTable : specificTables) {
                backupTable(service, tenantId, specificTable, baseDir, timeFrom, timeTo);
            }
        } else {
            List<String> tables = service.listTables(tenantId);
            System.out.println(tables.size() + " table(s) available.");
            for (String table : tables) {
                backupTable(service, tenantId, table, baseDir, timeFrom, timeTo);
            }
        }
    }
    
    private static void restore(AnalyticsDataService service, int tenantId, File baseDir, long timeFrom, 
            long timeTo, String[] specificTables) throws IOException {
        if (specificTables != null) {
            for (String specificTable : specificTables) {
                restoreTable(service, tenantId, specificTable, baseDir, timeFrom, timeTo);
            }
        } else {
            String[] tables = baseDir.list();
            System.out.println(tables.length + " table(s) available.");
            for (String table : tables) {
                restoreTable(service, tenantId, table, baseDir, timeFrom, timeTo);
            }
        }
    }
    
    private static void restoreTable(AnalyticsDataService service, int tenantId, String table, File baseDir, 
            long timeFrom, long timeTo) {
        try {
            System.out.print("Restoring table '" + table + "'..");
            service.createTable(tenantId, table);
            File myDir = new File(baseDir.getAbsolutePath() + File.separator + table);
            if (!myDir.isDirectory()) {
                System.out.println(myDir.getAbsolutePath() + " is not a directory to contain table data, skipping.");
                return;
            }
            AnalyticsSchema schema = readTableSchema(baseDir.getAbsolutePath());
            service.setTableSchema(tenantId, table, schema);
            File[] files = myDir.listFiles();
            int count = 0;
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(TABLE_SCHEMA_FILE_NAME)) {
                    continue;
                }
                if (count % 5000 == 0) {
                    System.out.print(".");
                }
                if (file.isDirectory()) {
                    System.out.println(file.getAbsolutePath() + "is a directory, which cannot contain record data, skipping.");
                }
                try {
                    List<Record> records= readRecordFromFile(file);
                    for (Record record : records) {
                        if (!table.equals(record.getTableName())) {
                            System.out.println("Invalid record, invalid table name in record compared to "
                                               + "current directory: " + record.getTableName());
                        }
                    /* check timestamp range */
                        if (!(record.getTimestamp() >= timeFrom && record.getTimestamp() < timeTo)) {
                            records.remove(record);
                        }
                    }
                    if (records.size() >= RECORD_BATCH_SIZE) {
                        service.put(records);
                        service.waitForIndexing(INDEX_PROCESS_WAIT_TIME);
                    }
                } catch (IOException e) {
                    System.out.println("Error in reading record data from file: " + file.getAbsoluteFile() + ", skipping.");
                }
                count++;
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in restoring table: " + table + " - " + e.getMessage());
        }
    }
    
    private static void backupTable(AnalyticsDataService service, int tenantId, String table, File basedir, 
            long timeFrom, long timeTo) {
        try {
            System.out.print("Backing up table '" + table + "'..");
            File myDir = new File(basedir.getAbsolutePath() + File.separator + table);
            if (!myDir.exists()) {
                myDir.mkdir();
            }
            AnalyticsSchema schema = service.getTableSchema(tenantId, table);
            writeTableSchema(schema, myDir.getAbsolutePath());
            AnalyticsDataResponse resp = service.get(tenantId, table, 1, null, timeFrom, timeTo, 0, -1);
            Iterator<Record> recordItr;
            int count = 0;
            for (RecordGroup rg : resp.getRecordGroups()) {
                recordItr = service.readRecords(resp.getRecordStoreName(), rg);
                List<Record> records;
                while (recordItr.hasNext()) {
                    if (count % 5000 == 0) {
                        System.out.print(".");
                    }
                    records = new ArrayList<>();
                    for (int i = 0; i < RECORD_BATCH_SIZE && recordItr.hasNext(); i++) {
                        records.add(recordItr.next());
                    }
                    try {
                        writeRecordToFile(records, myDir.getAbsolutePath());
                    } catch (IOException e) {
                        System.out.println("Error in writing record data to file, skipping.");
                    }
                    count++;
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error in backing up table: " + table + " - " + e.getMessage());
        }
    }
    
    private static void writeTableSchema(AnalyticsSchema schema, String basePath) throws IOException {
        String filePath = basePath + File.separator + TABLE_SCHEMA_FILE_NAME;
        byte[] data = GenericUtils.serializeObject(schema);
        FileOutputStream fileOut = null;
        DataOutputStream dataOut = null;
        try {
            fileOut = new FileOutputStream(filePath);
            dataOut = new DataOutputStream(fileOut);
            dataOut.write(data);
        } finally {
            if (dataOut != null) {
                dataOut.close();
            }
            if (fileOut != null) {
                fileOut.close();
            }
        }
    }
    
    private static void writeRecordToFile(List<Record> records, String basePath) throws IOException {
        Record record = records.get(0);
        String filePath = basePath + File.separator + record.getId();
        byte[] data = GenericUtils.serializeObject(records);
        FileOutputStream fileOut = null;
        DataOutputStream dataOut = null;
        try {
            fileOut = new FileOutputStream(filePath);
            dataOut = new DataOutputStream(fileOut);
            dataOut.write(data);
        } finally {
            if (dataOut != null) {
                dataOut.close();
            }
            if (fileOut != null) {
                fileOut.close();
            }
        }
    }
    
    private static List<Record> readRecordFromFile(File file) throws IOException {
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(file);
            return (List<Record>) GenericUtils.deserializeObject(fileIn);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
    }
    
    private static AnalyticsSchema readTableSchema(String basePath) throws IOException {
        File file = new File(basePath + File.separator + TABLE_SCHEMA_FILE_NAME);
        if (!file.exists()) {
            return new AnalyticsSchema();
        }
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(file);
            return (AnalyticsSchema) GenericUtils.deserializeObject(fileIn);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
    }
    
}
