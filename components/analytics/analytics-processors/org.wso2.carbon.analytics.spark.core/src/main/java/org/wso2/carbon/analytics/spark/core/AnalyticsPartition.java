/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.spark.Partition;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;

/**
 * Spark analytics partition implementation, based on {@link RecordGroup}.
 */
public class AnalyticsPartition implements Partition, Serializable {
    
    private static final long serialVersionUID = -5286081735740660738L;
    
    private RecordGroup recordGroup;
    
    public AnalyticsPartition(RecordGroup recordGroup) {
        this.recordGroup = recordGroup;
    }
    
    public RecordGroup getRecordGroup() {
        return recordGroup;
    }
    
    @Override
    public int index() {
        try {
            return Arrays.asList(this.recordGroup.getLocations()).hashCode();
        } catch (AnalyticsException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int hashCode() {
        return this.recordGroup.hashCode();
    }
    
}
