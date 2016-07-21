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
package org.wso2.carbon.analytics.tools.backup;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse.Entry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.base.MultitenantConstants;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a tool to backup and restore and re-index analytics data.
 */
public class AnalyticsDataBackupTool {

    private static final String TIME_PATTERN = "yy-MM-dd-HH:mm:ss";
    private static final String DIR = "dir";
    private static final String TABLES = "tables";
    private static final String TIMETO = "timeTo";
    private static final String TIMEFROM = "timeFrom";
    private static final String TENANT_ID = "tenantId";
    private static final String PURGETABLE = "table";
    private static final String BATCH_SIZE = "batchSize";
    private static final String REINDEX_EVENTS = "reindexEvents";
    private static final String MIGRATE_TABLE_SCHEMA_V30TO31 = "migrateTableSchemaV30To31";
    private static final String PURGE_DATA = "purge";
    private static final String DELETE_TABLE = "deleteTables";
    private static final String RESTORE_RECORD_STORE = "restoreRecordStore";
    private static final String BACKUP_RECORD_STORE = "backupRecordStore";
    private static final String TABLE_SCHEMA_FILE_NAME = "__TABLE_SCHEMA__";
    private static final String ENABLE_INDEXING = "enableIndexing";
    private static final int INDEX_PROCESS_WAIT_TIME = -1;
    private static final String RECORD_BATCH_SIZE = "1000";
    private static int batchSize = 0;
    private static boolean forceIndexing = false;
    private static final int RECORD_INDEX_CHUNK_SIZE = 1000;

    @SuppressWarnings("static-access")
    private static Options populateOptions() {
        Options options = new Options();
        options.addOption(new Option(BACKUP_RECORD_STORE, false, "backup analytics data"));
        options.addOption(new Option(RESTORE_RECORD_STORE, false, "restores analytics data"));
        options.addOption(new Option(REINDEX_EVENTS, false, "re-indexes records in the given table data"));
        options.addOption(new Option(MIGRATE_TABLE_SCHEMA_V30TO31, false, "migrate v3.x analytics tables to v3.1+"));
        options.addOption(new Option(ENABLE_INDEXING, false, "enables indexing while restoring"));
        options.addOption(new Option(PURGE_DATA, false, "Purges Data for a given time range"));
        options.addOption(new Option(DELETE_TABLE, false, "Deletes given tables"));
        options.addOption(
                OptionBuilder.withArgName("directory").hasArg().withDescription("source/target directory").create(DIR));
        options.addOption(OptionBuilder.withArgName("table list").hasArg()
                .withDescription("analytics tables (comma separated) to backup/restore").create(TABLES));
        options.addOption(OptionBuilder.withArgName(TIME_PATTERN).hasArg()
                .withDescription("consider records from this time (inclusive)").create(TIMEFROM));
        options.addOption(OptionBuilder.withArgName(TIME_PATTERN).hasArg()
                .withDescription("consider records to this time (non-inclusive)").create(TIMETO));
        options.addOption(OptionBuilder.withArgName("tenant id (default is super tenant)").hasArg()
                .withDescription("specify tenant id of the tenant considered").create(TENANT_ID));
        options.addOption(OptionBuilder.withArgName("purge table name").hasArg()
                .withDescription("specify table name of which the data would be purged").create(PURGETABLE));
        options.addOption(OptionBuilder.withArgName("restore record batch size (default is " +
                RECORD_BATCH_SIZE + ")").hasArg().withDescription("specify the number of records per batch for backup")
                .create(BATCH_SIZE));
        return options;
    }
    
    private static void checkAndSetv301MigrationFlag(CommandLine line) {
        if (line.hasOption(MIGRATE_TABLE_SCHEMA_V30TO31)) {
            AnalyticsDataServiceImpl.setInitIndexedTableStore(false);
        }
    }

