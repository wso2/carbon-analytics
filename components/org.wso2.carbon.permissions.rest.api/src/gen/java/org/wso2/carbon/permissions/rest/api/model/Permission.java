/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.permissions.rest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Permission Model class
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-12-07T14:05:52.168Z")
public class Permission {
    @JsonProperty("appName")
    private String appName = null;

    @JsonProperty("permissionString")
    private String permissionString = null;

    public Permission appName(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * Get appName
     *
     * @return appName
     **/
    @ApiModelProperty(value = "")
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Permission permissionString(String permissionString) {
        this.permissionString = permissionString;
        return this;
    }

    /**
     * Get permissionString
     *
     * @return permissionString
     **/
    @ApiModelProperty(value = "")
    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Permission permission = (Permission) o;
        return Objects.equals(this.appName, permission.appName) &&
                Objects.equals(this.permissionString, permission.permissionString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, permissionString);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Permission {\n");

        sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
        sb.append("    permissionString: ").append(toIndentedString(permissionString)).append("\n");
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

