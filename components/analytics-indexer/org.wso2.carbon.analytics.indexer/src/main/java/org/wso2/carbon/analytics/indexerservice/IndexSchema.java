package org.wso2.carbon.analytics.indexerservice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the Solr schema.
 */
public class IndexSchema implements Serializable {

    private static final long serialVersionUID = 4547647202218007934L;
    private String uniqueKey;
    private String defaultSearchField;
    private Map<String, IndexSchemaField> fields;

    public static final String TYPE_STRING = "string";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_INT = "int";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_BOOLEAN = "boolean";

    /**
     * This constructor will only be called by the deserializer
     */
    public IndexSchema() {
        fields = new HashMap<>();
    }

    public IndexSchema(String uniqueKey, String defaultSearchField,
                       Map<String, IndexSchemaField> fields) {
        this.fields = new HashMap<>();
        this.uniqueKey = uniqueKey;
        this.defaultSearchField = defaultSearchField;
        this.fields.putAll(fields);
    }

    public String getUniqueKey() {
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

    public Map<String, IndexSchemaField> getFields() {

        return new HashMap<>(fields);
    }

    public void setFields(Map<String, IndexSchemaField> fields) {
        this.fields = new HashMap<>(fields);
    }

    public void addField(String name, IndexSchemaField indexSchemaField) {
        IndexSchemaField field = new IndexSchemaField(indexSchemaField);
        fields.put(name, field);
    }

    public IndexSchemaField getField(String fieldName) {
        if (fields.get(fieldName) != null) {
            return new IndexSchemaField(fields.get(fieldName));
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IndexSchema)) {
            return false;
        }

        IndexSchema that = (IndexSchema) o;

        if (defaultSearchField != null ? !defaultSearchField.equals(that.defaultSearchField) : that.defaultSearchField != null) {
            return false;
        }
        if (fields != null ? !fields.equals(that.fields) : that.fields != null) {
            return false;
        }
        if (uniqueKey != null ? !uniqueKey.equals(that.uniqueKey) : that.uniqueKey != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uniqueKey != null ? uniqueKey.hashCode() : 0;
        result = 31 * result + (defaultSearchField != null ? defaultSearchField.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }
}
