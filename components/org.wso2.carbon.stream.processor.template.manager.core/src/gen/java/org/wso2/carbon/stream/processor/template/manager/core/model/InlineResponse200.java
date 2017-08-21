package org.wso2.carbon.stream.processor.template.manager.core.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * InlineResponse200
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-21T12:49:26.982Z")
public class InlineResponse200   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("ruleTemplateName")
  private String ruleTemplateName = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("properties")
  private String properties = null;

  public InlineResponse200 name(String name) {
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

  public InlineResponse200 ruleTemplateName(String ruleTemplateName) {
    this.ruleTemplateName = ruleTemplateName;
    return this;
  }

   /**
   * Get ruleTemplateName
   * @return ruleTemplateName
  **/
  @ApiModelProperty(value = "")
  public String getRuleTemplateName() {
    return ruleTemplateName;
  }

  public void setRuleTemplateName(String ruleTemplateName) {
    this.ruleTemplateName = ruleTemplateName;
  }

  public InlineResponse200 type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public InlineResponse200 properties(String properties) {
    this.properties = properties;
    return this;
  }

   /**
   * Get properties
   * @return properties
  **/
  @ApiModelProperty(value = "")
  public String getProperties() {
    return properties;
  }

  public void setProperties(String properties) {
    this.properties = properties;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse200 inlineResponse200 = (InlineResponse200) o;
    return Objects.equals(this.name, inlineResponse200.name) &&
        Objects.equals(this.ruleTemplateName, inlineResponse200.ruleTemplateName) &&
        Objects.equals(this.type, inlineResponse200.type) &&
        Objects.equals(this.properties, inlineResponse200.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, ruleTemplateName, type, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse200 {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    ruleTemplateName: ").append(toIndentedString(ruleTemplateName)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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

