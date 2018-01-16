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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Execution all for check elected leader
 */
public class CheckElectedLeaderExecutionCall implements Callable<Boolean>, Serializable{

    private static final long serialVersionUID = -155083315280606063L;

    private static Log log = LogFactory.getLog(CheckElectedLeaderExecutionCall.class);

    @Override
    public Boolean call() throws Exception {
        boolean result = ServiceHolder.getAnalyticskExecutor().isElectedLeader();
        if (log.isDebugEnabled()) {
            log.debug("Check Elected Leader Request: " + result);
        }
        return result;
    }
}
