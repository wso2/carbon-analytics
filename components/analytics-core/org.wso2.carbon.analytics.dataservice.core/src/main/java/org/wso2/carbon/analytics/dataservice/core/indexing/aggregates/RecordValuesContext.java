package org.wso2.carbon.analytics.dataservice.core.indexing.aggregates;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the values of the record being processed in the aggregate Function
 */
public class RecordValuesContext {
    private Map<String, Object> values;

    private RecordValuesContext(Map<String, Object> recordValues) {
        if (recordValues == null) {
            this.values = new HashMap<>();
        } else {
            this.values = recordValues;
        }
    }

    public static RecordValuesContext create(Map<String, Object> recordValues) {
        return new RecordValuesContext(recordValues);
    }

    public Object getValue(String fieldName) {
        return values.get(fieldName);
    }
}
