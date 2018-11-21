/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stream.processor.core.persistence.beans;

import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.database.query.manager.config.Queries;

import java.util.List;

public class PersistenceStoreConfigs {
    private String location = "siddhi-app-persistence";
    private String datasource;
    private String table = "PERSISTENCE_TABLE";
    @Element(description = "Database query map")
    private List<Queries> queries;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<Queries> getQueries() {
        return queries;
    }

    public void setQueries(List<Queries> queries) {
        this.queries = queries;
    }
}
