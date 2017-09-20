package org.wso2.carbon.business.rules.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * BusinessRuleDeploymentStatus
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class BusinessRuleDeploymentStatus   {
  @JsonProperty("uuid")
  private String uuid = null;

  @JsonProperty("name")
  private String name = null;

  public BusinessRuleDeploymentStatus uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

   /**
   * Get uuid
   * @return uuid
  **/
  @ApiModelProperty(value = "")
  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public BusinessRuleDeploymentStatus name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BusinessRuleDeploymentStatus businessRuleDeploymentStatus = (BusinessRuleDeploymentStatus) o;
    return Objects.equals(this.uuid, businessRuleDeploymentStatus.uuid) &&
        Objects.equals(this.name, businessRuleDeploymentStatus.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BusinessRuleDeploymentStatus {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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

