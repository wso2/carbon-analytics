package org.wso2.carbon.analytics.auth.rest.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * ErrorDTO.
 */
public class ErrorDTO {
    @JsonProperty("error")
    private String error = null;

    @JsonProperty("description")
    private String description = null;

    public ErrorDTO error(String error) {
        this.error = error;
        return this;
    }

    /**
     * Get error.
     *
     * @return error
     **/
    @ApiModelProperty(value = "")
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ErrorDTO description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Error message.
     *
     * @return description
     **/
    @ApiModelProperty(value = "Error message")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErrorDTO error = (ErrorDTO) o;
        return Objects.equals(this.error, error.error) &&
                Objects.equals(this.description, error.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, description);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorDTO {\n");

        sb.append("    error: ").append(toIndentedString(error)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

