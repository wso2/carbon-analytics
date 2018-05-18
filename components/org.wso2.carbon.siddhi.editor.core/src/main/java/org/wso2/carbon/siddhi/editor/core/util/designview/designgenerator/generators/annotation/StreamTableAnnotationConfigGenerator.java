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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.annotation;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.AnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.AnnotationValue;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.ListAnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.factories.AnnotationConfigFactory;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.DesignGeneratorHelper;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator to create AnnotationConfig for Siddhi Annotations of Siddhi Streams and Tables
 */
public class StreamTableAnnotationConfigGenerator {
    /**
     * Generates AnnotationConfig for the given Siddhi Annotation
     * @param annotation    AnnotationConfig object
     */
    public AnnotationConfig generateAnnotationConfig(Annotation annotation) {
        Map<String, AnnotationValue> annotationElements = new HashMap<>();
        List<AnnotationValue> annotationValues = new ArrayList<>();
        for (Element element : annotation.getElements()) {
            if (null == element.getKey()) {
                annotationValues.add(
                        new AnnotationValue(element.getValue(), DesignGeneratorHelper.isStringValue(element)));
            } else {
                annotationElements.put(
                        element.getKey(),
                        new AnnotationValue(element.getValue(), DesignGeneratorHelper.isStringValue(element)));
            }
        }
        if (annotationElements.isEmpty() && annotationValues.isEmpty()) {
            // No elements inside the annotation. Consider as an empty ListAnnotationConfig
            return new ListAnnotationConfig(annotation.getName(), new ArrayList<>(0));
        }
        AnnotationConfigFactory annotationConfigFactory = new AnnotationConfigFactory();
        if (annotationElements.isEmpty()) {
            return annotationConfigFactory.getAnnotationConfig(annotation.getName(), annotationValues);
        }
        return annotationConfigFactory.getAnnotationConfig(annotation.getName(), annotationElements);
    }
}
