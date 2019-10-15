/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.dataservice.core.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsIndexedTableStore;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.util.*;

public class AnalyticsTableDeleteTask implements Runnable {

    private static final Log logger = LogFactory.getLog(AnalyticsTableDeleteTask.class);
    private int tenantId;
    private String tableName;
    private long timeFrom;
    private long timeTo;
    private AnalyticsDataServiceImpl analyticsDataServiceImpl;

    public AnalyticsTableDeleteTask(AnalyticsDataServiceImpl analyticsDataServiceImpl, int tenantId, String tableName,
                                    long timeFrom, long timeTo) {
        this.analyticsDataServiceImpl = analyticsDataServiceImpl;
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
    }

    @Override
    public void run() {
        try {
            tableName = GenericUtils.normalizeTableName(tableName);
            /* this is done to make sure, raw record data as well as the index data are also deleted,
             * even if the table is not indexed now, it could have been indexed earlier, so delete operation
             * must be done in the indexer as well */
            AnalyticsIndexedTableStore.IndexedTableId indexedTableId =
                    new AnalyticsIndexedTableStore.IndexedTableId(tenantId, tableName);
            AnalyticsIndexedTableStore.IndexedTableId[] indexedTableIds =
                    analyticsDataServiceImpl.getIndexedTableStore().getAllIndexedTables();
            boolean isTableIndexed = false;
            for (AnalyticsIndexedTableStore.IndexedTableId indexedTableId1 : indexedTableIds) {
                if (indexedTableId.equals(indexedTableId1)) {
                    isTableIndexed = true;
                    break;
                }
            }
            long numberOfRecordsDelete = 0;
            long startedTime = System.currentTimeMillis();
            long initialStartTime = startedTime;
            if (isTableIndexed) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Purging started for " + tableName + "indexed table.");
                }
                BackoffRetryCounter backoffRetryCounter = new BackoffRetryCounter();
                Set<SearchResultEntry> prevResultEntriesSet = null;
                boolean doSleep = false;

                while (true) {
                    List<SearchResultEntry> searchResultEntries = analyticsDataServiceImpl.getIndexer().search(
                            tenantId, tableName, "_timestamp : [" + timeFrom + " TO " + timeTo + "}", 0,
                            10000, new ArrayList<SortByField>(0));

                    if (searchResultEntries.isEmpty()) {
                        break;
                    }

                    if (prevResultEntriesSet != null) {
                        for (SearchResultEntry searchResultEntry : searchResultEntries) {
                            if (prevResultEntriesSet.contains(searchResultEntry)) {
                                doSleep = true;
                                break;
                            }
                        }
                    }

                    if (doSleep) {
                        //If current batch of records are already sent for deletion sleep wait till records are removed
                        // from the Indexer
                        if (logger.isDebugEnabled()) {
                            logger.debug("Deletion of previous batch of records are pending. " +
                                    "Waiting for " + backoffRetryCounter.getTimeInterval() + " till deletion task "
                                    + "completion on " + tableName + " table.");
                        }
                        try {
                            Thread.sleep(backoffRetryCounter.getTimeIntervalMillis());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            doSleep = false;
                        }
                        backoffRetryCounter.increment();
                        continue;
                    }
                    backoffRetryCounter.reset();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Deleting " + searchResultEntries.size() + " records from table: " + tableName);
                    }
                    analyticsDataServiceImpl.delete(tenantId, tableName, getRecordIdsBatch(searchResultEntries));
                    prevResultEntriesSet = new HashSet<>(searchResultEntries);
                    numberOfRecordsDelete += searchResultEntries.size();
                    if (logger.isDebugEnabled()) {
                        if (0 == numberOfRecordsDelete % 100000) {
                            logger.debug("Total No of records deleted: " + numberOfRecordsDelete);
                            logger.debug("Time(seconds) taken to delete current batch of 100000: " +
                                    (System.currentTimeMillis() - startedTime) / 1000);
                            startedTime = System.currentTimeMillis();
                        }
                    }
                }
                logger.info("Purging ended for " + tableName + " indexed table.");
                if (logger.isDebugEnabled()) {
                    logger.debug("Time taken for purging in millies " +
                            (System.currentTimeMillis() - initialStartTime));
                    logger.debug("No of indexed records deleted: " + numberOfRecordsDelete);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Started purging " + tableName + " table data through database query.");
                }
                String arsName = analyticsDataServiceImpl.getRecordStoreNameByTable(tenantId, tableName);
                if (arsName == null) {
                    throw new AnalyticsTableNotAvailableException(tenantId, tableName);
                }
                analyticsDataServiceImpl.getAnalyticsRecordStore(arsName).delete(tenantId, tableName, timeFrom, timeTo);
                if (logger.isDebugEnabled()) {
                    logger.debug("Time(seconds) taken to purge the table: " + tableName
                            + " through database query." + (System.currentTimeMillis() - initialStartTime) / 1000);
                }
            }
        } catch (Exception e) {
            logger.error("Unable to perform analytics table deletion for table: " + tableName + ", tenant ID: "
                    + tenantId + "_timestamp : [" + timeFrom + " TO " + timeTo + "]."
                    + " Reason: " + e.getMessage(), e);
        }
    }

    private List<String> getRecordIdsBatch(List<SearchResultEntry> searchResultEntries) {
        List<String> result = new ArrayList<>(searchResultEntries.size());
        for (SearchResultEntry searchResultEntry : searchResultEntries) {
            result.add(searchResultEntry.getId());
        }
        return result;
    }

    private class BackoffRetryCounter {
        private final String[] timeIntervalNames = new String[]{"2 sec", "5 sec", "10 sec", "15 sec", "30 sec"
                , "1 min", "1 min", "2 min", "5 min"};
        private final long[] timeIntervals = new long[]{2000, 5000, 10000, 15000, 30000, 60000, 60000, 120000, 300000};

        private int intervalIndex = 0;

        public synchronized void reset() {
            intervalIndex = 0;
        }

        protected synchronized void increment() {
            if (intervalIndex < timeIntervals.length - 2) {
                intervalIndex++;
            }
        }

        protected long getTimeIntervalMillis() {
            return timeIntervals[intervalIndex];
        }

        protected String getTimeInterval() {
            return timeIntervalNames[intervalIndex];
        }
    }
}
