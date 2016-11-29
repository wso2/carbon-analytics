package org.wso2.analytics.indexerservice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the fields in the solr schema
 */
public class IndexField implements Serializable {

    private static final long serialVersionUID = 7243610548183777241L;
    private String fieldName;
    private boolean isStored;
    private boolean isIndexed;
    private String type;
    private Map<String, Object> otherProperties;

    public IndexField() {
        isStored = false;
        isIndexed = false;
        otherProperties = new HashMap<>();
    }

    public IndexField(String fieldName, boolean isStored, boolean isIndexed, String type,
                      Map<String, Object> otherProperties) {
        this.fieldName = fieldName;
        this.isStored = isStored;
        this.isIndexed = isIndexed;
        this.type = type;
        this.otherProperties = new HashMap<>();
        this.otherProperties.putAll(otherProperties);
    }


    public IndexField(IndexField indexField) {
        this.fieldName = indexField.getFieldName();
        this.isStored = indexField.isStored();
        this.isIndexed = indexField.isIndexed();
        this.type = indexField.getType();

        this.otherProperties = indexField.getOtherProperties();
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
}
