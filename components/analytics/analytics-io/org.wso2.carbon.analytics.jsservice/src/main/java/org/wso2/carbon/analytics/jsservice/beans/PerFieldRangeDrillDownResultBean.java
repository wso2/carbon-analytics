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

package org.wso2.carbon.analytics.jsservice.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * This class represents the drilldown results of a single  numeric field
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PerFieldRangeDrillDownResultBean {

    @XmlElement(name = "fieldName")
    private String fieldName;
    @XmlElement(name = "ranges")
    private List<PerRangeDrillDownResultBean> perRangeResult;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<PerRangeDrillDownResultBean> getPerRangeResult() {
        return perRangeResult;
    }

    public void setPerRangeResult(List<PerRangeDrillDownResultBean> perRangeResult) {
        this.perRangeResult = perRangeResult;
    }
}
