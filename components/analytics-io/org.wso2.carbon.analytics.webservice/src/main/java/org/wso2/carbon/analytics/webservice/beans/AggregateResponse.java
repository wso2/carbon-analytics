/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class represents bean class containing arrays of aggregated recordbeans
 */
public class AggregateResponse implements Serializable{

    private static final long serialVersionUID = -8116265543465777004L;
    private String tableName;
    private RecordBean[] recordBeans;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public RecordBean[] getRecordBeans() {
        return recordBeans;
    }

    public void setRecordBeans(RecordBean[] recordBeans) {
        this.recordBeans = recordBeans;
    }
}
