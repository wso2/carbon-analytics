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

package org.wso2.carbon.event.builder.core.internal.type.text;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.text.config.RegexData;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class TextInputMapper implements InputMapper {
    private List<RegexData> attributeRegexList = new ArrayList<RegexData>();
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private int[] attributePositions;
    private static final Log log = LogFactory.getLog(TextInputMapper.class);

    public TextInputMapper(EventBuilderConfiguration eventBuilderConfiguration) throws EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof TextInputMapping) {
            TextInputMapping textInputMapping = (TextInputMapping) eventBuilderConfiguration.getInputMapping();
            RegexData regexData = null;
            if (textInputMapping.getInputMappingAttributes() != null) {
                attributePositions = new int[textInputMapping.getInputMappingAttributes().size()];
            } else {
                throw new EventBuilderConfigurationException("Text input mapping attribute list cannot be null!");
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
                        throw new EventBuilderConfigurationException("Error parsing regular expression: " + regex, e);
                    }
                }
                if (EventBuilderUtil.isMetaAttribute(inputMappingAttribute.getToElementKey())) {
                    attribPositionList.add(metaCount++, attributeCount++);
                } else if (EventBuilderUtil.isCorrelationAttribute(inputMappingAttribute.getToElementKey())) {
                    attribPositionList.add(metaCount + correlationCount++, attributeCount++);
                } else {
                    attribPositionList.add(attributeCount++);
                }
                String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                String defaultValue = inputMappingAttribute.getDefaultValue();
                regexData.addMapping(type, defaultValue);
            }
            for (int i = 0; i < attribPositionList.size(); i++) {
                attributePositions[attribPositionList.get(i)] = i;
            }
        }

    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderConfigurationException {
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
                            throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                        } catch (InvocationTargetException e) {
                            log.warn("Cannot convert " + value + " to type " + type + ": " + e.getMessage() + "; Sending null value.");
                            returnedAttribute = null;
                        } catch (NoSuchMethodException e) {
                            throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                        } catch (IllegalAccessException e) {
                            throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                        }
                    }
                    attributeArray[attributePositions[attributeCount++]] = returnedAttribute;
                }
            }
        }
        return attributeArray;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderConfigurationException {
        throw new UnsupportedOperationException("This feature is not yet supported for TextInputMapping");
    }

    @Override
    public Attribute[] getOutputAttributes() {
        TextInputMapping textInputMapping = (TextInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = textInputMapping.getInputMappingAttributes();
        return EventBuilderConfigHelper.getAttributes(inputMappingAttributes);

    }

}
