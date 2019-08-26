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
package org.wso2.siddhi.parser.service.model;

import java.util.List;

/**
 * Represents a deployable partial Siddhi App.
 */
public class DeployableSiddhiApp {
    private boolean persistenceEnabled = false;
    int replicas = 1;
    private String siddhiApp;
    private List<SourceDeploymentConfig> sourceDeploymentConfigs = null;

    public void setSourceDeploymentConfigs(List<SourceDeploymentConfig> sourceDeploymentConfigs) {
        this.sourceDeploymentConfigs = sourceDeploymentConfigs;
    }

    public DeployableSiddhiApp(String siddhiApp) {
        this.siddhiApp = siddhiApp;
    }

    public DeployableSiddhiApp(String siddhiApp, List<SourceDeploymentConfig> sourceList, boolean persistenceEnabled) {
        this.siddhiApp = siddhiApp;
        this.sourceDeploymentConfigs = sourceList;
        this.persistenceEnabled = persistenceEnabled;
    }

    public DeployableSiddhiApp(String siddhiApp, boolean persistenceEnabled) {
        this.siddhiApp = siddhiApp;
        this.persistenceEnabled = persistenceEnabled;
    }

    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }

    public int getReplicas() {
        return replicas;
    }

    public String getSiddhiApp() {
        return siddhiApp;
    }

    public List<SourceDeploymentConfig> getSourceDeploymentConfigs() {
        return sourceDeploymentConfigs;
    }
}
