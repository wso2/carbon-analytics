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

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to hold the snapshot state of the active node as well as the last processed events timestamp
 * to pass to the passive node in 2 node minimum HA.
 */
public class HAStateSyncObject {

    /**
     * Indicates if the object is populated with snapshot
     */
    @JsonProperty("hasState")
    private boolean hasState;

    @JsonProperty("sourceTimestamps")
    private Map<String, Long> sourceTimestamps = new HashMap<>();

    @JsonProperty("snapshotMap")
    private Map<String, byte[]> snapshotMap = new HashMap<>();

    public HAStateSyncObject(Map<String, Long> sourceTimestamps, Map<String, byte[]> snapshotMap) {
        this.sourceTimestamps = sourceTimestamps;
        this.snapshotMap = snapshotMap;
        this.hasState = true;
    }

    public HAStateSyncObject(boolean hasState) {
        this.hasState = hasState;
    }

    public Map<String, Long> getSourceTimestamps() {
        return sourceTimestamps;
    }

    public void setSourceTimestamps(Map<String, Long> sourceTimestamps) {
        this.sourceTimestamps = sourceTimestamps;
    }

    public Map<String, byte[]> getSnapshotMap() {
        return snapshotMap;
    }

    public void setSnapshotMap(Map<String, byte[]> snapshotMap) {
        this.snapshotMap = snapshotMap;
    }

    public boolean hasState() {
        return hasState;
    }

    public void setHasState(boolean hasState) {
        this.hasState = hasState;
    }
}
