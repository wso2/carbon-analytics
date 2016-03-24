
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
