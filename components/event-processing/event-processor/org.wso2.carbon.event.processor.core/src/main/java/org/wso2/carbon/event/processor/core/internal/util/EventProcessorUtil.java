/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class EventProcessorUtil {
    private static Log log = LogFactory.getLog(EventProcessorUtil.class);

    public static StreamDefinition convertToDatabridgeStreamDefinition(
            org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition,
            StreamConfiguration streamConfiguration) {
        StreamDefinition databridgeDefinition = null;
        try {
            databridgeDefinition = new StreamDefinition(streamConfiguration.getName(), streamConfiguration.getVersion());
        } catch (MalformedStreamDefinitionException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        List<org.wso2.carbon.databridge.commons.Attribute> payload = new ArrayList<org.wso2.carbon.databridge.commons.Attribute>();
        List<org.wso2.carbon.databridge.commons.Attribute> meta = new ArrayList<org.wso2.carbon.databridge.commons.Attribute>();
        List<org.wso2.carbon.databridge.commons.Attribute> correlation = new ArrayList<org.wso2.carbon.databridge.commons.Attribute>();
        if (siddhiStreamDefinition.getAttributeList() != null) {
            for (Attribute attribute : siddhiStreamDefinition.getAttributeList()) {
                if (attribute.getName().startsWith("meta_")) {
                    meta.add(convertToDatabridgeAttribute(attribute, "meta_"));
                } else if (attribute.getName().startsWith("correlation_")) {
                    correlation.add(convertToDatabridgeAttribute(attribute, "correlation_"));
                } else {
                    payload.add(convertToDatabridgeAttribute(attribute, null));

                }
            }
        }
        if (!payload.isEmpty()) {
            databridgeDefinition.setPayloadData(payload);
        }
        if (!meta.isEmpty()) {
            databridgeDefinition.setMetaData(meta);
        }
        if (!correlation.isEmpty()) {
            databridgeDefinition.setCorrelationData(correlation);
        }
        return databridgeDefinition;
    }

    public static org.wso2.siddhi.query.api.definition.StreamDefinition convertToSiddhiStreamDefinition(
            StreamDefinition streamDefinition, StreamConfiguration streamConfiguration) {
        org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition = new org.wso2.siddhi.query.api.definition.StreamDefinition();
        siddhiStreamDefinition.name(streamConfiguration.getSiddhiStreamName());
        if (streamDefinition.getMetaData() != null) {
            for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getMetaData()) {
                siddhiStreamDefinition.attribute(attribute.getName(), convertToSiddhiAttribute(attribute, EventProcessorConstants.META + EventProcessorConstants.ATTRIBUTE_SEPARATOR).getType());
            }
        }

        if (streamDefinition.getCorrelationData() != null) {
            for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getCorrelationData()) {
                siddhiStreamDefinition.attribute(attribute.getName(), convertToSiddhiAttribute(attribute, EventProcessorConstants.CORRELATION + EventProcessorConstants.ATTRIBUTE_SEPARATOR).getType());
            }
        }

        if (streamDefinition.getPayloadData() != null) {
            for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getPayloadData()) {
                siddhiStreamDefinition.attribute(attribute.getName(), convertToSiddhiAttribute(attribute, "").getType());
            }
        }
        return siddhiStreamDefinition;
    }

    public static org.wso2.carbon.databridge.commons.Attribute convertToDatabridgeAttribute(
            Attribute attribute, String prefixToDrop) {
        AttributeType type;
        switch (attribute.getType()) {
            case LONG:
                type = AttributeType.LONG;
                break;
            case INT:
                type = AttributeType.INT;
                break;
            case FLOAT:
                type = AttributeType.FLOAT;
                break;
            case DOUBLE:
                type = AttributeType.DOUBLE;
                break;
            case BOOL:
                type = AttributeType.BOOL;
                break;
            default:
                type = AttributeType.STRING;
                break;
        }
        String name = (prefixToDrop != null) ? attribute.getName().replaceFirst(prefixToDrop, "") : attribute.getName();
        return new org.wso2.carbon.databridge.commons.Attribute(name, type);

    }

    public static Attribute convertToSiddhiAttribute(org.wso2.carbon.databridge.commons.Attribute attribute, String prefix) {
        Attribute.Type type;
        switch (attribute.getType()) {
            case LONG:
                type = Attribute.Type.LONG;
                break;
            case INT:
                type = Attribute.Type.INT;
                break;
            case FLOAT:
                type = Attribute.Type.FLOAT;
                break;
            case DOUBLE:
                type = Attribute.Type.DOUBLE;
                break;
            case BOOL:
                type = Attribute.Type.BOOL;
                break;
            default:
                type = Attribute.Type.STRING;
                break;
        }
        return new Attribute(prefix + attribute.getName(), type);

    }

    public static String formatXml(String unformattedXml) throws
            ExecutionPlanConfigurationException {
        try {
            final Document document = parseXmlFile(unformattedXml);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new ExecutionPlanConfigurationException(e);
        }
    }

    private static Document parseXmlFile(String in) throws ExecutionPlanConfigurationException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new ExecutionPlanConfigurationException(e);
        } catch (SAXException e) {
            throw new ExecutionPlanConfigurationException(e);
        } catch (IOException e) {
            throw new ExecutionPlanConfigurationException(e);
        }
    }

    public static String getStreamId(String streamName, String version) {
        return streamName + EventProcessorConstants.STREAM_SEPARATOR + version;
    }

    public static String getStreamName(String streamId) {
        return streamId.split(EventProcessorConstants.STREAM_SEPARATOR)[0];
    }
}
