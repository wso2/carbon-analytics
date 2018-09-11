/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.status.dashboard.core.bean;

/**
 * Bean class that contains resource cluster information.
 */
public class ResourceClusterInfo {
    private String nodeId;
    private String https_host;
    private String https_port;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getHttps_host() {
        return https_host;
    }

    public void setHttps_host(String https_host) {
        this.https_host = https_host;
    }

    public String getHttps_port() {
        return https_port;
    }

    public void setHttps_port(String https_port) {
        this.https_port = https_port;
    }
}
