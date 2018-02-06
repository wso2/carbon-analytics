package org.wso2.carbon.sp.jobmanager.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

/**
 * ChildApps
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-03T14:53:27.713Z")
public class ChildApps {
//  @JsonProperty("appContent")
//  private String parentAppName = null;

    @JsonProperty("siddhiContent")
    private List siddhiContent = null;

    @JsonProperty("siddhiAppStatus")
    private String siddhiAppStatus = null;


//  @JsonProperty("executionGroupName")
//  private String executionGroupName = null;
//
//  @JsonProperty("childAppName")
//  private String childAppName = null;
//
//  @JsonProperty("DeployedNode")
//  private String deployedNode = null;
//
//  @JsonProperty("status")
//  private String status = null;
//
//  @JsonProperty("sourceKafkaTopic")
//  private String sourceKafkaTopic = null;
//
//  @JsonProperty("sourcePartitionName")
//  private String sourcePartitionName = null;
//
//  @JsonProperty("sinkKafkaTopicName")
//  private String sinkKafkaTopicName = null;
//
//  @JsonProperty("sinkKafkaPartitionName")
//  private String sinkKafkaPartitionName = null;

//  public ChildApps parentAppName(String parentAppName) {
//    this.parentAppName = parentAppName;
//    return this;
//  }

    /**
     * Get parentAppName
     *
     * @return parentAppName
     **/
//  @ApiModelProperty(required = true, value = "")
//  public String getParentAppName() {
//    return parentAppName;
//  }
//
//  public void setParentAppName(String parentAppName) {
//    this.parentAppName = parentAppName;
//  }
//
    public ChildApps siddhiContent(List siddhiContent) {
        this.siddhiContent = siddhiContent;
        return this;
    }

    /**
     * Get siddhiContent
     *
     * @return siddhiContent
     **/
    @ApiModelProperty(required = true, value = "")
    public List getSiddhiContent() {
        return siddhiContent;
    }

    public void setSiddhiContent(List siddhiContent) {
        this.siddhiContent = siddhiContent;
    }

    public ChildApps siddhiAppStatus(String siddhiAppStatus) {
        this.siddhiAppStatus = siddhiAppStatus;
        return this;
    }

    /**
     * Get siddhi status
     *
     * @return siddhiStatus
     **/
    @ApiModelProperty(required = true, value = "")
    public String getSiddhiAppStatus() {
        return siddhiAppStatus;
    }

    public void setSiddhiAppStatus(String siddhiAppStatus) {
        this.siddhiAppStatus = siddhiAppStatus;
    }

