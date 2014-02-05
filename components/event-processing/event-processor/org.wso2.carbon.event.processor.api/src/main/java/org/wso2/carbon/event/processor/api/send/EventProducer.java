/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.event.processor.api.send;

import org.wso2.carbon.event.processor.api.passthrough.PassthroughSenderConfigurator;
import org.wso2.carbon.event.processor.api.send.exception.EventProducerException;

public interface EventProducer {

    /**
     *
     * @param tenantId
     * @param streamId
     * @param eventSender
     * @throws EventProducerException
     */
    public void subscribe(int tenantId, String streamId, EventSender eventSender)
            throws EventProducerException;

    /**
     *
     * @param streamId
     * @param eventSender
     * @throws EventProducerException
     */
    public void subscribe(String streamId, EventSender eventSender)
            throws EventProducerException;

    /**
     *
     * @param tenantId
     * @param streamId
     * @param eventSender
     * @throws EventProducerException
     */
    public void unsubscribe(int tenantId, String streamId, EventSender eventSender)
            throws EventProducerException;

    /**
     *
     * @param eventProducerStreamNotificationListener
     */
    public void subscribeNotificationListener(EventProducerStreamNotificationListener eventProducerStreamNotificationListener);

    /**
     *
     * @param passthroughSenderConfigurator
     */
    public void registerPassthroughSenderConfigurator(PassthroughSenderConfigurator passthroughSenderConfigurator);

}
