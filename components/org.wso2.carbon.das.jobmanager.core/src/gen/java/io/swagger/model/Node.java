package io.swagger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Representation of a Node which consists of a host and a port
 */
@ApiModel(description = "Representation of a Node which consists of a host and a port")
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-17T16:36:16.751Z"
)
public class Node {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("host")
    private String host = null;

    @JsonProperty("port")
    private Integer port = null;

    public Node id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    @ApiModelProperty(required = true, value = "")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Node host(String host) {
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

    public Node port(Integer port) {
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


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Node node = (Node) o;
        return Objects.equals(this.id, node.id) &&
                Objects.equals(this.host, node.host) &&
                Objects.equals(this.port, node.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, port);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Node {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    host: ").append(toIndentedString(host)).append("\n");
        sb.append("    port: ").append(toIndentedString(port)).append("\n");
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

