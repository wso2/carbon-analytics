package org.wso2.carbon.analytics.webservice.beans;

/**
 * This class represents the primary key values batch
 */
public class ValuesBatchBean {
    private RecordValueEntryBean[] keyValues;

    public ValuesBatchBean() {
    }

    public RecordValueEntryBean[] getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(RecordValueEntryBean[] keyValues) {
        this.keyValues = keyValues;
    }
}
