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

package org.wso2.carbon.sp.jobmanager.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * ChildApps class that holds the details of the child app and the status of the child apps
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2018-02-03T14:53:27.713Z")

public class SiddhiAppDetails {
    @JsonProperty("parentAppName")
    private String parentAppName;
    @JsonProperty("groupName")
    private String groupName;
    @JsonProperty("childAppName")
    private String appName;
    @JsonProperty("siddhiApp")
    private String siddhiApp;
    @JsonProperty("deployedNodeHost")
    private String host;
    @JsonProperty("deployedNodePort")
    private String port;
    @JsonProperty("deployedNodeId")
    private String id;
    @JsonProperty("deployedNodeState")
    private String state;
    @JsonProperty("lastPingTimeStamp")
    private String lastPingTimestamp;
    @JsonProperty("failedPingAttempts")
    private String failedPingAttempts;
    @JsonProperty("failedPingAttempts")
    private String appStatus;

    public SiddhiAppDetails parentAppName(String parentAppName) {
        this.parentAppName = parentAppName;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    public String getParentAppName() {
        return parentAppName;
    }

    public void setParentAppName(String parentAppName) {
        this.parentAppName = parentAppName;
    }

    public SiddhiAppDetails appName(String appName) {
        this.appName = appName;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public SiddhiAppDetails siddhiApp(String siddhiApp) {
        this.siddhiApp = siddhiApp;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    public String getSiddhiApp() {
        return siddhiApp;
    }

    public void setSiddhiApp(String siddhiApp) {
        this.siddhiApp = siddhiApp;
    }

    public SiddhiAppDetails groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public SiddhiAppDetails host(String host) {
        this.host = host;
        return this;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public SiddhiAppDetails port(String port) {
        this.port = port;
        return this;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public SiddhiAppDetails id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SiddhiAppDetails state(String state) {
        this.state = state;
        return this;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public SiddhiAppDetails lastePingTimeStamp(String lastPingTimestamp) {
        this.lastPingTimestamp = lastPingTimestamp;
        return this;
    }

    public String getLastPingTimestamp() {
        return lastPingTimestamp;
    }

    public void setLastPingTimestamp(String lastPingTimestamp) {
        this.lastPingTimestamp = lastPingTimestamp;
    }

    public SiddhiAppDetails failedAttempts(String failedPingAttempts) {
        this.failedPingAttempts = failedPingAttempts;
        return this;
    }

    public String getFailedPingAttempts() {
        return failedPingAttempts;
    }

    public void setFailedPingAttempts(String failedPingAttempts) {
        this.failedPingAttempts = failedPingAttempts;
    }

    public String getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(String appStatus) {
        this.appStatus = appStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentAppName, groupName, appName, siddhiApp, id, state, host, port, lastPingTimestamp,
                failedPingAttempts, appStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Manager {\n");

        sb.append("    parentAppName: ").append(toIndentedString(parentAppName)).append("\n");
        sb.append("    groupName: ").append(toIndentedString(groupName)).append("\n");
        sb.append("    childAppName: ").append(toIndentedString(appName)).append("\n");
        sb.append("    childApp: ").append(toIndentedString(siddhiApp)).append("\n");
        sb.append("    deployedNodeId: ").append(toIndentedString(id)).append("\n");
        sb.append("    deployedNodeState: ").append(toIndentedString(state)).append("\n");
        sb.append("    deployedNodeHost: ").append(toIndentedString(host)).append("\n");
        sb.append("    deploedNodePort: ").append(toIndentedString(port)).append("\n");
        sb.append("    lastPingTimeStamp: ").append(toIndentedString(lastPingTimestamp)).append("\n");
        sb.append("    failedPingTime: ").append(toIndentedString(failedPingAttempts)).append("\n");
        sb.append("    appStatus: ").append(toIndentedString(appStatus)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
