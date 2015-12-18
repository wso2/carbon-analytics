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
package org.wso2.carbon.event.receiver.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.InputMapper;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.XMLInputMapping;
import org.wso2.carbon.event.receiver.core.config.mapping.XPathDefinition;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverProcessingException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.internal.type.xml.config.ReflectionBasedObjectSupplier;
import org.wso2.carbon.event.receiver.core.internal.type.xml.config.XPathData;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationHelper;
import org.wso2.siddhi.core.event.Event;

import javax.xml.stream.XMLStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class XMLInputMapper implements InputMapper {

    private static final Log log = LogFactory.getLog(XMLInputMapper.class);
    private EventReceiverConfiguration eventReceiverConfiguration = null;
    private List<XPathData> attributeXpathList = null;
    private List<XPathDefinition> xPathDefinitions = null;
    private ReflectionBasedObjectSupplier reflectionBasedObjectSupplier = new ReflectionBasedObjectSupplier();
    private AXIOMXPath parentSelectorXpath = null;

    public XMLInputMapper(EventReceiverConfiguration eventReceiverConfiguration,
                          StreamDefinition exportedStreamDefinition)
            throws EventReceiverConfigurationException {
        this.eventReceiverConfiguration = eventReceiverConfiguration;

        if (eventReceiverConfiguration != null && eventReceiverConfiguration.getInputMapping() instanceof XMLInputMapping) {
            XMLInputMapping xmlInputMapping = (XMLInputMapping) eventReceiverConfiguration.getInputMapping();
            if (xmlInputMapping.isCustomMappingEnabled()) {
                try {

                    List<XPathDefinition> xPathDefinitions = xmlInputMapping.getXPathDefinitions();
                    XPathData[] xpathDataArray = new XPathData[xmlInputMapping.getInputMappingAttributes().size()];
                    for (InputMappingAttribute inputMappingAttribute : xmlInputMapping.getInputMappingAttributes()) {
                        String xpathExpr = inputMappingAttribute.getFromElementKey();

                        AXIOMXPath xpath = new AXIOMXPath(xpathExpr);
                        for (XPathDefinition xPathDefinition : xPathDefinitions) {
                            if (xPathDefinition != null && !xPathDefinition.isEmpty()) {
                                xpath.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
                            }
                        }
                        String type = EventReceiverConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                        int position = EventReceiverUtil.getAttributePosition(inputMappingAttribute.getToElementKey(), exportedStreamDefinition);
                        if (position < 0 || position > xpathDataArray.length) {
                            throw new EventReceiverStreamValidationException("Could not determine the stream position for attribute : "
                                    + inputMappingAttribute.getToElementKey() + " in stream exported by event receiver "
                                    + exportedStreamDefinition.getStreamId(), exportedStreamDefinition.getStreamId());
                        }
                        xpathDataArray[position] = new XPathData(xpath, type, inputMappingAttribute.getDefaultValue());
                    }
                    attributeXpathList = Arrays.asList(xpathDataArray);
                    if (xmlInputMapping.getParentSelectorXpath() != null && !xmlInputMapping.getParentSelectorXpath().isEmpty()) {
                        this.parentSelectorXpath = new AXIOMXPath(xmlInputMapping.getParentSelectorXpath());
                        for (XPathDefinition xPathDefinition : xPathDefinitions) {
                            if (xPathDefinition != null && !xPathDefinition.isEmpty()) {
                                this.parentSelectorXpath.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
                            }
                        }
                    }
                } catch (JaxenException e) {
                    throw new EventReceiverConfigurationException("Error parsing XPath expression: " + e.getMessage(), e);
                }
            } else {

                try {
                    this.parentSelectorXpath = new AXIOMXPath("//" + EventReceiverConstants.MULTIPLE_EVENTS_PARENT_TAG);
                    attributeXpathList = new ArrayList<XPathData>();
                    if (exportedStreamDefinition.getMetaData() != null) {
                        for (Attribute attribute : exportedStreamDefinition.getMetaData()) {
                            String xpathExpr = "//" + EventReceiverConstants.EVENT_META_TAG;
                            AXIOMXPath xpath = new AXIOMXPath(xpathExpr + "/" + attribute.getName());
                            String type = EventReceiverConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(attribute.getType());
                            attributeXpathList.add(new XPathData(xpath, type, null));

                        }
                    }

                    if (exportedStreamDefinition.getCorrelationData() != null) {
                        for (Attribute attribute : exportedStreamDefinition.getCorrelationData()) {
                            String xpathExpr = "//" + EventReceiverConstants.EVENT_CORRELATION_TAG;
                            AXIOMXPath xpath = new AXIOMXPath(xpathExpr + "/" + attribute.getName());
                            String type = EventReceiverConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(attribute.getType());
                            attributeXpathList.add(new XPathData(xpath, type, null));

                        }
                    }

                    if (exportedStreamDefinition.getPayloadData() != null) {
                        for (Attribute attribute : exportedStreamDefinition.getPayloadData()) {
                            String xpathExpr = "//" + EventReceiverConstants.EVENT_PAYLOAD_TAG;
                            AXIOMXPath xpath = new AXIOMXPath(xpathExpr + "/" + attribute.getName());
                            String type = EventReceiverConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(attribute.getType());
                            attributeXpathList.add(new XPathData(xpath, type, null));

                        }
                    }
                } catch (JaxenException e) {
                    throw new EventReceiverConfigurationException("Error parsing XPath expression: " + e.getMessage(), e);
                }
            }
        }

    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventReceiverProcessingException {
        if (this.parentSelectorXpath != null) {
            return processMultipleEvents(obj);
        } else {
            return processSingleEvent(obj);
        }
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventReceiverProcessingException {
        if (this.parentSelectorXpath != null) {
            return processMultipleEvents(obj);
        } else {
            return processSingleEvent(obj);
        }
    }

    @Override
    public Attribute[] getOutputAttributes() {
        XMLInputMapping xmlInputMapping = (XMLInputMapping) eventReceiverConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = xmlInputMapping.getInputMappingAttributes();
        return EventReceiverConfigurationHelper.getAttributes(inputMappingAttributes);
    }

    private Event[] processMultipleEvents(Object obj) {
        if (obj instanceof String) {
            String textMessage = (String) obj;
            try {
                obj = AXIOMUtil.stringToOM(textMessage);
            } catch (XMLStreamException e) {
                throw new EventReceiverProcessingException("Error parsing incoming XML event : " + e.getMessage(), e);
            }
        }
        if (obj instanceof OMElement) {
            OMElement events;
            try {
                events = (OMElement) this.parentSelectorXpath.selectSingleNode(obj);
            } catch (JaxenException e) {
                throw new EventReceiverProcessingException("Unable to parse XPath for parent selector: " + e.getMessage(), e);
            }
            if (events == null) {
                throw new EventReceiverProcessingException("Parent Selector XPath \"" + parentSelectorXpath.toString() + "\" cannot be processed on event:" + obj.toString());
            }

            List<Event> objArrayList = new ArrayList<Event>();
            Iterator childIterator = events.getChildElements();
            while (childIterator.hasNext()) {
                Object eventObj = childIterator.next();
                try {
                    objArrayList.add(processSingleEvent(eventObj));
                } catch (EventReceiverProcessingException e) {
                    log.error("Dropping event. Error processing event : ", e);
                }
                /**
                 * Usually the global lookup '//' is used in the XPATH expression which works fine for 'single event mode'.
                 * However, if global lookup is used, it will return the first element from the whole document as specified in
                 * XPATH-2.0 Specification. Therefore the same XPATH expression that works fine in 'single event mode' will
                 * always return the first element of a batch in 'batch mode'. Therefore to return what the
                 * user expects, each child element is removed after sending to simulate an iteration for the
                 * global lookup.
                 */
                childIterator.remove();
            }
            return objArrayList.toArray(new Event[objArrayList.size()]);
        }
        return null;
    }

    private Event processSingleEvent(Object obj) throws EventReceiverProcessingException {
        Object[] outObjArray = null;
        OMElement eventOMElement = null;
        if (obj instanceof String) {
            String textMessage = (String) obj;
            try {
                eventOMElement = AXIOMUtil.stringToOM(textMessage);
            } catch (XMLStreamException e) {
                throw new EventReceiverProcessingException("Error parsing incoming XML event : " + e.getMessage(), e);
            }
        } else if (obj instanceof OMElement) {
            eventOMElement = (OMElement) obj;
        }

        if (eventOMElement != null) {
            OMNamespace omNamespace = null;
            if (this.xPathDefinitions == null || this.xPathDefinitions.isEmpty()) {
                omNamespace = eventOMElement.getNamespace();
            }
            List<Object> objList = new ArrayList<Object>();
            for (XPathData xpathData : attributeXpathList) {
                AXIOMXPath xpath = xpathData.getXpath();
                OMElement omElementResult = null;
                String type = xpathData.getType();
                try {
                    if (omNamespace != null) {
                        xpath.addNamespaces(eventOMElement);
                    }
                    omElementResult = (OMElement) xpath.selectSingleNode(eventOMElement);
                    Class<?> beanClass = Class.forName(type);
                    Object returnedObj = null;
                    if (omElementResult != null) {
                        if(omElementResult.getFirstElement() != null){
                            returnedObj = omElementResult.toString();
                        }else {
                            returnedObj = BeanUtil.deserialize(beanClass,
                                    omElementResult, reflectionBasedObjectSupplier, null);
                        }
                    }

                    if (omElementResult == null || returnedObj == null) {
                        if (xpathData.getDefaultValue() != null) {
                            if (!beanClass.equals(String.class)) {
                                Class<?> stringClass = String.class;
                                Method valueOfMethod = beanClass.getMethod("valueOf", stringClass);
                                returnedObj = valueOfMethod.invoke(null, xpathData.getDefaultValue());
                            } else {
                                returnedObj = xpathData.getDefaultValue();
                            }
                        } else if (!type.equals(EventReceiverConstants.CLASS_FOR_STRING)) {
                            if (omElementResult == null) {
                                throw new EventReceiverProcessingException("Unable to parse XPath " + xpathData.getXpath() + " to retrieve required attribute, hence dropping the event " + obj.toString());
                            } else {
                                throw new EventReceiverProcessingException("Valid attribute value not found for " + xpathData.getXpath() + ", hence dropping the event " + obj.toString());
                            }
                        }
                    }
                    objList.add(returnedObj);
                } catch (JaxenException e) {
                    throw new EventReceiverProcessingException("Error parsing xpath for " + xpath, e);
                } catch (ClassNotFoundException e) {
                    throw new EventReceiverProcessingException("Cannot find specified class for type " + type);
                } catch (AxisFault axisFault) {
                    throw new EventReceiverProcessingException("Error de-serializing OMElement " + omElementResult, axisFault);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new EventReceiverProcessingException("Error trying to convert default value to specified target type.", e);
                }
            }
            outObjArray = objList.toArray(new Object[objList.size()]);
        }
        return new Event(System.currentTimeMillis(), outObjArray);
    }
}
