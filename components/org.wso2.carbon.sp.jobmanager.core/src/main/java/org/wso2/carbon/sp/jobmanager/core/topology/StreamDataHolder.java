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

package org.wso2.carbon.sp.jobmanager.core.topology;

import org.wso2.carbon.sp.jobmanager.core.util.EventHolder;

/**
 * Temporary class for enable mapping.
 */
public class StreamDataHolder {

    private String streamDefinition;
    private EventHolder eventHolderType;
    private boolean isUserGiven;

    public StreamDataHolder(String streamDefinition, EventHolder eventHolderType, boolean isUserGiven) {
        this.streamDefinition = streamDefinition;
        this.eventHolderType = eventHolderType;
        this.isUserGiven = isUserGiven;
    }


    public StreamDataHolder(boolean isUserGiven) {
        this.isUserGiven = isUserGiven;
    }

    public boolean isUserGiven() {
        return isUserGiven;
    }

    public String getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(String streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public EventHolder getEventHolderType() {
        return eventHolderType;
    }

    public void setEventHolderType(EventHolder eventHolderType) {
        this.eventHolderType = eventHolderType;
    }
}
