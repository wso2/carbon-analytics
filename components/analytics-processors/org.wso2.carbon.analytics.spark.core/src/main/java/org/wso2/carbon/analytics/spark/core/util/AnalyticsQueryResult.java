/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.spark.core.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * This class represents an analytics query result.
 */
public class AnalyticsQueryResult implements Serializable {

    private static final long serialVersionUID = -4154776904892662809L;

    private String[] columns;
    
    private List<List<Object>> rows;
    
    public AnalyticsQueryResult() { }
    
    public AnalyticsQueryResult(String[] columns, List<List<Object>> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public String[] getColumns(){
        return columns;
    }
    
    public List<List<Object>> getRows() {
        return rows;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String column : this.getColumns()) {
            builder.append("|" + column + "|\t");
        }
        builder.append("\n");
        for (List<Object> row : this.getRows()) {
            for (Object obj : row) {
                builder.append(obj + "\t");
            }
            builder.append("\n");
        }
        builder.append("\n");
        return builder.toString();
    }

    public static AnalyticsQueryResult emptyAnalyticsQueryResult(){
        return new AnalyticsQueryResult(new String[0], Collections.<List<Object>>emptyList());
    }
}
