/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.processor.api.receive;

import org.wso2.carbon.event.processor.api.receive.exception.EventReceiverException;

public interface EventReceiver {

    /**
     * Subscribes to a particular event builder identified by the stream definition. After subscribing, the passed in
     * event listener will receive events from the event builder associated with the event builder
     *
     * @param streamId          the stream definition id of the {@link org.wso2.carbon.databridge.commons.StreamDefinition} that is associated with the event builder
     * @param wso2EventListener {@link Wso2EventListener} that will listen to events from a particular event builder
     * @param tenantId          the tenant id of the calling thread
     */
    public void subscribe(String streamId, Wso2EventListener wso2EventListener, int tenantId)
            throws EventReceiverException;

    /**
     * Unsubscribes from a particular event builder for the given stream definition and event listener
     *
     * @param streamId          the stream definition id of {@link org.wso2.carbon.databridge.commons.StreamDefinition} that is associated with the event builder
     * @param wso2EventListener the {@link Wso2EventListener} that needs to be unsubscribed
     * @param tenantId          tenant id of the particular event builder
     */
    public void unsubsribe(String streamId, Wso2EventListener wso2EventListener, int tenantId)
            throws EventReceiverException;

    /**
     * Subscribes to a particular event builder identified by the stream definition. After subscribing, the passed in
     * event listener will receive events from the event builder associated with the event builder
     *
     * @param streamId           the stream definition id of {@link org.wso2.carbon.databridge.commons.StreamDefinition} that is associated with the event builder
     * @param basicEventListener {@link BasicEventListener} that will listen to events from a particular event builder
     * @param tenantId           the tenant id of the calling thread
     */
    public void subscribe(String streamId, BasicEventListener basicEventListener, int tenantId)
            throws EventReceiverException;

    /**
     * Unsubscribes from a particular event builder for the given stream definition and event listener
     *
     * @param streamId           the stream definition id of {@link org.wso2.carbon.databridge.commons.StreamDefinition} instance that is associated with the event builder
     * @param basicEventListener the {@link BasicEventListener} that needs to be unsubscribed
     * @param tenantId           tenant id of the particular event builder
     */
    public void unsubsribe(String streamId, BasicEventListener basicEventListener, int tenantId)
            throws EventReceiverException;

    /**
     * Registers a notification listener that will receive notifications regarding addition/removal of stream definitions
     *
     * @param eventReceiverStreamNotificationListener
     *         the {@link EventReceiverStreamNotificationListener} implementation
     *         object that will receive the notifications
     */
    public void subscribeNotificationListener(
            EventReceiverStreamNotificationListener eventReceiverStreamNotificationListener);

}
