/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.spark.admin.dto;

/**
 * DTO which transfers the result of execution query between the admin service and the client.
 */
public class AnalyticsQueryResultDto {

    private String query;

    private String[] columnNames;

    private AnalyticsRowResultDto[] rowsResults;

    public AnalyticsQueryResultDto(String[] columnNames, AnalyticsRowResultDto[] results) {
        this.columnNames = columnNames;
        this.rowsResults = results;
    }

    public AnalyticsQueryResultDto(String query) {
        this.query = query;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public AnalyticsRowResultDto[] getRowsResults() {
        return rowsResults;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
