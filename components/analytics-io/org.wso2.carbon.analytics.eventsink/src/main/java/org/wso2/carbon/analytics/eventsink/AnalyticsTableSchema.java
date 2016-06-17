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
package org.wso2.carbon.analytics.eventsink;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * JAXB configuration class for the analytics table schema for the deployer.
 */

public class AnalyticsTableSchema {
    private List<Column> columns;

    @XmlElement(name = "ColumnDefinition")
    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }


    public static class Column {
        private String columnName;
        private AnalyticsSchema.ColumnType type;
        private boolean scoreParam = false;
        private boolean indexed = false;
        private boolean isFacet = false;
        private boolean primaryKey = false;

        @XmlElement(name = "Name")
        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        @XmlElement(name = "Type")
        public AnalyticsSchema.ColumnType getType() {
            return type;
        }

        public void setType(AnalyticsSchema.ColumnType type) {
            this.type = type;
        }

        @XmlElement(name = "EnableScoreParam")
        public boolean isScoreParam() {
            return scoreParam;
        }

        public void setScoreParam(boolean scoreParam) {
            this.scoreParam = scoreParam;
        }

        @XmlElement(name = "EnableIndexing")
        public boolean isIndexed() {
            return indexed;
        }

        public void setIndexed(boolean indexed) {
            this.indexed = indexed;
        }

        @XmlElement(name = "IsFacet")
        public boolean isFacet() {
            return isFacet;
        }

        public void setFacet(boolean isFacet) {
            this.isFacet = isFacet;
        }

        @XmlElement(name = "IsPrimaryKey")
        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }

        public String getHash() {
            return new StringBuilder()
                    .append(this.scoreParam ? "1" : "0")
                    .append(this.indexed ? "1" : "0")
                    .append(this.isFacet ? "1" : "0")
                    .append(this.primaryKey ? "1" : "0").toString();

        }
    }
}
