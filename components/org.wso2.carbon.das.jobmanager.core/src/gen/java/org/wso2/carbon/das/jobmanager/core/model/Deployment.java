package org.wso2.carbon.das.jobmanager.core.model;

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
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-17T16:36:16.751Z"
)
public class Deployment {
    @JsonProperty("managers")
    private List<ManagerNode> managers = new ArrayList<ManagerNode>();

    @JsonProperty("resources")
    private List<Node> resources = new ArrayList<Node>();

    public Deployment managers(List<ManagerNode> managers) {
        this.managers = managers;
        return this;
    }

    public Deployment addManagersItem(ManagerNode managersItem) {
        this.managers.add(managersItem);
        return this;
    }

    /**
     * list of Manager Nodes
     *
     * @return managers
     **/
    @ApiModelProperty(required = true, value = "list of Manager Nodes")
    public List<ManagerNode> getManagers() {
        return managers;
    }

    public void setManagers(List<ManagerNode> managers) {
        this.managers = managers;
    }

    public Deployment resources(List<Node> resources) {
        this.resources = resources;
        return this;
    }

    public Deployment addResourcesItem(Node resourcesItem) {
        this.resources.add(resourcesItem);
        return this;
    }

    /**
     * Nodes in the resource pool
     *
     * @return resources
     **/
    @ApiModelProperty(required = true, value = "Nodes in the resource pool")
    public List<Node> getResources() {
        return resources;
    }

    public void setResources(List<Node> resources) {
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
        return Objects.equals(this.managers, deployment.managers) &&
                Objects.equals(this.resources, deployment.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(managers, resources);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Deployment {\n");

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

