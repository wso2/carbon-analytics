/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.siddhi.store.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * This class represents the bean class for the response of the query api
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-11-01T11:26:25.925Z")
public class ModelApiResponse {
    @JsonProperty("records")
    private List<Record> records = null;

    public ModelApiResponse records(List<Record> records) {
        this.records = records;
        return this;
    }

    public ModelApiResponse addRecordsItem(Record recordsItem) {
        if (this.records == null) {
            this.records = new ArrayList<Record>();
        }
        this.records.add(recordsItem);
        return this;
    }

    /**
     * Get records
     *
     * @return records
     */
    @ApiModelProperty(value = "")
    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelApiResponse _apiResponse = (ModelApiResponse) o;
        return Objects.equals(this.records, _apiResponse.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(records);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelApiResponse {\n");

        sb.append("    records: ").append(toIndentedString(records)).append("\n");
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

