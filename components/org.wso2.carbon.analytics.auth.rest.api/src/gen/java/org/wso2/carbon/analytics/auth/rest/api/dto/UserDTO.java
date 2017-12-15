/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.auth.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * UserDTO.
 */
public class UserDTO {
    @JsonProperty("authUser")
    private String authUser = null;

    @JsonProperty("PID")
    private String pID = null;

    @JsonProperty("LID")
    private String lID = null;

    @JsonProperty("validityPeriod")
    private Integer validityPeriod = null;

    public UserDTO authUser(String authUser) {
        this.authUser = authUser;
        return this;
    }

    /**
     * Get authUser.
     *
     * @return authUser
     **/
    @ApiModelProperty(value = "")
    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public UserDTO pID(String pID) {
        this.pID = pID;
        return this;
    }

    /**
     * Get pID.
     *
     * @return pID
     **/
    @ApiModelProperty(value = "")
    public String getpID() {
        return pID;
    }

    public void setpID(String pID) {
        this.pID = pID;
    }

    public UserDTO lID(String lID) {
        this.lID = lID;
        return this;
    }

    /**
     * Get lID.
     *
     * @return lID
     **/
    @ApiModelProperty(value = "")
    public String getlID() {
        return lID;
    }

    public void setlID(String lID) {
        this.lID = lID;
    }

    public UserDTO validityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
        return this;
    }

    /**
     * Get validityPeriod.
     *
     * @return validityPeriod
     **/
    @ApiModelProperty(value = "")
    public Integer getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
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
        return Objects.equals(this.authUser, user.authUser) &&
                Objects.equals(this.pID, user.pID) &&
                Objects.equals(this.lID, user.lID) &&
                Objects.equals(this.validityPeriod, user.validityPeriod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authUser, pID, lID);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserDTO {\n");

        sb.append("    authUser: ").append(toIndentedString(authUser)).append("\n");
        sb.append("    PID: ").append(toIndentedString(pID)).append("\n");
        sb.append("    LID: ").append(toIndentedString(lID)).append("\n");
        sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
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

