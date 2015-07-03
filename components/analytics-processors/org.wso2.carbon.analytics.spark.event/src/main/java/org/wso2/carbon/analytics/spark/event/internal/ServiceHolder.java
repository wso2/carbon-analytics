/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.spark.event.internal;

import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * This class holds the OSGI services registered with Declarative service component.
 */
public class ServiceHolder {

    private static EventStreamService eventStreamService;

    private ServiceHolder() {
        /**
         * Avoid instantiation of this class.
         */
    }

    public static EventStreamService getEventStreamService() {
        return ServiceHolder.eventStreamService;
    }

    public static void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.eventStreamService = eventStreamService;
    }
}
