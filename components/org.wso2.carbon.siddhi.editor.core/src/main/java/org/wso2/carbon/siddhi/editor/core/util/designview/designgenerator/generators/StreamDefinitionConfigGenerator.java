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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.AttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.annotation.AnnotationConfigGenerator;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create Stream definition config
 */
public class StreamDefinitionConfigGenerator {
    /**
     * Generates StreamConfig object, with given Siddhi StreamDefinition
     * @param streamDefinition  Siddhi StreamDefinition object
     * @param isInnerStream     Whether the stream is an inner stream
     * @return                  StreamConfig object
     */
    public StreamConfig generateStreamConfig(StreamDefinition streamDefinition, boolean isInnerStream) {
        List<AttributeConfig> attributeConfigs = new ArrayList<>();
        for (Attribute attribute : streamDefinition.getAttributeList()) {
            attributeConfigs.add(new AttributeConfig(attribute.getName(), attribute.getType().name().toLowerCase()));
        }
        List<String> annotationConfigs = new ArrayList<>();
        for (Annotation annotation : streamDefinition.getAnnotations()) {
            annotationConfigs.add(new AnnotationConfigGenerator().generateAnnotationConfig(annotation));
        }
        return new StreamConfig(streamDefinition.getId(),
                streamDefinition.getId(),
                isInnerStream,
                attributeConfigs,
                annotationConfigs);
    }
}
