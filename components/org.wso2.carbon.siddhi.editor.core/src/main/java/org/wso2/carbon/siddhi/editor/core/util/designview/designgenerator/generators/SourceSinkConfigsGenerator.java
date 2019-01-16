/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.MapperConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperListPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperMapPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.MapperPayloadOrAttributeType;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator to create Source or Sink config
 */
public class SourceSinkConfigsGenerator extends CodeSegmentsPreserver {
    private static final String TYPE = "TYPE";
    private static final String SINK_ID = "SINK.ID";
    private static final String SOURCE_ID = "SOURCE.ID";
    private static final String MAP = "MAP";

    /**
     * Generates a list of Source configs
     * @param sourceList                        List of Siddhi Sources
     * @return                                  List of SourceSinkConfigs
     * @throws DesignGenerationException        Error while generating SourceConfigs
     */
    public List<SourceSinkConfig> generateSourceConfigs(List<Source> sourceList) throws DesignGenerationException {
        return generateSourceOrSinkConfigs(SourceOrSinkAnnotation.SOURCE, sourceList);
    }

    /**
     * Generates a list of Sink configs
     * @param sinkList                          List of Siddhi Sinks
     * @return                                  List of SourceSinkConfigs
     * @throws DesignGenerationException        Error while generating SinkConfigs
     */
    public List<SourceSinkConfig> generateSinkConfigs(List<Sink> sinkList) throws DesignGenerationException {
        return generateSourceOrSinkConfigs(SourceOrSinkAnnotation.SINK, sinkList);
    }

    /**
     * Generates a list of Source/Sink configs based on the given sourceOrSinkAnnotation,
     * and the given list of Siddhi Sources/Sinks
     * @param sourceOrSinkAnnotation            The annotation, whether 'source' or 'sink'
     * @param sourceOrSinkList                  List of Siddhi Sources/Sinks
     * @param <T>                               Generic Type that is either a Siddhi Source/Sink
     * @return                                  List of SourceSinkConfigs
     * @throws DesignGenerationException        Error while generating Source or Sink Configs
     */
    private <T> List<SourceSinkConfig> generateSourceOrSinkConfigs(
            SourceOrSinkAnnotation sourceOrSinkAnnotation, List<T> sourceOrSinkList) throws DesignGenerationException {
        Map<Annotation, String> connectedElementsMap =
                getSourceOrSinkAnnotationStreamIdMap(sourceOrSinkAnnotation, sourceOrSinkList);
        List<SourceSinkConfig> sourceOrSinkConfigs = new ArrayList<>();
        for (Map.Entry<Annotation, String> connectedElement : connectedElementsMap.entrySet()) {
            sourceOrSinkConfigs.add(generateSourceOrSinkConfig(sourceOrSinkAnnotation, connectedElement));
        }
        return sourceOrSinkConfigs;
    }

    /**
     * Generates config for a Siddhi Source/Sink
     * @param annotationType                        The annotation, whether 'source' or 'sink'
     * @param sourceOrSinkAndConnectedElement       Object which contains the complete details of the Source/Sink,
     *                                              and the element name, to which it is connected to
     * @return                                      SourceSinkConfig
     * @throws DesignGenerationException            Error while generating Source or Sink Configs
     */
    private SourceSinkConfig generateSourceOrSinkConfig(SourceOrSinkAnnotation annotationType,
                                                        Map.Entry<Annotation, String> sourceOrSinkAndConnectedElement)
            throws DesignGenerationException {
        String connectedElementName = sourceOrSinkAndConnectedElement.getValue();
        // Options of the Source/Sink
        String type = null;
        String correlateId = null;
        List<String> options = new ArrayList<>();
        for (Element element : sourceOrSinkAndConnectedElement.getKey().getElements()) {
            if (element.getKey().equalsIgnoreCase(TYPE)) {
                type = element.getValue();
            } else {
                options.add(element.toString());
            }
            if (element.getKey().equalsIgnoreCase(SINK_ID) || element.getKey().equalsIgnoreCase(SOURCE_ID)) {
                correlateId = element.getValue();
            }
        }
        if (type == null) {
            throw new DesignGenerationException("Unable to find the 'type' of the " +
                    annotationType.toString().toLowerCase());
        }
        // Annotations in the Source/Sink
        MapperConfig map = null;
        AnnotationConfigGenerator annotationConfigGenerator = new AnnotationConfigGenerator();
        for (Annotation sourceOrSinkAnnotation : sourceOrSinkAndConnectedElement.getKey().getAnnotations()) {
            if (MAP.equalsIgnoreCase(sourceOrSinkAnnotation.getName())) {
                map = generateMapperConfig(sourceOrSinkAnnotation);
            } else {
                options.add(annotationConfigGenerator.generateAnnotationConfig(sourceOrSinkAnnotation));
            }
        }
        boolean correlateIdExist = false;
        if (correlateId != null) {
            correlateIdExist = true;
        }
        SourceSinkConfig sourceSinkConfig =
                new SourceSinkConfig(annotationType.toString(), connectedElementName, type, options, map,
                        correlateIdExist, correlateId);
        preserveCodeSegmentsOf(annotationConfigGenerator);
        preserveAndBindCodeSegment(sourceOrSinkAndConnectedElement.getKey(), sourceSinkConfig);
        return sourceSinkConfig;
    }

