package org.wso2.carbon.analytics.auth.rest.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * RedirectionDTO.
 */
public class RedirectionDTO {
    @JsonProperty("clientId")
    private String clientId = null;

    @JsonProperty("redirectUrl")
    private String redirectUrl = null;

    @JsonProperty("callbackUrl")
    private String callbackUrl = null;

    public RedirectionDTO clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Get clientId.
     *
     * @return clientId
     **/
    @ApiModelProperty(value = "")
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public RedirectionDTO redirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    /**
     * Get redirectUrl.
     *
     * @return redirectUrl
     **/
    @ApiModelProperty(value = "")
    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public RedirectionDTO callbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

    /**
     * Get callbackUrl.
     *
     * @return callbackUrl
     **/
    @ApiModelProperty(value = "")
    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedirectionDTO redirection = (RedirectionDTO) o;
        return Objects.equals(this.clientId, redirection.clientId) &&
                Objects.equals(this.redirectUrl, redirection.redirectUrl) &&
                Objects.equals(this.callbackUrl, redirection.callbackUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, redirectUrl, callbackUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RedirectionDTO {\n");

        sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
        sb.append("    redirectUrl: ").append(toIndentedString(redirectUrl)).append("\n");
        sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
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

