
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.dataservice.core.indexing.aggregates;

import org.wso2.carbon.analytics.dataservice.commons.Constants;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * This class represents the FIRST() aggregate which returns the specified field value of the first
 * matching record in a specific group.
 */
public class FirstAggregateFunction implements AggregateFunction {

    private Object firstValue;
    private String[] aggregateFields;

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
        if (firstValue == null) {
            firstValue = ctx.getValue(aggregateFields[0]);
        }
    }

    @Override
    public Object finish() throws AnalyticsException {
        return firstValue;
    }

    @Override
    public String getAggregateName() {
        return Constants.FIRST_AGGREGATE;
    }
}
