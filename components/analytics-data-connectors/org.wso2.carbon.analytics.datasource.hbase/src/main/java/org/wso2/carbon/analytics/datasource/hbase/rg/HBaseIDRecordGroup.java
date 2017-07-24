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

public class HBaseIDRecordGroup implements RecordGroup {

    private static final long serialVersionUID = -478424013928152032L;
    private int tenantId;
    private String tableName;
    private List<String> columns;
    private List<String> ids;

    @Override
    public String[] getLocations() throws AnalyticsException {
        return new String[] { "localhost" };
    }

    public HBaseIDRecordGroup() { }
    
    public HBaseIDRecordGroup(int tenantId, String tableName, List<String> columns, List<String> ids) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.columns = columns;
        this.ids = ids;
    }

    public int getTenantId() {
        return this.tenantId;
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<String> getColumns() {
        return this.columns;
    }

    public List<String> getIds() {
        return this.ids;
    }

}
