/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.utils.persistence;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.utils.config.CFConfigBean;
import org.wso2.carbon.bam.utils.config.ConfigurationConstants;
import org.wso2.carbon.bam.utils.config.ConfigurationHolder;
import org.wso2.carbon.bam.utils.config.KeyPart;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class QueryManager {

    private static final Log log = LogFactory.getLog(QueryUtils.class);

    private static DataAccessService dataAccessService;

    private static volatile Keyspace bamKeySpace;

    private static int tenantId;

    private static StringSerializer stringSerializer = StringSerializer.get();

    public static final String INDEX_ROW_KEY = "indexRowKey";
    public static final String GRANULARITY = "granularity";
    public static final String SECONDARY_COLUMN_FAMILY = "secondaryCF";
    public static final String DEFAULT_COLUMN_FAMILY = "defaultCF";
    public static final String ROW_KEY = "rowKey";

    /**
     * Meta Column families.
     * <p/>
     * 3. CFCursors - Stores last fetched row for each get operation in each sequence. This is used
     * for batched access to column families.
     * Structure :
     * ---------------------  --------------------------------------------------------------------------------
     * |  Row Key : CF Name  |  [Key : Sequence + Index of Get in Sequence] : [Value : Last fetched row's key] |
     * ---------------------- ---------------------------------------------------------------------------------
     */
    public static final String META_COLUMN_FAMILY_NAME = "CFInfo";
    public static final String INDEX_COLUMN_FAMILY_NAME = "CFIndexes";
    public static final String CURSORS_COLUMN_FAMILY_NAME = "CFCursors";

    public void initializeManager(Map<String, String> credentials, int tenantId) {

        this.tenantId = tenantId;

        if (bamKeySpace == null) {
            synchronized (QueryManager.class) {
                if (bamKeySpace == null) {
                    String userName = credentials.get(PersistencyConstants.USER_NAME);
                    String password = credentials.get(PersistencyConstants.PASSWORD);

                    Cluster bamCluster = CassandraUtils.createCluster(
                            new ClusterInformation(userName, password));
                    bamKeySpace = (HFactory.createKeyspace(ConfigurationConstants.BAM_KEYSPACE, bamCluster));
                }
            }
        }
    }

    public Keyspace getBamKeyspace() {
        return bamKeySpace;
    }

    /**
     * Here three levels of querying is done.
     * 1. First get range slice of CF row keys from CF index row.
     * 2. For each CF row key get the list of default CF row keys
     * 3. Iterate all default CF row keys and fetch event key value pairs
     *
     * @param cfName    The column family to query
     * @param indexList Each index will carry index name, start and ending keys for range search
     * @return
     */
    public List<ResultRow> querySecondaryColumnFamily(String cfName,
                                                      List<QueryIndex> indexList) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ArrayList<ResultRow>();
        }

        List<CFConfigBean> cfConfigs = ConfigurationHolder.getInstance().
                getIndexConfigurations(tenantId);

        CFConfigBean cfConfig = null;

        for (CFConfigBean cf : cfConfigs) {
            if (cf.getCfName().equals(cfName)) {
                cfConfig = cf;
                break;
            }
        }

        if (cfConfig == null) {
            return null;
        }

        CFConfigBean defaultCfConfig = null;
        for (CFConfigBean cf : cfConfigs) {
            if (cf.isDefaultCF()) {
                defaultCfConfig = cf;
                break;
            }
        }

        if (defaultCfConfig == null) {
            return null;
        }

        boolean equalRanges = false;

        for (QueryIndex index : indexList) {
            String firstIndex = index.getRangeFirst();
            String secondIndex = index.getRangeLast();

            if (firstIndex.equals(secondIndex)) {
                equalRanges = true;
                secondIndex = getNextStringInLexicalOrder(secondIndex);

            }

            index.setRangeLast(secondIndex);
        }

        String rangeFirst = createRangeKey(indexList, true);
        String rangeLast = createRangeKey(indexList, false);

        List<HColumn<String, String>> cfIndexes = getColumnsOfRow(getBamKeyspace(), cfName,
                                                                  cfConfig.getIndexRowKey(),
                                                                  rangeFirst, rangeLast);

        if (equalRanges) {
            cfIndexes = removeDifferentResults(cfIndexes, rangeFirst);
        }

        ListIterator<HColumn<String, String>> cfIndexIterator = cfIndexes.listIterator();

        List<ResultRow> queryResults = new ArrayList<ResultRow>();
        while (cfIndexIterator.hasNext()) {
            String rowKey = cfIndexIterator.next().getName(); // CF row key

            List<HColumn<String, String>> defaultCfIndexes = getColumnsOfRow(getBamKeyspace(),
                                                                             cfName, rowKey, "", "");
            ListIterator<HColumn<String, String>> defaultCfIndexIterator = defaultCfIndexes.
                    listIterator();

            while (defaultCfIndexIterator.hasNext()) {
                String defaultCfRowKey = defaultCfIndexIterator.next().getName();  // Default CF row key
                ResultRow row = getRowData(getBamKeyspace(), defaultCfConfig,
                                           defaultCfRowKey);
                queryResults.add(row);
            }
        }

        return queryResults;

    }

    private List<HColumn<String, String>> removeDifferentResults(
            List<HColumn<String, String>> columnSlice,
            String rangeFirst) {

        List<HColumn<String, String>> columnList = new ArrayList<HColumn<String, String>>();
        for (int i = 0; i < columnSlice.size(); i++) {
            HColumn<String, String> column = columnSlice.get(i);
            String rowKeyFirstPart = rangeFirst.split("---")[0];
            if (column.getName().split("---")[0].equals(rowKeyFirstPart)) {
                columnList.add(column);
            }
        }

        return columnList;

    }

    public List<ResultRow> getQueryResults(String cfName,
                                           List<QueryIndex> indexList) {
        List<CFConfigBean> cfConfigs = ConfigurationHolder.getInstance().
                getIndexConfigurations(tenantId);

        CFConfigBean cfConfig = null;

        for (CFConfigBean cf : cfConfigs) {
            if (cf.getCfName().equals(cfName)) {
                cfConfig = cf;
                break;
            }
        }

        if (cfConfig == null) {
            return null;
        }

        CFConfigBean defaultCfConfig = null;
        for (CFConfigBean cf : cfConfigs) {
            if (cf.isDefaultCF()) {
                defaultCfConfig = cf;
                break;
            }
        }

        if (defaultCfConfig == null) {
            return null;
        }

        for (QueryIndex index : indexList) {
            String firstIndex = index.getRangeFirst();
            String secondIndex = index.getRangeLast();

            if (firstIndex.equals(secondIndex)) {
                secondIndex = getNextStringInLexicalOrder(secondIndex);
            }

            index.setRangeLast(secondIndex);
        }

        String rangeFirst = createRangeKey(indexList, true);
        String rangeLast = createRangeKey(indexList, false);

        List<HColumn<String, String>> rows = getColumnsOfRow(getBamKeyspace(), cfName,
                                                             cfConfig.getIndexRowKey(),
                                                             rangeFirst, rangeLast);
        ListIterator<HColumn<String, String>> rowsIterator = rows.listIterator();

        List<ResultRow> queryResults = new ArrayList<ResultRow>();
        while (rowsIterator.hasNext()) {
            ResultRow resultRow = new ResultRow();

            String rowKey = rowsIterator.next().getName(); // CF row key

            List<HColumn<String, String>> columnList = getColumnsOfRow(getBamKeyspace(),
                                                                       cfName, rowKey, "", "");
            resultRow.setRowKey(rowKey);
            ListIterator<HColumn<String, String>> columnListIterator = columnList.
                    listIterator();

            List<ResultColumn> resultColumnList = new ArrayList<ResultColumn>();
            while (columnListIterator.hasNext()) {
                HColumn<String, String> column = columnListIterator.next();
                ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());

                resultColumnList.add(resultColumn);
            }
            resultRow.setColumns(resultColumnList);
            queryResults.add(resultRow);
        }

        return queryResults;

    }


    public List<ResultRow> queryColumnFamily(String cfName, List<QueryIndex> indexList) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ArrayList<ResultRow>();
        }

        CFConfigBean cfConfig = ConfigurationHolder.getInstance().getIndexConfiguration(cfName,
                                                                                        tenantId);

        List<QueryIndex> reorderedIndexes = reorderIndexes(indexList, cfConfig);

        String rangeFirst = createRangeKey(reorderedIndexes, true);
        String rangeLast = createRangeKey(reorderedIndexes, false);

        List<HColumn<String, String>> cfIndexes = getColumnsOfRow(getBamKeyspace(), cfName,
                                                                  cfConfig.getIndexRowKey(),
                                                                  rangeFirst, rangeLast);
        ListIterator<HColumn<String, String>> cfIndexIterator = cfIndexes.listIterator();

        List<ResultRow> queryResults = new ArrayList<ResultRow>();
        while (cfIndexIterator.hasNext()) {
            String rowKey = cfIndexIterator.next().getName(); // CF row key
            List<HColumn<String, String>> queryColumns = getColumnsOfRow(getBamKeyspace(),
                                                                         cfName, rowKey, "", "");
            ListIterator<HColumn<String, String>> queryColumnIterator = queryColumns.
                    listIterator();

            List<ResultColumn> columns = new ArrayList<ResultColumn>();
            while (queryColumnIterator.hasNext()) {
                HColumn<String, String> column = queryColumnIterator.next();
                String key = column.getName();
                String value = column.getValue();

                ResultColumn resultColumn = new ResultColumn(key, value);
                columns.add(resultColumn);
            }

            ResultRow row = new ResultRow(rowKey, columns);
            queryResults.add(row);
        }

        return queryResults;
    }

    public List<ResultColumn> queryIndexRow(String cfName, List<QueryIndex> indexList,
                                            String startFrom, int batchSize) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ArrayList<ResultColumn>();
        }

        CFConfigBean cfConfig = ConfigurationHolder.getInstance().
                getIndexConfiguration(cfName, tenantId);

        List<ResultColumn> queryResult = new ArrayList<ResultColumn>();
        if (cfConfig != null) {
            List<QueryIndex> reorderedIndexes = indexList;
            if (cfConfig != null) {
                reorderedIndexes = reorderIndexes(indexList, cfConfig);
            }

            String rangeFirst = createRangeKey(reorderedIndexes, true);
            String rangeLast = createRangeKey(reorderedIndexes, false);

            if (startFrom != null && rangeFirst.compareTo(startFrom) < 0) { // startFrom occurs after rangeFirst
                rangeFirst = startFrom;
            }

            List<HColumn<String, String>> cfIndexes = getColumnsOfRow(getBamKeyspace(), cfName,
                                                                      cfConfig.getIndexRowKey(),
                                                                      rangeFirst, rangeLast, batchSize);
            ListIterator<HColumn<String, String>> cfIndexIterator = cfIndexes.listIterator();

            while (cfIndexIterator.hasNext()) {
                HColumn<String, String> column = cfIndexIterator.next();
                ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());
                queryResult.add(resultColumn);
            }
        }

        return queryResult;
    }

    /*
     * Here first we get the batch using time stamp (in arrival order) and filter it out using
     * index criteria. This way we ensure that all the rows are processed exactly once and while also
     * satisfying the filter criteria
     */

    public List<ResultColumn> getBatch(String cfName, String startFrom, List<QueryIndex> indexList,
                                       int batchSize) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ArrayList<ResultColumn>();
        }

        CFConfigBean cfConfig = ConfigurationHolder.getInstance().
                getIndexConfiguration(cfName, tenantId);

        List<ResultColumn> queryResult = new ArrayList<ResultColumn>();
        if (cfConfig != null) {
            List<QueryIndex> reorderedIndexes = indexList;
            if (cfConfig != null) {
                reorderedIndexes = reorderIndexes(indexList, cfConfig);
            }

            String rangeFirst = createRangeKey(reorderedIndexes, true);
            String rangeLast = createRangeKey(reorderedIndexes, false);

            List<HColumn<String, String>> timeStamps = getColumnsOfRow(
                    getBamKeyspace(), cfName, PersistencyConstants.TIMESTAMP_INDEX_ROW, startFrom, "",
                    batchSize);
            ListIterator<HColumn<String, String>> timeStampIterator = timeStamps.listIterator();

            while (timeStampIterator.hasNext()) {
                HColumn<String, String> column = timeStampIterator.next();
                String timeStamp = column.getName();
                String rowKey = column.getValue();

                if (rangeFirst != null && !rangeFirst.trim().equals("") && rangeLast != null &&
                    !rangeLast.trim().equals("")) {
                    if (rowKey.compareTo(rangeFirst) >= 0 && rowKey.compareTo(rangeLast) < 0) {
                        ResultColumn resultColumn = new ResultColumn(rowKey, timeStamp);
                        queryResult.add(resultColumn);
                    }
                } else {
                    ResultColumn resultColumn = new ResultColumn(rowKey, timeStamp);
                    queryResult.add(resultColumn);
                }
            }
        }

        return queryResult;

    }

    // lookUpColumnFamily(String cfName, List<ResultRow> rows);

    public List<ResultRow> lookUpColumnFamily(String cfName, List<ResultRow> rows) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ArrayList<ResultRow>();
        }

        List<ResultRow> queryResults = new ArrayList<ResultRow>();
        for (ResultRow row : rows) {
            for (ResultColumn column : row.getColumns()) {
                String rowKey = column.getKey();
                List<HColumn<String, String>> queryColumns = getColumnsOfRow(getBamKeyspace(),
                                                                             cfName, rowKey, "",
                                                                             "");
                ListIterator<HColumn<String, String>> columnsIterator = queryColumns.
                        listIterator();

                List<ResultColumn> columns = new ArrayList<ResultColumn>();
                while (columnsIterator.hasNext()) {
                    HColumn<String, String> hColumn = columnsIterator.next();
                    String key = hColumn.getName();
                    String value = hColumn.getValue();

                    ResultColumn resultColumn = new ResultColumn(key, value);
                    columns.add(resultColumn);
                }

                ResultRow resultRow = new ResultRow(rowKey, columns);
                queryResults.add(resultRow);
            }
        }

        return queryResults;
    }

    public List<ResultRow> lookupColumnFamily(String cfName, List<String> lookupKeys) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ArrayList<ResultRow>();
        }

        List<ResultRow> queryResults = new ArrayList<ResultRow>();
        for (String lookupKey : lookupKeys) {
            List<HColumn<String, String>> queryColumns = getColumnsOfRow(getBamKeyspace(),
                                                                         cfName, lookupKey, "",
                                                                         "");
            ListIterator<HColumn<String, String>> columnsIterator = queryColumns.listIterator();

            List<ResultColumn> columns = new ArrayList<ResultColumn>();
            while (columnsIterator.hasNext()) {
                HColumn<String, String> hColumn = columnsIterator.next();
                String key = hColumn.getName();
                String value = hColumn.getValue();

                ResultColumn resultColumn = new ResultColumn(key, value);
                columns.add(resultColumn);
            }

            ResultRow resultRow = new ResultRow(lookupKey, columns);
            queryResults.add(resultRow);
        }

        return queryResults;
    }

    public String getColumnValue(String cfName, String rowKey, String colName) {
        ColumnQuery<String, String, String> columnQuery = HFactory.createStringColumnQuery(
                getBamKeyspace());
        columnQuery.setColumnFamily(cfName).setKey(rowKey).setName(colName);
        QueryResult<HColumn<String, String>> result = columnQuery.execute();

        if (result != null && result.get() != null) {
            return result.get().getValue();
        }

        return null;
    }