//  public ChildApps executionGroupName(String executionGroupName) {
//    this.executionGroupName = executionGroupName;
//    return this;
//  }
//
//   /**
//   * Get executionGroupName
//   * @return executionGroupName
//  **/
//  @ApiModelProperty(required = true, value = "")
//  public String getExecutionGroupName() {
//    return executionGroupName;
//  }
//
//  public void setExecutionGroupName(String executionGroupName) {
//    this.executionGroupName = executionGroupName;
//  }
//
//  public ChildApps childAppName(String childAppName) {
//    this.childAppName = childAppName;
//    return this;
//  }
//
//   /**
//   * Get childAppName
//   * @return childAppName
//  **/
//  @ApiModelProperty(required = true, value = "")
//  public String getChildAppName() {
//    return childAppName;
//  }
//
//  public void setChildAppName(String childAppName) {
//    this.childAppName = childAppName;
//  }
//
//  public ChildApps deployedNode(String deployedNode) {
//    this.deployedNode = deployedNode;
//    return this;
//  }
//
//   /**
//   * Get deployedNode
//   * @return deployedNode
//  **/
//  @ApiModelProperty(required = true, value = "")
//  public String getDeployedNode() {
//    return deployedNode;
//  }
//
//  public void setDeployedNode(String deployedNode) {
//    this.deployedNode = deployedNode;
//  }
//
//  public ChildApps status(String status) {
//    this.status = status;
//    return this;
//  }
//
//   /**
//   * Get status
//   * @return status
//  **/
//  @ApiModelProperty(required = true, value = "")
//  public String getStatus() {
//    return status;
//  }
//
//  public void setStatus(String status) {
//    this.status = status;
//  }
//
//  public ChildApps sourceKafkaTopic(String sourceKafkaTopic) {
//    this.sourceKafkaTopic = sourceKafkaTopic;
//    return this;
//  }
//
//   /**
//   * Get sourceKafkaTopic
//   * @return sourceKafkaTopic
//  **/
//  @ApiModelProperty(required = true, value = "")
//  public String getSourceKafkaTopic() {
//    return sourceKafkaTopic;
//  }
//
//  public void setSourceKafkaTopic(String sourceKafkaTopic) {
//    this.sourceKafkaTopic = sourceKafkaTopic;
//  }
//
//  public ChildApps sourcePartitionName(String sourcePartitionName) {
//    this.sourcePartitionName = sourcePartitionName;
//    return this;
//  }
//
//   /**
//   * Get sourcePartitionName
//   * @return sourcePartitionName
//  **/
//  @ApiModelProperty(required = true, value = "")
//  public String getSourcePartitionName() {
//    return sourcePartitionName;
//  }
//
//  public void setSourcePartitionName(String sourcePartitionName) {
//    this.sourcePartitionName = sourcePartitionName;
//  }
//
//  public ChildApps sinkKafkaTopicName(String sinkKafkaTopicName) {
//    this.sinkKafkaTopicName = sinkKafkaTopicName;
//    return this;
//  }
//
//   /**
//   * Get sinkKafkaTopicName
//   * @return sinkKafkaTopicName
//  **/
//  @ApiModelProperty(required = true, value = "")
//  public String getSinkKafkaTopicName() {
//    return sinkKafkaTopicName;
//  }
//
//  public void setSinkKafkaTopicName(String sinkKafkaTopicName) {
//    this.sinkKafkaTopicName = sinkKafkaTopicName;
//  }
//
//  public ChildApps sinkKafkaPartitionName(String sinkKafkaPartitionName) {
//    this.sinkKafkaPartitionName = sinkKafkaPartitionName;
//    return this;
//  }
//
//   /**
//   * Get sinkKafkaPartitionName
//   * @return sinkKafkaPartitionName
//  **/
//  @ApiModelProperty(required = true, value = "")
//  public String getSinkKafkaPartitionName() {
//    return sinkKafkaPartitionName;
//  }
//
//  public void setSinkKafkaPartitionName(String sinkKafkaPartitionName) {
//    this.sinkKafkaPartitionName = sinkKafkaPartitionName;
    // }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChildApps childApps = (ChildApps) o;
        return //Objects.equals(this.parentAppName, childApps.parentAppName) &&
                Objects.equals(this.siddhiContent, childApps.siddhiContent) &&
                        Objects.equals(this.siddhiAppStatus, childApps.siddhiAppStatus);
//        Objects.equals(this.executionGroupName, childApps.executionGroupName) &&
//        Objects.equals(this.childAppName, childApps.childAppName) &&
//        Objects.equals(this.deployedNode, childApps.deployedNode) &&
//        Objects.equals(this.status, childApps.status) &&
//        Objects.equals(this.sourceKafkaTopic, childApps.sourceKafkaTopic) &&
//        Objects.equals(this.sourcePartitionName, childApps.sourcePartitionName) &&
//        Objects.equals(this.sinkKafkaTopicName, childApps.sinkKafkaTopicName) &&
//        Objects.equals(this.sinkKafkaPartitionName, childApps.sinkKafkaPartitionName);
    }

    //  @Override
//  public int hashCode() {
//    return Objects.hash(parentAppName, siddhiContent, executionGroupName, childAppName, deployedNode, status,
// sourceKafkaTopic, sourcePartitionName, sinkKafkaTopicName, sinkKafkaPartitionName);
//  }
    @Override
    public int hashCode() {
        return Objects.hash(siddhiContent, siddhiAppStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChildApps {\n");

        //sb.append("    parentAppName: ").append(toIndentedString(parentAppName)).append("\n");
        sb.append("    siddhiContent: ").append(toIndentedString(siddhiContent)).append("\n");
        sb.append("    siddhiStatus:  ").append(toIndentedString(siddhiAppStatus)).append("\n");
//    sb.append("    executionGroupName: ").append(toIndentedString(executionGroupName)).append("\n");
//    sb.append("    childAppName: ").append(toIndentedString(childAppName)).append("\n");
//    sb.append("    deployedNode: ").append(toIndentedString(deployedNode)).append("\n");
//    sb.append("    status: ").append(toIndentedString(status)).append("\n");
//    sb.append("    sourceKafkaTopic: ").append(toIndentedString(sourceKafkaTopic)).append("\n");
//    sb.append("    sourcePartitionName: ").append(toIndentedString(sourcePartitionName)).append("\n");
//    sb.append("    sinkKafkaTopicName: ").append(toIndentedString(sinkKafkaTopicName)).append("\n");
//    sb.append("    sinkKafkaPartitionName: ").append(toIndentedString(sinkKafkaPartitionName)).append("\n");
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

