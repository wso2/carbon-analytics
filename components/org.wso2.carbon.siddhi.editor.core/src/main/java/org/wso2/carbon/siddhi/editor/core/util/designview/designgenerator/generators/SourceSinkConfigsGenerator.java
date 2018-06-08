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
public class SourceSinkConfigsGenerator {
    private static final String TYPE = "TYPE";
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
        List<String> options = new ArrayList<>();
        for (Element element : sourceOrSinkAndConnectedElement.getKey().getElements()) {
            if (element.getKey().equalsIgnoreCase(TYPE)) {
                type = element.getValue();
            } else {
                options.add(element.toString());
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
        return new SourceSinkConfig(annotationType.toString(), connectedElementName, type, options, map);
    }

    /**
     * Generates config for a Mapper
     * @param mapAnnotation
     * @return
     * @throws DesignGenerationException
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
        AnnotationConfigGenerator annotationConfigGenerator = new AnnotationConfigGenerator();
        List<String> attributes =
                annotationConfigGenerator.generateAnnotationConfigList(mapAnnotation.getAnnotations());

        return new MapperConfig(type, options, attributes);
    }

    private <T> Map<Annotation, String> getSourceOrSinkAnnotationStreamIdMap(
            SourceOrSinkAnnotation sourceOrSinkAnnotation, List<T> sourceOrSinkList) throws DesignGenerationException {
        Map<Annotation, String> sinksAndConnectedElements = new HashMap<>();
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
                    sinksAndConnectedElements.put(streamDefinitionAnnotation, streamId);
                }
            }
        }
        return sinksAndConnectedElements;
    }

    private enum SourceOrSinkAnnotation {
        SOURCE,
        SINK;
    }
}
