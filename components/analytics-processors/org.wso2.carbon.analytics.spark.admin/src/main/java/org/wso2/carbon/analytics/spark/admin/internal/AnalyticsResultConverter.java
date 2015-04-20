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
package org.wso2.carbon.analytics.spark.admin.internal;

import org.wso2.carbon.analytics.spark.admin.dto.AnalyticsQueryResultDto;
import org.wso2.carbon.analytics.spark.admin.dto.AnalyticsRowResultDto;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;

import java.util.List;

public class AnalyticsResultConverter {

    public static AnalyticsQueryResultDto convertResults(AnalyticsQueryResult analyticsQueryResult) {
        if (analyticsQueryResult != null) {
            List<List<Object>> rows = analyticsQueryResult.getRows();
            AnalyticsRowResultDto[] rowResults = new AnalyticsRowResultDto[rows.size()];
            int rowIndex = 0;
            for (List<Object> row : rows) {
                String[] columnValues = new String[row.size()];
                int colIndex = 0;
                for (Object aColValue : row) {
                    if (aColValue != null) {
                        columnValues[colIndex] = aColValue.toString();
                    }
                    colIndex++;
                }
                rowResults[rowIndex] = new AnalyticsRowResultDto(columnValues);
                rowIndex++;
            }
            return new AnalyticsQueryResultDto(analyticsQueryResult.getColumns(), rowResults);
        } else {
            return null;
        }
    }
}