    /**
     * Generates config for a Mapper
     * @param mapAnnotation                     Siddhi annotation that contains details of a Siddhi Mapper
     * @return                                  MapperConfig object
     * @throws DesignGenerationException        Error while generating MapperConfig
     */
    private MapperConfig generateMapperConfig(Annotation mapAnnotation) throws DesignGenerationException {
        String type = null;
        List<String> options = new ArrayList<>();
        for (Element element : mapAnnotation.getElements()) {
            if (element.getKey().equalsIgnoreCase(TYPE)) {
                type = element.getValue();
            } else {
                options.add(element.toString());
            }
        }
        if (type == null) {
            throw new DesignGenerationException("Unable to find 'type' of the mapper");
        }
        MapperPayloadOrAttribute payloadOrAttribute = null;
        if (!mapAnnotation.getAnnotations().isEmpty()) {
            payloadOrAttribute = generateMapperPayloadOrAttributes(mapAnnotation.getAnnotations().get(0));
        }
        MapperConfig mapperConfig = new MapperConfig(type, options, payloadOrAttribute);
        preserveAndBindCodeSegment(mapAnnotation, mapperConfig);
        return mapperConfig;
    }

    /**
     * Generates MapperPayloadOrAttribute object, with the given Siddhi Annotation
     * @param attributesOrPayloadAnnotation         Siddhi Annotation, denoting @attribute or @payload
     * @return                                      MapperPayloadOrAttribute object
     */
    private MapperPayloadOrAttribute generateMapperPayloadOrAttributes(Annotation attributesOrPayloadAnnotation) {
        List<PayloadOrAttributeElement> elements = new ArrayList<>();
        for (Element element : attributesOrPayloadAnnotation.getElements()) {
            elements.add(generatePayloadOrAttributesElement(element));
        }
        MapperPayloadOrAttributeType mapperPayloadOrAttributesType = getMapperAttributeOrPayloadType(elements);
        if (mapperPayloadOrAttributesType == MapperPayloadOrAttributeType.MAP) {
            return generateMapperMapAttribute(attributesOrPayloadAnnotation.getName(), elements);
        }
        return generateMapperListAttribute(attributesOrPayloadAnnotation.getName(), elements);
    }

    /**
     * Generates a PayloadOrAttributeElement object from the given Siddhi Element object
     * @param element       Siddhi Element object
     * @return              PayloadOrAttributeElement object
     */
    private PayloadOrAttributeElement generatePayloadOrAttributesElement(Element element) {
        PayloadOrAttributeElement payloadOrAttributeElement = new PayloadOrAttributeElement();
        payloadOrAttributeElement.key = element.getKey();
        payloadOrAttributeElement.value = element.getValue();
        return payloadOrAttributeElement;
    }

