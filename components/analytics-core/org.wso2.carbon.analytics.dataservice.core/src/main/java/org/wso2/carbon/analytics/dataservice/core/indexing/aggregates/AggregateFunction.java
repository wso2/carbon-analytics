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
     * Perform aggregation with the current record values.
     * @param ctx Keeps the record values of the current record.
     * @param aggregateFields user-defined array of fields which will be used for aggregation
     * @throws AnalyticsException
     */
    public void process(RecordValuesContext ctx, String[] aggregateFields) throws AnalyticsException;
    public Object finish() throws AnalyticsException;
    public String getAggregateName();
}
