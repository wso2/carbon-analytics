/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

/**
 * ChildApps
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-03T14:53:27.713Z")
public class ChildApps {

    @JsonProperty("siddhiContent")
    private List siddhiContent = null;

    @JsonProperty("siddhiAppStatus")
    private String siddhiAppStatus = null;


    public ChildApps siddhiContent(List siddhiContent) {
        this.siddhiContent = siddhiContent;
        return this;
    }

    /**
     * Get siddhiContent
     *
     * @return siddhiContent
     **/
    @ApiModelProperty(required = true, value = "")
    public List getSiddhiContent() {
        return siddhiContent;
    }

    public void setSiddhiContent(List siddhiContent) {
        this.siddhiContent = siddhiContent;
    }

    public ChildApps siddhiAppStatus(String siddhiAppStatus) {
        this.siddhiAppStatus = siddhiAppStatus;
        return this;
    }

    /**
     * Get siddhi status
     *
     * @return siddhiStatus
     **/
    @ApiModelProperty(required = true, value = "")
    public String getSiddhiAppStatus() {
        return siddhiAppStatus;
    }

    public void setSiddhiAppStatus(String siddhiAppStatus) {
        this.siddhiAppStatus = siddhiAppStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChildApps childApps = (ChildApps) o;
        return Objects.equals(this.siddhiContent, childApps.siddhiContent) &&
                Objects.equals(this.siddhiAppStatus, childApps.siddhiAppStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siddhiContent, siddhiAppStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChildApps {\n");
        sb.append("    siddhiContent: ").append(toIndentedString(siddhiContent)).append("\n");
        sb.append("    siddhiStatus:  ").append(toIndentedString(siddhiAppStatus)).append("\n");
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

