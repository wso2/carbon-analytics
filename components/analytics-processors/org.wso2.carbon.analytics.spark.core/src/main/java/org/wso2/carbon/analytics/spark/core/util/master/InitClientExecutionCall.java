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

package org.wso2.carbon.analytics.spark.core.util.master;

import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import scala.Tuple2;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Created by niranda on 6/24/15.
 */
public class InitClientExecutionCall implements Callable<Integer>, Serializable{

    private static final long serialVersionUID = -155083315280606063L;

    private Tuple2<String, String>[] confs;
    public InitClientExecutionCall(Tuple2<String, String>[] confs) {
        this.confs= confs;
    }

    @Override
    public Integer call() throws Exception {
        ServiceHolder.getAnalyticskExecutor().initializeClient(this.confs);
        return 0;
    }
}
