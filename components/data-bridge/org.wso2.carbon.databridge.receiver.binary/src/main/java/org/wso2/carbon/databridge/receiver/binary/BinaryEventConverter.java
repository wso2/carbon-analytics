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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConstants;
import org.wso2.carbon.databridge.commons.exception.MalformedEventException;
import org.wso2.carbon.databridge.core.EventConverter;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.core.exception.EventConversionException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a implementation EventConverter to create the event from the Binary message.
 * This is used within data bridge to create the event from the row message received.
 *
 */
public class BinaryEventConverter implements EventConverter {
    private static BinaryEventConverter instance = new BinaryEventConverter();

    private BinaryEventConverter() {
    }

    @Override
    public List<Event> toEventList(Object eventBundle, StreamTypeHolder streamTypeHolder) {
        List<String> eventMessages = getSubMessage(BinaryMessageConstants.START_EVENT,
                BinaryMessageConstants.END_EVENT, eventBundle.toString());
        List<Event> eventList = new ArrayList<Event>();
        for (String aEventMsg : eventMessages) {
            eventList.add(getEvent(aEventMsg, streamTypeHolder));
        }
        return eventList;
    }

    public Event getEvent(String eventMessage, StreamTypeHolder streamTypeHolder) throws MalformedEventException {
        String metaDataMessage = null;
        String correlationDataMessage = null;
        String payloadDataMessage = null;
        String arbitraryDataMessage = null;
        String[] eventProps = eventMessage.split("\n");
        Event event = new Event();
        event.setStreamId(eventProps[0].replace(BinaryMessageConstants.STREAM_ID_PREFIX, ""));
        event.setTimeStamp(Long.parseLong(eventProps[1].replace(BinaryMessageConstants.TIME_STAMP_PREFIX, "")));
        List<String> metaData = getSubMessage(BinaryMessageConstants.START_META_DATA,
                BinaryMessageConstants.END_META_DATA, eventMessage);
        if (!metaData.isEmpty()) {
            if (metaData.size() > 1) {
                throw new MalformedEventException("More than one meta data element found on event : " + eventMessage);
            }
            metaDataMessage = metaData.get(0);
        }
        List<String> correlationData = getSubMessage(BinaryMessageConstants.START_CORRELATION_DATA,
                BinaryMessageConstants.END_CORRELATION_DATA, eventMessage);
        if (!correlationData.isEmpty()) {
            if (correlationData.size() > 1) {
                throw new MalformedEventException("More than one correlation data element found on event : " + eventMessage);
            }
            correlationDataMessage = correlationData.get(0);
        }
        List<String> payloadData = getSubMessage(BinaryMessageConstants.START_PAYLOAD_DATA,
                BinaryMessageConstants.END_PAYLOAD_DATA, eventMessage);
        if (!payloadData.isEmpty()) {
            if (payloadData.size() > 1) {
                throw new MalformedEventException("More than one meta payload element found on event : " + eventMessage);
            }
            payloadDataMessage = payloadData.get(0);
        }
        List<String> arbitraryData = getSubMessage(BinaryMessageConstants.START_ARBITRARY_DATA,
                BinaryMessageConstants.END_ARBITRARY_DATA, eventMessage);
        if (!arbitraryData.isEmpty()) {
            if (arbitraryData.size() > 1) {
                throw new MalformedEventException("More than one meta arbitrary element found on event : " + eventMessage);
            }
            arbitraryDataMessage = arbitraryData.get(0);
        }

        AttributeType[][] attributeTypeOrder = streamTypeHolder.getDataType(event.getStreamId());
        if (attributeTypeOrder == null) {
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            if (privilegedCarbonContext.getTenantDomain() == null) {
                privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            }
            streamTypeHolder.reloadStreamTypeHolder();
            attributeTypeOrder = streamTypeHolder.getDataType(event.getStreamId());
            if (attributeTypeOrder == null) {
                throw new EventConversionException("No StreamDefinition for streamId " + event.getStreamId()
                        + " present in cache ");
            }
        }
        event.setMetaData(this.toObjectArray(metaDataMessage, attributeTypeOrder[0],
                BinaryDataReceiverConstants.META_DATA_FIELD));
        event.setCorrelationData(this.toObjectArray(correlationDataMessage, attributeTypeOrder[1],
                BinaryDataReceiverConstants.CORRELATION_DATA_FIELD));
        event.setPayloadData(this.toObjectArray(payloadDataMessage, attributeTypeOrder[2],
                BinaryDataReceiverConstants.PAYLOAD_DATA_FIELD));
        event.setArbitraryDataMap(this.toStringMap(arbitraryDataMessage));
        return event;
    }

    public Object[] toObjectArray(String attributeMessage,
                                  AttributeType[] attributeTypeOrder,
                                  String type) {
        if (attributeTypeOrder != null) {
            if (attributeMessage == null) {
                throw new MalformedEventException("Expected event attribute type: " + type +
                        " but it's is missing in the event");
            }
            Object[] objects = new Object[attributeTypeOrder.length];
            String[] stringAttributes = attributeMessage.split("\n");
            if (stringAttributes.length != attributeTypeOrder.length) {
                throw new MalformedEventException("Expected " + attributeTypeOrder.length +
                        " elements but found " + stringAttributes.length);
            }
            for (int i = 0; i < attributeTypeOrder.length; i++) {
                switch (attributeTypeOrder[i]) {
                    case INT:
                        objects[i] = Integer.parseInt(stringAttributes[i]);
                        break;
                    case LONG:
                        objects[i] = Long.parseLong(stringAttributes[i]);
                        break;
                    case STRING:
                        String stringValue = stringAttributes[i];
                        if (stringValue.isEmpty()) {
                            objects[i] = null;
                        } else {
                            objects[i] = stringValue;
                        }
                        break;
                    case DOUBLE:
                        objects[i] = Double.parseDouble(stringAttributes[i]);
                        break;
                    case FLOAT:
                        objects[i] = Float.parseFloat(stringAttributes[i]);
                        break;
                    case BOOL:
                        objects[i] = Boolean.parseBoolean(stringAttributes[i]);
                        break;
                }
            }
            return objects;
        } else {
            return null;
        }
    }

    public Map<String, String> toStringMap(String attributeMessage) {
        if (attributeMessage != null) {
            String[] attributes = attributeMessage.trim().split("\n");
            Map<String, String> eventProps = new HashMap<String, String>();
            for (String aAttribute : attributes) {
                String[] aProperty = aAttribute.trim().split(BinaryMessageConstants.PARAMS_SEPARATOR);
                if (aProperty.length != 2) {
                    throw new MalformedEventException("Arbitrary data sent in the events are not in " +
                            "expected pattern, found : " + Arrays.toString(aProperty));
                }
                eventProps.put(aProperty[0], aProperty[1]);
            }
            return eventProps;
        }
        return null;
    }


    private List<String> getSubMessage(String startTag, String endTag, String originalMessage) {
        List<String> subMessage = new ArrayList<String>();
        Pattern pattern = Pattern.compile(Pattern.quote(startTag) + "(.*?)" + Pattern.quote(endTag), Pattern.DOTALL);
        Matcher matcher = pattern.matcher(originalMessage);
        while (matcher.find()) {
            subMessage.add(matcher.group(1).trim());
        }
        return subMessage;
    }


    public static BinaryEventConverter getConverter() {
        return instance;
    }

}
