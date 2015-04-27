/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.receiver.core.internal.type.map;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.InputMapper;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.MapInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverProcessingException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapInputMapper implements InputMapper {
    private int noMetaData;
    private int noCorrelationData;
    private int noPayloadData;
    private StreamDefinition streamDefinition;
    private Object[] attributePositionKeyMap = null;
    private EventReceiverConfiguration eventReceiverConfiguration = null;

    public MapInputMapper(EventReceiverConfiguration eventReceiverConfiguration,
                          StreamDefinition streamDefinition)
            throws EventReceiverConfigurationException {
        this.eventReceiverConfiguration = eventReceiverConfiguration;
        this.streamDefinition = streamDefinition;

        if (eventReceiverConfiguration != null && eventReceiverConfiguration.getInputMapping() instanceof MapInputMapping) {
            MapInputMapping mapInputMapping = (MapInputMapping) eventReceiverConfiguration.getInputMapping();
            if (mapInputMapping.isCustomMappingEnabled()) {

                Map<Integer, Object> positionKeyMap = new HashMap<Integer, Object>();
                for (InputMappingAttribute inputMappingAttribute : mapInputMapping.getInputMappingAttributes()) {
                    positionKeyMap.put(inputMappingAttribute.getToStreamPosition(), inputMappingAttribute.getFromElementKey());
                    if (positionKeyMap.get(inputMappingAttribute.getToStreamPosition()) == null) {
                        this.attributePositionKeyMap = null;
                        throw new EventReceiverStreamValidationException("Error creating map mapping. '"+inputMappingAttribute.getToElementKey()+"' position not found.",streamDefinition.getStreamId());
                    }
                }
                this.attributePositionKeyMap = new Object[positionKeyMap.size()];
                for (int i = 0; i < attributePositionKeyMap.length; i++) {
                    attributePositionKeyMap[i] = positionKeyMap.get(i);
                }
            } else {
                this.noMetaData = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
                this.noCorrelationData += streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
                this.noPayloadData += streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
            }

        }
    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventReceiverProcessingException {
        if (attributePositionKeyMap == null) {
            throw new EventReceiverProcessingException("Input mapping is not available for the current input stream definition:");
        }
        Object[] outObjArray;
        if (obj instanceof Map) {
            Map eventMap = (Map) obj;
            List<Object> outObjList = new ArrayList<Object>();
            for (int i = 0; i < this.attributePositionKeyMap.length; i++) {
                outObjList.add(eventMap.get(this.attributePositionKeyMap[i]));
            }
            outObjArray = outObjList.toArray();
        } else {
            throw new EventReceiverProcessingException("Received event object is not of type map." + this.getClass() + " cannot convert this event.");
        }

        return outObjArray;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventReceiverProcessingException {

        Object attributeArray[] = new Object[noMetaData + noCorrelationData + noPayloadData];
        int attributeCount = 0;
        if (obj instanceof Map) {
            Map<Object, Object> eventMap = (Map<Object, Object>) obj;

            for (Map.Entry<Object, Object> eventAttribute : eventMap.entrySet()) {
                boolean setFlag = false;

                if (noMetaData > 0) {
                    for (Attribute metaData : streamDefinition.getMetaData()) {
                        if (eventAttribute.getKey().equals(EventReceiverConstants.META_DATA_PREFIX + metaData.getName())) {
                            attributeArray[attributeCount++] = eventAttribute.getValue();
                            setFlag = true;
                            break;
                        }
                    }
                }

                if (noCorrelationData > 0 && !setFlag) {
                    for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                        if (eventAttribute.getKey().equals(EventReceiverConstants.CORRELATION_DATA_PREFIX + correlationData.getName())) {
                            attributeArray[attributeCount++] = eventAttribute.getValue();
                            setFlag = true;
                            break;
                        }
                    }
                }

                if (noPayloadData > 0 && !setFlag) {
                    for (Attribute payloadData : streamDefinition.getPayloadData()) {
                        if (eventAttribute.getKey().equals(payloadData.getName())) {
                            attributeArray[attributeCount++] = eventAttribute.getValue();
                            break;
                        }
                    }
                }

            }

            if (noMetaData + noCorrelationData + noPayloadData != attributeCount) {
                throw new EventReceiverProcessingException("Event attributes are not matching with the stream : " + this.eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion());
            }

        } else {
            throw new EventReceiverProcessingException("Received event object is not of type map." + this.getClass() + " cannot convert this event.");
        }

        return attributeArray;

    }

    @Override
    public Attribute[] getOutputAttributes() {
        MapInputMapping mapInputMapping = (MapInputMapping) eventReceiverConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = mapInputMapping.getInputMappingAttributes();
        return EventReceiverConfigurationHelper.getAttributes(inputMappingAttributes);
    }

}
