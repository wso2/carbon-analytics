package org.wso2.carbon.analytics.restapi.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the REST API bean class for sorting fields in search APIs
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SortByFieldBean {
    private String field;
    private String sortType;
    private boolean reversed;

    public SortByFieldBean() {

    }

    public SortByFieldBean(String field, String sortType, boolean reversed) {
        this.field = field;
        this.sortType = sortType;
        this.reversed = reversed;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
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
