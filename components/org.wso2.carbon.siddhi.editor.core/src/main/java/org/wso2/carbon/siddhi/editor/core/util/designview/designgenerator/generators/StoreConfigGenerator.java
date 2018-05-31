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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Generator to create Store config
 */
public class StoreConfigGenerator {
    public StoreConfig generateStoreConfig(Annotation storeAnnotation) throws DesignGenerationException {
        String type = null;
        Map<String, String> options = new HashMap<>();
        for (Element element : storeAnnotation.getElements()) {
            if (element.getKey().equalsIgnoreCase("TYPE")) {
                type = element.getValue();
            } else {
                options.put(element.getKey(), element.getValue());
            }
        }

        if (type == null) {
            throw new DesignGenerationException("Can not find type for the Store");
        }

        return new StoreConfig(
                type,
                options);
    }
}
