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

package org.wso2.carbon.event.output.adaptor.manager.core.internal.util.helper;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.InternalOutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.output.adaptor.manager.core.internal.ds.OutputEventAdaptorHolder;
import org.wso2.carbon.event.output.adaptor.manager.core.internal.util.OutputEventAdaptorManagerConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class used to OM element related stuffs and for validating the xml files.
 */

public class OutputEventAdaptorConfigurationHelper {

    private static final Log log = LogFactory.getLog(OutputEventAdaptorConfigurationHelper.class);

    private OutputEventAdaptorConfigurationHelper() {
    }

    public static OutputEventAdaptorConfiguration fromOM(OMElement eventAdaptorConfigOMElement) {

        OutputEventAdaptorConfiguration eventAdaptorConfiguration = new OutputEventAdaptorConfiguration();
        eventAdaptorConfiguration.setName(eventAdaptorConfigOMElement.getAttributeValue(
                new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_NAME)));
        eventAdaptorConfiguration.setType(eventAdaptorConfigOMElement.getAttributeValue(
                new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_TYPE)));

        if (eventAdaptorConfigOMElement.getAttributeValue(new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_STATISTICS)) != null && eventAdaptorConfigOMElement.getAttributeValue(new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_STATISTICS)).equals(OutputEventAdaptorManagerConstants.OEA_VALUE_ENABLE)) {
            eventAdaptorConfiguration.setEnableStatistics(true);
        }else if (eventAdaptorConfigOMElement.getAttributeValue(new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_STATISTICS)) != null && eventAdaptorConfigOMElement.getAttributeValue(new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_STATISTICS)).equals(OutputEventAdaptorManagerConstants.OEA_VALUE_DISABLE)) {
            eventAdaptorConfiguration.setEnableStatistics(false);
        }

        if (eventAdaptorConfigOMElement.getAttributeValue(new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_TRACING)) != null && eventAdaptorConfigOMElement.getAttributeValue(new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_TRACING)).equals(OutputEventAdaptorManagerConstants.OEA_VALUE_ENABLE)) {
            eventAdaptorConfiguration.setEnableTracing(true);
        }else if (eventAdaptorConfigOMElement.getAttributeValue(new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_TRACING)) != null && eventAdaptorConfigOMElement.getAttributeValue(new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_TRACING)).equals(OutputEventAdaptorManagerConstants.OEA_VALUE_DISABLE)) {
            eventAdaptorConfiguration.setEnableTracing(false);
        }
        //Output Adaptor Properties

        Iterator propertyIter = eventAdaptorConfigOMElement.getChildrenWithName(
                new QName(OutputEventAdaptorManagerConstants.OEA_CONF_NS, OutputEventAdaptorManagerConstants.OEA_ELE_PROPERTY));
        InternalOutputEventAdaptorConfiguration outputEventAdaptorPropertyConfiguration = new InternalOutputEventAdaptorConfiguration();
        if (propertyIter.hasNext()) {
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(OutputEventAdaptorManagerConstants.OEA_ATTR_NAME));
                String value = propertyOMElement.getText();
                outputEventAdaptorPropertyConfiguration.addEventAdaptorProperty(name, value);
            }
        }
        eventAdaptorConfiguration.setOutputConfiguration(outputEventAdaptorPropertyConfiguration);

        return eventAdaptorConfiguration;
    }


    public static OMElement eventAdaptorConfigurationToOM(
            OutputEventAdaptorConfiguration eventAdaptorConfiguration) {
        String eventAdaptorName = eventAdaptorConfiguration.getName();
        String eventAdaptorType = eventAdaptorConfiguration.getType();

        Map<String, String> outputEventAdaptorProperties = null;

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventAdaptorItem = factory.createOMElement(new QName(
                OutputEventAdaptorManagerConstants.OEA_ELE_ROOT_ELEMENT));
        eventAdaptorItem.declareDefaultNamespace(OutputEventAdaptorManagerConstants.OEA_CONF_NS);
        eventAdaptorItem.addAttribute(OutputEventAdaptorManagerConstants.OEA_ATTR_NAME, eventAdaptorName,
                                          null);
        eventAdaptorItem.addAttribute(OutputEventAdaptorManagerConstants.OEA_ATTR_TYPE, eventAdaptorType,
                                          null);

        if (eventAdaptorConfiguration.isEnableStatistics()) {
            eventAdaptorItem.addAttribute(OutputEventAdaptorManagerConstants.OEA_ATTR_STATISTICS, OutputEventAdaptorManagerConstants.OEA_VALUE_ENABLE,
                                              null);
        }else if (! eventAdaptorConfiguration.isEnableStatistics()) {
            eventAdaptorItem.addAttribute(OutputEventAdaptorManagerConstants.OEA_ATTR_STATISTICS, OutputEventAdaptorManagerConstants.OEA_VALUE_DISABLE,
                                              null);
        }

        if (eventAdaptorConfiguration.isEnableTracing()) {
            eventAdaptorItem.addAttribute(OutputEventAdaptorManagerConstants.OEA_ATTR_TRACING, OutputEventAdaptorManagerConstants.OEA_VALUE_ENABLE,
                                              null);
        }else if (! eventAdaptorConfiguration.isEnableTracing()) {
            eventAdaptorItem.addAttribute(OutputEventAdaptorManagerConstants.OEA_ATTR_TRACING, OutputEventAdaptorManagerConstants.OEA_VALUE_DISABLE,
                                              null);
        }


        if (eventAdaptorConfiguration.getOutputConfiguration() != null) {
            outputEventAdaptorProperties = eventAdaptorConfiguration.getOutputConfiguration().getProperties();
            for (Map.Entry<String, String> outputPropertyEntry : outputEventAdaptorProperties.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        OutputEventAdaptorManagerConstants.OEA_ELE_PROPERTY));
                propertyElement.declareDefaultNamespace(OutputEventAdaptorManagerConstants.OEA_CONF_NS);
                propertyElement.addAttribute(OutputEventAdaptorManagerConstants.OEA_ATTR_NAME, outputPropertyEntry.getKey(), null);
                propertyElement.setText(outputPropertyEntry.getValue());
                eventAdaptorItem.addChild(propertyElement);
            }
        }

        return eventAdaptorItem;
    }


    public static boolean validateEventAdaptorConfiguration(
            OutputEventAdaptorConfiguration eventAdaptorConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {

        OutputEventAdaptorService eventAdaptorService = OutputEventAdaptorHolder.getInstance().getOutputEventAdaptorService();
        OutputEventAdaptorDto eventAdaptorDto = eventAdaptorService.getEventAdaptorDto(eventAdaptorConfiguration.getType());

        if (eventAdaptorDto == null) {
            return false;
        }


        List<Property> outputEventAdaptorProperties = eventAdaptorDto.getAdaptorPropertyList();

        Map<String, String> outputAdaptorConfigurationPropertyList = null;

        if (eventAdaptorConfiguration.getOutputConfiguration() != null) {
            outputAdaptorConfigurationPropertyList = eventAdaptorConfiguration.getOutputConfiguration().getProperties();
        }


        if (outputEventAdaptorProperties != null && (outputAdaptorConfigurationPropertyList != null)) {
            Iterator propertyIterator = outputEventAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property eventAdaptorProperty = (Property) propertyIterator.next();
                if (eventAdaptorProperty.isRequired()) {
                    if (!outputAdaptorConfigurationPropertyList.containsKey(eventAdaptorProperty.getPropertyName())) {
                        log.error("Required output property : " + eventAdaptorProperty.getPropertyName() + " not in the event adaptor configuration");
                        throw new OutputEventAdaptorManagerConfigurationException("Required output property : " + eventAdaptorProperty.getPropertyName() + " not in the event adaptor configuration");
                    }
                }
            }

            Iterator outputPropertyIterator = outputEventAdaptorProperties.iterator();
            List<String> outputPropertyNames = new ArrayList<String>();
            while (outputPropertyIterator.hasNext()) {
                Property outputProperty = (Property) outputPropertyIterator.next();
                outputPropertyNames.add(outputProperty.getPropertyName());
            }

            Iterator propertyConfigurationIterator = outputAdaptorConfigurationPropertyList.keySet().iterator();
            while (propertyConfigurationIterator.hasNext()) {
                String eventAdaptorPropertyName = (String) propertyConfigurationIterator.next();
                if (!outputPropertyNames.contains(eventAdaptorPropertyName)) {
                    log.error(eventAdaptorPropertyName + " is not a valid property for this event adaptor type : " + eventAdaptorConfiguration.getType());
                    throw new OutputEventAdaptorManagerConfigurationException(eventAdaptorPropertyName + " is not a valid property for this event adaptor type : " + eventAdaptorConfiguration.getType());
                }

            }
        }

        return true;
    }
}
