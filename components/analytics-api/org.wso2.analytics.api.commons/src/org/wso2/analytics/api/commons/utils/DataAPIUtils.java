package org.wso2.analytics.api.commons.utils;

import org.wso2.analytics.data.commons.sources.Record;
import org.wso2.analytics.indexerservice.CarbonIndexDocument;
import org.wso2.analytics.indexerservice.IndexSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains the static Utility methods required by the Data API
 */
public class DataAPIUtils {

    private static final String FIELD_TIMESTAMP = "_timestamp";
    private static final String FIELD_ID = "id";

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
        indexDocument.addField(FIELD_ID, record.getId());
        indexDocument.addField(FIELD_TIMESTAMP, record.getTimestamp());
        return indexDocument;
    }

    public static String createTimeRangeQuery(long fromTime, long toTime) {
        return FIELD_TIMESTAMP.concat(":[" + fromTime + "TO " + toTime + "]");
    }
}
