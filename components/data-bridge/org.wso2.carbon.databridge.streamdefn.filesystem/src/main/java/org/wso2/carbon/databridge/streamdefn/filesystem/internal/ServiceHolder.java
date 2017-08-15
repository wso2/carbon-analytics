/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.databridge.streamdefn.filesystem.internal;

import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.stream.core.EventStreamService;

public class ServiceHolder {
    private static EventStreamService eventStreamService;
    private static AbstractStreamDefinitionStore streamDefinitionStore;

    public static EventStreamService getEventStreamService() {
        return eventStreamService;
    }

    public static void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.eventStreamService = eventStreamService;
    }

    public static void setStreamDefinitionStore(AbstractStreamDefinitionStore streamDefinitionStore) {
        ServiceHolder.streamDefinitionStore = streamDefinitionStore;
    }

    public static AbstractStreamDefinitionStore getStreamDefinitionStore() {
        return streamDefinitionStore;
    }
}
