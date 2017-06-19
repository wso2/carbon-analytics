/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stream.processor.core.internal;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Map;

/**
 * Class which holds the Siddhi internal constructs related to Siddhi App
 */
public class SiddhiAppConfiguration {

    private Map<String, InputHandler> inputHandlerMap;
    private ExecutionPlanRuntime executionPlanRuntime;

    public Map<String, InputHandler> getInputHandlerMap() {
        return inputHandlerMap;
    }

    public void setInputHandlerMap(Map<String, InputHandler> inputHandlerMap) {
        this.inputHandlerMap = inputHandlerMap;
    }


    public ExecutionPlanRuntime getExecutionPlanRuntime() {
        return executionPlanRuntime;
    }

    public void setExecutionPlanRuntime(ExecutionPlanRuntime executionPlanRuntime) {
        this.executionPlanRuntime = executionPlanRuntime;
    }

    public SiddhiAppConfiguration(Map<String, InputHandler> inputHandlerMap,
                                  ExecutionPlanRuntime executionPlanRuntime) {
        this.inputHandlerMap = inputHandlerMap;
        this.executionPlanRuntime = executionPlanRuntime;
    }

    public SiddhiAppConfiguration() {
    }
}
