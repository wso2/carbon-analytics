package org.wso2.carbon.sp.jobmanager.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * ManagerDetails
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-05T14:14:09.496Z")
public class ManagerDetails {
    @JsonProperty("host")
    private String host = null;

    @JsonProperty("port")
    private Integer port = null;

    @JsonProperty("haStatus")
    private String haStatus = null;

    @JsonProperty("username")
    private String username = null;

    @JsonProperty("password")
    private String password = null;

    public ManagerDetails host(String host) {
        this.host = host;
        return this;
    }

    /**
     * Get host
     *
     * @return host
     **/
    @ApiModelProperty(required = true, value = "")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ManagerDetails port(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Get port
     *
     * @return port
     **/
    @ApiModelProperty(required = true, value = "")
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ManagerDetails haStatus(String haStatus) {
        this.haStatus = haStatus;
        return this;
    }

    /**
     * Get haStatus
     *
     * @return haStatus
     **/
    @ApiModelProperty(required = true, value = "")
    public String getHaStatus() {
        return haStatus;
    }

    public void setHaStatus(String haStatus) {
        this.haStatus = haStatus;
    }

    public ManagerDetails username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get username
     *
     * @return username
     **/
    @ApiModelProperty(required = true, value = "")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ManagerDetails password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get password
     *
     * @return password
     **/
    @ApiModelProperty(required = true, value = "")
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
        ManagerDetails managerDetails = (ManagerDetails) o;
        return Objects.equals(this.host, managerDetails.host) &&
                Objects.equals(this.port, managerDetails.port) &&
                Objects.equals(this.haStatus, managerDetails.haStatus) &&
                Objects.equals(this.username, managerDetails.username) &&
                Objects.equals(this.password, managerDetails.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, haStatus, username, password);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ManagerDetails {\n");

        sb.append("    host: ").append(toIndentedString(host)).append("\n");
        sb.append("    port: ").append(toIndentedString(port)).append("\n");
        sb.append("    haStatus: ").append(toIndentedString(haStatus)).append("\n");
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

