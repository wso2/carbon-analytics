/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core.bean;

import java.util.List;

/**
 * Bean class for kafkaTransport Details.
 */
public class KafkaTransportDetails {
    private String appName;
    private String siddhiApp;
    private String deployedHost;
    private String deployedPort;
    private List<String> sourceList;
    private List<String> sinkList;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getSiddhiApp() {
        return siddhiApp;
    }

    public void setSiddhiApp(String siddhiApp) {
        this.siddhiApp = siddhiApp;
    }

    public List<String> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<String> sourceList) {
        this.sourceList = sourceList;
    }

    public List<String> getSinkList() {
        return sinkList;
    }

    public void setSinkList(List<String> sinkList) {
        this.sinkList = sinkList;
    }

    public String getDeployedHost() {
        return deployedHost;
    }

    public void setDeployedHost(String deployedHost) {
        this.deployedHost = deployedHost;
    }

    public String getDeployedPort() {
        return deployedPort;
    }

    public void setDeployedPort(String deployedPort) {
        this.deployedPort = deployedPort;
    }
}
