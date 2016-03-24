package org.wso2.carbon.analytics.dataservice.core.indexing.aggregates;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the values of the record being processed in the aggregate Function
 */
public class RecordContext {
    private Map<String, Object> values;

    private RecordContext(Map<String, Object> recordValues) {
        if (recordValues == null) {
            this.values = new HashMap<>();
        } else {
            this.values = recordValues;
        }
    }

    public static RecordContext create(Map<String, Object> recordValues) {
        return new RecordContext(recordValues);
    }

    public Object getValue(String fieldName) {
        return values.get(fieldName);
    }
}
