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

package org.wso2.carbon.event.builder.core.internal.type.xml;

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
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.ReflectionBasedObjectSupplier;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathData;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;

import javax.xml.stream.XMLStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XMLInputMapper implements InputMapper {

    private static final Log log = LogFactory.getLog(XMLInputMapper.class);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<XPathData> attributeXpathList = new ArrayList<XPathData>();
    private List<XPathDefinition> xPathDefinitions = null;
    private ReflectionBasedObjectSupplier reflectionBasedObjectSupplier = new ReflectionBasedObjectSupplier();
    private AXIOMXPath parentSelectorXpath = null;

    public XMLInputMapper(EventBuilderConfiguration eventBuilderConfiguration) throws EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof XMLInputMapping) {
            try {
                XMLInputMapping xmlInputMapping = (XMLInputMapping) eventBuilderConfiguration.getInputMapping();
                List<XPathDefinition> xPathDefinitions = xmlInputMapping.getXPathDefinitions();
                for (InputMappingAttribute inputMappingAttribute : xmlInputMapping.getInputMappingAttributes()) {
                    String xpathExpr = inputMappingAttribute.getFromElementKey();

                    AXIOMXPath xpath = new AXIOMXPath(xpathExpr);
                    for (XPathDefinition xPathDefinition : xPathDefinitions) {
                        if (xPathDefinition != null && !xPathDefinition.isEmpty()) {
                            xpath.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
                        }
                    }
                    String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                    attributeXpathList.add(new XPathData(xpath, type, inputMappingAttribute.getDefaultValue()));
                }
                if (xmlInputMapping.getParentSelectorXpath() != null && !xmlInputMapping.getParentSelectorXpath().isEmpty()) {
                    this.parentSelectorXpath = new AXIOMXPath(xmlInputMapping.getParentSelectorXpath());
                    for (XPathDefinition xPathDefinition : xPathDefinitions) {
                        if (xPathDefinition != null && !xPathDefinition.isEmpty()) {
                            this.parentSelectorXpath.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
                        }
                    }
                }
            } catch (JaxenException e) {
                throw new EventBuilderConfigurationException("Error parsing XPath expression: " + e.getMessage(), e);
            }

        }

    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderConfigurationException {
        if (this.parentSelectorXpath != null) {
            return processMultipleEvents(obj);
        } else {
            return processSingleEvent(obj);
        }
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderConfigurationException {
        throw new UnsupportedOperationException("This feature is not yet supported for XMLInputMapping");
    }

    @Override
    public Attribute[] getOutputAttributes() {
        XMLInputMapping xmlInputMapping = (XMLInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = xmlInputMapping.getInputMappingAttributes();
        return EventBuilderConfigHelper.getAttributes(inputMappingAttributes);
    }

    private Object[][] processMultipleEvents(Object obj) throws EventBuilderConfigurationException {
        if (obj instanceof OMElement) {
            OMElement events;
            try {
                events = (OMElement) this.parentSelectorXpath.selectSingleNode(obj);
                List<Object[]> objArrayList = new ArrayList<Object[]>();
                Iterator childIterator = events.getChildElements();
                while (childIterator.hasNext()) {
                    Object eventObj = childIterator.next();
                    objArrayList.add(processSingleEvent(eventObj));
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
                return objArrayList.toArray(new Object[objArrayList.size()][]);
            } catch (JaxenException e) {
                throw new EventBuilderConfigurationException("Unable to parse XPath for parent selector: " + e.getMessage(), e);
            }
        }
        return null;
    }

    private Object[] processSingleEvent(Object obj) throws EventBuilderConfigurationException {
        Object[] outObjArray = null;
        OMElement eventOMElement = null;
        if (obj instanceof String) {
            String textMessage = (String) obj;
            try {
                eventOMElement = AXIOMUtil.stringToOM(textMessage);
            } catch (XMLStreamException e) {
                throw new EventBuilderConfigurationException("Error parsing incoming XML event : " + e.getMessage(), e);
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
                        returnedObj = BeanUtil.deserialize(beanClass,
                                omElementResult, reflectionBasedObjectSupplier, null);
                    } else if (xpathData.getDefaultValue() != null) {
                        if (!beanClass.equals(String.class)) {
                            Class<?> stringClass = String.class;
                            Method valueOfMethod = beanClass.getMethod("valueOf", stringClass);
                            returnedObj = valueOfMethod.invoke(null, xpathData.getDefaultValue());
                        } else {
                            returnedObj = xpathData.getDefaultValue();
                        }
                        log.warn("Unable to parse XPath to retrieve required attribute. Sending defaults.");
                    } else {
                        log.warn("Unable to parse XPath to retrieve required attribute. Skipping to next attribute.");
                    }
                    objList.add(returnedObj);
                } catch (JaxenException e) {
                    throw new EventBuilderConfigurationException("Error parsing xpath for " + xpath, e);
                } catch (ClassNotFoundException e) {
                    throw new EventBuilderConfigurationException("Cannot find specified class for type " + type);
                } catch (AxisFault axisFault) {
                    throw new EventBuilderConfigurationException("Error deserializing OMElement " + omElementResult, axisFault);
                } catch (NoSuchMethodException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                } catch (InvocationTargetException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                } catch (IllegalAccessException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                }
            }
            outObjArray = objList.toArray(new Object[objList.size()]);
        }
        return outObjArray;
    }
}
