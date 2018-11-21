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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Deployment details of the managers and the resource pool
 */
@ApiModel(description = "Deployment details of the managers and the resource pool")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-23T12:20:42.963Z")
public class Deployment {
    @JsonProperty("leader")
    private ManagerNodeConfig leader = null;

    @JsonProperty("managers")
    private List<InterfaceConfig> managers = new ArrayList<InterfaceConfig>();

    @JsonProperty("resources")
    private List<NodeConfig> resources = new ArrayList<NodeConfig>();

    public Deployment leader(ManagerNodeConfig leader) {
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

    public Deployment managers(List<InterfaceConfig> managers) {
        this.managers = managers;
        return this;
    }

    public Deployment addManagersItem(InterfaceConfig managersItem) {
        this.managers.add(managersItem);
        return this;
    }

    /**
     * Get managers
     *
     * @return managers
     **/
    @ApiModelProperty(required = true, value = "")
    public List<InterfaceConfig> getManagers() {
        return managers;
    }

    public void setManagers(List<InterfaceConfig> managers) {
        this.managers = managers;
    }

    public Deployment resources(List<NodeConfig> resources) {
        this.resources = resources;
        return this;
    }

    public Deployment addResourcesItem(NodeConfig resourcesItem) {
        this.resources.add(resourcesItem);
        return this;
    }

    /**
     * Get resources
     *
     * @return resources
     **/
    @ApiModelProperty(required = true, value = "")
    public List<NodeConfig> getResources() {
        return resources;
    }

    public void setResources(List<NodeConfig> resources) {
        this.resources = resources;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Deployment deployment = (Deployment) o;
        return Objects.equals(this.leader, deployment.leader) &&
                Objects.equals(this.managers, deployment.managers) &&
                Objects.equals(this.resources, deployment.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leader, managers, resources);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Deployment {\n");

        sb.append("    leader: ").append(toIndentedString(leader)).append("\n");
        sb.append("    managers: ").append(toIndentedString(managers)).append("\n");
        sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
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

