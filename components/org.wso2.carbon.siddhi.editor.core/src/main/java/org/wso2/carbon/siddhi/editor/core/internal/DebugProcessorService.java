/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.siddhi.editor.core.internal;


import org.wso2.carbon.siddhi.editor.core.exception.NoSuchExecutionPlanException;

/**
 * Class which manage execution plans and their runtime
 */
public class DebugProcessorService {

    public DebugRuntime getExecutionPlanRuntimeHolder(String executionPlanName) {
        return EditorDataHolder.getExecutionPlanMap().get(executionPlanName);
    }

    public synchronized void start(String executionPlanName) {
        if (EditorDataHolder.getExecutionPlanMap().containsKey(executionPlanName)) {
            DebugRuntime runtimeHolder = EditorDataHolder.getExecutionPlanMap().get(executionPlanName);
            runtimeHolder.start();
        } else {
            throw new NoSuchExecutionPlanException(
                    String.format("Execution Plan %s does not exists", executionPlanName)
            );
        }
    }

    public synchronized void debug(String executionPlanName) {
        if (EditorDataHolder.getExecutionPlanMap().containsKey(executionPlanName)) {
            DebugRuntime runtimeHolder = EditorDataHolder.getExecutionPlanMap().get(executionPlanName);
            runtimeHolder.debug();
        } else {
            throw new NoSuchExecutionPlanException(
                    String.format("Execution Plan %s does not exists", executionPlanName)
            );
        }
    }

    public synchronized void stop(String executionPlanName) {
        if (EditorDataHolder.getExecutionPlanMap().containsKey(executionPlanName)) {
            DebugRuntime runtimeHolder = EditorDataHolder.getExecutionPlanMap().get(executionPlanName);
            runtimeHolder.stop();
        } else {
            throw new NoSuchExecutionPlanException(
                    String.format("Execution Plan %s does not exists", executionPlanName)
            );
        }
    }

    public synchronized String deploy(String executionPlanName, String executionPlan) {
        if (!EditorDataHolder.getExecutionPlanMap().containsKey(executionPlanName)) {
            DebugRuntime runtimeHolder = new DebugRuntime(executionPlanName, executionPlan);
            EditorDataHolder.getExecutionPlanMap().put(executionPlanName, runtimeHolder);
        } else {
            EditorDataHolder.getExecutionPlanMap().get(executionPlanName).reload(executionPlan);
        }
        return executionPlanName;
    }

    public synchronized void undeploy(String executionPlanName) {
        if (EditorDataHolder.getExecutionPlanMap().containsKey(executionPlanName)) {
            DebugRuntime runtimeHolder = EditorDataHolder.getExecutionPlanMap().get(executionPlanName);
            runtimeHolder.stop();
            EditorDataHolder.getExecutionPlanMap().remove(executionPlanName);
        } else {
            throw new NoSuchExecutionPlanException(
                    String.format("Execution Plan %s does not exists", executionPlanName)
            );
        }
    }


}
