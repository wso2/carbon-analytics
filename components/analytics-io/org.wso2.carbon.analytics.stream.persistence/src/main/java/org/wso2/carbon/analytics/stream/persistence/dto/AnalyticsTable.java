/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.stream.persistence.dto;

import java.io.Serializable;

/**
 * This class represent Analytics Table
 */
public class AnalyticsTable implements Serializable {
    private static final long serialVersionUID = 2451467215330903630L;

    private String tableName;
    private String streamVersion;
    private String recordStoreName;
    private boolean persist;
    private boolean mergeSchema;
    private AnalyticsTableRecord[] analyticsTableRecords;

    public AnalyticsTable() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getStreamVersion() {
        return streamVersion;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
    }

    public AnalyticsTableRecord[] getAnalyticsTableRecords() {
        return analyticsTableRecords;
    }

    public String getRecordStoreName() {
        return recordStoreName;
    }

    public void setRecordStoreName(String recordStoreName) {
        this.recordStoreName = recordStoreName;
    }

    public boolean isPersist() {
        return persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    public void setAnalyticsTableRecords(AnalyticsTableRecord[] analyticsTableRecords) {
        this.analyticsTableRecords = analyticsTableRecords;
    }

    public boolean isMergeSchema() {
        return mergeSchema;
    }

    public void setMergeSchema(boolean isMergeSchema) {
        this.mergeSchema = isMergeSchema;
    }
}
