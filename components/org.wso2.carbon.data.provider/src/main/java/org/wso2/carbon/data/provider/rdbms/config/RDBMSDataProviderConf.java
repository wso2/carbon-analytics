/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.wso2.carbon.data.provider.rdbms.config;

import org.wso2.carbon.data.provider.ProviderConfig;

/**
 * Model class for the RDBMS provider configurations.
 */
public class RDBMSDataProviderConf implements ProviderConfig {
    private String datasourceName;
    private String query;
    private String tableName;
    private String incrementalColumn;
    private String timeColumns;
    private long publishingInterval;
    private long purgingInterval;
    private long publishingLimit;
    private long purgingLimit;
    private boolean isPurgingEnable;

    public RDBMSDataProviderConf() {
        this.publishingInterval = 1;
        this.purgingInterval = 60;
        this.isPurgingEnable = false;
        this.publishingLimit = 30;
        this.purgingLimit = 30;
        this.datasourceName = "";
        this.query = "";
        this.tableName = "";
        this.incrementalColumn = "";
        this.timeColumns = "";
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public String getQuery() {
        return query;
    }

    public String getTableName() {
        return tableName;
    }

    public String getIncrementalColumn() {
        return incrementalColumn;
    }

    public long getPublishingLimit() {
        return publishingLimit;
    }

    public long getPurgingLimit() {
        return purgingLimit;
    }

    public String getTimeColumns() {
        return timeColumns;
    }

    @Override
    public long getPublishingInterval() {
        return publishingInterval;
    }

    @Override
    public long getPurgingInterval() {
        return purgingInterval;
    }

    @Override
    public boolean isPurgingEnable() {
        return isPurgingEnable;
    }
}
