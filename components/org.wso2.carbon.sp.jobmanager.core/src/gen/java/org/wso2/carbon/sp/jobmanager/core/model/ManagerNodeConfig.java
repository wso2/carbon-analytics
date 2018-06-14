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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Representation of a Manager Node configuration
 */
@ApiModel(description = "Representation of a Manager Node configuration")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-23T12:20:42.963Z")
public class ManagerNodeConfig {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("httpsInterface")
    private InterfaceConfig httpsInterface = null;

    @JsonProperty("heartbeatInterval")
    private Integer heartbeatInterval = null;

    @JsonProperty("heartbeatMaxRetry")
    private Integer heartbeatMaxRetry = null;

    public ManagerNodeConfig id(String id) {
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

    public ManagerNodeConfig httpsInterface(InterfaceConfig httpsInterface) {
        this.httpsInterface = httpsInterface;
        return this;
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

    public void setHttpsInterface(InterfaceConfig httpsInterface) {
        this.httpsInterface = httpsInterface;
    }

    public ManagerNodeConfig heartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return this;
    }

    /**
     * Get heartbeatInterval
     *
     * @return heartbeatInterval
     **/
    @ApiModelProperty(required = true, value = "")
    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public ManagerNodeConfig heartbeatMaxRetry(Integer heartbeatMaxRetry) {
        this.heartbeatMaxRetry = heartbeatMaxRetry;
        return this;
    }

    /**
     * Get heartbeatMaxRetry
     *
     * @return heartbeatMaxRetry
     **/
    @ApiModelProperty(required = true, value = "")
    public Integer getHeartbeatMaxRetry() {
        return heartbeatMaxRetry;
    }

    public void setHeartbeatMaxRetry(Integer heartbeatMaxRetry) {
        this.heartbeatMaxRetry = heartbeatMaxRetry;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ManagerNodeConfig managerNodeConfig = (ManagerNodeConfig) o;
        return Objects.equals(this.id, managerNodeConfig.id) &&
                Objects.equals(this.heartbeatInterval, managerNodeConfig.heartbeatInterval) &&
                Objects.equals(this.heartbeatMaxRetry, managerNodeConfig.heartbeatMaxRetry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, httpsInterface, heartbeatInterval, heartbeatMaxRetry);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ManagerNodeConfig {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    heartbeatInterval: ").append(toIndentedString(heartbeatInterval)).append("\n");
        sb.append("    heartbeatMaxRetry: ").append(toIndentedString(heartbeatMaxRetry)).append("\n");
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
}
