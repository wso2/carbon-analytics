/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.parser.service.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Source Deployment Configuration Model.
 */
public class SourceDeploymentConfig {
    private String serviceProtocol;
    private boolean secured = false;
    private int port;
    private boolean isPulling;
    private Map<String, String> deploymentProperties = new HashMap<>();

    public SourceDeploymentConfig(int port, String serviceProtocol, boolean secured, boolean isPulling, Map<String,
            String> deploymentProperties) {
        this.port = port;
        this.serviceProtocol = serviceProtocol;
        this.secured = secured;
        this.isPulling = isPulling;
        this.deploymentProperties = deploymentProperties;
    }

    public String getServiceProtocol() {
        return serviceProtocol;
    }

    public SourceDeploymentConfig setServiceProtocol(String serviceProtocol) {
        this.serviceProtocol = serviceProtocol;
        return this;
    }

    public boolean isSecured() {
        return secured;
    }

    public SourceDeploymentConfig setSecured(boolean secured) {
        this.secured = secured;
        return this;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isPulling() {
        return isPulling;
    }

    public void setPulling(boolean pulling) {
        isPulling = pulling;
    }

    public Map<String, String> getDeploymentProperties() {
        return deploymentProperties;
    }

    public SourceDeploymentConfig setDeploymentProperties(Map<String, String> deploymentProperties) {
        this.deploymentProperties = deploymentProperties;
        return this;
    }
}
