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
package org.wso2.carbon.analytics.dataservice.config;

import org.wso2.carbon.analytics.dataservice.commons.IndexType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "AnalyticsIndexConfiguration")
public class AnalyticsIndexConfiguration {
    private List<AnalyticsIndexColumn> indexColumns;
    private List<String> scoreParams;

    @XmlElement(name = "IndexColumns")
    public List<AnalyticsIndexColumn> getIndexColumns() {
        return indexColumns;
    }

    @XmlElement(name = "ScoreParams")
    public List<String> getScoreParams() {
        return scoreParams;
    }

    public void setScoreParams(List<String> scoreParams) {
        this.scoreParams = scoreParams;
    }

    public void setIndexColumns(List<AnalyticsIndexColumn> indexColumns) {
        this.indexColumns = indexColumns;
    }

    public Map<String, IndexType> getIndexColumnsMap(){
        Map<String, IndexType> indexColMap = new HashMap<>();
        if (indexColumns != null){
            for (AnalyticsIndexColumn indexColumn: indexColumns){
                indexColMap.put(indexColumn.columnName, indexColumn.getIndexType());
            }
        }
        return indexColMap;
    }

    @XmlRootElement(name = "IndexColumn")
    public static class AnalyticsIndexColumn {
        private String columnName;
        private IndexType indexType;

        @XmlElement(name = "ColumnName")
        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        @XmlElement(name = "IndexType")
        public IndexType getIndexType() {
            return indexType;
        }

        public void setIndexType(IndexType indexType) {
            this.indexType = indexType;
        }
    }
}
