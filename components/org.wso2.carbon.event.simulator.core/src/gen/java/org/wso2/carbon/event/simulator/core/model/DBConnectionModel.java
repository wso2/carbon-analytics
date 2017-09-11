package org.wso2.carbon.event.simulator.core.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.status.dashboard.api.annotations.ApiModelProperty;


/**
 * DBConnectionModel
 */
@javax.annotation.Generated(value = "org.wso2.status.dashboard.core.codegen.languages.JavaMSF4JServerCodegen", date = "2017-07-20T09:30:14.336Z")
public class DBConnectionModel   {
  @JsonProperty("dataSourceLocation")
  private String dataSourceLocation = null;

  @JsonProperty("driver")
  private String driver = null;

  @JsonProperty("username")
  private String username = null;

  @JsonProperty("password")
  private String password = null;

  public DBConnectionModel dataSourceLocation(String dataSourceLocation) {
    this.dataSourceLocation = dataSourceLocation;
    return this;
  }

   /**
   * Get dataSourceLocation
   * @return dataSourceLocation
  **/
  @ApiModelProperty(example = "jdbc:mysql://localhost:3306/DatabaseFeedSimulation", required = true, value = "")
  public String getDataSourceLocation() {
    return dataSourceLocation;
  }

  public void setDataSourceLocation(String dataSourceLocation) {
    this.dataSourceLocation = dataSourceLocation;
  }

  public DBConnectionModel driver(String driver) {
    this.driver = driver;
    return this;
  }

   /**
   * Get driver
   * @return driver
  **/
  @ApiModelProperty(example = "com.mysql.jdbc.Driver", required = true, value = "")
  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public DBConnectionModel username(String username) {
    this.username = username;
    return this;
  }

   /**
   * Get username
   * @return username
  **/
  @ApiModelProperty(example = "root", required = true, value = "")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public DBConnectionModel password(String password) {
    this.password = password;
    return this;
  }

   /**
   * Get password
   * @return password
  **/
  @ApiModelProperty(example = "password", required = true, value = "")
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DBConnectionModel dbConnectionModel = (DBConnectionModel) o;
    return Objects.equals(this.dataSourceLocation, dbConnectionModel.dataSourceLocation) &&
        Objects.equals(this.driver, dbConnectionModel.driver) &&
        Objects.equals(this.username, dbConnectionModel.username) &&
        Objects.equals(this.password, dbConnectionModel.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataSourceLocation, driver, username, password);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DBConnectionModel {\n");
    
    sb.append("    dataSourceLocation: ").append(toIndentedString(dataSourceLocation)).append("\n");
    sb.append("    driver: ").append(toIndentedString(driver)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
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

