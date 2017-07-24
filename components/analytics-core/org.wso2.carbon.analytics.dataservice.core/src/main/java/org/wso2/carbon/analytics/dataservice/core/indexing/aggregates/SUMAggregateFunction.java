/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.analytics.dataservice.core.indexing.aggregates;

import org.wso2.carbon.analytics.dataservice.commons.Constants;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * This class represents the SUM aggregate which performs summation over a record field
 */
public class SUMAggregateFunction implements AggregateFunction {

    private double sum;
    private String[] aggregateFields;

    public SUMAggregateFunction() {
        sum = 0;
    }

    @Override
    public void setAggregateFields(String[] aggregateFields) throws AnalyticsException {
        this.aggregateFields = aggregateFields;
    }

    @Override
    public void process(RecordContext ctx)
            throws AnalyticsException {
        if (aggregateFields == null || aggregateFields.length == 0) {
            throw new AnalyticsException("Field to be aggregated, is missing");
        }
        Object value = ctx.getValue(aggregateFields[0]);
        if (value == null) {
            throw new AnalyticsException("Error while calculating SUM: value of the field, " +
                                         aggregateFields[0] + " is null");
        }
        if (value instanceof Number) {
            sum += ((Number)value).doubleValue();
        } else {
            throw new AnalyticsException("Error while calculating SUM: Value '" + value.toString() +
                                         "', being aggregated is not numeric.");
        }
    }

    @Override
    public Number finish() throws AnalyticsException {
        return sum;
    }

    @Override
    public String getAggregateName() {
        return Constants.SUM_AGGREGATE;
    }
}
