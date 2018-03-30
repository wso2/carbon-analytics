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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.factories;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.AnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.ListAnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.MapAnnotationConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Factory to create Siddhi Annotation config
 */
public class AnnotationConfigFactory {
    public AnnotationConfig getAnnotationConfig(String name, String value) {
        return new ListAnnotationConfig(name, new ArrayList<>(Arrays.asList(value)));
    }

    public AnnotationConfig getAnnotationConfig(String name, List<String> values) {
        return new ListAnnotationConfig(name, values);
    }

    public AnnotationConfig getAnnotationConfig(String name, Map<String, String> elements) {
        return new MapAnnotationConfig(name, elements);
    }
}
