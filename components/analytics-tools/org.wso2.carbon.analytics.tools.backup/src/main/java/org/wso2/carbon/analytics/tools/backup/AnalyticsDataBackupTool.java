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
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataServiceConfigProperty;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataServiceConfiguration;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
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

    private static final String TABLE_SCHEMA_FILE_NAME = "__TABLE_SCHEMA__";
    private static final String ANALYTICS_DS_CONFIG_FILE = "analytics-config.xml";
    private static final int INDEX_PROCESS_WAIT_TIME = -1;
    private static final String RECORD_BATCH_SIZE = "1000";
    private static int batchSize = 0;
    private static boolean forceIndexing = false;
    private static final int READ_BUFFER_SIZE = 10;
    private static final int RECORD_INDEX_CHUNK_SIZE = 1000;

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        String timePattern = "yy-mm-dd hh:mm:ss";
        Options options = new Options();
        options.addOption(new Option("backupRecordStore", false, "backup analytics data"));
        options.addOption(new Option("backupFileSystem", false, "backup filesystem data"));

        options.addOption(new Option("restoreRecordStore", false, "restores analytics data"));
        options.addOption(new Option("restoreFileSystem", false, "restores filesystem data"));

        options.addOption(new Option("reindexEvents", false, "re-indexes records in the given table data"));

        options.addOption(new Option("enableIndexing", false, "enables indexing while restoring"));
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
        options.addOption(OptionBuilder.withArgName("restore record batch size (default is 1000)").hasArg().withDescription(
                "specify the number of records per batch for backup").create("batch"));
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);
        if (args.length < 2) {
            new HelpFormatter().printHelp("analytics-backup.sh|cmd", options);
            System.exit(1);
        }
        if (line.hasOption("restoreRecordStore")) {
            if (line.hasOption("enableIndexing")) {
                System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
                forceIndexing = true;
            } else {
                System.setProperty(AnalyticsDataIndexer.DISABLE_INDEX_THROTTLING_ENV_PROP, Boolean.TRUE.toString());
            }
        }
        if (line.hasOption("backupRecordStore")) {
            batchSize = Integer.parseInt(line.getOptionValue("batch", RECORD_BATCH_SIZE));
        }
        AnalyticsDataService service = null;
        AnalyticsFileSystem analyticsFileSystem = null;
        try {
            AnalyticsDataServiceConfiguration config = loadAnalyticsDataServiceConfig();
            String afsClass = config.getAnalyticsFileSystemConfiguration().getImplementation();
            analyticsFileSystem = (AnalyticsFileSystem) Class.forName(afsClass).newInstance();
            analyticsFileSystem.init(convertToMap(config.getAnalyticsFileSystemConfiguration()
                    .getProperties()));
            service = AnalyticsServiceHolder.getAnalyticsDataService();

            int tenantId = Integer.parseInt(line.getOptionValue("tenant_id", "" + MultitenantConstants.SUPER_TENANT_ID));
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

            File baseDir;
            String baseDirPath;
            if(line.getOptionValue("dir") != null){
                baseDir = new File(line.getOptionValue("dir"));
                baseDirPath = baseDir.getAbsolutePath();
                if (!baseDir.exists()) {
                    baseDir.mkdirs();
                }
            } else {
                baseDir = null;
                baseDirPath = "Not specified";
            }

            System.out.println("Intializing [tenant=" + tenantId + "] [timefrom='" + tfStr + "'] [timeto='" + ttStr + "'] [dir='" + baseDirPath + "']" +
                    (specificTables != null ? (" [table=" + Arrays.toString(specificTables) + "]") : "") + "...");
            if (line.hasOption("backupRecordStore")) {
                backupRecordStore(service, tenantId, baseDir, timeFrom, timeTo, specificTables);
            } else if (line.hasOption("backupFileSystem")) {
                backupFileSystem(analyticsFileSystem, tenantId, baseDir);
            } else if (line.hasOption("restoreRecordStore")) {
                restoreRecordStore(service, tenantId, baseDir, timeFrom, timeTo, specificTables);
            } else if (line.hasOption("restoreFileSystem")) {
                restoreFileSystem(analyticsFileSystem, baseDir);
            } else if (line.hasOption("reindexEvents")) {
                for (int i = 0; i < specificTables.length; i++) {
                    System.out.printf("Reindexing data for the table: " + specificTables[i]);
                    reindexData(service, tenantId, specificTables[i]);
                }
            }
            System.out.println("Done.");
        } finally {
            if (service != null) {
                service.destroy();
            }
            if (analyticsFileSystem != null) {
                analyticsFileSystem.destroy();
            }
        }
    }

    private static void backupRecordStore(AnalyticsDataService service, int tenantId, File baseDir,
                                          long timeFrom, long timeTo, String[] specificTables)
            throws AnalyticsException {
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

    private static void restoreRecordStore(AnalyticsDataService service, int tenantId, File baseDir,
                                           long timeFrom, long timeTo, String[] specificTables)
            throws IOException {
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
            AnalyticsSchema schema = readTableSchema(baseDir.getAbsolutePath() + File.separator + table);
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
                    continue;
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

                    service.put(records);
                    if (forceIndexing) {
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
     * @param analyticsFileSystem
     * @param tenantId
     * @param baseDir
     * @throws IOException
     */
    private static void backupFileSystem(AnalyticsFileSystem analyticsFileSystem, int tenantId,
                                         File targetBaseDir) throws IOException {
        System.out.println("Backing up the filesystem to: " + targetBaseDir);
        backupFileSystemToLocal(analyticsFileSystem, "/", targetBaseDir.getAbsolutePath());
    }

    /**
     * Backing up the filesystem to the local recursively.
     * @param analyticsFileSystem
     * @param path
     * @param baseDir
     * @throws IOException
     */
    private static void backupFileSystemToLocal(AnalyticsFileSystem analyticsFileSystem,
                                                String path, String targetBaseDir) throws IOException {
        targetBaseDir = GenericUtils.normalizePath(targetBaseDir);
        List<String> nodeList = analyticsFileSystem.list(path);
        String parentPath = (path.equals("/")) ? path : path + "/";
        String nodePath;            // dependent on the DAS filesystem
        String fileSystemNodePath; // dependent on the file system

        for (String node : nodeList) {
            nodePath = parentPath + node;
            //convert the filesystem target path to match the filesystem path settings
            fileSystemNodePath = targetBaseDir + nodePath;
            if(!File.separator.equals('/')) {
                fileSystemNodePath = fileSystemNodePath.replace("/", "\\");
            }
            if (analyticsFileSystem.length(nodePath) == 0) { // the node is a directory
                createDirectoryInLocalSystem(fileSystemNodePath);
                backupFileSystemToLocal(analyticsFileSystem, nodePath, targetBaseDir);

            } else {                                          // the node is a file
                AnalyticsFileSystem.DataInput input = analyticsFileSystem.createInput(nodePath);
                byte[] dataInBuffer = new byte[READ_BUFFER_SIZE];
                try (FileOutputStream out = new FileOutputStream(fileSystemNodePath)) {
                    while (input.read(dataInBuffer, 0, dataInBuffer.length) > 0) {
                        out.write(dataInBuffer);
                    }
                } catch (IOException e) {
                    throw new IOException("Could not write to the output file: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Restores the a local filesystem to the DAS filesystem.
     *
     * @param analyticsFileSystem
     * @param baseDir
     * @throws IOException
     */
    private static void restoreFileSystem(AnalyticsFileSystem analyticsFileSystem, File baseDir)
            throws IOException {
        System.out.println("Restoring the file system with the Directory: " + baseDir);
        restoreFileStructure(analyticsFileSystem, baseDir, baseDir);
    }

    /**
     * Recursively travels through the filestructure and restores them.
     *
     * @param analyticsFileSystem
     * @param node
     * @param baseDir
     * @throws IOException
     */
    private static void restoreFileStructure(AnalyticsFileSystem analyticsFileSystem, File node, File baseDir) throws IOException {
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
            } catch (IOException e) {
                throw new IOException("Error in restoring the file to the filesystem: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Read the file into a byte array.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static byte[] readFile(File file) throws IOException {

        byte[] buffer = new byte[READ_BUFFER_SIZE];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = new FileInputStream(file)) {
            int nRead;
            while ((nRead = inputStream.read(buffer, 0, (int) file.length())) != -1) {
                byteArrayOutputStream.write(buffer, 0, nRead);
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Error in reading the file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("Error in reading the file: " + e.getMessage(), e);
        } finally {
            byteArrayOutputStream.flush();
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * returning a map of properties read from the config.
     *
     * @param props
     * @return
     */
    private static Map<String, String> convertToMap(AnalyticsDataServiceConfigProperty[] props) {
        Map<String, String> result = new HashMap<>();
        for (AnalyticsDataServiceConfigProperty prop : props) {
            result.put(prop.getName(), prop.getValue());
        }
        return result;
    }

    private static AnalyticsDataServiceConfiguration loadAnalyticsDataServiceConfig()
            throws AnalyticsException {
        try {
            File confFile =
                    new File(GenericUtils.getAnalyticsConfDirectory() + File.separator +
                            AnalyticsDataSourceConstants.ANALYTICS_CONF_DIR +
                            File.separator + ANALYTICS_DS_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException(
                        "Cannot initalize analytics data service, " +
                                "the analytics data service configuration file cannot be found at: " +
                                confFile.getPath());
            }
            System.out.printf("conf: " + confFile.getAbsolutePath());
            JAXBContext ctx = JAXBContext.newInstance(AnalyticsDataServiceConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (AnalyticsDataServiceConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsException(
                    "Error in processing analytics data service configuration: " +
                            e.getMessage(), e);
        }
    }

    /**
     * Creates a directory in the local file system for the given path.
     *
     * @param path
     */
    private static void createDirectoryInLocalSystem(String path) {
        File dir = new File(path);
        // if the directory does not exist, create it.
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * Re-indexes the published events.
     * @param dataService
     * @param indexer
     * @param tenantId
     * @param tableName
     * @throws AnalyticsException
     */
    private static void reindexData(AnalyticsDataService dataService, int tenantId,String tableName) throws AnalyticsException {
        AnalyticsDataResponse analyticsDataResponse = dataService.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
        RecordGroup[] recordGroups = analyticsDataResponse.getRecordGroups();
        String recordStoreName = analyticsDataResponse.getRecordStoreName();
        List<Record> recordList = new ArrayList<>();
        dataService.clearIndexData(tenantId,tableName);

        int j = 1;
        //iterating the record groups
        for (int i = 0; i < recordGroups.length; i++) {
            AnalyticsIterator<Record> recordAnalyticsIterator = dataService.readRecords(recordStoreName, recordGroups[i]);

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
}
