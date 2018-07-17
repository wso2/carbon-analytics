/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.stream.processor.core.ha.transport;

import java.io.Serializable;

/**
 * Class to hold the message and the corresponding getChannelId.
 */
public class EventComposite implements Serializable {
    private String channelId;
    private String sessionId;
    private byte[] message;

    public EventComposite(String sessionId, String channelId, byte[] message) {
        this.sessionId = sessionId;
        this.channelId = channelId;
        this.message = message;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public byte[] getMessage() {
        return message;
    }
}
