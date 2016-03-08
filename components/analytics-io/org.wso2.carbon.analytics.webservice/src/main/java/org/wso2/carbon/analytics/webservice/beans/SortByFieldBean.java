package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class represents the Webservice bean class for sorting fields information : sorting field,
 * their sort type and whether the order is reversed or not
 */
public class SortByFieldBean implements Serializable {
    private static final long serialVersionUID = -6075564963783989008L;
    private String fieldName;
    private String sortType;
    private boolean reversed;

    public SortByFieldBean() {

    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
}
