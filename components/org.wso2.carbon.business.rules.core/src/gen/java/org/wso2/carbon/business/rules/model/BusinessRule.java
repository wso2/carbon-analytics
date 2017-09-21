package org.wso2.carbon.business.rules.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BusinessRule
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class BusinessRule   {
  @JsonProperty("uuid")
  private String uuid = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("templateGroupName")
  private String templateGroupName = null;

  @JsonProperty("ruleTemplateName")
  private String ruleTemplateName = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("properties")
  private Map<String, String> properties = null;

  public BusinessRule uuid(String uuid) {
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

  public BusinessRule name(String name) {
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

  public BusinessRule templateGroupName(String templateGroupName) {
    this.templateGroupName = templateGroupName;
    return this;
  }

   /**
   * Get templateGroupName
   * @return templateGroupName
  **/
  @ApiModelProperty(value = "")
  public String getTemplateGroupName() {
    return templateGroupName;
  }

  public void setTemplateGroupName(String templateGroupName) {
    this.templateGroupName = templateGroupName;
  }

  public BusinessRule ruleTemplateName(String ruleTemplateName) {
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

  public BusinessRule type(String type) {
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

  public BusinessRule properties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  public BusinessRule putPropertiesItem(String key, String propertiesItem) {
    if (this.properties == null) {
      this.properties = new HashMap<String, String>();
    }
    this.properties.put(key, propertiesItem);
    return this;
  }

   /**
   * Get properties
   * @return properties
  **/
  @ApiModelProperty(value = "")
  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
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
    BusinessRule businessRule = (BusinessRule) o;
    return Objects.equals(this.uuid, businessRule.uuid) &&
        Objects.equals(this.name, businessRule.name) &&
        Objects.equals(this.templateGroupName, businessRule.templateGroupName) &&
        Objects.equals(this.ruleTemplateName, businessRule.ruleTemplateName) &&
        Objects.equals(this.type, businessRule.type) &&
        Objects.equals(this.properties, businessRule.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, name, templateGroupName, ruleTemplateName, type, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BusinessRule {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    templateGroupName: ").append(toIndentedString(templateGroupName)).append("\n");
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

