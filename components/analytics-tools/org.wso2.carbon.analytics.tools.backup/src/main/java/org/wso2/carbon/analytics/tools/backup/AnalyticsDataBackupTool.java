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
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataServiceConfigProperty;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataServiceConfiguration;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.datasource.commons.*;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.base.MultitenantConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents a tool to backup and restore and re-index analytics data.
 */
public class AnalyticsDataBackupTool {

    private static final String TIME_PATTERN = "yy-mm-dd-hh:mm:ss";
    private static final String DIR = "dir";
    private static final String TABLES = "tables";
    private static final String TIMETO = "timeTo";
    private static final String TIMEFROM = "timeFrom";
    private static final String TENANT_ID = "tenantId";
    private static final String PURGETABLE = "table";
    private static final String BATCH_SIZE = "batchSize";
    private static final String REINDEX_EVENTS = "reindexEvents";
    private static final String DISABLE_STAGING = "disableStaging";
    private static final String RESTORE_FILE_SYSTEM = "restoreFileSystem";
    private static final String RESTORE_RECORD_STORE = "restoreRecordStore";
    private static final String BACKUP_FILE_SYSTEM = "backupFileSystem";
    private static final String BACKUP_RECORD_STORE = "backupRecordStore";
    private static final String TABLE_SCHEMA_FILE_NAME = "__TABLE_SCHEMA__";
    private static final String ANALYTICS_DS_CONFIG_FILE = "analytics-config.xml";
    private static final String ENABLE_INDEXING = "enableIndexing";
    private static final String PURGE_DATA = "purge";
    private static final int INDEX_PROCESS_WAIT_TIME = -1;
    private static final String RECORD_BATCH_SIZE = "1000";
    private static int batchSize = 0;
    private static boolean forceIndexing = false;
    private static final int READ_BUFFER_SIZE = 10240;
    private static final int RECORD_INDEX_CHUNK_SIZE = 1000;

