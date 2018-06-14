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

import java.util.Objects;

/**
 * Representation of a Node which consists of an id and an InterfaceConfig
 */
@ApiModel(description = "Representation of a Node which consists of an id and an InterfaceConfig")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-23T12:20:42.963Z")
public class NodeConfig {
    @JsonProperty("id")
    private String id = null;
    @JsonProperty("state")
    private StateEnum state = null;
    @JsonProperty("httpsInterface")
    private InterfaceConfig httpsInterface = null;
    @JsonProperty("WorkerMetrics")
    private WorkerMetrics workerMetrics = null;
    @JsonProperty("isReceiverNode")
    private boolean isReceiverNode;

    public NodeConfig id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    @ApiModelProperty(required = true, value = "")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeConfig state(StateEnum state) {
        this.state = state;
        return this;
    }

    /**
     * Get state
     *
     * @return state
     **/
    @ApiModelProperty(value = "")
    public StateEnum getState() {
        return state;
    }

    public void setState(StateEnum state) {
        this.state = state;
    }

    public NodeConfig httpsInterface(InterfaceConfig httpsInterface) {
        this.httpsInterface = httpsInterface;
        return this;
    }

    public void setHttpsInterface(InterfaceConfig httpsInterface) {
        this.httpsInterface = httpsInterface;
    }

    /**
     * Get httpsInterface
     *
     * @return httpsInterface
     **/
    @ApiModelProperty(required = true, value = "")
    public InterfaceConfig getHttpsInterface() {
        return httpsInterface;
    }

    public NodeConfig workerMetrics(WorkerMetrics workerMetrics) {
        this.workerMetrics = workerMetrics;
        return this;
    }

    @ApiModelProperty(required = false, value = "")
    public WorkerMetrics getWorkerMetrics() {
        return workerMetrics;
    }

    public boolean isReceiverNode() {
        return isReceiverNode;
    }

    public NodeConfig setReceiverNode(boolean receiverNode) {
        this.isReceiverNode = receiverNode;
        return this;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeConfig nodeConfig = (NodeConfig) o;
        return Objects.equals(this.id, nodeConfig.id) &&
                Objects.equals(this.state, nodeConfig.state) &&
                Objects.equals(this.workerMetrics, nodeConfig.workerMetrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, state, httpsInterface, workerMetrics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NodeConfig {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    httpsInterface: ").append(toIndentedString(httpsInterface)).append("\n");
        sb.append("    workerMetrix: ").append(toIndentedString(workerMetrics)).append("\n");
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
     * Gets or Sets state
     */
    public enum StateEnum {
        NEW("NEW"),

        EXISTS("EXISTS");

        private String value;

        StateEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static StateEnum fromValue(String text) {
            for (StateEnum b : StateEnum.values()) {
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

