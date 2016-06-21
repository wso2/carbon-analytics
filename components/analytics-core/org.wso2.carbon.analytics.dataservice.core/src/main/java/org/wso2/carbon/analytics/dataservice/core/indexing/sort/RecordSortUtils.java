
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.dataservice.core.indexing.sort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Util class which contains the methods used to sort the records
 */
public class RecordSortUtils {

    private static Log logger = LogFactory.getLog(RecordSortUtils.class);

    public static List<SearchResultEntry> getSortedSearchResultEntries(int tenantId,
                                                                       String tableName,
                                                                       List<SortByField> sortByFields,
                                                                       Map<String, ColumnDefinition> indices,
                                                                       AnalyticsDataService ads,
                                                                       List<List<SearchResultEntry>> unsortedResults)
            throws AnalyticsIndexException {

        List<List<SearchResultEntry>> unsortedResultsAfterRemovingEmpties = new ArrayList<List<SearchResultEntry>>();
        for (List<SearchResultEntry> searchResultEntries : unsortedResults) {
            // remove entries with no search results
            if (searchResultEntries.size() > 0) {
                unsortedResultsAfterRemovingEmpties.add(searchResultEntries);
            }
        }
        
        if (sortByFields != null && !sortByFields.isEmpty()) {
            List<String> ids = new ArrayList<>();
            Map<String, SearchResultEntry> unsortedSearchResultEntries = new HashMap<>();
            for (List<SearchResultEntry> searchResultEntries : unsortedResultsAfterRemovingEmpties) {
                for (SearchResultEntry searchResultEntry : searchResultEntries) {
                    ids.add(searchResultEntry.getId());
                    unsortedSearchResultEntries.put(searchResultEntry.getId(), searchResultEntry);
                }
            }
            try {
                AnalyticsDataResponse response = ads.get(tenantId, tableName, 1, null, ids);
                List<List<Record>> sortedRecordListsPerNode = getSortedRecordListsPerNode(ads, unsortedResultsAfterRemovingEmpties, response);
                List<Record> records = getSortedList(sortedRecordListsPerNode, indices, sortByFields);
                return getFinalSortedSearchResultEntries(unsortedSearchResultEntries, records);
            } catch (AnalyticsException e) {
                throw new AnalyticsIndexException("Error while sorting search results: " + e.getMessage(), e);
            }
        } else {
            List<SearchResultEntry> sortedSearchResultEntries = new ArrayList<>();
            for (List<SearchResultEntry> searchResultEntries : unsortedResultsAfterRemovingEmpties) {
                sortedSearchResultEntries.addAll(searchResultEntries);
            }
            Collections.sort(sortedSearchResultEntries);
            Collections.reverse(sortedSearchResultEntries);
            return sortedSearchResultEntries;
        }
    }

    private static List<SearchResultEntry> getFinalSortedSearchResultEntries(
            Map<String, SearchResultEntry> unsortedSearchResultEntries, List<Record> records) {
        List<SearchResultEntry> sortedSearchResultEntries = new ArrayList<>();
        for (Record record : records) {
            sortedSearchResultEntries.add(unsortedSearchResultEntries.get(record.getId()));
        }
        return sortedSearchResultEntries;
    }

    private static List<List<Record>> getSortedRecordListsPerNode(AnalyticsDataService ads,
                                                                  List<List<SearchResultEntry>> unsortedResults,
                                                                  AnalyticsDataResponse response)
            throws AnalyticsException {
        List<Record> unsortedRecords = AnalyticsDataServiceUtils.listRecords(ads, response);
        Map<String, Record> unsortedRecordMap = getRecordIdswithRecords(unsortedRecords);
        List<List<Record>> sortedRecordsSubLists = new ArrayList<>();
        for (List<SearchResultEntry> searchResultEntries : unsortedResults) {
            List<Record> records = new ArrayList<>();
            for (SearchResultEntry entry : searchResultEntries) {
                if (unsortedRecordMap.containsKey(entry.getId())) {
                    records.add(unsortedRecordMap.get(entry.getId()));
                }
            }
            sortedRecordsSubLists.add(records);
        }
        return sortedRecordsSubLists;
    }

    private static Map<String, Record> getRecordIdswithRecords(List<Record> unsortedRecords) {
        Map<String, Record> unsortedRecordMap = new HashMap<>();
        for (Record record : unsortedRecords) {
            unsortedRecordMap.put(record.getId(), record);
        }
        return unsortedRecordMap;
    }

    private static List<Record> getSortedList(List<List<Record>> records,
                                              Map<String, ColumnDefinition> indices,
                                              List<SortByField> sortByFields) {
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        if (records.size() > 1) {
            List<Record> mergeSortedRecords = records.get(0);
            for (int i = 1; i < records.size(); i++) {
                mergeSortedRecords = mergeSort(mergeSortedRecords, records.get(i), sortByFields, indices);
            }
            return mergeSortedRecords;
        }
        return records.get(0);
    }

