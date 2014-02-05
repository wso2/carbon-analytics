package org.wso2.carbon.event.stream.manager.core.internal.util;

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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Random;

public class SampleEventGenerator {

    private static final Log log = LogFactory.getLog(SampleEventGenerator.class);

    private static double[] doubleValues = {1.23434, 4.504343, 5.443435, 20.44345, 90.34344};
    private static int[] intValues = {4, 50, 100, 45, 70};
    private static boolean[] booleanValues = {true, false};
    private static float[] floatValues = {6.6f, 4.5f, 8.9f, 2.3f, 9.8f};
    private static long[] longValues = {56783, 545455, 645565, 323232, 4354643};
    private static String[] stringValues = {"data1", "data2", "data3", "data4", "data5"};

    private SampleEventGenerator() {
    }

    public static String generateXMLEvent(StreamDefinition streamDefinition)
            throws EventStreamConfigurationException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement sampleEventElement = factory.createOMElement(new QName(
                EventStreamConstants.SAMPLE_EVENT_PARENT_TAG));
        sampleEventElement.declareDefaultNamespace(EventStreamConstants.SAMPLE_EVENT_DEFAULT_NAMESPACE);

        List<Attribute> metaDatAttributes = streamDefinition.getMetaData();
        if (metaDatAttributes != null && metaDatAttributes.size() > 0) {
            sampleEventElement.addChild(createPropertyElement(factory, metaDatAttributes, EventStreamConstants.SAMPLE_EVENT_META_TAG));
        }

        List<Attribute> correlationAttributes = streamDefinition.getCorrelationData();
        if (correlationAttributes != null && correlationAttributes.size() > 0) {
            sampleEventElement.addChild(createPropertyElement(factory, correlationAttributes, EventStreamConstants.SAMPLE_EVENT_CORRELATION_TAG));
        }

        List<Attribute> payloadAttributes = streamDefinition.getPayloadData();
        if (payloadAttributes != null && payloadAttributes.size() > 0) {
            sampleEventElement.addChild(createPropertyElement(factory, payloadAttributes, EventStreamConstants.SAMPLE_EVENT_PAYLOAD_TAG));
        }

        return sampleEventElement.toString();
    }

    public static String generateJSONEvent(StreamDefinition streamDefinition)
            throws EventStreamConfigurationException {

        return "";
    }

    public static String generateTextEvent(StreamDefinition streamDefinition)
            throws EventStreamConfigurationException {


        String sampleEvent = "";

        List<Attribute> metaDatAttributes = streamDefinition.getMetaData();
        if (metaDatAttributes != null && metaDatAttributes.size() > 0) {
            for (Attribute attribute : metaDatAttributes) {
                sampleEvent += "\n"+EventStreamConstants.META_PREFIX + attribute.getName() + EventStreamConstants.EVENT_ATTRIBUTE_SEPARATOR + getSampleValue(attribute.getType());
            }
        }

        List<Attribute> correlationAttributes = streamDefinition.getCorrelationData();
        if (correlationAttributes != null && correlationAttributes.size() > 0) {
            for (Attribute attribute : correlationAttributes) {
                sampleEvent += "\n"+EventStreamConstants.CORRELATION_PREFIX + attribute.getName() + EventStreamConstants.EVENT_ATTRIBUTE_SEPARATOR + getSampleValue(attribute.getType());
            }
        }


        List<Attribute> payloadAttributes = streamDefinition.getPayloadData();
        if (payloadAttributes != null && payloadAttributes.size() > 0) {
            for (Attribute attribute : payloadAttributes) {
                sampleEvent += "\n"+ attribute.getName() + EventStreamConstants.EVENT_ATTRIBUTE_SEPARATOR + getSampleValue(attribute.getType());
            }
        }

        return sampleEvent;
    }


    private static OMElement createPropertyElement(OMFactory factory, List<Attribute> attributeList,
                                                   String propertyTag) {
        OMElement metaDataPropertyElement = factory.createOMElement(new QName(
                propertyTag));
        metaDataPropertyElement.declareDefaultNamespace(EventStreamConstants.SAMPLE_EVENT_DEFAULT_NAMESPACE);

        for (Attribute attribute : attributeList) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    attribute.getName()));
            propertyElement.declareDefaultNamespace(EventStreamConstants.SAMPLE_EVENT_DEFAULT_NAMESPACE);
            propertyElement.setText(getSampleValue(attribute.getType()));
            metaDataPropertyElement.addChild(propertyElement);
        }
        return metaDataPropertyElement;
    }


    private static String getSampleValue(AttributeType attributeType) {
        Random rand = new Random();

        if (attributeType.name().toLowerCase().equals(EventStreamConstants.ATTR_TYPE_FLOAT)) {
            int hostIndex = rand.nextInt(5);
            return floatValues[hostIndex] + "";
        } else if (attributeType.name().toLowerCase().equals(EventStreamConstants.ATTR_TYPE_DOUBLE)) {
            int hostIndex = rand.nextInt(5);
            return doubleValues[hostIndex] + "";
        } else if (attributeType.name().toLowerCase().equals(EventStreamConstants.ATTR_TYPE_INTEGER)) {
            int hostIndex = rand.nextInt(5);
            return intValues[hostIndex] + "";
        } else if (attributeType.name().toLowerCase().equals(EventStreamConstants.ATTR_TYPE_LONG)) {
            int hostIndex = rand.nextInt(5);
            return longValues[hostIndex] + "";
        } else if (attributeType.name().toLowerCase().equals(EventStreamConstants.ATTR_TYPE_BOOL)) {
            int hostIndex = rand.nextInt(2);
            return booleanValues[hostIndex] + "";
        } else if (attributeType.name().toLowerCase().equals(EventStreamConstants.ATTR_TYPE_STRING)) {
            int hostIndex = rand.nextInt(5);
            return stringValues[hostIndex] + "";
        }
        return "";
    }

}
