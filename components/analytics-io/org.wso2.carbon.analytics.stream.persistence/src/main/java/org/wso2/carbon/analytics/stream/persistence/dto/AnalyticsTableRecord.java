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
 * This class represent single analytics table column
 */
public class AnalyticsTableRecord implements Serializable {
    private static final long serialVersionUID = 3291248719712541000L;

    private String columnName;
    private String columnType;
    private boolean scoreParam;
    private boolean indexed;
    private boolean primaryKey;
    private boolean persist;
    private boolean isFacet;

    public AnalyticsTableRecord() {
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public boolean isScoreParam() {
        return scoreParam;
    }

    public void setScoreParam(boolean scoreParam) {
        this.scoreParam = scoreParam;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isPersist() {
        return persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    public boolean isFacet() {
        return isFacet;
    }

    public void setFacet(boolean isFacet) {
        this.isFacet = isFacet;
    }
}
