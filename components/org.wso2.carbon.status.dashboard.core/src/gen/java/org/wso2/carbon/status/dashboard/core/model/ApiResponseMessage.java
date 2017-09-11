package org.wso2.carbon.status.dashboard.core.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;


/**
 * ApiResponseMessage
 */
@javax.annotation.Generated(value = "org.wso2.status.dashboard.core.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T05:58:14.415Z")
public class ApiResponseMessage   {
  @JsonProperty("message")
  private String message = null;

  public ApiResponseMessage message(String message) {
    this.message = message;
    return this;
  }

   /**
   * Error message
   * @return message
  **/
  @ApiModelProperty(value = "Error message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiResponseMessage apiResponseMessage = (ApiResponseMessage) o;
    return Objects.equals(this.message, apiResponseMessage.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiResponseMessage {\n");
    
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

