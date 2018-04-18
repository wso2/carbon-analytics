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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.factories;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.AnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.AnnotationValue;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.ListAnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.MapAnnotationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory to create Siddhi AnnotationConfig
 */
public class AnnotationConfigFactory {
    /**
     * Returns a ListAnnotationConfig with the given name and list of valueConfigs
     * @param name      Name of the annotation
     * @param values    List of values, of the annotation
     * @return          ListAnnotationConfig, with given list's members
     */
    public AnnotationConfig getAnnotationConfig(String name, List<AnnotationValue> values) {
        return new ListAnnotationConfig(name, values);
    }

    /**
     * Returns a MapAnnotationConfig with the given name and map of valueConfigs
     * @param name      Name of the annotation
     * @param elements  Option Value pairs, of the annotation
     * @return          MapAnnotationConfig, with the given map's members
     */
    public AnnotationConfig getAnnotationConfig(String name, Map<String, AnnotationValue> elements) {
        return new MapAnnotationConfig(name, elements);
    }

    /**
     * Returns a ListAnnotationConfig with the given name and valueConfig.
     * Single valueConfig will be the only member in the list.
     * @param name      Name of the annotation
     * @param value     Value of the annotation
     * @return          ListAnnotationConfig, with the given value as the only member of the list
     */
    public AnnotationConfig getAnnotationConfig(String name, AnnotationValue value) {
        List<AnnotationValue> valueConfig = new ArrayList<>(1);
        valueConfig.add(value);
        return new ListAnnotationConfig(name, new ArrayList<>(valueConfig));
    }
}
