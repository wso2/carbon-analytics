/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.manager.core.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;


public class EventManagementServiceValueHolder {
    private static EventManagementService eventManagementService;
    private static HazelcastInstance hazelcastInstance;

    public static EventManagementService getEventManagementService() {
        return eventManagementService;
    }

    public static void setEventManagementService(EventManagementService eventManagementService) {
        EventManagementServiceValueHolder.eventManagementService = eventManagementService;
    }

    public static void registerHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventManagementServiceValueHolder.hazelcastInstance = hazelcastInstance;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

//    public static EventProcessorManagementService getEventProcessorManagementService() {
//        return eventProcessorManagementService;
//    }
//
//    public static void registerEventProcessorManagementService(EventProcessorManagementService eventProcessorManagementService) {
//        EventManagementServiceValueHolder.eventProcessorManagementService = eventProcessorManagementService;
//    }
//
//    public static EventReceiverManagementService getEventReceiverManagementService() {
//        return eventReceiverManagementService;
//    }
//
//    public static void registerEventReceiverManagementService(EventReceiverManagementService eventReceiverManagementService) {
//        EventManagementServiceValueHolder.eventReceiverManagementService = eventReceiverManagementService;
//    }

//    public static EventPublisherManagementService getEventPublisherManagementService() {
//        return eventPublisherManagementService;
//    }
//
//    public static void registerEventPublisherManagementService(EventPublisherManagementService eventPublisherManagementService) {
//        EventManagementServiceValueHolder.eventPublisherManagementService = eventPublisherManagementService;
//    }
}
