package org.wso2.carbon.analytics.dataservice.commons;

import java.io.Serializable;

/**
 * This class represents an object which is used to sort the result by any given field name in a given
 * order.
 *
 */
public class SortByField implements Serializable {

    private static final long serialVersionUID = 7743069351742987010L;
    private String fieldName;
    private SORT sort;
    private boolean reversed;

    public SortByField(String field, SORT sort, boolean reversed) {
        this.fieldName = field;
        this.sort = sort;
        this.reversed = reversed;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SORT getSort() {
        return sort;
    }

    public void setSort(SORT sort) {
        this.sort = sort;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
}
