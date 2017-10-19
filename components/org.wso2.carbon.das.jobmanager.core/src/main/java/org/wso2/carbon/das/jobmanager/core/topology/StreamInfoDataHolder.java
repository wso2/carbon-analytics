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
/**
 * Temporary class for enable mapping*/
package org.wso2.carbon.das.jobmanager.core.topology;

import org.wso2.carbon.das.jobmanager.core.util.EventHolder;

public class StreamInfoDataHolder {

    private String StreamDefinition;
    private EventHolder eventHolderType;
    private boolean isUserGiven;

    public StreamInfoDataHolder(String streamDefinition, EventHolder eventHolderType, boolean isUserGiven) {
        StreamDefinition = streamDefinition;
        this.eventHolderType = eventHolderType;
        this.isUserGiven = isUserGiven;
    }


    public StreamInfoDataHolder(boolean isUserGiven) {
        this.isUserGiven = isUserGiven;
    }

    public boolean isUserGiven() {
        return isUserGiven;
    }

    public void setUserGiven(boolean userGiven) {
        isUserGiven = userGiven;
    }

    public String getStreamDefinition() {
        return StreamDefinition;
    }

    public EventHolder getEventHolderType() {
        return eventHolderType;
    }

    public void setEventHolderType(EventHolder eventHolderType) {
        this.eventHolderType = eventHolderType;
    }

    public void setStreamDefinition(String streamDefinition) {
        StreamDefinition = streamDefinition;
    }
}
