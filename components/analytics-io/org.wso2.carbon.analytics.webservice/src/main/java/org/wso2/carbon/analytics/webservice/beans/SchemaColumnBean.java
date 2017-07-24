/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class represents columns in SchemaBean
 */
public class SchemaColumnBean implements Serializable {
    private static final long serialVersionUID = -4156435823075540631L;
    private String columnName;
    private String columnType;
    private boolean isIndex;
    private boolean isScoreParam;
    private boolean isFacet;

    public SchemaColumnBean() {
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


    public boolean isIndex() {
        return isIndex;
    }

    public void setIndex(boolean isIndex) {
        this.isIndex = isIndex;
    }

    public boolean isScoreParam() {
        return isScoreParam;
    }

    public void setScoreParam(boolean isScoreParam) {
        this.isScoreParam = isScoreParam;
    }

    public boolean isFacet() {
        return isFacet;
    }

    public void setFacet(boolean isFacet) {
        this.isFacet = isFacet;
    }
}
