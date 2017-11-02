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

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Map;

/**
 * Class which contains information about Siddhi App File
 */
public class SiddhiAppData {

    private String SiddhiApp;
    private boolean isActive;
    private Map<String, InputHandler> inputHandlerMap;
    private SiddhiAppRuntime siddhiAppRuntime;
    private long deploymentTimeInMillis;

    public SiddhiAppData(String siddhiApp) {
        SiddhiApp = siddhiApp;
    }

    public SiddhiAppData(String siddhiApp, boolean isActive) {
        SiddhiApp = siddhiApp;
        this.isActive = isActive;
    }

    public SiddhiAppData(String siddhiApp, boolean isActive, Map<String, InputHandler> inputHandlerMap,
                         SiddhiAppRuntime siddhiAppRuntime) {
        SiddhiApp = siddhiApp;
        this.isActive = isActive;
        this.inputHandlerMap = inputHandlerMap;
        this.siddhiAppRuntime = siddhiAppRuntime;
    }

    public Map<String, InputHandler> getInputHandlerMap() {
        return inputHandlerMap;
    }

    public void setInputHandlerMap(Map<String, InputHandler> inputHandlerMap) {
        this.inputHandlerMap = inputHandlerMap;
    }

    public SiddhiAppRuntime getSiddhiAppRuntime() {
        return siddhiAppRuntime;
    }

    public void setSiddhiAppRuntime(SiddhiAppRuntime siddhiAppRuntime) {
        this.siddhiAppRuntime = siddhiAppRuntime;
    }

    public String getSiddhiApp() {
        return SiddhiApp;
    }

    public void setSiddhiApp(String siddhiApp) {
        SiddhiApp = siddhiApp;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getDeploymentTime() {
        return deploymentTimeInMillis;
    }

    public void setDeploymentTime(long deploymentTimeInMillis) {
        this.deploymentTimeInMillis = deploymentTimeInMillis;
    }
}
