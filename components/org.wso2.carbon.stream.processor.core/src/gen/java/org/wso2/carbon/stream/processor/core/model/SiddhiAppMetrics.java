package org.wso2.carbon.stream.processor.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * SiddhiAppMetrics
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-12T04:28:50.744Z")
public class SiddhiAppMetrics {
  @JsonProperty("appName")
  private String appName = null;

  @JsonProperty("status")
  private String status = "Active";

  @JsonProperty("age")
  private long age = 0;

  @JsonProperty("isStatEnabled")
  private boolean isStatEnabled = false;

  public SiddhiAppMetrics appName(String appName) {
    this.appName = appName;
    return this;
  }

   /**
   * Name of the siddhi app
   * @return appName
  **/
  @ApiModelProperty(value = "Name of the siddhi app")
  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public SiddhiAppMetrics status(String status) {
    this.status = status;
    return this;
  }

   /**
   * Current status of the siddhi app ex. Active/Innactive
   * @return status
  **/
  @ApiModelProperty(value = "Current status of the siddhi app ex. Active/Innactive")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public SiddhiAppMetrics age(long age) {
    this.age = age;
    return this;
  }

   /**
   * Time gap between deployment and current time
   * @return age
  **/
  @ApiModelProperty(value = "Time gap between deployment and current time")
  public long getAge() {
    return age;
  }

  public void setAge(long age) {
    this.age = age;
  }

  public SiddhiAppMetrics isStatEnabled(boolean isStatEnabled) {
    this.isStatEnabled = isStatEnabled;
    return this;
  }

   /**
   * is statistics enabled for this app
   * @return isStatEnabled
  **/
  @ApiModelProperty(value = "is statistics enabled for this app")
  public boolean getIsStatEnabled() {
    return isStatEnabled;
  }

  public void setIsStatEnabled(boolean isStatEnabled) {
    this.isStatEnabled = isStatEnabled;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SiddhiAppMetrics siddhiAppMetrics = (SiddhiAppMetrics) o;
    return Objects.equals(this.appName, siddhiAppMetrics.appName) &&
        Objects.equals(this.status, siddhiAppMetrics.status) &&
        Objects.equals(this.age, siddhiAppMetrics.age) &&
        Objects.equals(this.isStatEnabled, siddhiAppMetrics.isStatEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appName, status, age, isStatEnabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SiddhiAppMetrics {\n");
    
    sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    age: ").append(toIndentedString(age)).append("\n");
    sb.append("    isStatEnabled: ").append(toIndentedString(isStatEnabled)).append("\n");
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

