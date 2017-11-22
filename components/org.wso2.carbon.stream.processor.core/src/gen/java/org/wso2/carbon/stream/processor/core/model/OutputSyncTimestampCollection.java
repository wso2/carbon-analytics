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
 * Class that holds a list of last timestamps at sink level and record table level
 */
public class OutputSyncTimestampCollection {

    @JsonProperty("publishedTimestamps")
    private Collection<OutputSyncTimestamps> lastPublishedTimestamps;

    @JsonProperty("recordTableTimestamps")
    private Collection<OutputSyncTimestamps> recordTableLastUpdatedTimestamps;

    public OutputSyncTimestampCollection(Collection<OutputSyncTimestamps> lastPublishedTimestamps,
                                         Collection<OutputSyncTimestamps> recordTableLastUpdatedTimestamps) {
        this.lastPublishedTimestamps = lastPublishedTimestamps;
        this.recordTableLastUpdatedTimestamps = recordTableLastUpdatedTimestamps;
    }

    @ApiModelProperty
    public Collection<OutputSyncTimestamps> getLastPublishedTimestamps() {
        return lastPublishedTimestamps;
    }

    public void setLastPublishedTimestamps(Collection<OutputSyncTimestamps> lastPublishedTimestamps) {
        this.lastPublishedTimestamps = lastPublishedTimestamps;
    }

    @ApiModelProperty
    public Collection<OutputSyncTimestamps> getRecordTableLastUpdatedTimestamps() {
        return recordTableLastUpdatedTimestamps;
    }

    public void setRecordTableLastUpdatedTimestamps(Collection<OutputSyncTimestamps> recordTableLastUpdatedTimestamps) {
        this.recordTableLastUpdatedTimestamps = recordTableLastUpdatedTimestamps;
    }

    public int sizeOfSinkTimestamps() {
        return lastPublishedTimestamps.size();
    }

    public int sizeOfRecordTableTimestamps() {
        return recordTableLastUpdatedTimestamps.size();
    }

}
