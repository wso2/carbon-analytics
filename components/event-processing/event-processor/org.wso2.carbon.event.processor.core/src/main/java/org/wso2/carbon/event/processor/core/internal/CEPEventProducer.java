/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.processor.core.internal;

import org.wso2.carbon.event.processor.api.passthrough.PassthroughSenderConfigurator;
import org.wso2.carbon.event.processor.api.send.EventProducer;
import org.wso2.carbon.event.processor.api.send.EventProducerStreamNotificationListener;
import org.wso2.carbon.event.processor.api.send.EventSender;
import org.wso2.carbon.event.processor.api.send.exception.EventProducerException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;


public class CEPEventProducer implements EventProducer {


    private CarbonEventProcessorService carbonEventProcessorService;

    public CEPEventProducer(CarbonEventProcessorService carbonEventProcessorService) {
        this.carbonEventProcessorService = carbonEventProcessorService;
    }

    @Override
    public void subscribe(int tenantId, String streamId, EventSender eventSender) throws EventProducerException {
        carbonEventProcessorService.subscribeStreamListener(streamId, eventSender);
    }

    @Override
    public void subscribe(String streamId, EventSender eventSender) throws EventProducerException {
        carbonEventProcessorService.subscribeStreamListener(streamId, eventSender);
    }

    @Override
    public void unsubscribe(int tenantId, String streamId, EventSender eventSender) throws EventProducerException {
        carbonEventProcessorService.unsubscribeStreamListener(streamId, eventSender, tenantId);
    }

    @Override
    public void subscribeNotificationListener(EventProducerStreamNotificationListener eventProducerStreamNotificationListener) {
        EventProcessorValueHolder.registerNotificationListener(eventProducerStreamNotificationListener);
        EventProcessorValueHolder.getEventProcessorService().notifyAllStreamsToFormatter();
    }

    @Override
    public void registerPassthroughSenderConfigurator(PassthroughSenderConfigurator passthroughSenderConfigurator) {
        carbonEventProcessorService.registerPassthroughSenderConfigurator(passthroughSenderConfigurator);
    }
}