    @SuppressWarnings("static-access")
    private static Options populateOptions() {
        Options options = new Options();
        options.addOption(new Option(BACKUP_RECORD_STORE, false, "backup analytics data"));
        options.addOption(new Option(BACKUP_FILE_SYSTEM, false, "backup filesystem data"));
        options.addOption(new Option(RESTORE_RECORD_STORE, false, "restores analytics data"));
        options.addOption(new Option(RESTORE_FILE_SYSTEM, false, "restores filesystem data"));
        options.addOption(new Option(REINDEX_EVENTS, false, "re-indexes records in the given table data"));
        options.addOption(new Option(ENABLE_INDEXING, false, "enables indexing while restoring"));
        options.addOption(new Option(DISABLE_STAGING, false, "disables staging while restoring"));
        options.addOption(new Option(PURGE_DATA, false, "Purges Data for a given time range"));
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

    public static void main(String[] args) throws Exception {
        Options options = populateOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);
        if (args.length < 2) {
            new HelpFormatter().printHelp("analytics-backup.sh|cmd", options);
            System.exit(1);
        }
        AnalyticsDataService service = null;
        AnalyticsFileSystem analyticsFileSystem = null;
        try {
            analyticsFileSystem = getAnalyticsFileSystem();
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
                    baseDir.mkdirs();
                }
            } else {
                baseDir = null;
            }
            System.out.println("Intializing [tenant=" + tenantId + "] [timefrom='" + timeFrom + "'] [timeto='" + timeTo
                    + "'] [dir='" + baseDir + "']" +
                    (specificTables != null ? (" [table=" + Arrays.toString(specificTables) + "]") : "") + "...");
            performAction(line, service, analyticsFileSystem, tenantId, timeTo, timeFrom, specificTables, baseDir);
        } finally {
            if (service != null) {
                service.destroy();
            }
            if (analyticsFileSystem != null) {
                analyticsFileSystem.destroy();
            }
            Thread.sleep(2000);
        }
        System.out.println("Done.");
    }

    private static void performAction(CommandLine line, AnalyticsDataService service,
            AnalyticsFileSystem analyticsFileSystem, int tenantId, Long timeTo, Long timeFrom, String[] specificTables,
            File baseDir) throws AnalyticsException, IOException {
        // this flag is used to control the staging for the records
        boolean disableStaging = line.hasOption(DISABLE_STAGING);
        String tableName = null;
        if (line.hasOption(RESTORE_RECORD_STORE)) {
            if (line.hasOption(ENABLE_INDEXING)) {
                System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
                forceIndexing = true;
            } else {
                System.setProperty(AnalyticsDataIndexer.DISABLE_INDEX_THROTTLING_ENV_PROP, Boolean.TRUE.toString());
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
        } else if (line.hasOption(BACKUP_RECORD_STORE)) {
            backupRecordStore(service, tenantId, baseDir, timeFrom, timeTo, specificTables);
        } else if (line.hasOption(BACKUP_FILE_SYSTEM)) {
            backupFileSystem(analyticsFileSystem, tenantId, baseDir);
        } else if (line.hasOption(RESTORE_RECORD_STORE)) {
            restoreRecordStore(service, tenantId, baseDir, timeFrom, timeTo, specificTables, disableStaging);
        } else if (line.hasOption(RESTORE_FILE_SYSTEM)) {
            restoreFileSystem(analyticsFileSystem, baseDir);
        } else if (line.hasOption(REINDEX_EVENTS)) {
            for (int i = 0; i < specificTables.length; i++) {
                System.out.printf("Reindexing data for the table: " + specificTables[i]);
                reindexData(service, tenantId, specificTables[i]);
            }
        }
    }

    private static AnalyticsFileSystem getAnalyticsFileSystem()
            throws AnalyticsException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        AnalyticsDataServiceConfiguration config = loadAnalyticsDataServiceConfig();
        String afsClass = config.getAnalyticsFileSystemConfiguration().getImplementation();
        AnalyticsFileSystem analyticsFileSystem = (AnalyticsFileSystem) Class.forName(afsClass).newInstance();
        analyticsFileSystem.init(convertToMap(config.getAnalyticsFileSystemConfiguration().getProperties()));
        return analyticsFileSystem;
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
            long timeTo, String[] specificTables, boolean disableStaging) throws IOException {
        checkBaseDir(baseDir);
        if (specificTables != null) {
            for (String specificTable : specificTables) {
                restoreTable(service, tenantId, specificTable, baseDir, timeFrom, timeTo, disableStaging);
            }
        } else {
            String[] tables = baseDir.list();
            System.out.println(tables.length + " table(s) available.");
            for (String table : tables) {
                restoreTable(service, tenantId, table, baseDir, timeFrom, timeTo, disableStaging);
            }
        }
    }

    private static void restoreTable(AnalyticsDataService service, int tenantId, String table, File baseDir,
            long timeFrom, long timeTo, boolean disableStaging) {
        try {
            checkBaseDir(baseDir);
            System.out.print("Restoring table '" + table + "'..");
            service.createTable(tenantId, table);
            File myDir = new File(baseDir.getAbsolutePath() + File.separator + table);
            if (!myDir.isDirectory()) {
                System.out.println(myDir.getAbsolutePath() + " is not a directory to contain table data, skipping.");
                return;
            }
            setTableSchema(service, tenantId, table, baseDir, disableStaging);
            restoreTableFromFiles(service, table, timeFrom, timeTo, myDir);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in restoring table: " + table + " - " + e.getMessage());
        }
    }

    private static void restoreTableFromFiles(AnalyticsDataService service, String table, long timeFrom, long timeTo,
            File myDir) throws AnalyticsException {
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
    }

    private static void setTableSchema(AnalyticsDataService service, int tenantId, String table, File baseDir,
            boolean disableStaging) throws IOException, AnalyticsException {
        // enabling/disabling the staging for the table
        AnalyticsSchema currentSchema = readTableSchema(baseDir.getAbsolutePath() + File.separator + table);
        AnalyticsSchema schema;
        if (disableStaging) {
            schema = removeIndexingFromSchema(currentSchema);
        } else {
            schema = currentSchema;
        }
        service.setTableSchema(tenantId, table, schema);
    }

    private static void backupTable(AnalyticsDataService service, int tenantId, String table, File basedir,
            long timeFrom, long timeTo) {
        try {
            checkBaseDir(basedir);
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
     * Backs up the file system to Local.
     *
     * @param analyticsFileSystem analyticsFileSystem object.
     * @param tenantId            tenant ID of the tenant.
     * @param baseDir             where the FileSystem should be backed upto.
     * @throws IOException if the data cannot be written to the files.
     */
    private static void backupFileSystem(AnalyticsFileSystem analyticsFileSystem, int tenantId, File targetBaseDir)
            throws IOException {
        System.out.println("Backing up the filesystem to: " + targetBaseDir);
        backupFileSystemToLocal(analyticsFileSystem, "/", targetBaseDir.getAbsolutePath());
    }

    /**
     * Backing up the filesystem to the local recursively.
     *
     * @param analyticsFileSystem analyticsFileSystem object.
     * @param tenantId            tenant ID of the tenant.
     * @param baseDir             where the FileSystem should be backed upto.
     * @throws IOException if the data cannot be written to the files.
     */
    private static void backupFileSystemToLocal(AnalyticsFileSystem analyticsFileSystem, String path,
            String targetBaseDir) throws IOException {
        targetBaseDir = GenericUtils.normalizePath(targetBaseDir);
        List<String> nodeList = analyticsFileSystem.list(path);
        String parentPath = (path.equals("/")) ? path : path + "/";
        String nodePath;            // dependent on the DAS filesystem
        String fileSystemNodePath; // dependent on the file system
        for (String node : nodeList) {
            nodePath = parentPath + node;
            //convert the filesystem target path to match the filesystem path settings
            fileSystemNodePath = targetBaseDir + nodePath;
            fileSystemNodePath = fileSystemNodePath.replace("/", File.separator);
            if (analyticsFileSystem.length(nodePath) == 0) { // the node is a directory
                createDirectoryInLocalSystem(fileSystemNodePath);
                backupFileSystemToLocal(analyticsFileSystem, nodePath, targetBaseDir);
            } else {                                          // the node is a file
                AnalyticsFileSystem.DataInput input = analyticsFileSystem.createInput(nodePath);
                byte[] dataInBuffer = new byte[READ_BUFFER_SIZE];
                int len;
                try (FileOutputStream out = new FileOutputStream(fileSystemNodePath)) {
                    while ((len = input.read(dataInBuffer, 0, dataInBuffer.length)) > 0) {
                        out.write(dataInBuffer, 0, len);
                    }
                } catch (IOException e) {
                    throw new IOException("Could not write to the output file: " + e.getMessage(), e);
                }
                System.out.println(nodePath + " -> " + fileSystemNodePath);
            }
        }
    }

    /**
     * Restores the a local filesystem to the DAS filesystem.
     *
     * @param analyticsFileSystem analyticsFileSystem object.
     * @param baseDir             from where the data would be restored from.
     * @throws IOException if the data cannot be written to the data layer.
     */
    private static void restoreFileSystem(AnalyticsFileSystem analyticsFileSystem, File baseDir) throws IOException {
        System.out.println("Restoring the file system with the Directory: " + baseDir);
        restoreFileStructure(analyticsFileSystem, baseDir, baseDir);
    }

    /**
     * Recursively travels through the filestructure and restores them.
     *
     * @param analyticsFileSystem analyticsFileSystem object.
     * @param node                where to start reading the files in the structure.
     * @param baseDir             from where the data would be restored from.
     * @throws IOException if the data cannot be written to the data layer.
     */
    private static void restoreFileStructure(AnalyticsFileSystem analyticsFileSystem, File node, File baseDir)
            throws IOException {
        //get the relative path
        final String relativePath = node.getAbsolutePath().substring(baseDir.getParent().length());
        if (node.isDirectory()) {
            analyticsFileSystem.mkdir(relativePath);
            String[] subNodes = node.list();
            for (String filename : subNodes) {
                restoreFileStructure(analyticsFileSystem, new File(node, filename), baseDir);
            }
        } else if (node.isFile()) {
            byte[] data = readFile(node);
            try (OutputStream out = analyticsFileSystem.createOutput(relativePath)) {
                out.write(data, 0, data.length);
                out.flush();
                System.out.println(node.getAbsoluteFile() + " -> " + relativePath);
            } catch (IOException e) {
                throw new IOException("Error in restoring the file to the filesystem: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Read the file into a byte array.
     *
     * @param file from which to read from.
     * @return byte array of the file content.
     * @throws IOException if the file cannot be read.
     */
    private static byte[] readFile(File file) throws IOException {
        byte[] buffer = new byte[READ_BUFFER_SIZE];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = new FileInputStream(file)) {
            int nRead;
            while ((nRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, nRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (FileNotFoundException e) {
            throw new IOException("Error in reading the file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("Error in reading the file: " + e.getMessage(), e);
        } finally {
            byteArrayOutputStream.flush();
        }
    }

    /**
     * returning a map of properties read from the config.
     *
     * @param props properties to be added to the map.
     * @return a map of property nam and values.
     */
    private static Map<String, String> convertToMap(AnalyticsDataServiceConfigProperty[] props) {
        Map<String, String> result = new HashMap<>();
        for (AnalyticsDataServiceConfigProperty prop : props) {
            result.put(prop.getName(), prop.getValue());
        }
        return result;
    }

    private static AnalyticsDataServiceConfiguration loadAnalyticsDataServiceConfig() throws AnalyticsException {
        try {
            File confFile = new File(GenericUtils.getAnalyticsConfDirectory() + File.separator +
                    AnalyticsDataSourceConstants.ANALYTICS_CONF_DIR +
                    File.separator + ANALYTICS_DS_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException("Cannot initalize analytics data service, " +
                        "the analytics data service configuration file cannot be found at: " +
                        confFile.getPath());
            }
            System.out.println("conf: " + confFile.getAbsolutePath());
            JAXBContext ctx = JAXBContext.newInstance(AnalyticsDataServiceConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (AnalyticsDataServiceConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsException("Error in processing analytics data service configuration: " +
                    e.getMessage(), e);
        }
    }

    /**
     * Creates a directory in the local file system for the given path.
     *
     * @param path the path of the directory to be created.
     */
    private static void createDirectoryInLocalSystem(String path) {
        File dir = new File(path);
        // if the directory does not exist, create it.
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * Returns an indexing disabled schema from the provided Analytics Schema.
     *
     * @param schema schema of the table.
     * @return analyticsSchema in which the indexing of the columns are set to false.
     */
    private static AnalyticsSchema removeIndexingFromSchema(AnalyticsSchema schema) {
        AnalyticsSchema indexLessSchema = null;
        List<ColumnDefinition> indexLessColumns = new ArrayList<>();

        if (schema != null) {
            List<String> primaryKeys = schema.getPrimaryKeys();
            Map<String, ColumnDefinition> columns = schema.getColumns();
            for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
                ColumnDefinition columnDefinition = entry.getValue();
                if (columnDefinition.isIndexed()) {
                    columnDefinition.setIndexed(false);
                }
                indexLessColumns.add(columnDefinition);
            }
            indexLessSchema = new AnalyticsSchema(indexLessColumns, primaryKeys);
        }
        return indexLessSchema;
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
        RecordGroup[] recordGroups = analyticsDataResponse.getRecordGroups();
        String recordStoreName = analyticsDataResponse.getRecordStoreName();
        List<Record> recordList = new ArrayList<>();
        dataService.clearIndexData(tenantId, tableName);
        int j = 1;
        //iterating the record groups
        for (int i = 0; i < recordGroups.length; i++) {
            AnalyticsIterator<Record> recordAnalyticsIterator = dataService
                    .readRecords(recordStoreName, recordGroups[i]);
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
        if (!recordList.isEmpty())
            dataService.put(recordList);
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
}
