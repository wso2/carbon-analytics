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
package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.spark.sql.SQLContext;

import java.util.concurrent.Callable;

/**
 * class which executes queries in separate threads.
 */
public class QueryExecutionTask implements Callable<Object> {
    private SQLContext ctx;
    private String query;

    public QueryExecutionTask(SQLContext ctx, String query) {
        this.ctx = ctx;
        this.query = query;
    }

    @Override
    public Object call() throws Exception {
        return ctx.sql(query);
    }
    
}
