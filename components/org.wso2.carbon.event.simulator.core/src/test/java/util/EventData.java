/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package util;

import org.wso2.siddhi.core.event.Event;

public class EventData {
    private String siddhiAppName;
    private String streamName;
    private Event event;

    public EventData(String siddhiAppName, String streamName, Event event) {
        this.siddhiAppName = siddhiAppName;
        this.streamName = streamName;
        this.event = event;
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public void setSiddhiAppName(String siddhiAppName) {
        this.siddhiAppName = siddhiAppName;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

}
