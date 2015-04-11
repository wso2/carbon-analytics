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
package org.wso2.carbon.event.receiver.core.internal.util;

import org.jaxen.JaxenException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.XMLInputMapping;
import org.wso2.carbon.event.receiver.core.config.mapping.XPathDefinition;

import java.util.ArrayList;
import java.util.List;

//todo do the testing using EventReceiver instead of the mapping
public class XMLMappingTestCase {
    private static final String XPATH_NS = "http://ws.cdyne.com/";
    private static final String XPATH_PREFIX = "quotedata";
    private static final String XPATH_FOR_SYMBOL = "//quotedata:StockQuoteEvent/quotedata:StockSymbol";
    private static final String XPATH_FOR_PRICE = "//quotedata:StockQuoteEvent/quotedata:LastTradeAmount";
    private EventReceiverConfiguration xmlEventReceiverConfig;

    @Before
    public void init() {
        xmlEventReceiverConfig = new EventReceiverConfiguration();
        xmlEventReceiverConfig.setToStreamName("stockQuotes");
        xmlEventReceiverConfig.setToStreamVersion("1.0.0");
        XMLInputMapping xmlInputMapping = new XMLInputMapping();
        List<XPathDefinition> xPathDefinitions = new ArrayList<XPathDefinition>();
        xPathDefinitions.add(new XPathDefinition(XPATH_PREFIX, XPATH_NS));
        xmlInputMapping.setXPathDefinitions(xPathDefinitions);
        xmlInputMapping.addInputMappingAttribute(new InputMappingAttribute(XPATH_FOR_SYMBOL, "symbol", AttributeType.STRING));
        xmlInputMapping.addInputMappingAttribute(new InputMappingAttribute(XPATH_FOR_PRICE, "price", AttributeType.DOUBLE));
        xmlEventReceiverConfig.setInputMapping(xmlInputMapping);
    }

    @Test
    public void testCreateMapping() throws MalformedStreamDefinitionException, JaxenException {
        Attribute[] attributeArray = EventReceiverUtil.getOrderedAttributeArray((XMLInputMapping) xmlEventReceiverConfig.getInputMapping());
        List<Attribute> expectedPayloadAttributes = new ArrayList<Attribute>();
        List<Attribute> actualAttributesList = new ArrayList<Attribute>();
        expectedPayloadAttributes.add(new Attribute("symbol", AttributeType.STRING));
        expectedPayloadAttributes.add(new Attribute("price", AttributeType.DOUBLE));
        for (Attribute attribute : attributeArray) {
            Assert.assertTrue(expectedPayloadAttributes.contains(attribute));
            actualAttributesList.add(attribute);
        }
        Assert.assertEquals(expectedPayloadAttributes, actualAttributesList);
    }

}
