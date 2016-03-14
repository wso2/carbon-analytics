package org.wso2.carbon.analytics.dataservice.core.indexing.sort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
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
            throws AnalyticsException {
        List<String> ids = new ArrayList<>();
        Map<String, SearchResultEntry> unsortedSearchResultEntries = new HashMap<>();
        for (SearchResultEntry searchResultEntry : unsortedResults) {
            ids.add(searchResultEntry.getId());
            unsortedSearchResultEntries.put(searchResultEntry.getId(), searchResultEntry);
        }
        AnalyticsDataResponse response = ads.get(tenantId, tableName, 1, null, ids);
        List<Record> records = getSortedList(AnalyticsDataServiceUtils.listRecords(ads, response), indices, sortByFields);
        List<SearchResultEntry> sortedSearchResultEntries = new ArrayList<>();
        for (Record record : records) {
            sortedSearchResultEntries.add(unsortedSearchResultEntries.get(record.getId()));
        }

        return sortedSearchResultEntries;
    }

    public static List<Record> getSortedList(List<Record> records, Map<String, ColumnDefinition> indices,
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
            int sortByFieldIndex = 0;
            return compare(record1, record2, sortByFieldIndex);
        }

        private int compare(Record record1, Record record2, int sortByFieldIndex) {
            SortByField sortByField = sortByFields.get(sortByFieldIndex);
            AnalyticsSchema.ColumnType type = indices.get(sortByField.getFieldName()).getType();
            Object value1 = record1.getValue(sortByField.getFieldName());
            Object value2 = record2.getValue(sortByField.getFieldName());
            int compareInt = 0;
            try {
                if (value1 != null && value2 != null) {
                    switch (type) {
                        case STRING:
                            compareInt = ((String) value1).compareTo(((String)value2));
                            break;
                        case INTEGER:
                            compareInt = Integer.compare((Integer)value1, (Integer)value2);
                            break;
                        case LONG:
                            compareInt = Long.compare((Long)value1, (Long)value2);
                            break;
                        case FLOAT:
                            compareInt = Float.compare((Float)value1, (Float)value2);
                            break;
                        case DOUBLE:
                            compareInt = Double.compare((Double)value1, (Double)value2);
                            break;
                        case BOOLEAN:
                            compareInt = Boolean.compare((Boolean)value1, (Boolean)value2);
                            break;
                        case FACET:
                            compareInt = ((String) value1).compareTo(((String)value2));
                        default:
                            throw new AnalyticsException("Cannot sort values of type: " + type);
                    }
                    if (compareInt == 0) {
                        return compare(record1, record2, sortByFieldIndex++);
                    }
                } else {
                    throw new AnalyticsException("Cannot find a field name called: " + sortByField.getFieldName());
                }
            } catch (AnalyticsException e) {
                logger.error("Sorting failed, Error while sorting records: " + e.getMessage(), e);
            }
            return compareInt;
        }
    }

}
