/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.manager.core;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;

public interface EventManagementService {

    /**
     * This method returns the {@link ManagementModeInfo} which contains the node type and its configurations
     *
     * @return {@link ManagementModeInfo}
     */
    ManagementModeInfo getManagementModeInfo();

    /**
     * This method specifies the node type
     *
     * @param manager manager node can be either a {@link EventProcessorManagementService},
     *                {@link EventReceiverManagementService} or {@link EventPublisherManagementService}
     */
    void subscribe(Manager manager);

    /**
     * This method unsubscribes the node according to the {@link Manager} type
     *
     * @param manager manager node can be either a {@link EventProcessorManagementService},
     *                {@link EventReceiverManagementService} or {@link EventPublisherManagementService}
     */
    void unsubscribe(Manager manager);

    /**
     * This method syncs a particular event with the other node. Depending on the node type and address, the events
     * will be sent
     *
     * @param syncId stream id for which StreamRuntimeInfo needed in the StreamRuntimeInfo map
     * @param type   syncing event type
     * @param event  the event which is syncing with the other node
     */
    void syncEvent(String syncId, Manager.ManagerType type, Event event);

    /**
     * This method registers the events in the EventPublisher which needed to be synced
     *
     * @param eventSync contains the stream id
     * @param type the manager type {Receiver, Processor, Publisher}
     */
    void registerEventSync(EventSync eventSync, Manager.ManagerType type);

    /**
     * This method unregisters the events in the EventPublisher which scheduled to be synced
     *
     * @param syncId id of the events to be synced by the EventPublisher
     * @param type the manager type {Receiver, Processor, Publisher}
     */
    void unregisterEventSync(String syncId, Manager.ManagerType type);

    /**
     * This method updates the hazelcast map with the latest processed event details
     *
     * @param publisherName event publisher name
     * @param tenantId      tenant id
     * @param timestamp     hazelcast cluster time
     */
    void updateLatestEventSentTime(String publisherName, int tenantId, long timestamp);

    /**
     * This method gets the latest event processed time
     *
     * @param publisherName event publisher name
     * @param tenantId      tenant id
     */
    long getLatestEventSentTime(String publisherName, int tenantId);

    /**
     * This method return hazelcast cluster time
     */
    long getClusterTimeInMillis();

}
