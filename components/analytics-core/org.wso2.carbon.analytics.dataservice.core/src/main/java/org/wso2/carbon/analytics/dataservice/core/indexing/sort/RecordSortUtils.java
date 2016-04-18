
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
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Util class which contains the methods used to sort the records
 */
public class RecordSortUtils {

    private static Log logger = LogFactory.getLog(RecordSortUtils.class);

    public static List<SearchResultEntry> getSortedSearchResultEntries(int tenantId, String tableName, List<SortByField> sortByFields,
            Map<String, ColumnDefinition> indices, AnalyticsDataService ads, List<SearchResultEntry> unsortedResults)
            throws AnalyticsIndexException {
        if (sortByFields != null && !sortByFields.isEmpty()) {
            List<String> ids = new ArrayList<>();
            Map<String, SearchResultEntry> unsortedSearchResultEntries = new HashMap<>();
            for (SearchResultEntry searchResultEntry : unsortedResults) {
                ids.add(searchResultEntry.getId());
                unsortedSearchResultEntries.put(searchResultEntry.getId(), searchResultEntry);
            }
            try {
                AnalyticsDataResponse response = ads.get(tenantId, tableName, 1, null, ids);
                List<Record> records = getSortedList(AnalyticsDataServiceUtils.listRecords(ads, response), indices, sortByFields);
                List<SearchResultEntry> sortedSearchResultEntries = new ArrayList<>();
                for (Record record : records) {
                    sortedSearchResultEntries.add(unsortedSearchResultEntries.get(record.getId()));
                }

                return sortedSearchResultEntries;
            } catch (AnalyticsException e) {
                throw new AnalyticsIndexException("Error while sorting search results: " + e.getMessage(), e);
            }
        } else {
            List<SearchResultEntry> sortedSearchResultEntries = new ArrayList<>(unsortedResults);
            Collections.sort(sortedSearchResultEntries);
            Collections.reverse(sortedSearchResultEntries);
            return sortedSearchResultEntries;
        }
    }

    private static List<Record> getSortedList(List<Record> records, Map<String, ColumnDefinition> indices,
                                             List<SortByField> sortByFields) {
        List<Record> tempRecords = new ArrayList<>(records);
        Collections.sort(tempRecords, new RecordComparator(indices, sortByFields));
        return tempRecords;
    }

    static class RecordComparator implements Comparator<Record> {
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
                for (int i = 0; i < sortByFields.size(); i++) {
                    SortByField sortByField = sortByFields.get(i);
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
                    //TODO: do we need a "reversed" ?
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
    }

    private static int doCompare(SortByField sortByField, Object value1,
                                 Object value2, AnalyticsSchema.ColumnType type)
            throws AnalyticsException {
        int compareInt = 0;
        if (value1 != null && value2 != null) {
            if (sortByField.getSort() == SORT.ASC && !sortByField.isReversed()) {
                compareInt = compareValues(type, value1, value2);
            } else if (sortByField.getSort() == SORT.ASC && sortByField.isReversed()){
                //value1 and value 2 is interchanged to sort the results in reversed order
                compareInt = compareValues(type, value2, value1);
            } else if (sortByField.getSort() != SORT.ASC && !sortByField.isReversed()){
                //value1 and value 2 is interchanged to sort the results in reversed order,
                //because relevance and descending order both work in the same way
                compareInt = compareValues(type, value2, value1);
            } else if (sortByField.getSort() != SORT.ASC && sortByField.isReversed()){
                compareInt = compareValues(type, value1, value2);
            }
        } else {
            throw new AnalyticsException("Cannot find a field name called: " + sortByField.getFieldName());
        }
        return compareInt;
    }

    private static int compareValues(AnalyticsSchema.ColumnType type, Object value1,
                                     Object value2) throws AnalyticsException {
        int compareInt = 0;
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
            case FACET:
                compareInt = ((String) value1).compareTo(((String) value2));
                break;
            default:
                throw new AnalyticsException("Cannot sort values of type: " + type);
        }
        return compareInt;
    }

}
