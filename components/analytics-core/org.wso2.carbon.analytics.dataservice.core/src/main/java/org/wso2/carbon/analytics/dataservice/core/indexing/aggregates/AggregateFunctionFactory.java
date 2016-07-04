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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.Constants;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the factory class for each aggregate function
 */
public class AggregateFunctionFactory {
    private Map<String, Class<? extends AggregateFunction>> aggregateFunctions;
    private Log logger = LogFactory.getLog(AggregateFunctionFactory.class);
    public AggregateFunctionFactory() {
        this.aggregateFunctions = new HashMap<>();
        //default Aggregates
        this.aggregateFunctions.put(Constants.AVG_AGGREGATE, AVGAggregateFunction.class);
        this.aggregateFunctions.put(Constants.SUM_AGGREGATE, SUMAggregateFunction.class);
        this.aggregateFunctions.put(Constants.MAX_AGGREGATE, MAXAggregateFunction.class);
        this.aggregateFunctions.put(Constants.MIN_AGGREGATE, MINAggregateFunction.class);
        this.aggregateFunctions.put(Constants.COUNT_AGGREGATE, COUNTAggregateFunction.class);
        this.aggregateFunctions.put(Constants.FIRST_AGGREGATE, FirstAggregateFunction.class);

        //Aggregates installed as OSGI Components
        if (!AnalyticsServiceHolder.getAggregateFunctions().isEmpty()) {
            for (Map.Entry<String, Class<? extends AggregateFunction>> entry : AnalyticsServiceHolder.getAggregateFunctions().entrySet()) {
                this.aggregateFunctions.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public AggregateFunction create(String type, String[] aggregateFunctionVariables) throws AnalyticsException {
        Class<?> aggregateClass =  this.aggregateFunctions.get(type);
        AggregateFunction aggregateFunction;
        if (aggregateClass != null) {
            try {
                Constructor<?> aggregateFuncConstructor = aggregateClass.getConstructor();
                //AggregateFunction classes should have a constructor with a Map<String, Number>
                aggregateFunction = (AggregateFunction) aggregateFuncConstructor.newInstance();
                aggregateFunction.setAggregateFields(aggregateFunctionVariables);
            } catch (Exception e) {
                logger.error("error while creating aggregateFunction," + e.getMessage(), e);
                throw new AnalyticsException("Error while creating aggregateFunction, " + e.getMessage(), e);
            }
        } else {
            logger.error("Failed to find the aggregateFunction: " + type);
            throw new AnalyticsException("Cannot find the aggregateFunction: " + type);
        }
        return aggregateFunction;
    }
}
