/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.distribution.editor.core.model;

/**
 * Class to return status of the siddhi apps.
 */
public class SiddhiAppStatus {
    private String siddhiAppName;
    private String mode;

    public SiddhiAppStatus(String siddhiAppName, String mode) {
        this.siddhiAppName = siddhiAppName;
        this.mode = mode;
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public void setSiddhiAppName(String siddhiAppName) {
        this.siddhiAppName = siddhiAppName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
