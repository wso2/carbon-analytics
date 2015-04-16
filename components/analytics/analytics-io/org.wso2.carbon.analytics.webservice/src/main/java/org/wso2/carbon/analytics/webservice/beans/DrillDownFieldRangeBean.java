/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class represents the bean class for containing the drilldown ranges per a field.
 */
public class DrillDownFieldRangeBean implements Serializable {

    private static final long serialVersionUID = -1094086094853006437L;
    private String fieldName;
    private DrillDownRangeBean[] ranges;

    public DrillDownFieldRangeBean() { }
    
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public DrillDownRangeBean[] getRanges() {
        return ranges;
    }

    public void setRanges(DrillDownRangeBean[] ranges) {
        this.ranges = ranges;
    }
}