    private static List<Record> mergeSort(List<Record> left, List<Record> right,
                                          List<SortByField> sortByFields,
                                          Map<String, ColumnDefinition> indices) {
        int leftIndex = 0;
        int rightIndex = 0;
        List<Record> sortedList = new ArrayList<>();

        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (compare(left.get(leftIndex), right.get(rightIndex), sortByFields, indices) < 0) {
                sortedList.add(left.get(leftIndex));
                leftIndex++;
            } else {
                sortedList.add(right.get(rightIndex));
                rightIndex++;
            }
        }
        List<Record> rest;
        int restIndex;
        if (leftIndex >= left.size()) {
            rest = right;
            restIndex = rightIndex;
        } else {
            rest = left;
            restIndex = leftIndex;
        }
        for (int i = restIndex; i < rest.size(); i++) {
            sortedList.add(rest.get(i));
        }
        return sortedList;
    }

    private static int compare(Record record1, Record record2, List<SortByField> sortByFields,
                               Map<String, ColumnDefinition> indices) {
        int compareInt = 0;
        try {
            for (SortByField sortByField : sortByFields) {
                Object value1, value2;
                AnalyticsSchema.ColumnType type;
                ColumnDefinition columnDefinition = indices.get(sortByField.getFieldName());
                if (columnDefinition == null) {
                    String fieldName = sortByField.getFieldName();
                    if (fieldName != null && fieldName.equals(AnalyticsDataIndexer.INDEX_INTERNAL_TIMESTAMP_FIELD)) {
                        type = AnalyticsSchema.ColumnType.LONG;
                        value1 = record1.getTimestamp();
                        value2 = record2.getTimestamp();
                    } else {
                        throw new AnalyticsException("Cannot find index information for field: " + fieldName);
                    }
                } else {
                    type = columnDefinition.getType();
                    value1 = record1.getValue(sortByField.getFieldName());
                    value2 = record2.getValue(sortByField.getFieldName());
                }
                compareInt = doCompare(sortByField, value1, value2, type);
                if (compareInt != 0) {
                    break;
                }
            }
        } catch (AnalyticsException e) {
            logger.error("Sorting failed, Error while sorting records: " + e.getMessage(), e);
        }
        return compareInt;
    }

    private static int doCompare(SortByField sortByField, Object value1,
                                 Object value2, AnalyticsSchema.ColumnType type)
            throws AnalyticsException {
        int compareInt = 0;
        if (value1 != null && value2 != null) {
            if (sortByField.getSortType() == SortType.ASC) {
                compareInt = compareValues(type, value1, value2);
            } else if (sortByField.getSortType() == SortType.DESC) {
                compareInt = compareValues(type, value2, value1);
            }
        } else {
            throw new AnalyticsException("Cannot find a field name called: " + sortByField.getFieldName());
        }
        return compareInt;
    }

    private static int compareValues(AnalyticsSchema.ColumnType type, Object value1,
                                     Object value2) throws AnalyticsException {
        int compareInt;
        switch (type) {
            case STRING:
                compareInt = ((String) value1).compareTo(((String) value2));
                break;
            case INTEGER:
                compareInt = Integer.compare((Integer) value1, (Integer) value2);
                break;
            case LONG:
                compareInt = Long.compare((Long) value1, (Long) value2);
                break;
            case FLOAT:
                compareInt = Float.compare((Float) value1, (Float) value2);
                break;
            case DOUBLE:
                compareInt = Double.compare((Double) value1, (Double) value2);
                break;
            case BOOLEAN:
                compareInt = Boolean.compare((Boolean) value1, (Boolean) value2);
                break;
            default:
                throw new AnalyticsException("Cannot sort values of type: " + type);
        }
        return compareInt;
    }

    /**
     * This class represents the sorting logic for the fields in records.
     */
    /*static class RecordComparator implements Comparator<Record>, Serializable {

        private static final long serialVersionUID = -5245292818023129728L;
        private List<SortByField> sortByFields;
        private Map<String, ColumnDefinition> indices;

        public RecordComparator(Map<String, ColumnDefinition> indices,
                                List<SortByField> sortByFields) {
            this.sortByFields = sortByFields;
            this.indices = indices;
        }

        @Override
        public int compare(Record record1, Record record2) {
            int compareInt = 0;
            try {
                for (SortByField sortByField : sortByFields) {
                    Object value1, value2;
                    AnalyticsSchema.ColumnType type;
                    ColumnDefinition columnDefinition = indices.get(sortByField.getFieldName());
                    if (columnDefinition == null) {
                        String fieldName = sortByField.getFieldName();
                        if (fieldName != null && fieldName.equals(AnalyticsDataIndexer.INDEX_INTERNAL_TIMESTAMP_FIELD)) {
                            type = AnalyticsSchema.ColumnType.LONG;
                            value1 = record1.getTimestamp();
                            value2 = record2.getTimestamp();
                        } else {
                            throw new AnalyticsException("Cannot find index information for field: " + fieldName);
                        }
                    } else {
                        type = columnDefinition.getType();
                        value1 = record1.getValue(sortByField.getFieldName());
                        value2 = record2.getValue(sortByField.getFieldName());
                    }
                    compareInt = doCompare(sortByField, value1, value2, type);
                    if (compareInt != 0) {
                        break;
                    }
                }
            } catch (AnalyticsException e) {
                logger.error("Sorting failed, Error while sorting records: " + e.getMessage(), e);
            }
            return compareInt;
        }
    }*/
}
