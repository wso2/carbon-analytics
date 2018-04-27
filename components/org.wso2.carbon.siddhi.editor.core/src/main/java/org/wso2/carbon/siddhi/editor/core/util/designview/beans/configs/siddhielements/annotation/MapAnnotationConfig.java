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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation;

import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AnnotationConfigType;

import java.util.Map;

/**
 * Represents a Siddhi annotation, whose content is in the following form:
 * - @element(option1=value1, option2=value2)
 */
public class MapAnnotationConfig extends AnnotationConfig {
    private Map<String, AnnotationValue> value;

    public MapAnnotationConfig(String name, Map<String, AnnotationValue> value) {
        super(name, AnnotationConfigType.MAP.toString());
        this.value = value;
    }

    public Map<String, AnnotationValue> getValue() {
        return value;
    }
}
