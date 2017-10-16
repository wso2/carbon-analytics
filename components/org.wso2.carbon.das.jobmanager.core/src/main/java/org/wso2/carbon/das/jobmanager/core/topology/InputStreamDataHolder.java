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
package org.wso2.carbon.das.jobmanager.core.topology;

import org.wso2.carbon.das.jobmanager.core.util.TransportStrategy;

/**
 * Data Holder to hold required details of Input Streams in {@link SiddhiTopology}
 */
public class InputStreamDataHolder {
    private String streamName;
    private TransportStrategy subscriptionStrategy;
    private boolean isUserGiven;

    public InputStreamDataHolder(String streamName,
                                 TransportStrategy subscriptionStrategy, boolean isUserGiven) {
        this.streamName = streamName;
        this.subscriptionStrategy = subscriptionStrategy;
        this.isUserGiven = isUserGiven;
    }

    public String getStreamName() {
        return streamName;
    }

    public TransportStrategy getSubscriptionStrategy() {
        return subscriptionStrategy;
    }

    public boolean isUserGiven() {
        return isUserGiven;
    }
}
