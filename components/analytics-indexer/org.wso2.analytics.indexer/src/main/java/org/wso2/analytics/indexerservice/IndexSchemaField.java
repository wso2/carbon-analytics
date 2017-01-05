package org.wso2.analytics.indexerservice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the fields in the solr schema
 */
public class IndexSchemaField implements Serializable {

    private static final long serialVersionUID = 7243610548183777241L;
    private String fieldName;
    private boolean isStored;
    private boolean isIndexed;
    private String type;
    private Map<String, Object> otherProperties;

    public IndexSchemaField() {
        isStored = false;
        isIndexed = false;
        otherProperties = new HashMap<>();
    }

    public IndexSchemaField(String fieldName, boolean isStored, boolean isIndexed, String type,
                            Map<String, Object> otherProperties) {
        this.fieldName = fieldName;
        this.isStored = isStored;
        this.isIndexed = isIndexed;
        this.type = type;
        this.otherProperties = new HashMap<>();
        this.otherProperties.putAll(otherProperties);
    }


    public IndexSchemaField(IndexSchemaField indexSchemaField) {
        this();
        if (indexSchemaField != null) {
            this.fieldName = indexSchemaField.getFieldName();
            this.isStored = indexSchemaField.isStored();
            this.isIndexed = indexSchemaField.isIndexed();
            this.type = indexSchemaField.getType();
            this.otherProperties = indexSchemaField.getOtherProperties();
        }

    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isStored() {
        return isStored;
    }

    public void setStored(boolean isStored) {
        this.isStored = isStored;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIndexed() {
        return isIndexed;
    }

    public void setIndexed(boolean isIndexed) {
        this.isIndexed = isIndexed;
    }

    public Map<String, Object> getOtherProperties() {
        return new HashMap<>(otherProperties);
    }

    public void setOtherProperties(Map<String, Object> otherProperties) {
        this.otherProperties.putAll(otherProperties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IndexSchemaField that = (IndexSchemaField) o;

        if (isIndexed != that.isIndexed) {
            return false;
        }
        if (isStored != that.isStored) {
            return false;
        }
        if (!fieldName.equals(that.fieldName)) {
            return false;
        }
        if (otherProperties != null ? !otherProperties.equals(that.otherProperties) : that.otherProperties != null) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + (isStored ? 1 : 0);
        result = 31 * result + (isIndexed ? 1 : 0);
        result = 31 * result + type.hashCode();
        result = 31 * result + (otherProperties != null ? otherProperties.hashCode() : 0);
        return result;
    }
}
