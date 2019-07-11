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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import io.siddhi.query.api.annotation.Annotation;
import io.siddhi.query.api.definition.WindowDefinition;
import io.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create WindowConfig
 */
public class WindowConfigGenerator extends CodeSegmentsPreserver {
    private String siddhiAppString;

    public WindowConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Generates WindowConfig from the given Siddhi WindowDefinition
     * @param windowDefinition                  Siddhi WindowDefinition
     * @return                                  WindowConfig object
     * @throws DesignGenerationException        Error while generating WindowConfig
     */
    public WindowConfig generateWindowConfig(WindowDefinition windowDefinition) throws DesignGenerationException {
        List<String> parameters = new ArrayList<>();
        for (Expression expression : windowDefinition.getWindow().getParameters()) {
            parameters.add(ConfigBuildingUtilities.getDefinition(expression, siddhiAppString));
        }
        List<String> annotationConfigs = new ArrayList<>();
        List<Annotation> annotationListObjects = new ArrayList<>();
        AnnotationConfigGenerator annotationConfigGenerator = new AnnotationConfigGenerator();
        AttributeConfigListGenerator attributeConfigListGenerator = new AttributeConfigListGenerator();
        for (Annotation annotation : windowDefinition.getAnnotations()) {
            annotationListObjects.add(annotation);
            annotationConfigs.add(annotationConfigGenerator.generateAnnotationConfig(annotation));
        }
        WindowConfig windowConfig =
                new WindowConfig(
                        windowDefinition.getId(),
                        windowDefinition.getId(),
                        attributeConfigListGenerator.generateAttributeConfigList(windowDefinition.getAttributeList()),
                        windowDefinition.getWindow().getName(),
                        parameters,
                        windowDefinition.getOutputEventType().name(),
                        annotationConfigs,
                        annotationListObjects);
        preserveCodeSegmentsOf(annotationConfigGenerator, attributeConfigListGenerator);
        preserveAndBindCodeSegment(windowDefinition, windowConfig);

        return windowConfig;
    }
}
