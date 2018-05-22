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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.MapperAttributesConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.MapperConfig;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceMapper;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceSinkConfigGenerator {
    public SourceSinkConfig generateSourceConfig(Source source) {
        SourceSinkConfig sourceSinkConfig =
                new SourceSinkConfig(
                        AnnotationType.SOURCE.toString(),
                        source.getType(),
                        generateOptions(source.getStreamDefinition().getAnnotations().get(0).getElements()),
                        generateSourceMapperConfig(source.getMapper()));
        return null;
    }

    private Map<String, String> generateOptions(List<Element> elements) {
        Map<String, String> options = new HashMap<>();
        for (Element element : elements) {
            if (element.getKey() == null) {
                throw new IllegalArgumentException(
                        "Unable to generate config for option '" + element.getValue() + "', since no key is present");
            }
            // Put all elements except 'type'
            if (!element.getKey().equalsIgnoreCase(ElementKey.TYPE.toString())) {
                options.put(element.getKey(), element.getValue());
            }
        }
        return options;
    }

    private MapperConfig generateSourceMapperConfig(SourceMapper sourceMapper) {
        Annotation sourceAnnotation =
                sourceMapper.getStreamDefinition().getAnnotations().stream()
                        .filter(annotation -> "source".equalsIgnoreCase(annotation.getName()))
                        .findFirst().orElse(null);
        if (sourceAnnotation == null) {
            throw new IllegalArgumentException("Unable to retrieve ");
        }
        String test = "";

        return null;
    }

    private MapperAttributesConfig generateMapperAttributesConfig(Annotation mapAnnotation) {
        for (Element element : mapAnnotation.getElements()) {

        }
        return null;
    }

    private enum AnnotationType {
        SOURCE,
        SINK,
        MAP;
    }

    private enum ElementKey {
        TYPE;
    }
}
