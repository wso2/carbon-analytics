/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.utils.persistence;

import java.util.List;

public class ResultRow {

    private String rowKey;

    // TODO: Convert this in to a hashmap. More efficient to manipulate.
    private List<ResultColumn> columns;

    public ResultRow(String rowKey, List<ResultColumn> columns) {
        this.setRowKey(rowKey);
        this.setColumns(columns);
    }

    public ResultRow() {

    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public List<ResultColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<ResultColumn> columns) {
        this.columns = columns;
    }
}
