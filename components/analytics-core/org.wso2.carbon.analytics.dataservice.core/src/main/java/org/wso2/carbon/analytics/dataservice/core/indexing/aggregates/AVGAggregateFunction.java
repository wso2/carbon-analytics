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

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.Map;

/**
 * This class represents the AVERAGE aggregate which computes average over a record field
 */
public class AVGAggregateFunction implements AggregateFunction {

    private double sum;
    private double count;


    public AVGAggregateFunction(Map<String, Number> optionalParams) {
        //No optional params are passed in initializing
        sum = 0;
        count = 0;
    }

    @Override
    public void process(Number value)
            throws AnalyticsException {
        sum += value.doubleValue();
        count++;
    }

    @Override
    public Number finish() throws AnalyticsException {
        if (count != 0) {
            return sum / count;
        } else {
            throw new AnalyticsException("Cannot compute average, count is zero (Division by Zero!");
        }
    }
}