/*    public List<CFConfigBean> getColumnFamilyConfigs() {
        List<ResultRow> rows = queryColumnFamily(META_COLUMN_FAMILY_NAME);
        List<CFConfigBean> cfConfigs = new ArrayList<CFConfigBean>();
        for (ResultRow row : rows) {
            CFConfigBean cfConfig = new CFConfigBean();
            String cfName = row.getRowKey();
            cfConfig.setCfName(cfName);

            String indexRowKey = null;
            String granularity = null;
            boolean isDefaultCF = false;
            boolean isPrimaryCF = false;
            List<KeyPart> keyParts = new ArrayList<KeyPart>();

            for (ResultColumn column : row.getColumns()) {
                if (INDEX_ROW_KEY.equals(column.getKey())) {
                    indexRowKey = column.getValue();
                } else if (GRANULARITY.equals(column.getKey())) {
                    granularity = column.getValue();
                } else if (DEFAULT_COLUMN_FAMILY.equals(column.getKey())) {
                    isDefaultCF = Boolean.parseBoolean(column.getValue());
                } else if (SECONDARY_COLUMN_FAMILY.equals(column.getKey())) {
                    isPrimaryCF = Boolean.parseBoolean(column.getValue());
                } else if (column.getKey().startsWith(ROW_KEY)) {
                    String[] tokens = column.getValue().split(":");

                    KeyPart part;
                    if (tokens != null && tokens.length == 3) {
                        boolean isIndexStored;
                        try {
                            isIndexStored = Boolean.parseBoolean(tokens[1]);
                        } catch (Exception e) {
                            isIndexStored = false;
                        }

                        part = new KeyPart(tokens[0], tokens[2], isIndexStored);

                    } else {
                        part = new KeyPart(column.getValue(), ConfigurationConstants.EVENT_TYPE,
                                           false);
                    }

                    keyParts.add(part);
                }
            }

            cfConfig.setDefaultCF(isDefaultCF);
            cfConfig.setPrimaryCF(isPrimaryCF);
            cfConfig.setIndexRowKey(indexRowKey);
            cfConfig.setRowKeyParts(keyParts);
            cfConfig.setGranularity(granularity);

            cfConfigs.add(cfConfig);
        }

        return cfConfigs;
    }

    public CFConfigBean getColumnFamilyConfig(String cfName) {
        List<ResultRow> rows = queryColumnFamily(META_COLUMN_FAMILY_NAME);
        CFConfigBean cfConfig = new CFConfigBean();
        for (ResultRow row : rows) {

            if (row.getRowKey().equals(cfName)) {
                cfConfig.setCfName(cfName);

                String indexRowKey = null;
                String granularity = null;
                boolean isDefaultCF = false;
                boolean isPrimaryCF = false;
                List<KeyPart> keyParts = new ArrayList<KeyPart>();

                for (ResultColumn column : row.getColumns()) {
                    if (INDEX_ROW_KEY.equals(column.getKey())) {
                        indexRowKey = column.getValue();
                    } else if (GRANULARITY.equals(column.getKey())) {
                        granularity = column.getValue();
                    } else if (DEFAULT_COLUMN_FAMILY.equals(column.getKey())) {
                        isDefaultCF = Boolean.getBoolean(column.getValue());
                    } else if (SECONDARY_COLUMN_FAMILY.equals(column.getKey())) {
                        isPrimaryCF = Boolean.parseBoolean(column.getValue());
                    } else if (column.getKey().startsWith(ROW_KEY)) {
                        String[] tokens = column.getValue().split(":");

                        KeyPart part;
                        if (tokens != null && tokens.length == 3) {
                            boolean isIndexStored;
                            try {
                                isIndexStored = Boolean.parseBoolean(tokens[1]);
                            } catch (Exception e) {
                                isIndexStored = false;
                            }

                            part = new KeyPart(tokens[0], tokens[2], isIndexStored);

                        } else {
                            part = new KeyPart(column.getValue(), ConfigurationConstants.EVENT_TYPE,
                                               false);
                        }

                        keyParts.add(part);
                    }
                }

                cfConfig.setDefaultCF(isDefaultCF);
                cfConfig.setPrimaryCF(isPrimaryCF);
                cfConfig.setIndexRowKey(indexRowKey);
                cfConfig.setGranularity(granularity);
                cfConfig.setRowKeyParts(keyParts);

                return cfConfig;

            }
        }

        return null;
    }*/


    public List<String> queryIndex(String cfName, String indexName) {
        String rowKey = cfName + "---" + indexName;
        List<HColumn<String, String>> indexValues =
                getColumnsOfRow(getBamKeyspace(), INDEX_COLUMN_FAMILY_NAME, rowKey, "", "");

        List<String> indexValueList = new ArrayList<String>();
        for (HColumn<String, String> indexValue : indexValues) {
            indexValueList.add(indexValue.getName());
        }

        return indexValueList;
    }

    public List<ResultRow> queryColumnFamily(String cfName) {
        RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(getBamKeyspace(), stringSerializer, stringSerializer,
                                                stringSerializer);
        rangeSlicesQuery.setColumnFamily(cfName);
        rangeSlicesQuery.setKeys("", "");
        rangeSlicesQuery.setRange("", "", false, 1000);
        QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();

        List<Row<String, String, String>> rowList = result.get().getList();
        List<ResultRow> queryResult = new ArrayList<ResultRow>();
        for (Row<String, String, String> row : rowList) {
            String rowKey = row.getKey();
            List<HColumn<String, String>> columns = row.getColumnSlice().getColumns();

            List<ResultColumn> resultColumnList = new ArrayList<ResultColumn>();
            for (HColumn<String, String> column : columns) {
                ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());
                resultColumnList.add(resultColumn);
            }

            ResultRow resultRow = new ResultRow(rowKey, resultColumnList);
            queryResult.add(resultRow);
        }

        return queryResult;
    }

    public BatchedResult getBatchedBaseColumnFamilyRows(String columnFamily, String seqName,
                                                        int positionInSeq,
                                                        int batchSize) {

        if (!isColumnFamilyPresent(getBamKeyspace(), columnFamily)) {
            BatchedResult result = new BatchedResult(new ArrayList<ResultRow>(), "");
            return result;
        }

        String lastCursor = getLastCursorForColumnFamily(columnFamily,
                                                         seqName,
                                                         positionInSeq);

        if (lastCursor == null) {
            lastCursor = "";
        }

        List<ResultColumn> rowKeys = queryDefaultColumnFamilyIndexRow(lastCursor, batchSize + 1);

        List<ResultRow> queryResult = new ArrayList<ResultRow>();
        for (ResultColumn rowKey : rowKeys) {
            String key = rowKey.getKey();

            List<ResultColumn> eventColumns = getResultColumnsOfRow(
                    getBamKeyspace(), PersistencyConstants.EVENT_TABLE, key, "", "",
                    Integer.MAX_VALUE);

            List<ResultColumn> metaColumns = getResultColumnsOfRow(
                    getBamKeyspace(), PersistencyConstants.META_TABLE, key, "", "",
                    Integer.MAX_VALUE);

            List<ResultColumn> correlationColumns = getResultColumnsOfRow(
                    getBamKeyspace(), PersistencyConstants.CORRELATION_TABLE, key, "", "",
                    Integer.MAX_VALUE);

            eventColumns.addAll(metaColumns);
            eventColumns.addAll(correlationColumns); // Merge columns of meta and correlation

            ResultRow resultRow = new ResultRow(key, eventColumns);
            queryResult.add(resultRow);

        }

        String nextCursor = null;
        if (queryResult.size() > 0) {
            ResultRow row = queryResult.get(queryResult.size() - 1);
            String[] keyParts = row.getRowKey().split("--");
            String timeStampAndNanoTime = keyParts[0] + "--" + keyParts[1];
            nextCursor = getNextStringInLexicalOrder(timeStampAndNanoTime);
        }

/*        RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(getBamKeyspace(), stringSerializer, stringSerializer,
                                                stringSerializer);
        rangeSlicesQuery.setColumnFamily(PersistencyConstants.EVENT_TABLE);
        rangeSlicesQuery.setKeys(lastCursor, "");
        rangeSlicesQuery.setRange("", "", false, 1000);
        rangeSlicesQuery.setRowCount(batchSize + 1);
        QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();

        List<Row<String, String, String>> rowList = result.get().getList();
        List<ResultRow> queryResult = new ArrayList<ResultRow>();
        for (Row<String, String, String> row : rowList) {
            String rowKey = row.getKey();
            List<HColumn<String, String>> columns = row.getColumnSlice().getColumns();

            List<ResultColumn> resultColumnList = new ArrayList<ResultColumn>();
            for (HColumn<String, String> column : columns) {
                ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());
                resultColumnList.add(resultColumn);
            }

            columns = getColumnsOfRow(getBamKeyspace(), PersistencyConstants.META_TABLE, rowKey,
                                      "", "");
            for (HColumn<String, String> column : columns) {
                ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());
                resultColumnList.add(resultColumn);
            }

            columns = getColumnsOfRow(getBamKeyspace(), PersistencyConstants.CORRELATION_TABLE, rowKey,
                                      "", "");
            for (HColumn<String, String> column : columns) {
                ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());
                resultColumnList.add(resultColumn);
            }

            ResultRow resultRow = new ResultRow(rowKey, resultColumnList.toArray(new ResultColumn[]{}));
            queryResult.add(resultRow);
        }*/

        // Algorithm to decide the next cursor (last timestamp of fetched events) and batch contents.
        // This algorithm removes results until events with previous timestamp found. So in next
        // batch all the events having latest timestamp will be fetched. This is due to the inability
        // order events with given timestamp and the requirement that events keys to be in
        // increasing order for batched retrieval.
/*        ResultRow row = null;
        String timeStamp = null;
        if (queryResult.size() > 0) {
            row = queryResult.remove(queryResult.size() - 1); // Take the next cursor

            timeStamp = row.getRowKey().split("--")[0];
            String previousTimestamp = "";

            boolean isEndOfResults = false;
            ResultRow previousRow = null;
            do {
                if (queryResult.size() > 0) {
                    previousRow = queryResult.remove(queryResult.size() - 1);
                    previousTimestamp = previousRow.getRowKey().split("--")[0];
                } else {
                    isEndOfResults = true;
                    break;
                }
            } while (previousTimestamp.equals(timeStamp));

            if (!isEndOfResults) { // Add back removed event with previous timestamp
                queryResult.add(previousRow);
            }
        }*/

        BatchedResult batchedResult = new BatchedResult(queryResult, nextCursor);

        return batchedResult;
    }

    public BatchedResult getBatchedBaseColumnFamilyRows(int batchSize) {

        return getBatchedBaseColumnFamilyRows(PersistencyConstants.BASE_COLUMN_FAMILY,
                                              PersistencyConstants.DEFAULT_SEQUENCE_NAME,
                                              PersistencyConstants.DEFAULT_SEQUENCE_INDEX, batchSize);

    }

    private List<ResultColumn> queryDefaultColumnFamilyIndexRow(String startFrom, int batchSize) {
        List<HColumn<String, String>> cfIndexes = getColumnsOfRow(getBamKeyspace(),
                                                                  PersistencyConstants.BASE_COLUMN_FAMILY,
                                                                  PersistencyConstants.DEFAULT_INDEX_ROW_KEY,
                                                                  startFrom, "", batchSize);
        ListIterator<HColumn<String, String>> cfIndexIterator = cfIndexes.listIterator();

        List<ResultColumn> queryResult = new ArrayList<ResultColumn>();
        while (cfIndexIterator.hasNext()) {
            HColumn<String, String> column = cfIndexIterator.next();
            ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());
            queryResult.add(resultColumn);
        }

        return queryResult;
    }

    public ResultRow getColumnFamilyRow(String cfName, String rowKey, String rangeFirst,
                                        String rangeLast) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ResultRow("", new ArrayList<ResultColumn>());
        }

        if (rangeFirst != null && rangeLast != null) {
            if (!rangeFirst.equals("") && !rangeLast.equals("")) { // Leave open ended queries as they are
                if (rangeFirst.equals(rangeLast)) {
                    // If this is the case this will return empty response. So let's increment rangeLast by one.
                    rangeLast = getNextStringInLexicalOrder(rangeLast);
                }
            }
        }
        List<HColumn<String, String>> columns = getColumnsOfRow(getBamKeyspace(), cfName, rowKey,
                                                                rangeFirst, rangeLast);
        List<ResultColumn> resultColumns = new ArrayList<ResultColumn>();
        for (HColumn<String, String> column : columns) {
            ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());
            resultColumns.add(resultColumn);
        }

        ResultRow resultRow = new ResultRow(rowKey, resultColumns);
        return resultRow;
    }

    public String getLastCursorForColumnFamily(String cfName, String sequenceName,
                                               int analyzerIndex) {
        String queryString = sequenceName + analyzerIndex;
        List<HColumn<String, String>> cursors = getColumnsOfRow(getBamKeyspace(),
                                                                CURSORS_COLUMN_FAMILY_NAME, cfName,
                                                                queryString, getNextStringInLexicalOrder(
                        queryString));
        String lastCursor = null;
        if (cursors != null) {
            for (HColumn<String, String> cursor : cursors) {
                if (cursor.getName().equals(queryString)) {
                    lastCursor = cursor.getValue();
                    break;
                }
            }
        }

        return lastCursor;

    }

    public void setDataAccessService(DataAccessService service) {
        dataAccessService = service;
    }

    private String createRangeKey(List<QueryIndex> indexList, boolean isRangeFirst) {
        StringBuffer sb = new StringBuffer("");
        String key;
        if (isRangeFirst) {
            for (QueryIndex index : indexList) {
                if (index.getRangeFirst() != null && !index.getRangeFirst().equals("")) {
                    sb.append(index.getRangeFirst());
                    sb.append("---");
                }
            }
        } else {
            for (QueryIndex index : indexList) {
                if (index.getRangeLast() != null && !index.getRangeLast().equals("")) {
                    sb.append(index.getRangeLast());
                    sb.append("---");
                }
            }
        }

        key = sb.toString();

        if (!"".equals(key)) {
            key = key.substring(0, (key.lastIndexOf("---")));
        }

        return key;

    }

    private ResultRow getRowData(Keyspace ks, CFConfigBean defaultCfConfig,
                                 String defaultCfRowKey) {

        List<HColumn<String, String>> eventCfColumns = getColumnsOfRow(ks,
                                                                       defaultCfConfig.getCfName(),
                                                                       defaultCfRowKey, "", "");
        ListIterator<HColumn<String, String>> eventCfColumnsIterator = eventCfColumns.listIterator();

        ResultRow row = new ResultRow();
        row.setRowKey(defaultCfRowKey);

        List<ResultColumn> columns = new ArrayList<ResultColumn>();
        //Map<String, String> dataMap = new HashMap<String, String>();
        while (eventCfColumnsIterator.hasNext()) {
            HColumn<String, String> tuple = eventCfColumnsIterator.next();
            ResultColumn column = new ResultColumn(tuple.getName(), tuple.getValue());
            columns.add(column);
            //dataMap.put(tuple.getName(), tuple.getValue());
        }

        row.setColumns(columns);

        return row;
    }

    private List<HColumn<String, String>> getColumnsOfRow(Keyspace ks, String cfName,
                                                          String rowKey, String rangeFirst,
                                                          String rangeLast) {
        MultigetSliceQuery<String, String, String> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(ks, stringSerializer,
                                                  stringSerializer, stringSerializer);
        multigetSliceQuery.setColumnFamily(cfName);
        multigetSliceQuery.setKeys(rowKey);
        multigetSliceQuery.setRange(rangeFirst, rangeLast, false, Integer.MAX_VALUE);
        QueryResult<Rows<String, String, String>> result = multigetSliceQuery.execute();

        Row<String, String, String> indexRow = result.get().getByKey(rowKey);
        List<HColumn<String, String>> list = indexRow.getColumnSlice().getColumns();

        return list;

    }

    private List<HColumn<String, String>> getColumnsOfRow(Keyspace ks, String cfName,
                                                          String rowKey, String rangeFirst,
                                                          String rangeLast, int columnSize) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ArrayList<HColumn<String, String>>();
        }

        MultigetSliceQuery<String, String, String> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(ks, stringSerializer,
                                                  stringSerializer, stringSerializer);
        multigetSliceQuery.setColumnFamily(cfName);
        multigetSliceQuery.setKeys(rowKey);
        multigetSliceQuery.setRange(rangeFirst, rangeLast, false, columnSize);

        QueryResult<Rows<String, String, String>> result = null;
        try {
            result = multigetSliceQuery.execute();
        } catch (HInvalidRequestException e) {
            // This is to guard against the exception when the column family is not present.
            // This is a hack due to the inability to check whether the column family is present
            // in Cassandra. (Method using KeyspaceDef doesn't work reliably. Gives false negatives)
            // This should be replaced with a proper way to detect a column family if such method is
            // found
            return new ArrayList<HColumn<String, String>>();
        }

        Row<String, String, String> indexRow = result.get().getByKey(rowKey);
        List<HColumn<String, String>> list = indexRow.getColumnSlice().getColumns();

        return list;

    }

    private List<ResultColumn> getResultColumnsOfRow(Keyspace ks, String cfName, String rowKey,
                                                     String rangeFirst, String rangeLast,
                                                     int columnSize) {

        if (!isColumnFamilyPresent(getBamKeyspace(), cfName)) {
            return new ArrayList<ResultColumn>();
        }

        MultigetSliceQuery<String, String, String> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(ks, stringSerializer,
                                                  stringSerializer, stringSerializer);
        multigetSliceQuery.setColumnFamily(cfName);
        multigetSliceQuery.setKeys(rowKey);
        multigetSliceQuery.setRange(rangeFirst, rangeLast, false, columnSize);
        QueryResult<Rows<String, String, String>> result = multigetSliceQuery.execute();

        Row<String, String, String> indexRow = result.get().getByKey(rowKey);
        List<HColumn<String, String>> list = indexRow.getColumnSlice().getColumns();

        ListIterator<HColumn<String, String>> cfIndexIterator = list.listIterator();

        List<ResultColumn> queryResult = new ArrayList<ResultColumn>();
        while (cfIndexIterator.hasNext()) {
            HColumn<String, String> column = cfIndexIterator.next();
            ResultColumn resultColumn = new ResultColumn(column.getName(), column.getValue());
            queryResult.add(resultColumn);
        }

        return queryResult;

    }

    private List<QueryIndex> reorderIndexes(List<QueryIndex> indexList,
                                            CFConfigBean cfConfig) {
        List<QueryIndex> reorderedIndexes = new ArrayList<QueryIndex>();
        for (KeyPart key : cfConfig.getRowKeyParts()) {
            for (QueryIndex index : indexList) {
                if (key.getName().equals(index.getIndexName())) {
                    reorderedIndexes.add(index);
                    break;
                }
            }
        }

        return reorderedIndexes;
    }

    public String getNextStringInLexicalOrder(String str) {

        if ((str == null) || (str.equals(""))) {
            return str;
        }

        byte[] bytes = str.getBytes();

        byte last = bytes[bytes.length - 1];
        last = (byte) (last + 1);        // Not very accurate. Need to improve this more to handle
        //  overflows.

        bytes[bytes.length - 1] = last;

        return new String(bytes);
    }

    // This is a hacky way to detect whether the column family is present in Cassandra.
    // (Method using KeyspaceDef doesn't work reliably. Gives false negatives).
    // This should be replaced with a proper way to detect a column family if such method is found

    private boolean isColumnFamilyPresent(Keyspace ks, String cfName) {
        MultigetSliceQuery<String, String, String> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(ks, stringSerializer,
                                                  stringSerializer, stringSerializer);
        multigetSliceQuery.setColumnFamily(cfName);
        multigetSliceQuery.setKeys("test");
        multigetSliceQuery.setRange("", "", false, 1);

        try {
            multigetSliceQuery.execute();
        } catch (HInvalidRequestException e) {
            return false;
        }

        return true;
    }


}
