package org.wso2.analytics.indexerservice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the Solr schema
 */
public class IndexSchema implements Serializable {

    private static final long serialVersionUID = 4547647202218007934L;
    private String uniqueKey;
    private String defaultSearchField;
    private Map<String, IndexField> fields;


    public IndexSchema() {
        fields = new HashMap<>();
    }

    public IndexSchema(String uniqueKey, String defaultSearchField,
                       Map<String, IndexField> fields) {
        this();
        this.uniqueKey = uniqueKey;
        this.defaultSearchField = defaultSearchField;
        this.fields.putAll(fields);
    }

    public Object getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getDefaultSearchField() {
        return defaultSearchField;
    }

    public void setDefaultSearchField(String defaultSearchField) {
        this.defaultSearchField = defaultSearchField;
    }

    public Map<String, IndexField> getFields() {
        return new LinkedHashMap<>(fields);
    }

    public void setFields(Map<String, IndexField> fields) {
        this.fields = fields;
    }

    public void setField(String name, IndexField indexField) {
        fields.put(name, indexField);
    }

    public IndexField getField(String fieldName) {
        return fields.get(fieldName);
    }
}
