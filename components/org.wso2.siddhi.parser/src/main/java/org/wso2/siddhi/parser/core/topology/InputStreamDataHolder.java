/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.parser.core.topology;

import org.wso2.siddhi.parser.core.util.EventHolder;
import org.wso2.siddhi.parser.core.util.SiddhiTopologyCreatorConstants;

/**
 * Data Holder to hold required details of Input Streams in {@link SiddhiTopology}.
 */
public class InputStreamDataHolder {
    private String streamName;
    private String streamDefinition;
    private EventHolder eventHolderType;
    private boolean isUserGiven;
    private SubscriptionStrategyDataHolder subscriptionStrategy;
    private boolean isInnerGroupStream;

    public InputStreamDataHolder(String streamName, String streamDefinition, EventHolder eventHolderType,
                                 boolean isUserGiven, SubscriptionStrategyDataHolder subscriptionStrategy) {
        this.streamName = streamName;
        this.streamDefinition = streamDefinition;
        this.eventHolderType = eventHolderType;
        this.isUserGiven = isUserGiven;
        this.subscriptionStrategy = subscriptionStrategy;
        isInnerGroupStream = false;
    }

    public String getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(String streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public String getStreamName() {
        return streamName;
    }

    public SubscriptionStrategyDataHolder getSubscriptionStrategy() {
        return subscriptionStrategy;
    }

    public boolean isUserGiven() {
        return isUserGiven;
    }

    public void setUserGiven(boolean userGiven) {
        isUserGiven = userGiven;
    }

    public EventHolder getEventHolderType() {
        return eventHolderType;
    }

    public boolean isInnerGroupStream() {
        return isInnerGroupStream;
    }

    public void setInnerGroupStream(boolean innerGroupStream) {
        isInnerGroupStream = innerGroupStream;
    }

    public boolean isUserGivenSource() {
        return streamDefinition.toLowerCase().contains(SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER);
    }
}
