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
package org.wso2.carbon.analytics.datasource.hbase.rg;

import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.List;

public class HBaseRegionSplitRecordGroup implements RecordGroup {

    private static final long serialVersionUID = 2275709979928947981L;
    private int tenantId;
    private String tableName;
    private byte[] startRow;
    private byte[] endRow;
    private String location;
    private int recordsCount;
    private List<String> columns;

    public HBaseRegionSplitRecordGroup() {
    }

    public HBaseRegionSplitRecordGroup(int tenantId, String tableName, List<String> columns, int recordsCount, byte[] startRow, byte[] endRow, String location) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.startRow = startRow;
        this.endRow = endRow;
        this.location = location;
        this.columns = columns;

        this.recordsCount = recordsCount;
    }

    @Override
    public String[] getLocations() throws AnalyticsException {
        return new String[]{this.location};
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public byte[] getStartRow() {
        return startRow;
    }

    public byte[] getEndRow() {
        return endRow;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

}
