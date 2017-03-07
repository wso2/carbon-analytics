package org.wso2.carbon.analytics.api.commons.utils;

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.api.commons.AnalyticsDataAPI;
import org.wso2.carbon.analytics.data.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.data.commons.service.AnalyticsDataResponse;
import org.wso2.carbon.analytics.data.commons.sources.Record;
import org.wso2.carbon.analytics.indexerservice.CarbonIndexDocument;
import org.wso2.carbon.analytics.indexerservice.IndexSchema;
import org.wso2.carbon.analytics.indexerservice.IndexSchemaField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains the static Utility methods required by the Data API
 */
public class DataAPIUtils {

    public static List<CarbonIndexDocument> getIndexDocuments(List<Record> records, IndexSchema schema) {
        List<CarbonIndexDocument> indexDocuments = new ArrayList<>();
        if (schema != null && !schema.getFields().isEmpty()) {
            for (Record record : records) {
                CarbonIndexDocument indexDocument = getIndexDocument(record, schema);
                indexDocuments.add(indexDocument);
            }
        }
        return indexDocuments;
    }

    public static CarbonIndexDocument getIndexDocument(Record record, IndexSchema schema) {
        Collection<String> fields = schema.getFields().keySet();
        CarbonIndexDocument indexDocument = new CarbonIndexDocument();
        for (String field : fields) {
            Object value = record.getValue(field);
            if (value != null) {
                indexDocument.addField(field, value);
            }
        }
        indexDocument.addField(IndexSchemaField.FIELD_ID, record.getId());
        indexDocument.addField(IndexSchemaField.FIELD_TIMESTAMP, record.getTimestamp());
        return indexDocument;
    }

    public static String createTimeRangeQuery(long fromTime, long toTime) {
        return IndexSchemaField.FIELD_TIMESTAMP.concat(":[" + fromTime + "TO " + toTime + "]");
    }

    public static List<Record> listRecords(AnalyticsDataAPI ads,
                                           AnalyticsDataResponse response) throws
                                                                           AnalyticsException {
        List<Record> result = new ArrayList<>();
        for (AnalyticsDataResponse.Entry entry : response.getEntries()) {
            result.addAll(IteratorUtils.toList(ads.readRecords(entry.getRecordStoreName(), entry.getRecordGroup())));
        }
        return result;
    }
}
