/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * SiddhiAppContent
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-05-31T15:43:24.557Z")
public class SiddhiAppRevision {

    @JsonProperty("revision")
    private String revision = null;

    public SiddhiAppRevision name(String name) {
        return this;
    }


    public SiddhiAppRevision revision(String revision) {
        this.revision = revision;
        return this;
    }

    /**
     * Siddhi revision
     *
     * @return revision
     **/
    @ApiModelProperty(value = "Siddhi revision")
    public String getrevision() {
        return revision;
    }

    public void setrevision(String revision) {
        this.revision = revision;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SiddhiAppRevision siddhiAppContent = (SiddhiAppRevision) o;
        return Objects.equals(this.revision, siddhiAppContent.revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(revision);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SiddhiAppContent {\n");
        sb.append("    revision: ").append(toIndentedString(revision)).append("\n");
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

