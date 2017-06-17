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

public class SiddhiAppFile {

    private String siddhiAppName;
    private String SiddhiApp;
    private boolean isActive;

    public SiddhiAppFile(String siddhiApp) {
        SiddhiApp = siddhiApp;
    }

    public SiddhiAppFile(String siddhiAppName, String siddhiApp, boolean isActive) {
        this.siddhiAppName = siddhiAppName;
        SiddhiApp = siddhiApp;
        this.isActive = isActive;
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public void setSiddhiAppName(String siddhiAppName) {
        this.siddhiAppName = siddhiAppName;
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
}
