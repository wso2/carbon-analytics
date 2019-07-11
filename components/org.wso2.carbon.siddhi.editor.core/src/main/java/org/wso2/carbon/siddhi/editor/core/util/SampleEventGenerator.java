/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.siddhi.editor.core.util;

import com.google.gson.JsonObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.definition.StreamDefinition;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Random;

public class SampleEventGenerator {

    private static double[] doubleValues = {1.23434, 4.504343, 5.443435, 20.44345, 90.34344};
    private static int[] intValues = {4, 50, 100, 45, 70};
    private static boolean[] booleanValues = {true, false};
    private static float[] floatValues = {6.6f, 4.5f, 8.9f, 2.3f, 9.8f};
    private static long[] longValues = {56783, 545455, 645565, 323232, 4354643};
    private static String[] stringValues = {"data1", "data2", "data3", "data4", "data5"};

    private SampleEventGenerator() {
    }

    public static String generateXMLEvent(StreamDefinition streamDefinition) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement sampleEventsElement = factory.createOMElement(new QName(
                Constants.SAMPLE_EVENTS_PARENT_TAG));
        OMElement sampleEventElement = factory.createOMElement(new QName(
                Constants.SAMPLE_EVENT_PARENT_TAG));
        List<Attribute> attributeList = streamDefinition.getAttributeList();
        if (attributeList != null && attributeList.size() > 0) {
            for (Attribute attribute : attributeList) {
                OMElement propertyElement = factory.createOMElement(new QName(attribute.getName()));
                propertyElement.setText(getSampleValue(attribute.getType()));
                sampleEventElement.addChild(propertyElement);
            }
        }
        sampleEventsElement.addChild(sampleEventElement);
        return sampleEventsElement.toString();
    }

    public static String generateJSONEvent(StreamDefinition streamDefinition) {
        JsonObject jsonEventObject = new JsonObject();
        JsonObject innerParentObject = new JsonObject();
        List<Attribute> attributeList = streamDefinition.getAttributeList();
        if (attributeList.size() > 0) {
            for (Attribute attribute : attributeList) {
                if (attribute.getType().name().toLowerCase().equals(Constants.ATTR_TYPE_STRING)) {
                    innerParentObject.addProperty(attribute.getName(), getSampleValue(attribute.getType()));
                } else if (attribute.getType().name().toLowerCase().
                        equals(Constants.ATTR_TYPE_BOOL)) {
                    innerParentObject.addProperty(attribute.getName(),
                            Boolean.parseBoolean(getSampleValue(attribute.getType())));
                } else {
                    innerParentObject.addProperty(attribute.getName(),
                            Double.parseDouble(getSampleValue(attribute.getType())));
                }
            }
            jsonEventObject.add(Constants.SAMPLE_EVENT_PARENT_TAG, innerParentObject);
        }

        return jsonEventObject.toString();
    }

    public static String generateTextEvent(StreamDefinition streamDefinition) {
        StringBuilder sampleEvent = new StringBuilder();
        List<Attribute> attributeList = streamDefinition.getAttributeList();
        if (attributeList != null && attributeList.size() > 0) {
            for (int i = 0; i < attributeList.size(); i++) {
                if (i != 0) {
                    sampleEvent.append(",\n");
                }
                if (Constants.ATTR_TYPE_STRING.equals(attributeList.get(i).getType().toString())) {
                    sampleEvent.append(attributeList.get(i).getName()).append(Constants.EVENT_ATTRIBUTE_VALUE_SEPARATOR).
                            append("'").append(getSampleValue(attributeList.get(i).getType())).append("'");
                } else {
                    sampleEvent.append(attributeList.get(i).getName()).append(Constants.EVENT_ATTRIBUTE_VALUE_SEPARATOR).
                            append(getSampleValue(attributeList.get(i).getType()));
                }
            }
        }
        return sampleEvent.toString();
    }

    private static String getSampleValue(Attribute.Type attributeType) {
        Random rand = new Random();
        switch (attributeType.name().toLowerCase()) {
            case Constants.ATTR_TYPE_FLOAT: {
                int hostIndex = rand.nextInt(5);
                return floatValues[hostIndex] + "";
            }
            case Constants.ATTR_TYPE_DOUBLE: {
                int hostIndex = rand.nextInt(5);
                return doubleValues[hostIndex] + "";
            }
            case Constants.ATTR_TYPE_INTEGER: {
                int hostIndex = rand.nextInt(5);
                return intValues[hostIndex] + "";
            }
            case Constants.ATTR_TYPE_LONG: {
                int hostIndex = rand.nextInt(5);
                return longValues[hostIndex] + "";
            }
            case Constants.ATTR_TYPE_BOOL: {
                int hostIndex = rand.nextInt(2);
                return booleanValues[hostIndex] + "";
            }
            case Constants.ATTR_TYPE_STRING: {
                int hostIndex = rand.nextInt(5);
                return stringValues[hostIndex] + "";
            }
        }
        return "";
    }
}
