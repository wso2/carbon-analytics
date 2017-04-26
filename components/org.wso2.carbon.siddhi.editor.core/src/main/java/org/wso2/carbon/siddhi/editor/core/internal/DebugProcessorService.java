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


import org.wso2.carbon.siddhi.editor.core.util.DebugCallbackEvent;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.debugger.SiddhiDebugger;
import org.wso2.siddhi.core.debugger.SiddhiDebuggerCallback;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class which manage execution plans and their runtime
 */
public class DebugProcessorService {

    private Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = new ConcurrentHashMap<>();
    private Map<String, SiddhiDebugger> siddhiDebuggerMap = new ConcurrentHashMap<>();
    private Map<String, Set<String>> runtimeSpecificStreamsMap = new ConcurrentHashMap<>();
    private Map<String, Set<String>> runtimeSpecificQueriesMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, InputHandler>> runtimeSpecificInputHandlerMap = new ConcurrentHashMap<>();
    private Map<String, LinkedBlockingQueue<DebugCallbackEvent>> runtimeSpecificCallbackMap = new ConcurrentHashMap<>();

    public String deployAndDebug(String executionPlan) {
        SiddhiManager siddhiManager = EditorDataHolder.getSiddhiManager();
        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        if (runtime != null) {
            final String runtimeId = runtime.getName();
            Set<String> streamNames = runtime.getStreamDefinitionMap().keySet();
            Map<String, InputHandler> inputHandlerMap = new ConcurrentHashMap<>(streamNames.size());
            for (String streamName : streamNames) {
                inputHandlerMap.put(streamName, runtime.getInputHandler(streamName));
            }
            SiddhiDebugger debugger = runtime.debug();
            debugger.setDebuggerCallback(new SiddhiDebuggerCallback() {
                @Override
                public void debugEvent(ComplexEvent event, String queryName,
                                       SiddhiDebugger.QueryTerminal queryTerminal,
                                       SiddhiDebugger debugger) {
                    runtimeSpecificCallbackMap.get(runtimeId)
                            .add(new DebugCallbackEvent(queryName, queryTerminal, event));
                }
            });
            executionPlanRunTimeMap.put(runtimeId, runtime);
            siddhiDebuggerMap.put(runtimeId, debugger);
            runtimeSpecificStreamsMap.put(runtimeId, streamNames);
            runtimeSpecificQueriesMap.put(runtimeId, runtime.getQueryNames());
            runtimeSpecificInputHandlerMap.put(runtimeId, inputHandlerMap);
            runtimeSpecificCallbackMap.put(runtimeId, new LinkedBlockingQueue<>(10));
            return runtimeId;
        }
        return null;
    }

    public void stopAndUndeploy(String runtimeId) {
        if (siddhiDebuggerMap.containsKey(runtimeId)) {
            siddhiDebuggerMap.get(runtimeId).releaseAllBreakPoints();
            siddhiDebuggerMap.get(runtimeId).play();
            siddhiDebuggerMap.remove(runtimeId);
        }
        if (executionPlanRunTimeMap.containsKey(runtimeId)) {
            executionPlanRunTimeMap.get(runtimeId).shutdown();
            executionPlanRunTimeMap.remove(runtimeId);
        }
        if (runtimeSpecificStreamsMap.containsKey(runtimeId)) {
            runtimeSpecificStreamsMap.remove(runtimeId);
        }
        if (runtimeSpecificInputHandlerMap.containsKey(runtimeId)) {
            runtimeSpecificInputHandlerMap.remove(runtimeId);
        }
        if (runtimeSpecificCallbackMap.containsKey(runtimeId)) {
            runtimeSpecificCallbackMap.remove(runtimeId);
        }
    }

    public Map<String, ExecutionPlanRuntime> getExecutionPlanRunTimeMap() {
        return executionPlanRunTimeMap;
    }

    public Map<String, Map<String, InputHandler>> getRuntimeSpecificInputHandlerMap() {
        return runtimeSpecificInputHandlerMap;
    }

    public Map<String, SiddhiDebugger> getSiddhiDebuggerMap() {
        return siddhiDebuggerMap;
    }

    public Map<String, Set<String>> getRuntimeSpecificStreamsMap() {
        return runtimeSpecificStreamsMap;
    }

    public Map<String, Set<String>> getRuntimeSpecificQueriesMap() {
        return runtimeSpecificQueriesMap;
    }

    public Map<String, LinkedBlockingQueue<DebugCallbackEvent>> getRuntimeSpecificCallbackMap() {
        return runtimeSpecificCallbackMap;
    }
}
