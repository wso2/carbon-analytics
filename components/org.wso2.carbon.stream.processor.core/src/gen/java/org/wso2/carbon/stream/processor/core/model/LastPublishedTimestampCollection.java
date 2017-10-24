/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stream.processor.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;

/**
 * Class that holds a list of last published event timestamps
 */
public class LastPublishedTimestampCollection {

    @JsonProperty("publishedTimestamps")
    private Collection<LastPublishedTimestamp> lastPublishedTimestamps;

    public LastPublishedTimestampCollection(Collection<LastPublishedTimestamp> lastPublishedTimestamps) {
        this.lastPublishedTimestamps = lastPublishedTimestamps;
    }

    @ApiModelProperty
    public Collection<LastPublishedTimestamp> getLastPublishedTimestamps() {
        return lastPublishedTimestamps;
    }

    public void setLastPublishedTimestamps(Collection<LastPublishedTimestamp> lastPublishedTimestamps) {
        this.lastPublishedTimestamps = lastPublishedTimestamps;
    }

    public int size() {
        return lastPublishedTimestamps.size();
    }

}
