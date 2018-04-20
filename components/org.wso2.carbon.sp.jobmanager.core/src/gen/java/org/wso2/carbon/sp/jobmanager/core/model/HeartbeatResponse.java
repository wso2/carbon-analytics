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

package org.wso2.carbon.sp.jobmanager.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Response for joining the resource pool or heartbeat update.
 */
@ApiModel(description = "Response for joining the resource pool or heartbeat update.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-23T12:20:42.963Z")
public class HeartbeatResponse {
    @JsonProperty("leader")
    private ManagerNodeConfig leader = null;

    @JsonProperty("connectedManagers")
    private List<InterfaceConfig> connectedManagers = new ArrayList<InterfaceConfig>();
    @JsonProperty("joinedState")
    private JoinedStateEnum joinedState = null;

    public HeartbeatResponse leader(ManagerNodeConfig leader) {
        this.leader = leader;
        return this;
    }

    /**
     * Get leader
     *
     * @return leader
     **/
    @ApiModelProperty(required = true, value = "")
    public ManagerNodeConfig getLeader() {
        return leader;
    }

    public void setLeader(ManagerNodeConfig leader) {
        this.leader = leader;
    }

    public HeartbeatResponse connectedManagers(List<InterfaceConfig> connectedManagers) {
        this.connectedManagers = connectedManagers;
        return this;
    }

    public HeartbeatResponse addConnectedManagersItem(InterfaceConfig connectedManagersItem) {
        this.connectedManagers.add(connectedManagersItem);
        return this;
    }

    /**
     * Get connectedManagers
     *
     * @return connectedManagers
     **/
    @ApiModelProperty(required = true, value = "")
    public List<InterfaceConfig> getConnectedManagers() {
        return connectedManagers;
    }

    public void setConnectedManagers(List<InterfaceConfig> connectedManagers) {
        this.connectedManagers = connectedManagers;
    }

    public HeartbeatResponse joinedState(JoinedStateEnum joinedState) {
        this.joinedState = joinedState;
        return this;
    }

    /**
     * Get joinedState
     *
     * @return joinedState
     **/
    @ApiModelProperty(required = true, value = "")
    public JoinedStateEnum getJoinedState() {
        return joinedState;
    }

    public void setJoinedState(JoinedStateEnum joinedState) {
        this.joinedState = joinedState;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HeartbeatResponse heartbeatResponse = (HeartbeatResponse) o;
        return Objects.equals(this.leader, heartbeatResponse.leader) &&
                Objects.equals(this.connectedManagers, heartbeatResponse.connectedManagers) &&
                Objects.equals(this.joinedState, heartbeatResponse.joinedState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leader, connectedManagers, joinedState);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HeartbeatResponse {\n");

        sb.append("    leader: ").append(toIndentedString(leader)).append("\n");
        sb.append("    connectedManagers: ").append(toIndentedString(connectedManagers)).append("\n");
        sb.append("    joinedState: ").append(toIndentedString(joinedState)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Gets or Sets joinedState
     */
    public enum JoinedStateEnum {
        NEW("NEW"),

        EXISTS("EXISTS"),

        REJECTED("REJECTED");

        private String value;

        JoinedStateEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static JoinedStateEnum fromValue(String text) {
            for (JoinedStateEnum b : JoinedStateEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}

