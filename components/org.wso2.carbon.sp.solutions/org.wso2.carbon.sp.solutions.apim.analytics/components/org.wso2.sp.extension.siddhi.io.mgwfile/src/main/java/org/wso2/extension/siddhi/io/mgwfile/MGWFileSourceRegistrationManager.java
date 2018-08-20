/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.extension.siddhi.io.mgwfile;

import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class which manages the Databridge receiver connections
 */
public class MGWFileSourceRegistrationManager {

    private static Map<String, SourceEventListener> streamSpecificEventListenerMap = new ConcurrentHashMap<>();

    static Map<String, SourceEventListener> getStreamSpecificEventListenerMap() {
        return streamSpecificEventListenerMap;
    }

    public static void registerEventConsumer(String streamId, SourceEventListener sourceEventListener) {
        streamSpecificEventListenerMap.put(streamId, sourceEventListener);
    }

    public static void unregisterEventConsumer(String streamId) {
            streamSpecificEventListenerMap.remove(streamId);
    }

}
