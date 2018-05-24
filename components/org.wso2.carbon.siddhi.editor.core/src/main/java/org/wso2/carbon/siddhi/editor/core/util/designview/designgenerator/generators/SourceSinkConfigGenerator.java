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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.*;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create Source/Sink config
 */
public class SourceSinkConfigGenerator {
    /**
     * Generates config for a given Siddhi Source
     * @param source        Siddhi Source
     * @return              SourceSinkConfig object
     */
    public SourceSinkConfig generateSourceConfig(Source source) {
        return new SourceSinkConfig(
                AnnotationType.SOURCE.toString(),
                source.getType(),
                generateSourceOrSinkOptions(source.getStreamDefinition().getAnnotations().get(0).getElements()),
                generateMapperConfig(
                        source.getStreamDefinition().getAnnotations().get(0).getAnnotations("map").get(0)));
    }

    /**
     * Generates config for a given Siddhi Sink
     * @param sink      Siddhi Sink
     * @return          SourceSinkConfig object
     */
    public SourceSinkConfig generateSinkConfig(Sink sink) {
        return new SourceSinkConfig(
                AnnotationType.SINK.toString(),
                sink.getType(),
                generateSourceOrSinkOptions(sink.getStreamDefinition().getAnnotations().get(0).getElements()),
                generateMapperConfig(
                        sink.getStreamDefinition().getAnnotations().get(0).getAnnotations("map").get(0)));
    }

    /**
     * Generates options of a Siddhi Source/Sink
     * @param sourceOrSinkElements      List of Elements, of a Source/Sink
     * @return                          List of Element Strings
     */
    private List<String> generateSourceOrSinkOptions(List<Element> sourceOrSinkElements) {
        List<String> options = new ArrayList<>();
        for (Element element : sourceOrSinkElements) {
            if (element.getKey() != null) {
                // Put elements except 'type'
                if (!element.getKey().equalsIgnoreCase(ElementKey.TYPE.toString())) {
                    options.add(element.toString());
                }
            } else {
                options.add(element.getValue());
            }
        }
        return options;
    }

    /**
     * Generates config for a Mapper of a Siddhi Source/Sink
     * @param mapAnnotation     Siddhi Annotation object, which represents @map
     * @return                  Mapper Config object
     */
    private MapperConfig generateMapperConfig(Annotation mapAnnotation) {
        return new MapperConfig(
                mapAnnotation.getElement(ElementKey.TYPE.toString()),
                generateMapperOptions(mapAnnotation),
                generateCustomMappingAttributes(mapAnnotation));
    }

    /**
     * Generates options of a Siddhi Mapper
     * @param mapAnnotation     Siddhi Annotation object, representing @map annotation
     * @return                  List of options of the Siddhi Mapper
     */
    private List<String> generateMapperOptions(Annotation mapAnnotation) {
        List<String> options = new ArrayList<>();
        for (Element element : mapAnnotation.getElements()) {
            // Put elements except 'type'
            if (!(ElementKey.TYPE.toString()).equalsIgnoreCase(element.getKey())) {
                options.add(element.toString());
            }
        }
        return options;
    }

    /**
     * Generates custom mapping attributes of a Siddhi Mapper
     * @param mapAnnotation     Siddhi Annotation object, representing @map annotation
     * @return                  List of custom mapping attributes of the Siddhi Mapper
     */
    private List<String> generateCustomMappingAttributes(Annotation mapAnnotation) {
        List<String> customMappingAttributes = new ArrayList<>();
        if (!mapAnnotation.getAnnotations(AnnotationType.ATTRIBUTES.toString()).isEmpty()) {
            Annotation attributesAnnotation = mapAnnotation.getAnnotations(AnnotationType.ATTRIBUTES.toString()).get(0);
            for (Element element : attributesAnnotation.getElements()) {
                customMappingAttributes.add(element.toString());
            }
        }

        return customMappingAttributes;
    }

    private enum AnnotationType {
        SOURCE,
        SINK,
        MAP,
        ATTRIBUTES;
    }

    private enum ElementKey {
        TYPE;
    }
}
