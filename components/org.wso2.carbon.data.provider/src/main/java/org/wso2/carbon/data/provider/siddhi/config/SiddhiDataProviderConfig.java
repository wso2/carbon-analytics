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

package org.wso2.carbon.data.provider.siddhi.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.wso2.carbon.data.provider.ProviderConfig;

public class SiddhiDataProviderConfig implements ProviderConfig {

    private String siddhiApp;
    private JsonElement queryData;
    private int publishingInterval;
    private String timeColumns;
    private int currentPage;
    private int pageSize;
    private boolean paginationEnable;


    public SiddhiDataProviderConfig() {
        this.queryData = new JsonParser().parse("{\"queryFunctionImpl\":\"return 'from TRANSACTIONS_TABLE select *'" +
                ";\"}");
        this.siddhiApp = "@store(type=\"rdbms\", datasource=\"SAMPLE_DB\")\n" +
                "define table TRANSACTIONS_TABLE (CREDITCARDNO string, COUNTRY string, TRANSACTION string, AMOUNT int);";
        this.publishingInterval = 5;
        this.timeColumns = "";
        this.currentPage = 0;
        this.pageSize = 0;
        this.paginationEnable = false;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setPaginationEnable(boolean paginationEnable) {
        this.paginationEnable = paginationEnable;
    }

    public JsonElement getQueryData() {
        return queryData;
    }

    public void setQueryData(JsonElement queryData) {
        this.queryData = queryData;
    }

    public String getSiddhiAppContext() {
        return siddhiApp;
    }

    public void setSiddhiAppContext(String siddhiAppContext) {
        this.siddhiApp = siddhiAppContext;
    }

    @Override
    public long getPublishingInterval() {
        return this.publishingInterval;
    }

    @Override
    public long getPurgingInterval() {
        return 0;
    }

    @Override
    public boolean isPurgingEnable() {
        return false;
    }

    @Override
    public boolean isPaginationEnable() {
        return paginationEnable;
    }

    public String getTimeColumns() {
        return timeColumns;
    }
}
