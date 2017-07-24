/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.message.tracer.data;

public class EventStreamDef {

    public String streamName = "BAM_MESSAGE_TRACE_FILTER";

    public String version = "1.0.0";

    public String nickName = "MessageTracerFilterAgent";

    public String description = "Publish Message Tracing Event From Web filter";

    public EventStreamDef() {
    }

    public String getStreamName() {
        return streamName;
    }

    public String getVersion() {
        return version;
    }


    public String getNickName() {
        return nickName;
    }

    public String getDescription() {
        return description;
    }
}
