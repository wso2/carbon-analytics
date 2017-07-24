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
package org.wso2.carbon.analytics.spark.core.sources;

import org.apache.spark.Partition;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;

import java.io.Serializable;

/**
 * Spark analytics partition implementation, based on {@link RecordGroup}.
 */
public class AnalyticsPartition implements Partition, Serializable {
    
    private static final long serialVersionUID = -5286081735740660738L;
    
    private String recordStoreName;
    
    private RecordGroup recordGroup;
    
    private int index;
    
    public AnalyticsPartition(String recordStoreName, RecordGroup recordGroup, int index) {
        this.recordStoreName = recordStoreName;
        this.recordGroup = recordGroup;
        this.index = index;
    }
    
    public String getRecordStoreName() {
        return recordStoreName;
    }
    
    public RecordGroup getRecordGroup() {
        return recordGroup;
    }
    
    @Override
    public int index() {
        return this.index;
    }

    @Override
    public int hashCode() {
        return this.recordGroup.hashCode();
    }
    
    @Override
    public boolean equals(Object rhs) {
        if (!(rhs instanceof AnalyticsPartition)) {
            return false;
        }
        AnalyticsPartition part = (AnalyticsPartition) rhs;
        return this.index == part.index && this.recordGroup.equals(part.getRecordGroup());
    }
    
}
