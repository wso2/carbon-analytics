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
package org.wso2.carbon.event.receiver.core.internal.type.json;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.InputMapper;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.JSONInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverProcessingException;
import org.wso2.carbon.event.receiver.core.exception.InvalidPropertyValueException;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSONInputMapper implements InputMapper {

    private static final Log log = LogFactory.getLog(JSONInputMapper.class);
    private EventReceiverConfiguration eventReceiverConfiguration = null;
    private List<JsonPathData> attributeJsonPathDataList = new ArrayList<JsonPathData>();
    private int noMetaData;
    private int noCorrelationData;
    private int noPayloadData;
    private StreamDefinition streamDefinition;

    public JSONInputMapper(EventReceiverConfiguration eventReceiverConfiguration,
                           StreamDefinition streamDefinition)
            throws EventReceiverConfigurationException {
        this.eventReceiverConfiguration = eventReceiverConfiguration;
        this.streamDefinition = streamDefinition;

        if (eventReceiverConfiguration != null && eventReceiverConfiguration.getInputMapping() instanceof JSONInputMapping) {
            JSONInputMapping jsonInputMapping = (JSONInputMapping) eventReceiverConfiguration.getInputMapping();
            if (jsonInputMapping.isCustomMappingEnabled()) {
                createAttributeJsonPathList(streamDefinition, jsonInputMapping.getInputMappingAttributes());
            } else {
                this.noMetaData = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
                this.noCorrelationData += streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
                this.noPayloadData += streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
            }
        }
    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventReceiverProcessingException {
        Object outObject = null;
        if (obj instanceof String) {
            String jsonString = (String) obj;
            if (jsonString.startsWith(EventReceiverConstants.JSON_ARRAY_START_CHAR)) {
                outObject = processMultipleEvents(obj);
            } else {
                outObject = processSingleEvent(obj);
            }
        }
        return outObject;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventReceiverProcessingException {

        Object outObject = null;
        if (obj instanceof String) {
            String jsonString = (String) obj;
            if (jsonString.startsWith(EventReceiverConstants.JSON_ARRAY_START_CHAR)) {
                outObject = processTypedMultipleEvents(obj);
            } else {
                outObject = processTypedSingleEvent(obj);
            }
        }
        return outObject;

    }

    @Override
    public Attribute[] getOutputAttributes() {
        JSONInputMapping jsonInputMapping = (JSONInputMapping) eventReceiverConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = jsonInputMapping.getInputMappingAttributes();
        return EventReceiverConfigurationHelper.getAttributes(inputMappingAttributes);
    }

    private Event[] processMultipleEvents(Object obj) throws EventReceiverProcessingException {
        Event[] events = null;
        if (obj instanceof String) {
            String stringEvents = (String) obj;
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(stringEvents);
                events = new Event[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    events[i] = processSingleEvent(jsonArray.getJSONObject(i).toString());
                }
            } catch (JSONException e) {
                throw new EventReceiverProcessingException("Error in parsing JSON: ", e);
            }
        }
        return events;
    }

    private Event processSingleEvent(Object obj) throws EventReceiverProcessingException {
        Object[] outObjArray = null;
        StreamDefinition outStreamDefinition = this.streamDefinition;
        int metaDataCount = outStreamDefinition.getMetaData() != null ? outStreamDefinition.getMetaData().size() : 0;
        int correlationDataCount = outStreamDefinition.getCorrelationData() != null ? outStreamDefinition.getCorrelationData().size() : 0;
        int payloadDataCount = outStreamDefinition.getPayloadData() != null ? outStreamDefinition.getPayloadData().size() : 0;
        Object[] metaDataArray = new Object[metaDataCount];
        Object[] correlationDataArray = new Object[correlationDataCount];
        Object[] payloadDataArray = new Object[payloadDataCount];
        if (obj instanceof String) {
            String jsonString = (String) obj;
            List<Object> objList = new ArrayList<Object>();
            for (JsonPathData jsonPathData : attributeJsonPathDataList) {
                JsonPath jsonPath = jsonPathData.getJsonPath();
                AttributeType type = jsonPathData.getType();
                try {
                    Object resultObject = jsonPath.read(jsonString);
                    Object returnedObj = null;

                    if (resultObject == null) {
                        if (jsonPathData.getDefaultValue() != null && !jsonPathData.getDefaultValue().isEmpty()) {
                            returnedObj = getPropertyValue(jsonPathData.getDefaultValue(), type);
                            log.warn("Unable to parse JSONPath to retrieve required attribute. Sending defaults.");
                        } else if (!(AttributeType.STRING.equals(jsonPathData.getType()))) {
                            throw new InvalidPropertyValueException("Found Invalid property value null for attribute ");
                        }
                    } else {
                        try {
                            returnedObj = getPropertyValue(resultObject, type);
                        } catch (NumberFormatException e) {
                            if ((!AttributeType.STRING.equals(type)) && jsonPathData.getDefaultValue() != null) {
                                returnedObj = getPropertyValue(jsonPathData.getDefaultValue(), type);
                            } else {
                                throw e;
                            }
                        }
                    }
                    objList.add(returnedObj);
                } catch (NumberFormatException e) {
                    log.error("Unable to cast the input data to required type :" + type + " ,hence dropping the event " + obj.toString(), e);
                    return null;
                } catch (InvalidPathException e) {
                    log.error("Could not find any matches for the incoming event with JSONPath : " + jsonPath.toString() + " ,hence dropping the event " + obj.toString());
                    return null;
                } catch (InvalidPropertyValueException e) {
                    log.error(e.getMessage() + " ,hence dropping the event : " + obj.toString());
                    return null;
                }
            }
            outObjArray = objList.toArray(new Object[objList.size()]);
        }
        return EventReceiverUtil.getEventFromArray(outObjArray, outStreamDefinition, metaDataArray, correlationDataArray, payloadDataArray);
    }

    private Event[] processTypedMultipleEvents(Object obj)
            throws EventReceiverProcessingException {
        Event[] eventArray;
        String events = (String) obj;
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(events);
            eventArray = new Event[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                eventArray[i] = processTypedSingleEvent(jsonArray.getJSONObject(i).toString());
            }
        } catch (JSONException e) {
            throw new EventReceiverProcessingException("Error in parsing JSON: ", e);
        }
        return eventArray;
    }

    private Event processTypedSingleEvent(Object obj)
            throws EventReceiverProcessingException {

        Object attributeArray[] = new Object[noMetaData + noCorrelationData + noPayloadData];
        int attributeCount = 0;
        StreamDefinition outStreamDefinition = this.streamDefinition;
        int metaDataCount = outStreamDefinition.getMetaData() != null ? outStreamDefinition.getMetaData().size() : 0;
        int correlationDataCount = outStreamDefinition.getCorrelationData() != null ? outStreamDefinition.getCorrelationData().size() : 0;
        int payloadDataCount = outStreamDefinition.getPayloadData() != null ? outStreamDefinition.getPayloadData().size() : 0;
        Object[] metaDataArray = new Object[metaDataCount];
        Object[] correlationDataArray = new Object[correlationDataCount];
        Object[] payloadDataArray = new Object[payloadDataCount];

        try {
            if (obj instanceof String) {
                String jsonString = (String) obj;

                if (noMetaData > 0) {
                    JsonPath jsonPath = JsonPath.compile("$." + EventReceiverConstants.EVENT_PARENT_TAG + "." + EventReceiverConstants.EVENT_META_TAG);
                    Map<Object, Object> eventMap = jsonPath.read(jsonString);
                    if (eventMap == null) {
                        throw new EventReceiverProcessingException("Missing event MetaData attributes, Event does not match with the stream : " + this.eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion());
                    } else {
                        for (Attribute metaData : streamDefinition.getMetaData()) {
                            if (eventMap.containsKey(metaData.getName())) {
                                attributeArray[attributeCount++] = getPropertyValue(eventMap.get(metaData.getName()), metaData.getType());
                            } else {
                                if (AttributeType.STRING.equals(metaData.getType())) {
                                    attributeArray[attributeCount++] = getPropertyValue(null, metaData.getType());
                                } else {
                                    throw new InvalidPropertyValueException("Attribute " + metaData.getName() + " tag not found in the event hence Dropping event " + obj.toString());
                                }
                            }
                        }
                    }
                }

                if (noCorrelationData > 0) {
                    JsonPath jsonPath = JsonPath.compile("$." + EventReceiverConstants.EVENT_PARENT_TAG + "." + EventReceiverConstants.EVENT_CORRELATION_TAG);
                    Map<Object, Object> eventMap = jsonPath.read(jsonString);
                    if (eventMap == null) {
                        throw new EventReceiverProcessingException("Missing CorrelationData attributes, Event does not match with the stream : " + this.eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion());
                    } else {
                        for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                            if (eventMap.containsKey(correlationData.getName())) {
                                attributeArray[attributeCount++] = getPropertyValue(eventMap.get(correlationData.getName()), correlationData.getType());
                            } else {
                                if (AttributeType.STRING.equals(correlationData.getType())) {
                                    attributeArray[attributeCount++] = getPropertyValue(null, correlationData.getType());
                                } else {
                                    throw new InvalidPropertyValueException("Attribute " + correlationData.getName() + " tag not found in the event hence Dropping event " + obj.toString());
                                }
                            }
                        }
                    }
                }
                if (noPayloadData > 0) {
                    JsonPath jsonPath = JsonPath.compile("$." + EventReceiverConstants.EVENT_PARENT_TAG + "." + EventReceiverConstants.EVENT_PAYLOAD_TAG);
                    Map<Object, Object> eventMap = jsonPath.read(jsonString);
                    if (eventMap == null) {
                        throw new EventReceiverProcessingException("Missing PayloadData attributes, Event does not match with the stream : " + this.eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion());
                    } else {
                        for (Attribute payloadData : streamDefinition.getPayloadData()) {
                            if (eventMap.containsKey(payloadData.getName())) {
                                attributeArray[attributeCount++] = getPropertyValue(eventMap.get(payloadData.getName()), payloadData.getType());
                            } else {
                                if (AttributeType.STRING.equals(payloadData.getType())) {
                                    attributeArray[attributeCount++] = getPropertyValue(null, payloadData.getType());
                                } else {
                                    throw new InvalidPropertyValueException("Attribute " + payloadData.getName() + " tag not found in the event hence Dropping event " + obj.toString());
                                }
                            }
                        }
                    }
                }
                if (noMetaData + noCorrelationData + noPayloadData != attributeCount) {
                    throw new EventReceiverProcessingException("Event attributes are not matching with the stream : " + this.eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion());
                }
            }
            return EventReceiverUtil.getEventFromArray(attributeArray, outStreamDefinition, metaDataArray, correlationDataArray, payloadDataArray);
        } catch (InvalidPropertyValueException e) {
            log.error(e.getMessage() + ", hence dropping the event : " + obj.toString());
            return null;
        } catch (NumberFormatException e) {
            log.error("Unable to cast the input data to required type, hence dropping the event: " + obj.toString(), e);
            return null;
        }
    }


    private Object getPropertyValue(Object propertyValue, AttributeType attributeType) throws InvalidPropertyValueException {

        if ((!AttributeType.STRING.equals(attributeType)) && propertyValue == null) {
            throw new InvalidPropertyValueException("Found Invalid property value 'null' for attribute of type " + attributeType);
        }

        if (AttributeType.BOOL.equals(attributeType)) {
            return Boolean.parseBoolean(propertyValue.toString());
        } else if (AttributeType.DOUBLE.equals(attributeType)) {
            return Double.parseDouble(propertyValue.toString());
        } else if (AttributeType.FLOAT.equals(attributeType)) {
            return Float.parseFloat(propertyValue.toString());
        } else if (AttributeType.INT.equals(attributeType)) {
            return Integer.parseInt(propertyValue.toString());
        } else if (AttributeType.LONG.equals(attributeType)) {
            return Long.parseLong(propertyValue.toString());
        } else {
            return propertyValue.toString();
        }

    }

    private class JsonPathData {
        private AttributeType type;
        private JsonPath jsonPath;
        private String defaultValue;

        public JsonPathData(JsonPath jsonPath, AttributeType type, String defaultValue) {
            this.type = type;
            this.jsonPath = jsonPath;
            this.defaultValue = defaultValue;
        }

        private String getDefaultValue() {
            return defaultValue;
        }

        private JsonPath getJsonPath() {
            return jsonPath;
        }

        private AttributeType getType() {
            return type;
        }
    }

    private void createAttributeJsonPathList(StreamDefinition streamDefinition,
                                             List<InputMappingAttribute> inputMappingAttributeList) {
        if (streamDefinition.getMetaData() != null && streamDefinition.getMetaData().size() > 0) {
            for (Attribute metaData : streamDefinition.getMetaData()) {
                InputMappingAttribute inputMappingAttribute = getInputMappingAttribute(EventReceiverConstants.META_DATA_PREFIX + metaData.getName(), inputMappingAttributeList);
                attributeJsonPathDataList.add(new JsonPathData(JsonPath.compile(inputMappingAttribute.getFromElementKey()), inputMappingAttribute.getToElementType(), inputMappingAttribute.getDefaultValue()));
            }
        }

        if (streamDefinition.getCorrelationData() != null && streamDefinition.getCorrelationData().size() > 0) {
            for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                InputMappingAttribute inputMappingAttribute = getInputMappingAttribute(EventReceiverConstants.CORRELATION_DATA_PREFIX + correlationData.getName(), inputMappingAttributeList);
                attributeJsonPathDataList.add(new JsonPathData(JsonPath.compile(inputMappingAttribute.getFromElementKey()), inputMappingAttribute.getToElementType(), inputMappingAttribute.getDefaultValue()));

            }
        }

        if (streamDefinition.getPayloadData() != null && streamDefinition.getPayloadData().size() > 0) {
            for (Attribute payloadData : streamDefinition.getPayloadData()) {
                InputMappingAttribute inputMappingAttribute = getInputMappingAttribute(payloadData.getName(), inputMappingAttributeList);
                attributeJsonPathDataList.add(new JsonPathData(JsonPath.compile(inputMappingAttribute.getFromElementKey()), inputMappingAttribute.getToElementType(), inputMappingAttribute.getDefaultValue()));

            }
        }
    }

    private InputMappingAttribute getInputMappingAttribute(String mappingAttribute,
                                                           List<InputMappingAttribute> inputMappingAttributeList) {
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributeList) {
            if (inputMappingAttribute.getToElementKey().equals(mappingAttribute)) {
                return inputMappingAttribute;
            }
        }

        log.error("Json input mapping attribute not found");
        return null;
    }

}
