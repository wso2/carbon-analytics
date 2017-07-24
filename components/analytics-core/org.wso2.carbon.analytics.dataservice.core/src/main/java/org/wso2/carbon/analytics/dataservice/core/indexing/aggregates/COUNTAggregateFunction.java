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
 * This class represents the COUNT aggregate which COUNT the records
 */
public class COUNTAggregateFunction implements AggregateFunction {

    private long count;

    public COUNTAggregateFunction() {
        count = 0;
    }

    @Override
    public void setAggregateFields(String[] aggregateFields) throws AnalyticsException {
        //count aggregate does not require any record field, since it is just counting
    }

    @Override
    public void process(RecordContext ctx)
            throws AnalyticsException {
            count++;
    }

    @Override
    public Number finish() throws AnalyticsException {
        return count;
    }

    @Override
    public String getAggregateName() {
        return Constants.COUNT_AGGREGATE;
    }
}