    /**
     * Gets type of the MapperPayloadOrAttribute
     * @param elements      List of PayloadOrAttributeElements
     * @return              MapperPayloadOrAttributeType
     */
    private MapperPayloadOrAttributeType getMapperAttributeOrPayloadType(List<PayloadOrAttributeElement> elements) {
        for (PayloadOrAttributeElement element : elements) {
            // Either all the keys are null, or all the keys are not null
            if (element.key != null) {
                return MapperPayloadOrAttributeType.MAP;
            }
        }
        return MapperPayloadOrAttributeType.LIST;
    }

    /**
     * Generates a MapperListPayloadOrAttribute object with the given annotation type and list of elements
     * @param annotationType        Type of the Siddhi annotation, whether 'attributes' or 'payload'
     * @param elements              List of PayloadOrAttributeElements
     * @return                      MapperListPayloadOrAttribute object
     */
    private MapperListPayloadOrAttribute generateMapperListAttribute(
            String annotationType, List<PayloadOrAttributeElement> elements) {
        List<String> values = new ArrayList<>();
        for (PayloadOrAttributeElement element : elements) {
            values.add(element.value);
        }
        return new MapperListPayloadOrAttribute(annotationType.toUpperCase(), values);
    }

    /**
     * Generates a MapperMapPayloadOrAttribute object with the given annotation type and list of elements
     * @param annotationType        Type of the Siddhi annotation, whether 'attributes' or 'payload'
     * @param elements              List of PayloadOrAttributeElements
     * @return                      MapperMapPayloadOrAttribute object
     */
    private MapperMapPayloadOrAttribute generateMapperMapAttribute(
            String annotationType, List<PayloadOrAttributeElement> elements) {
        Map<String, String> values = new HashMap<>();
        for (PayloadOrAttributeElement element : elements) {
            values.put(element.key, element.value);
        }
        return new MapperMapPayloadOrAttribute(annotationType.toUpperCase(), values);
    }

    /**
     * Generates a map that contains Siddhi Annotation for Source/Sink, and their respective connected element names
     * @param sourceOrSinkAnnotation            Annotation, that is either 'Source' or 'Sink'
     * @param sourceOrSinkList                  List of Siddhi Sources/Sinks
     * @param <T>                               Generic Type for Siddhi Source/Sink
     * @return                                  Map of Siddhi Annotations for Source/Sink, and connected element names
     * @throws DesignGenerationException        Mandatory properties are null
     */
    private <T> Map<Annotation, String> getSourceOrSinkAnnotationStreamIdMap(
            SourceOrSinkAnnotation sourceOrSinkAnnotation, List<T> sourceOrSinkList) throws DesignGenerationException {
        Map<Annotation, String> connectedElements = new HashMap<>();
        for (T sourceOrSink : sourceOrSinkList) {
            List<Annotation> streamDefinitionAnnotations = null;
            String streamId = null;
            if (sourceOrSink instanceof Source) {
                streamId = ((Source) sourceOrSink).getStreamDefinition().getId();
                streamDefinitionAnnotations = ((Source) sourceOrSink).getStreamDefinition().getAnnotations();
            } else if (sourceOrSink instanceof Sink) {
                streamId = ((Sink) sourceOrSink).getStreamDefinition().getId();
                streamDefinitionAnnotations = ((Sink) sourceOrSink).getStreamDefinition().getAnnotations();
            }
            if (streamDefinitionAnnotations == null) {
                throw new DesignGenerationException("Unable to find annotations for the stream definition");
            }
            if (streamId == null) {
                throw new DesignGenerationException("Unable to find the name for the stream definition");
            }
            for(Annotation streamDefinitionAnnotation : streamDefinitionAnnotations) {
                if (streamDefinitionAnnotation.getName().equalsIgnoreCase(sourceOrSinkAnnotation.toString())) {
                    connectedElements.put(streamDefinitionAnnotation, streamId);
                }
            }
        }
        return connectedElements;
    }

    /**
     * Represents a PayloadOrAttribute element
     */
    private static class PayloadOrAttributeElement {
        private String key;
        private String value;
    }

    /**
     * Type of the SourceSinkConfig
     */
    private enum SourceOrSinkAnnotation {
        SOURCE,
        SINK;
    }
}
