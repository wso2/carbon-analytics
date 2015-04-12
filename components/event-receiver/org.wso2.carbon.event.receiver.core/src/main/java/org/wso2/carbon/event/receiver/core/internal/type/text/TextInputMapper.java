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
package org.wso2.carbon.event.receiver.core.internal.type.text;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.InputMapper;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.TextInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverProcessingException;
import org.wso2.carbon.event.receiver.core.internal.type.text.config.RegexData;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class TextInputMapper implements InputMapper {
    private List<RegexData> attributeRegexList = new ArrayList<RegexData>();
    private EventReceiverConfiguration eventReceiverConfiguration = null;
    private int[] attributePositions;
    private static final Log log = LogFactory.getLog(TextInputMapper.class);
    private int noMetaData;
    private int noCorrelationData;
    private int noPayloadData;
    private StreamDefinition streamDefinition;

    public TextInputMapper(EventReceiverConfiguration eventReceiverConfiguration,
                           StreamDefinition streamDefinition)
            throws EventReceiverConfigurationException {
        this.eventReceiverConfiguration = eventReceiverConfiguration;
        this.streamDefinition = streamDefinition;

        if (eventReceiverConfiguration != null && eventReceiverConfiguration.getInputMapping() instanceof TextInputMapping) {
            if (eventReceiverConfiguration.getInputMapping().isCustomMappingEnabled()) {
                TextInputMapping textInputMapping = (TextInputMapping) eventReceiverConfiguration.getInputMapping();
                RegexData regexData = null;
                if (textInputMapping.getInputMappingAttributes() != null || textInputMapping.getInputMappingAttributes().size() == 0) {
                    attributePositions = new int[textInputMapping.getInputMappingAttributes().size()];
                } else {
                    throw new EventReceiverConfigurationException("Text input mapping attribute list cannot be null!");
                }
                List<Integer> attribPositionList = new LinkedList<Integer>();
                int metaCount = 0;
                int correlationCount = 0;
                int attributeCount = 0;
                for (InputMappingAttribute inputMappingAttribute : textInputMapping.getInputMappingAttributes()) {
                    String regex = inputMappingAttribute.getFromElementKey();
                    if (regexData == null || !regex.equals(regexData.getRegex())) {
                        try {
                            regexData = new RegexData(regex);
                            attributeRegexList.add(regexData);
                        } catch (PatternSyntaxException e) {
                            throw new EventReceiverConfigurationException("Error parsing regular expression: " + regex, e);
                        }
                    }
                    if (EventReceiverUtil.isMetaAttribute(inputMappingAttribute.getToElementKey())) {
                        attribPositionList.add(metaCount++, attributeCount++);
                    } else if (EventReceiverUtil.isCorrelationAttribute(inputMappingAttribute.getToElementKey())) {
                        attribPositionList.add(metaCount + correlationCount++, attributeCount++);
                    } else {
                        attribPositionList.add(attributeCount++);
                    }
                    String type = EventReceiverConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                    String defaultValue = inputMappingAttribute.getDefaultValue();
                    regexData.addMapping(type, defaultValue);
                }
                for (int i = 0; i < attribPositionList.size(); i++) {
                    attributePositions[attribPositionList.get(i)] = i;
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
        Object attributeArray[] = new Object[attributePositions.length];
        if (obj instanceof String) {
            String inputString = (String) obj;
            int attributeCount = 0;
            for (RegexData regexData : attributeRegexList) {
                String formattedInputString = inputString.replaceAll("\\r", "");
                regexData.matchInput(formattedInputString);
                while (regexData.hasNext()) {
                    Object returnedAttribute = null;
                    String value = regexData.next();
                    if (value != null) {
                        String type = regexData.getType();
                        try {
                            Class<?> beanClass = Class.forName(type);
                            if (!beanClass.equals(String.class)) {
                                Class<?> stringClass = String.class;
                                Method valueOfMethod = beanClass.getMethod("valueOf", stringClass);
                                returnedAttribute = valueOfMethod.invoke(null, value);
                            } else {
                                returnedAttribute = value;
                            }
                        } catch (ClassNotFoundException e) {
                            throw new EventReceiverProcessingException("Cannot convert " + value + " to type " + type, e);
                        } catch (InvocationTargetException e) {
                            log.warn("Cannot convert " + value + " to type " + type + ": " + e.getMessage() + "; Sending null value.");
                            returnedAttribute = null;
                        } catch (NoSuchMethodException e) {
                            throw new EventReceiverProcessingException("Cannot convert " + value + " to type " + type, e);
                        } catch (IllegalAccessException e) {
                            throw new EventReceiverProcessingException("Cannot convert " + value + " to type " + type, e);
                        }
                    }
                    attributeArray[attributePositions[attributeCount++]] = returnedAttribute;
                }
            }
        }
        return attributeArray;
    }

    //TODO use = for default text mapping  use a map instead of multiple for loops
    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventReceiverProcessingException {

        Object attributeArray[] = new Object[noMetaData + noCorrelationData + noPayloadData];
        if (obj instanceof String) {
            String inputString = ((String) obj).trim();
            int attributeCount = 0;

            String[] eventAttributes = inputString.trim().split(",");

            for (String eventAttribute : eventAttributes) {
                boolean setFlag = false;

                if(noMetaData>0) {
                    for (Attribute metaData : streamDefinition.getMetaData()) {
                        if (eventAttribute.trim().startsWith(EventReceiverConstants.META_DATA_PREFIX + metaData.getName())) {
                            attributeArray[attributeCount++] = getPropertyValue(eventAttribute.split(EventReceiverConstants.EVENT_ATTRIBUTE_SEPARATOR)[1], metaData.getType());
                            setFlag = true;
                            break;
                        }
                    }
                }

                if(noCorrelationData>0) {
                    if (!setFlag) {
                        for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                            if (eventAttribute.trim().startsWith(EventReceiverConstants.CORRELATION_DATA_PREFIX + correlationData.getName())) {
                                attributeArray[attributeCount++] = getPropertyValue(eventAttribute.split(EventReceiverConstants.EVENT_ATTRIBUTE_SEPARATOR)[1], correlationData.getType());
                                setFlag = true;
                                break;
                            }
                        }
                    }
                }

                if(noPayloadData>0) {
                    if (!setFlag) {
                        for (Attribute payloadData : streamDefinition.getPayloadData()) {
                            if (eventAttribute.trim().startsWith(payloadData.getName())) {
                                attributeArray[attributeCount++] = getPropertyValue(eventAttribute.split(EventReceiverConstants.EVENT_ATTRIBUTE_SEPARATOR)[1], payloadData.getType());
                                break;
                            }
                        }
                    }
                }
            }

            if (noMetaData + noCorrelationData + noPayloadData != attributeCount) {
                throw new EventReceiverProcessingException("Event attributes are not matching with the stream : " + this.eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion());
            }
        }
        return attributeArray;
    }

    @Override
    public Attribute[] getOutputAttributes() {
        TextInputMapping textInputMapping = (TextInputMapping) eventReceiverConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = textInputMapping.getInputMappingAttributes();
        return EventReceiverConfigurationHelper.getAttributes(inputMappingAttributes);

    }

    private Object getPropertyValue(Object propertyValue, AttributeType attributeType) {
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

}
