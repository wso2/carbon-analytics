/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.analytics.jsservice.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

/**
 * This class is a bean class which represent the primary keys with values and columns
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnKeyValueBean {
    @XmlElement(name = "valueBatches", required = true)
    private List<Map<String, Object>> valueBatches;
    @XmlElement(name = "columns", required = false)
    private List<String> columns;

    public ColumnKeyValueBean() {

    }

    public List<Map<String, Object>> getValueBatches() {
        return valueBatches;
    }

    public List<String> getColumns() {
        return columns;
    }
}
