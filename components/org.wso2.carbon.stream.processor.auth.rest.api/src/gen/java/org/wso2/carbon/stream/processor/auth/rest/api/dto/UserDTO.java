package org.wso2.carbon.stream.processor.auth.rest.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * UserDTO
 */
public class UserDTO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("isTokenValid")
  private Boolean isTokenValid = null;

  @JsonProperty("validityPeriod")
  private Integer validityPeriod = null;

  @JsonProperty("partialAccessToken")
  private String partialAccessToken = null;

  @JsonProperty("partialRefreshToken")
  private String partialRefreshToken = null;

  @JsonProperty("authUser")
  private String authUser = null;

  public UserDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UserDTO isTokenValid(Boolean isTokenValid) {
    this.isTokenValid = isTokenValid;
    return this;
  }

   /**
   * Get isTokenValid
   * @return isTokenValid
  **/
  @ApiModelProperty(value = "")
  public Boolean getIsTokenValid() {
    return isTokenValid;
  }

  public void setIsTokenValid(Boolean isTokenValid) {
    this.isTokenValid = isTokenValid;
  }

  public UserDTO validityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

   /**
   * Get validityPeriod
   * @return validityPeriod
  **/
  @ApiModelProperty(value = "")
  public Integer getValidityPeriod() {
    return validityPeriod;
  }

  public void setValidityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
  }

  public UserDTO partialAccessToken(String partialAccessToken) {
    this.partialAccessToken = partialAccessToken;
    return this;
  }

   /**
   * Get partialAccessToken
   * @return partialAccessToken
  **/
  @ApiModelProperty(value = "")
  public String getPartialAccessToken() {
    return partialAccessToken;
  }

  public void setPartialAccessToken(String partialAccessToken) {
    this.partialAccessToken = partialAccessToken;
  }

  public UserDTO partialRefreshToken(String partialRefreshToken) {
    this.partialRefreshToken = partialRefreshToken;
    return this;
  }

   /**
   * Get partialRefreshToken
   * @return partialRefreshToken
  **/
  @ApiModelProperty(value = "")
  public String getPartialRefreshToken() {
    return partialRefreshToken;
  }

  public void setPartialRefreshToken(String partialRefreshToken) {
    this.partialRefreshToken = partialRefreshToken;
  }

  public UserDTO authUser(String authUser) {
    this.authUser = authUser;
    return this;
  }

   /**
   * Get authUser
   * @return authUser
  **/
  @ApiModelProperty(value = "")
  public String getAuthUser() {
    return authUser;
  }

  public void setAuthUser(String authUser) {
    this.authUser = authUser;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserDTO user = (UserDTO) o;
    return Objects.equals(this.id, user.id) &&
        Objects.equals(this.isTokenValid, user.isTokenValid) &&
        Objects.equals(this.validityPeriod, user.validityPeriod) &&
        Objects.equals(this.partialAccessToken, user.partialAccessToken) &&
        Objects.equals(this.partialRefreshToken, user.partialRefreshToken) &&
        Objects.equals(this.authUser, user.authUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, isTokenValid, validityPeriod, partialAccessToken, partialRefreshToken, authUser);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    isTokenValid: ").append(toIndentedString(isTokenValid)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
    sb.append("    partialAccessToken: ").append(toIndentedString(partialAccessToken)).append("\n");
    sb.append("    partialRefreshToken: ").append(toIndentedString(partialRefreshToken)).append("\n");
    sb.append("    authUser: ").append(toIndentedString(authUser)).append("\n");
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

