/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.stream.processor.core.util;

import org.wso2.carbon.stream.processor.core.event.queue.EventDataMetaInfo;
import org.wso2.carbon.stream.processor.core.event.queue.EventMetaInfo;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;


/**
 * This is a Util class help to convert from Siddhi event to Binary message.
 */
public class BinaryEventConverter {

    public static ByteBuffer convertToBinaryMessage(QueuedEvent[] queuedEvents)
            throws IOException {
        ByteBuffer messageBuffer = null;
        for (QueuedEvent queuedEvent : queuedEvents) {
            Event event = queuedEvent.getEvent();
            int messageSize = 4 + BinaryMessageConverterUtil.getSize(queuedEvent.getSourceHandlerElementId());
            EventMetaInfo eventMetaInfo = getEventMetaInfo(event);
            String attributes = Arrays.toString(eventMetaInfo.getAttributeTypeOrder());
            messageSize += BinaryMessageConverterUtil.getSize(attributes) + BinaryMessageConverterUtil.getSize
                    (queuedEvent.getSequenceID()) + BinaryMessageConverterUtil.getSize(queuedEvent.getSiddhiAppName())
                    + BinaryMessageConverterUtil.getSize(queuedEvent.getEvent().getTimestamp()) + getEventSize(event);

            messageBuffer = ByteBuffer.wrap(new byte[messageSize]);
            messageBuffer.putInt(queuedEvents.length);
            messageBuffer.putLong(queuedEvent.getSequenceID());
            messageBuffer.putInt((queuedEvent.getSourceHandlerElementId()).length());
            messageBuffer.put(((queuedEvent.getSourceHandlerElementId()).getBytes(Charset.defaultCharset())));
            messageBuffer.putInt((queuedEvent.getSiddhiAppName()).length());
            messageBuffer.put(((queuedEvent.getSiddhiAppName()).getBytes(Charset.defaultCharset())));

            messageBuffer.putInt(attributes.length());
            messageBuffer.put(((attributes).getBytes(Charset.defaultCharset())));
            messageBuffer.putLong(event.getTimestamp());
            if (event.getData() != null && event.getData().length != 0) {
                Object[] data = event.getData();
                for (int i = 0; i < data.length; i++) {
                    Object aData = data[i];
                    BinaryMessageConverterUtil.assignData(aData, messageBuffer);
                }
            }
        }
        return messageBuffer;
    }

    private static int getEventSize(Event event) {
        int eventSize = 8;
        Object[] data = event.getData();
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                Object aData = data[i];
                eventSize += BinaryMessageConverterUtil.getSize(aData);
            }
        }
        return eventSize;
    }

    private static EventMetaInfo getEventMetaInfo(Event event) {
        int eventSize = 8;
        Object[] data = event.getData();
        Attribute.Type[] attributeTypeOrder = new Attribute.Type[data.length];
        EventDataMetaInfo eventDataMetaInfo;
        for (int i = 0; i < data.length; i++) {
            Object aData = data[i];
            eventDataMetaInfo = BinaryMessageConverterUtil.getEventMetaInfo(aData);
            eventSize += eventDataMetaInfo.getEventSize();
            attributeTypeOrder[i] = eventDataMetaInfo.getAttributeType();
        }
        return new EventMetaInfo(eventSize, attributeTypeOrder);
    }
}