    public static void main(String[] args) throws Exception {
        // Set $CARBON_HOME
        final String carbonHome = Paths.get("").toAbsolutePath().toString();
        System.setProperty("CARBON_HOME", carbonHome);
        Options options = populateOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);
        if (args.length < 1) {
            new HelpFormatter().printHelp("analytics-backup.sh|cmd", options);
            System.exit(1);
        }
        AnalyticsDataService service = null;
        try {
            checkAndSetv301MigrationFlag(line);
            service = AnalyticsServiceHolder.getAnalyticsDataService();
            int tenantId = Integer.parseInt(line.getOptionValue(TENANT_ID, "" + MultitenantConstants.SUPER_TENANT_ID));
            Long timeTo = getTime(line, TIMETO);
            if (timeTo == null) {
                timeTo = Long.MAX_VALUE;
            }
            Long timeFrom = getTime(line, TIMEFROM);
            if (timeFrom == null) {
                timeFrom = Long.MIN_VALUE;
            }
            String[] specificTables = null;
            if (line.hasOption(TABLES)) {
                specificTables = line.getOptionValue(TABLES).split(",");
            }
            File baseDir;
            if (line.getOptionValue(DIR) != null) {
                baseDir = new File(line.getOptionValue(DIR));
                if (!baseDir.exists()) {
                    boolean created = baseDir.mkdirs();
                    if (!created) {
                        System.out.println("Failed to create the Directory: " + baseDir.getCanonicalPath());
                    }
                }
            } else {
                baseDir = null;
            }
            System.out.println("Intializing [tenant=" + tenantId + "] [timefrom='" + timeFrom + "'] [timeto='" + timeTo
                    + "'] [dir='" + baseDir + "']" +
                    (specificTables != null ? (" [table=" + Arrays.toString(specificTables) + "]") : "") + "...");
            performAction(line, service, tenantId, timeTo, timeFrom, specificTables, baseDir);
        } finally {
            if (service != null) {
                service.destroy();
            }
            Thread.sleep(2000);
        }
        System.out.println("Done.");
    }

    private static void performAction(CommandLine line, AnalyticsDataService service, int tenantId, Long timeTo,
            Long timeFrom, String[] specificTables, File baseDir) throws AnalyticsException, IOException {
        String tableName = null;
        if (line.hasOption(RESTORE_RECORD_STORE)) {
            if (line.hasOption(ENABLE_INDEXING)) {
                System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
                forceIndexing = true;
            }
        }
        if (line.hasOption(BACKUP_RECORD_STORE)) {
            batchSize = Integer.parseInt(line.getOptionValue(BATCH_SIZE, RECORD_BATCH_SIZE));
        }
        if (line.hasOption(PURGE_DATA)) {
            tableName = line.getOptionValue(PURGETABLE, null);
            if (tableName != null) {
                purgeDataWithRange(service, tenantId, timeFrom, timeTo, tableName);
            } else {
                System.out.println("Please specify the table name for data purging!");
            }
        } else if (line.hasOption(DELETE_TABLE)) {
            deleteTables(service, tenantId, specificTables);
        } else if (line.hasOption(BACKUP_RECORD_STORE)) {
            backupRecordStore(service, tenantId, baseDir, timeFrom, timeTo, specificTables);
        } else if (line.hasOption(RESTORE_RECORD_STORE)) {
            restoreRecordStore(service, tenantId, baseDir, timeFrom, timeTo, specificTables);
        } else if (line.hasOption(REINDEX_EVENTS)) {
            for (int i = 0; i < specificTables.length; i++) {
                System.out.printf("Reindexing data for the table: " + specificTables[i]);
                reindexData(service, tenantId, specificTables[i]);
            }
        } else if (line.hasOption(MIGRATE_TABLE_SCHEMA_V30TO31)) {
            migrateTablesV30ToV31(service);
        }
    }

    private static Long getTime(CommandLine line, String option) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        if (line.hasOption(option)) {
            String tfStr = line.getOptionValue(option);
            return dateFormat.parse(tfStr).getTime();
        }
        return null;
    }

    private static void checkBaseDir(File baseDir) {
        if (baseDir == null) {
            System.out.println("The basedir must be given.");
            System.exit(1);
        }
    }

    private static void backupRecordStore(AnalyticsDataService service, int tenantId, File baseDir, long timeFrom,
            long timeTo, String[] specificTables) throws AnalyticsException {
        checkBaseDir(baseDir);
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

    private static void restoreRecordStore(AnalyticsDataService service, int tenantId, File baseDir, long timeFrom,
            long timeTo, String[] specificTables) throws IOException {
        checkBaseDir(baseDir);
        if (specificTables != null) {
            for (String specificTable : specificTables) {
                restoreTable(service, tenantId, specificTable, baseDir, timeFrom, timeTo);
            }
        } else {
            String[] tables = baseDir.list();
            if (tables != null) {
                System.out.println(tables.length + " table(s) available.");
                for (String table : tables) {
                    restoreTable(service, tenantId, table, baseDir, timeFrom, timeTo);
                }
            } else {
                System.out.println("No tables found in location: " + baseDir.getCanonicalPath());
            }
        }
    }

    private static void restoreTable(AnalyticsDataService service, int tenantId, String table, File baseDir,
            long timeFrom, long timeTo) {
        try {
            checkBaseDir(baseDir);
            System.out.print("Restoring table '" + table + "'..");
            service.createTable(tenantId, table);
            File myDir = new File(baseDir.getAbsolutePath() + File.separator + table);
            if (!myDir.isDirectory()) {
                System.out.println(myDir.getAbsolutePath() + " is not a directory to contain table data, skipping.");
                return;
            }
            setTableSchema(service, tenantId, table, baseDir);
            restoreTableFromFiles(service, table, timeFrom, timeTo, myDir);
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error in restoring table: " + table + " - " + e.getMessage());
        }
    }

    private static void restoreTableFromFiles(AnalyticsDataService service, String table, long timeFrom, long timeTo,
            File myDir) throws AnalyticsException {
        File[] files = myDir.listFiles();
        int count = 0;
        if (files != null) {
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(TABLE_SCHEMA_FILE_NAME)) {
                    continue;
                }
                if (count % 5000 == 0) {
                    System.out.print(".");
                }
                if (file.isDirectory()) {
                    System.out.println(
                            file.getAbsolutePath() + "is a directory, which cannot contain record data, skipping.");
                    continue;
                }
                try {
                    List<Record> records = readRecordFromFile(file);
                    for (Record record : records) {
                        if (!table.equals(record.getTableName())) {
                            System.out.println(
                                    "Invalid record, invalid table name in record compared to " + "current directory: "
                                    + record.getTableName());
                        }
                /* check timestamp range */
                        if (!(record.getTimestamp() >= timeFrom && record.getTimestamp() < timeTo)) {
                            records.remove(record);
                        }
                    }
                    service.put(records);
                    if (forceIndexing) {
                        service.waitForIndexing(INDEX_PROCESS_WAIT_TIME);
                    }
                } catch (IOException e) {
                    System.out.println(
                            "Error in reading record data from file: " + file.getAbsoluteFile() + ", skipping.");
                }
                count++;
            }
        } else {
            try {
                System.out.println("No files found related to table '" + table + "', in location: " + myDir.getCanonicalPath());
            } catch (IOException e) {
                throw new AnalyticsException("Error while getting the directory path, " + e.getMessage(), e);
            }
        }
    }

    private static void setTableSchema(AnalyticsDataService service, int tenantId, String table, File baseDir) throws IOException, AnalyticsException {
        AnalyticsSchema currentSchema = readTableSchema(baseDir.getAbsolutePath() + File.separator + table);
        service.setTableSchema(tenantId, table, currentSchema);
    }

    private static void backupTable(AnalyticsDataService service, int tenantId, String table, File basedir,
            long timeFrom, long timeTo) {
        try {
            checkBaseDir(basedir);
            System.out.print("Backing up table '" + table + "'..");
            File myDir = new File(basedir.getAbsolutePath() + File.separator + table);
            if (!myDir.exists()) {
                boolean created = myDir.mkdir();
                if (!created) {
                    System.out.println("Failed to create the Directory: " + myDir.getCanonicalPath());
                }
            }
            AnalyticsSchema schema = service.getTableSchema(tenantId, table);
            writeTableSchema(schema, myDir.getAbsolutePath());
            AnalyticsDataResponse resp = service.get(tenantId, table, 1, null, timeFrom, timeTo, 0, -1);
            Iterator<Record> recordItr;
            int count = 0;
            for (Entry entry : resp.getEntries()) {
                recordItr = service.readRecords(entry.getRecordStoreName(), entry.getRecordGroup());
                List<Record> records;
                while (recordItr.hasNext()) {
                    if (count % 5000 == 0) {
                        System.out.print(".");
                    }
                    records = new ArrayList<>();
                    for (int i = 0; i < batchSize && recordItr.hasNext(); i++) {
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

    @SuppressWarnings("unchecked")
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

    /**
     * Re-indexes the published events.
     *
     * @param dataService the dataservice object.
     * @param tenantId    id of the tenant,
     * @param tableName   name of the table in which the data should be reindexed.
     * @throws AnalyticsException
     */
    private static void reindexData(AnalyticsDataService dataService, int tenantId, String tableName)
            throws AnalyticsException {
        AnalyticsDataResponse analyticsDataResponse = dataService
                .get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
        List<Entry> entries = analyticsDataResponse.getEntries();
        List<Record> recordList = new ArrayList<>();
        dataService.clearIndexData(tenantId, tableName);
        int j = 1;
        //iterating the record groups
        for (int i = 0; i < entries.size(); i++) {
            AnalyticsIterator<Record> recordAnalyticsIterator = dataService
                    .readRecords(entries.get(i).getRecordStoreName(), entries.get(i).getRecordGroup());
            //iterating each record in the record group
            while (recordAnalyticsIterator.hasNext()) {
                recordList.add(recordAnalyticsIterator.next());
                // index the data as chuncks
                if (j % RECORD_INDEX_CHUNK_SIZE == 0) {
                    dataService.put(recordList);
                    recordList.clear();
                }
                j++;
            }
        }
        //write the remaining records in the records list
        if (!recordList.isEmpty()) {
            dataService.put(recordList);
        }
    }
    
    private static void migrateTablesV30ToV31(AnalyticsDataService dataService) throws AnalyticsException {
        System.out.println("Starting Analytics Table Migration from v3.x to v3.1+");
        AnalyticsDataServiceImpl ads = ((AnalyticsDataServiceImpl) dataService);
        ads.convertTableInfoFromv30Tov31();
        System.out.println("Analytics Table Migration done.");
    }

    /**
     * Deletes data from the table for a given time range.
     *
     * @param service   Analytics Data Service.
     * @param tenantId  Tenant ID.
     * @param timeFrom  Start time of the records for the purging.
     * @param timeTo    Stop time of the records for the purging.
     * @param tableName Name of the table of which the data will be purged.
     * @throws AnalyticsException Thrown when unable to access the Analytics Service.
     */
    private static void purgeDataWithRange(AnalyticsDataService service, int tenantId, long timeFrom,
            long timeTo, String tableName) throws AnalyticsException {
        if (service.tableExists(tenantId, tableName)) {
            service.delete(tenantId, tableName, timeFrom, timeTo);
        }
    }

    /**
     * Deletes a specified set of tables.
     *
     * @param service   Analytics Data Service.
     * @param tenantId  Tenant ID
     * @param tableList List of tables to be deleted.
     */
    private static void deleteTables(AnalyticsDataService service, int tenantId, String[] tableList)
            throws AnalyticsException {
        for (String tableName : tableList) {
            service.deleteTable(tenantId,tableName);
        }
    }
}
