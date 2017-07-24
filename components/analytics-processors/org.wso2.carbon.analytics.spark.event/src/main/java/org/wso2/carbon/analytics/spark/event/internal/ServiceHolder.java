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

import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * This class holds the OSGI services registered with declarative service component.
 */
public class ServiceHolder {

    private static EventStreamService eventStreamService;
    
    private static TaskService taskService;
    
    private static EventManagementService eventManagementService;
    
    private static EventPublisherManagementService eventPublisherManagementService;
    
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

    public static TaskService getTaskService() {
        return taskService;
    }

    public static void setTaskService(TaskService taskService) {
        ServiceHolder.taskService = taskService;
    }
    
    public static EventManagementService getEventManagementService() {
        return eventManagementService;
    }

    public static void setEventManagementService(EventManagementService eventManagementService) {
        ServiceHolder.eventManagementService = eventManagementService;
    }
    
    public static void setEventPublisherManagementService(EventPublisherManagementService eventPublisherManagementService) {
        ServiceHolder.eventPublisherManagementService = eventPublisherManagementService;
    }
    
    public static EventPublisherManagementService getEventPublisherManagementService() {
        return eventPublisherManagementService;
    }
    
}
