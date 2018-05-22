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

import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.List;

public class AnnotationConfigGenerator {
    /**
     * Generates AnnotationConfig String for the given Siddhi Annotation
     * @param annotation    Siddhi Annotation
     * @return              String representing the Annotation
     */
    public String generateAnnotationConfig(Annotation annotation) {
        StringBuilder annotationConfig = new StringBuilder();
        annotationConfig.append("@");
        annotationConfig.append(annotation.getName());
        annotationConfig.append("(");

        List<String> elements = new ArrayList<>();
        for (Element element : annotation.getElements()) {
            if (element.getKey() == null) {
                elements.add(String.format("'%s'", element.getValue()));
            } else {
                elements.add(String.format("%s='%s'", element.getKey(), element.getValue()));
            }
        }

        annotationConfig.append(String.join(", ", elements));
        annotationConfig.append(")");

        return annotationConfig.toString();
    }
}
