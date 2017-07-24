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

package org.wso2.carbon.analytics.spark.core.deploy;

import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Execution call for start workers
 */
public class StartWorkerExecutionCall implements Callable<Integer>, Serializable{

    private static final long serialVersionUID = 5985391498542101515L;

    @Override
    public Integer call() throws Exception {
        ServiceHolder.getAnalyticskExecutor().startWorker();
        return 0;
    }
}
