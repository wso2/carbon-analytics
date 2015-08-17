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

package org.wso2.carbon.analytics.dataservice.indexing.aggregates;

import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.Iterator;
import java.util.Map;

/**
 * This class represents the MIN aggregate which returns the minimum value of a record field.
 */
public class MINAggregate implements Aggregate {
    @Override
    public Object aggregate(Iterator<Record> iterator, String fieldName, Map<String, Object> params)
            throws AnalyticsException {
        double min = Double.MAX_VALUE;
        while (iterator.hasNext()) {
            double value = ((Number) iterator.next().getValue(fieldName)).doubleValue();
            if (min > value) {
                min = value;
            }
        }
        return min;
    }
}
