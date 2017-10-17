package org.wso2.carbon.das.jobmanager.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Representation of a Manager Node
 */
@ApiModel(description = "Representation of a Manager Node")
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-17T16:36:16.751Z"
)
public class ManagerNode extends Node {
    @JsonProperty("isLeader")
    private Boolean isLeader = false;

    @JsonProperty("heartbeatInterval")
    private Long heartbeatInterval = null;

    public ManagerNode isLeader(Boolean isLeader) {
        this.isLeader = isLeader;
        return this;
    }

    /**
     * Get isLeader
     *
     * @return isLeader
     **/
    @ApiModelProperty(required = true, value = "")
    public Boolean getIsLeader() {
        return isLeader;
    }

    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }

    public ManagerNode heartbeatInterval(Long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return this;
    }

    /**
     * Get heartbeatInterval
     *
     * @return heartbeatInterval
     **/
    @ApiModelProperty(required = true, value = "")
    public Long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ManagerNode managerNode = (ManagerNode) o;
        return Objects.equals(this.isLeader, managerNode.isLeader) &&
                Objects.equals(this.heartbeatInterval, managerNode.heartbeatInterval) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLeader, heartbeatInterval, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ManagerNode {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    isLeader: ").append(toIndentedString(isLeader)).append("\n");
        sb.append("    heartbeatInterval: ").append(toIndentedString(heartbeatInterval)).append("\n");
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

