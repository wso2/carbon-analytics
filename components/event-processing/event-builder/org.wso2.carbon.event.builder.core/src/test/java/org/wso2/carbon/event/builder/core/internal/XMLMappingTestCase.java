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

package org.wso2.carbon.event.builder.core.internal;

import org.jaxen.JaxenException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLInputMapping;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;

import java.util.ArrayList;
import java.util.List;

//todo do the testing using EventBuilder instead of the mapping
public class XMLMappingTestCase {
    private static final String XPATH_NS = "http://ws.cdyne.com/";
    private static final String XPATH_PREFIX = "quotedata";
    private static final String XPATH_FOR_SYMBOL = "//quotedata:StockQuoteEvent/quotedata:StockSymbol";
    private static final String XPATH_FOR_PRICE = "//quotedata:StockQuoteEvent/quotedata:LastTradeAmount";
    private EventBuilderConfiguration xmlEventBuilderConfig;

    @Before
    public void init() {
        xmlEventBuilderConfig = new EventBuilderConfiguration();
        xmlEventBuilderConfig.setToStreamName("stockQuotes");
        xmlEventBuilderConfig.setToStreamVersion("1.0.0");
        XMLInputMapping xmlInputMapping = new XMLInputMapping();
        List<XPathDefinition> xPathDefinitions = new ArrayList<XPathDefinition>();
        xPathDefinitions.add(new XPathDefinition(XPATH_PREFIX, XPATH_NS));
        xmlInputMapping.setXPathDefinitions(xPathDefinitions);
        xmlInputMapping.addInputMappingAttribute(new InputMappingAttribute(XPATH_FOR_SYMBOL, "symbol", AttributeType.STRING));
        xmlInputMapping.addInputMappingAttribute(new InputMappingAttribute(XPATH_FOR_PRICE, "price", AttributeType.DOUBLE));
        xmlEventBuilderConfig.setInputMapping(xmlInputMapping);
    }

    @Test
    public void testCreateMapping() throws MalformedStreamDefinitionException, JaxenException {
        Attribute[] attributeArray = EventBuilderUtil.getOrderedAttributeArray((XMLInputMapping) xmlEventBuilderConfig.getInputMapping());
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
