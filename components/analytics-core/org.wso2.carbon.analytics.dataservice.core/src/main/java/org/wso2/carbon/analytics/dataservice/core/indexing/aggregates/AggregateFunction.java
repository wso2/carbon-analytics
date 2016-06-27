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

/**
 * This interface is used to implement custom aggregates over the record fields.
 */
public interface AggregateFunction {

    /**
     * This method needs to be called before the process method is called. Use this method to
     * set the record fields which will be used to calculate the aggregated value. The order of the
     * record fields matters since it is used in process() method, in the given order.
     * @param aggregateFields Fields which will be used to calculate the aggregated value
     * @throws AnalyticsException
     */
    public void setAggregateFields(String[] aggregateFields) throws AnalyticsException;

    /**
     * Perform aggregation with the current record values.
     * @param ctx Keeps the record values of the current record.
     * @throws AnalyticsException
     */
    public void process(RecordContext ctx) throws AnalyticsException;

    /**
     * Returns the aggregated value.
     * @return The aggregated value
     * @throws AnalyticsException
     */
    public Object finish() throws AnalyticsException;

    /**
     * Returns the name of the Aggregate Function
     * @return The name of the Aggregate function.
     */
    public String getAggregateName();
}
