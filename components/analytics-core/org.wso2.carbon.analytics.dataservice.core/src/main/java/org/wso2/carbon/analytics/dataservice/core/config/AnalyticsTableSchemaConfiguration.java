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
package org.wso2.carbon.analytics.dataservice.core.config;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * JAXB configuration class for the analytics table schema for the Carbon application deployer.
 */
@XmlRootElement(name = "AnalyticsTableSchema")
public class AnalyticsTableSchemaConfiguration {
    private List<AnalyticsColumnDefinition> columnDefinitions;

    @XmlElement(name = "ColumnDefinition")
    public List<AnalyticsColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColumnDefinitions(List<AnalyticsColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    private List<ColumnDefinition> getAnalyticsSchemaColumns() {
       List<ColumnDefinition> schemaColumns = new ArrayList<>();
        if (columnDefinitions != null) {
            for (AnalyticsColumnDefinition schemaColumn : columnDefinitions) {
                ColumnDefinitionExt columnDefinition = new ColumnDefinitionExt();
                columnDefinition.setName(schemaColumn.getColumnName());
                columnDefinition.setIndexed(schemaColumn.isIndexed());
                columnDefinition.setScoreParam(schemaColumn.isScoreParam());
                columnDefinition.setType(schemaColumn.getType());
                columnDefinition.setFacet(schemaColumn.isFacet());
                schemaColumns.add(columnDefinition);
            }
        }
        return schemaColumns;
    }

    private List<String> getPrimaryKeys(){
        List<String> primaryKeys = new ArrayList<>();
        if (columnDefinitions != null){
            for (AnalyticsColumnDefinition schemaColumn: columnDefinitions){
                if (schemaColumn.isPrimaryKey()){
                    primaryKeys.add(schemaColumn.getColumnName());
                }
            }
        }
        return primaryKeys;
    }

    public AnalyticsSchema getAnalyticsSchema() {
        return new AnalyticsSchema(getAnalyticsSchemaColumns(), getPrimaryKeys());
    }

    public static class AnalyticsColumnDefinition {
        private String columnName;
        private AnalyticsSchema.ColumnType type;
        private boolean scoreParam = false;
        private boolean indexed = false;
        private boolean primaryKey = false;
        private boolean isFacet = false;

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

        public void setType(AnalyticsSchema.ColumnType indexType) {
            this.type = indexType;
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

        @XmlElement (name = "IsPrimaryKey")
        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }

        @XmlElement(name = "IsFacet")
        public boolean isFacet() {
            return isFacet;
        }

        public void setFacet(boolean isFacet) {
            this.isFacet = isFacet;
        }
    }
}
