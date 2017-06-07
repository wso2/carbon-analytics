/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.databridge.receiver.binary;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil;
import org.wso2.carbon.databridge.commons.exception.MalformedEventException;
import org.wso2.carbon.databridge.core.EventConverter;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.core.exception.EventConversionException;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a implementation EventConverter to create the event from the Binary message.
 * This is used within data bridge to create the event from the row message received.
 */
public class BinaryEventConverter implements EventConverter {
    private static BinaryEventConverter instance = new BinaryEventConverter();

    private BinaryEventConverter() {
    }

    @Override
    public List<Event> toEventList(Object eventBundle, StreamTypeHolder streamTypeHolder) {

        ByteBuffer byteBuffer = ByteBuffer.wrap((byte[]) eventBundle);
        int sessionIdSize = byteBuffer.getInt();
        byteBuffer.get(new byte[sessionIdSize]);
        int events = byteBuffer.getInt();

        List<Event> eventList = new ArrayList<>();
        for (int i = 0; i < events; i++) {
            int eventSize = byteBuffer.getInt();
            byte[] bytes= new byte[eventSize];
            byteBuffer.get(bytes);
            ByteBuffer eventByteBuffer = ByteBuffer.wrap(bytes);
            eventList.add(getEvent(eventByteBuffer, streamTypeHolder));
        }
        return eventList;
    }

    @Override
    public int getSize(Object eventBundle) {
        return ((byte[])eventBundle).length;
    }

    @Override
    public int getNumberOfEvents(Object eventBundle) {
        ByteBuffer byteBuffer = ByteBuffer.wrap((byte[]) eventBundle);
        int sessionIdSize = byteBuffer.getInt();
        byteBuffer.get(new byte[sessionIdSize]);
        return byteBuffer.getInt();
    }

    public Event getEvent(ByteBuffer byteBuffer, StreamTypeHolder streamTypeHolder) throws MalformedEventException {
        long timeStamp = byteBuffer.getLong();
        int streamIdSize = byteBuffer.getInt();
        String streamId = BinaryMessageConverterUtil.getString(byteBuffer, streamIdSize);

        Event event = new Event();
        event.setStreamId(streamId);
        event.setTimeStamp(timeStamp);

        AttributeType[][] attributeTypeOrder = streamTypeHolder.getDataType(event.getStreamId());
        if (attributeTypeOrder == null) {
            streamTypeHolder.reloadStreamTypeHolder();
            attributeTypeOrder = streamTypeHolder.getDataType(event.getStreamId());
            if (attributeTypeOrder == null) {
                throw new EventConversionException("No StreamDefinition for streamId " + event.getStreamId()
                        + " present in cache ");
            }
        }

        event.setMetaData(this.toObjectArray(byteBuffer, attributeTypeOrder[0],
                BinaryDataReceiverConstants.META_DATA_FIELD));
        event.setCorrelationData(this.toObjectArray(byteBuffer, attributeTypeOrder[1],
                BinaryDataReceiverConstants.CORRELATION_DATA_FIELD));
        event.setPayloadData(this.toObjectArray(byteBuffer, attributeTypeOrder[2],
                BinaryDataReceiverConstants.PAYLOAD_DATA_FIELD));
        event.setArbitraryDataMap(this.toStringMap(byteBuffer));
        return event;
    }

    public Object[] toObjectArray(ByteBuffer byteBuffer,
                                  AttributeType[] attributeTypeOrder,
                                  String type) {
        if (attributeTypeOrder != null) {
            if (byteBuffer == null) {
                throw new MalformedEventException("Expected event attribute type: " + type +
                        " but it's is missing in the event");
            }
            Object[] objects = new Object[attributeTypeOrder.length];
            for (int i = 0; i < attributeTypeOrder.length; i++) {
                switch (attributeTypeOrder[i]) {
                    case INT:
                        objects[i] = byteBuffer.getInt();
                        break;
                    case LONG:
                        objects[i] = byteBuffer.getLong();
                        break;
                    case STRING:
                        int stringSize = byteBuffer.getInt();
                        if (stringSize == 0) {
                            objects[i] = null;
                        } else {
                            objects[i] = BinaryMessageConverterUtil.getString(byteBuffer, stringSize);
                        }
                        break;
                    case DOUBLE:
                        objects[i] = byteBuffer.getDouble();
                        break;
                    case FLOAT:
                        objects[i] = byteBuffer.getFloat();
                        break;
                    case BOOL:
                        objects[i] = byteBuffer.get() == 1;
                        break;
                }
            }
            return objects;
        } else {
            return null;
        }
    }

    public Map<String, String> toStringMap(ByteBuffer byteBuffer) {
        if (byteBuffer != null) {
            Map<String, String> eventProps = new HashMap<String, String>();

            while (byteBuffer.remaining() > 0) {
                int keySize = byteBuffer.getInt();
                String key = BinaryMessageConverterUtil.getString(byteBuffer, keySize);
                int valueSize = byteBuffer.getInt();
                String value = BinaryMessageConverterUtil.getString(byteBuffer,valueSize);
                eventProps.put(key, value);
            }
            return eventProps;
        }
        return null;
    }

    public static BinaryEventConverter getConverter() {
        return instance;
    }

}
