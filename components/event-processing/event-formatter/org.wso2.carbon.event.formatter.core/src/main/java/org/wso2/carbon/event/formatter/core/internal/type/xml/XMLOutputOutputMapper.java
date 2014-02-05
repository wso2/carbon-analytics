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

package org.wso2.carbon.event.formatter.core.internal.type.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterProcessingException;
import org.wso2.carbon.event.formatter.core.internal.OutputMapper;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XMLOutputOutputMapper implements OutputMapper {

    private static final Log log = LogFactory.getLog(XMLOutputOutputMapper.class);
    EventFormatterConfiguration eventFormatterConfiguration = null;
    Map<String, Integer> propertyPositionMap = null;
    private String outputXMLText = "";

    public XMLOutputOutputMapper(EventFormatterConfiguration eventFormatterConfiguration,
                                 Map<String, Integer> propertyPositionMap,
                                 int tenantId) throws
            EventFormatterConfigurationException {
        this.eventFormatterConfiguration = eventFormatterConfiguration;
        this.propertyPositionMap = propertyPositionMap;

        validateStreamDefinitionWithOutputProperties(tenantId);

    }

    private List<String> getOutputMappingPropertyList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains("{{") && text.indexOf("}}") > 0) {
            mappingTextList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return mappingTextList;
    }

    private void validateStreamDefinitionWithOutputProperties(int tenantId)
            throws EventFormatterConfigurationException {

        XMLOutputMapping textOutputMapping = ((XMLOutputMapping) eventFormatterConfiguration.getOutputMapping());
        String actualMappingText = textOutputMapping.getMappingXMLText();
        if (textOutputMapping.isRegistryResource()) {
            actualMappingText = EventFormatterServiceValueHolder.getCarbonEventFormatterService().getRegistryResourceContent(textOutputMapping.getMappingXMLText(), tenantId);
        }
        this.outputXMLText = actualMappingText;
        List<String> mappingProperties = getOutputMappingPropertyList(actualMappingText);

        Iterator<String> mappingTextListIterator = mappingProperties.iterator();
        for (; mappingTextListIterator.hasNext(); ) {
            String property = mappingTextListIterator.next();
            if (!propertyPositionMap.containsKey(property)) {
                throw new EventFormatterConfigurationException("Property " + property + " is not in the input stream definition. ");
            }
        }
    }

    private String getPropertyValue(Object obj, String mappingProperty) {
        Object[] inputObjArray = (Object[]) obj;
        if (inputObjArray.length != 0) {
            int position = propertyPositionMap.get(mappingProperty);
            return inputObjArray[position].toString();
        }
        return "";
    }

    private OMElement buildOuputOMElement(Object event, OMElement omElement)
            throws EventFormatterConfigurationException {
        Iterator<OMElement> iterator = omElement.getChildElements();
        if (iterator.hasNext()) {
            for (; iterator.hasNext(); ) {
                OMElement childElement = iterator.next();
                Iterator<OMAttribute> iteratorAttr = childElement.getAllAttributes();
                while (iteratorAttr.hasNext()) {
                    OMAttribute omAttribute = iteratorAttr.next();
                    String text = omAttribute.getAttributeValue();
                    if (text != null) {
                        if (text.contains("{{") && text.indexOf("}}") > 0) {
                            String propertyToReplace = text.substring(text.indexOf("{{") + 2, text.indexOf("}}"));
                            String value = getPropertyValue(event, propertyToReplace);
                            omAttribute.setAttributeValue(value);
                        }
                    }
                }

                String text = childElement.getText();
                if (text != null) {
                    if (text.contains("{{") && text.indexOf("}}") > 0) {
                        String propertyToReplace = text.substring(text.indexOf("{{") + 2, text.indexOf("}}"));
                        String value = getPropertyValue(event, propertyToReplace);
                        childElement.setText(value);
                    }
                }

                buildOuputOMElement(event, childElement);
            }
        } else {
            String text = omElement.getText();
            if (text != null) {
                if (text.contains("{{") && text.indexOf("}}") > 0) {
                    String propertyToReplace = text.substring(text.indexOf("{{") + 2, text.indexOf("}}"));
                    String value = getPropertyValue(event, propertyToReplace);
                    omElement.setText(value);
                }
            }
        }
        return omElement;
    }

    @Override
    public Object convertToMappedInputEvent(Object obj)
            throws EventFormatterConfigurationException {
        Object[] inputObjArray = (Object[]) obj;
        if (inputObjArray.length > 0) {
            try {
                return buildOuputOMElement(inputObjArray, AXIOMUtil.stringToOM(outputXMLText));
            } catch (XMLStreamException e) {
                throw new EventFormatterConfigurationException("XML mapping is not in XML format :" + outputXMLText, e);
            }
        } else {
            throw new EventFormatterProcessingException("Input Object array is empty!");
        }
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventFormatterConfigurationException {
        throw new UnsupportedOperationException("This feature is not yet supported for XMLOutputMapping");
    }

}
