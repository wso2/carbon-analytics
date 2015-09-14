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

import java.util.Map;

/**
 * This class represents the COUNT aggregate which COUNT the records
 */
public class COUNTAggregateFunction implements AggregateFunction {

    private Number count;

    public COUNTAggregateFunction(Map<String, Number> optionalParams) {
        count = optionalParams.get(Constants.AggregateOptionalParams.COUNT);
        if (count == null) {
            count = 0;
        }
    }

    @Override
    public void process(Number value)
            throws AnalyticsException {
        //no specific processing
    }

    @Override
    public Number finish() throws AnalyticsException {
        return count;
    }
}
