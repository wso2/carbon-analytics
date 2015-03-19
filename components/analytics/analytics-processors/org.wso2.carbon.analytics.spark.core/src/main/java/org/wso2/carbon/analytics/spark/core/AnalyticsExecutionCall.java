/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.carbon.analytics.spark.core;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;

/**
 * This class represents a remote call to the analytics executor.
 */
public class AnalyticsExecutionCall implements Callable<AnalyticsQueryResult>, Serializable {

    private static final long serialVersionUID = -4144692904875233092L;

    private int tenantId;
    
    private String query;
    
    public AnalyticsExecutionCall(int tenantId, String query) {
        this.tenantId = tenantId;
        this.query = query;
    }
    
    @Override
    public AnalyticsQueryResult call() throws Exception {
        return ServiceHolder.getAnalyticskExecutor().executeQuery(this.tenantId, this.query);
    }
    
}
