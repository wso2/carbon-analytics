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

package org.wso2.carbon.event.input.adaptor.manager.core.internal.util.helper;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorDto;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.ds.InputEventAdaptorHolder;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.config.InternalInputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.util.InputEventAdaptorManagerConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class used to OM element related stuffs and for validating the xml files.
 */

public class InputEventAdaptorConfigurationHelper {

    private static final Log log = LogFactory.getLog(InputEventAdaptorConfigurationHelper.class);

    private InputEventAdaptorConfigurationHelper() {
    }

    public static InputEventAdaptorConfiguration fromOM(OMElement eventAdaptorConfigOMElement) {

        InputEventAdaptorConfiguration eventAdaptorConfiguration = new InputEventAdaptorConfiguration();
        eventAdaptorConfiguration.setName(eventAdaptorConfigOMElement.getAttributeValue(
                new QName(InputEventAdaptorManagerConstants.IEA_ATTR_NAME)));
        eventAdaptorConfiguration.setType(eventAdaptorConfigOMElement.getAttributeValue(
                new QName(InputEventAdaptorManagerConstants.IEA_ATTR_TYPE)));

        if (eventAdaptorConfigOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_STATISTICS)) != null && eventAdaptorConfigOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_STATISTICS)).equals(InputEventAdaptorManagerConstants.IEA_VALUE_ENABLE)) {
            eventAdaptorConfiguration.setEnableStatistics(true);
        } else if (eventAdaptorConfigOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_STATISTICS)) != null && eventAdaptorConfigOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_STATISTICS)).equals(InputEventAdaptorManagerConstants.IEA_VALUE_DISABLE)) {
            eventAdaptorConfiguration.setEnableStatistics(false);
        }

        if (eventAdaptorConfigOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_TRACING)) != null && eventAdaptorConfigOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_TRACING)).equals(InputEventAdaptorManagerConstants.IEA_VALUE_ENABLE)) {
            eventAdaptorConfiguration.setEnableTracing(true);
        } else if (eventAdaptorConfigOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_TRACING)) != null && eventAdaptorConfigOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_TRACING)).equals(InputEventAdaptorManagerConstants.IEA_VALUE_DISABLE)) {
            eventAdaptorConfiguration.setEnableTracing(false);
        }


        //Input Adaptor Properties

        Iterator propertyIter = eventAdaptorConfigOMElement.getChildrenWithName(
                new QName(InputEventAdaptorManagerConstants.IEA_CONF_NS, InputEventAdaptorManagerConstants.IEA_ELE_PROPERTY));
        InternalInputEventAdaptorConfiguration internalInputEventAdaptorConfiguration = new InternalInputEventAdaptorConfiguration();
        if (propertyIter.hasNext()) {
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(InputEventAdaptorManagerConstants.IEA_ATTR_NAME));
                String value = propertyOMElement.getText();
                internalInputEventAdaptorConfiguration.addEventAdaptorProperty(name, value);
            }
        }
        eventAdaptorConfiguration.setInputConfiguration(internalInputEventAdaptorConfiguration);

        return eventAdaptorConfiguration;
    }


    public static OMElement eventAdaptorConfigurationToOM(
            InputEventAdaptorConfiguration eventAdaptorConfiguration) {
        String eventAdaptorName = eventAdaptorConfiguration.getName();
        String eventAdaptorType = eventAdaptorConfiguration.getType();

        Map<String, String> inputEventAdaptorProperties = null;

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventAdaptorItem = factory.createOMElement(new QName(
                InputEventAdaptorManagerConstants.IEA_ELE_ROOT_ELEMENT));
        eventAdaptorItem.declareDefaultNamespace(InputEventAdaptorManagerConstants.IEA_CONF_NS);
        eventAdaptorItem.addAttribute(InputEventAdaptorManagerConstants.IEA_ATTR_NAME, eventAdaptorName,
                                          null);
        eventAdaptorItem.addAttribute(InputEventAdaptorManagerConstants.IEA_ATTR_TYPE, eventAdaptorType,
                                          null);

        if (eventAdaptorConfiguration.isEnableStatistics()) {
            eventAdaptorItem.addAttribute(InputEventAdaptorManagerConstants.IEA_ATTR_STATISTICS, InputEventAdaptorManagerConstants.IEA_VALUE_ENABLE,
                                              null);
        } else if (!eventAdaptorConfiguration.isEnableStatistics()) {
            eventAdaptorItem.addAttribute(InputEventAdaptorManagerConstants.IEA_ATTR_STATISTICS, InputEventAdaptorManagerConstants.IEA_VALUE_DISABLE,
                                              null);
        }


        if (eventAdaptorConfiguration.isEnableTracing()) {
            eventAdaptorItem.addAttribute(InputEventAdaptorManagerConstants.IEA_ATTR_TRACING, InputEventAdaptorManagerConstants.IEA_VALUE_ENABLE,
                                              null);
        } else if (!eventAdaptorConfiguration.isEnableTracing()) {
            eventAdaptorItem.addAttribute(InputEventAdaptorManagerConstants.IEA_ATTR_TRACING, InputEventAdaptorManagerConstants.IEA_VALUE_DISABLE,
                                              null);
        }

        if (eventAdaptorConfiguration.getInputConfiguration() != null) {
            inputEventAdaptorProperties = eventAdaptorConfiguration.getInputConfiguration().getProperties();
            for (Map.Entry<String, String> inputPropertyEntry : inputEventAdaptorProperties.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        InputEventAdaptorManagerConstants.IEA_ELE_PROPERTY));
                propertyElement.declareDefaultNamespace(InputEventAdaptorManagerConstants.IEA_CONF_NS);
                propertyElement.addAttribute(InputEventAdaptorManagerConstants.IEA_ATTR_NAME, inputPropertyEntry.getKey(), null);
                propertyElement.setText(inputPropertyEntry.getValue());
                eventAdaptorItem.addChild(propertyElement);
            }
        }

        return eventAdaptorItem;
    }


    public static boolean validateEventAdaptorConfiguration(
            InputEventAdaptorConfiguration eventAdaptorConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        InputEventAdaptorService eventAdaptorService = InputEventAdaptorHolder.getInstance().getInputEventAdaptorService();
        InputEventAdaptorDto eventAdaptorDto = eventAdaptorService.getEventAdaptorDto(eventAdaptorConfiguration.getType());

        if (eventAdaptorDto == null) {
            return false;
        }


        List<Property> inputEventAdaptorProperties = eventAdaptorDto.getAdaptorPropertyList();

        Map<String, String> inputAdaptorConfigurationPropertyList = null;

        if (eventAdaptorConfiguration.getInputConfiguration() != null) {
            inputAdaptorConfigurationPropertyList = eventAdaptorConfiguration.getInputConfiguration().getProperties();
        }


        if (inputEventAdaptorProperties != null && (inputAdaptorConfigurationPropertyList != null)) {
            Iterator propertyIterator = inputEventAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property eventAdaptorProperty = (Property) propertyIterator.next();
                if (eventAdaptorProperty.isRequired()) {
                    if (!inputAdaptorConfigurationPropertyList.containsKey(eventAdaptorProperty.getPropertyName())) {
                        log.error("Required input property : " + eventAdaptorProperty.getPropertyName() + " not in the event adaptor configuration");
                        throw new InputEventAdaptorManagerConfigurationException("Required input property : " + eventAdaptorProperty.getPropertyName() + " not in the event adaptor configuration");
                    }
                }
            }

            Iterator inputPropertyIterator = inputEventAdaptorProperties.iterator();
            List<String> inputPropertyNames = new ArrayList<String>();
            while (inputPropertyIterator.hasNext()) {
                Property inputProperty = (Property) inputPropertyIterator.next();
                inputPropertyNames.add(inputProperty.getPropertyName());
            }


            Iterator propertyConfigurationIterator = inputAdaptorConfigurationPropertyList.keySet().iterator();
            while (propertyConfigurationIterator.hasNext()) {
                String eventAdaptorPropertyName = (String) propertyConfigurationIterator.next();
                if (!inputPropertyNames.contains(eventAdaptorPropertyName)) {
                    log.error(eventAdaptorPropertyName + " is not a valid property for this event adaptor type : " + eventAdaptorConfiguration.getType());
                    throw new InputEventAdaptorManagerConfigurationException(eventAdaptorPropertyName + " is not a valid property for this Input Event Adaptor type : " + eventAdaptorConfiguration.getType());
                }


            }
        }

        return true;
    }
}
