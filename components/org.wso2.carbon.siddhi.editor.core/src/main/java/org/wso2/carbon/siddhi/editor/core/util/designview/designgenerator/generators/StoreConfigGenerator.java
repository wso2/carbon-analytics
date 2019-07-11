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
import io.siddhi.query.api.annotation.Annotation;
import io.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create Store config
 */
public class StoreConfigGenerator extends CodeSegmentsPreserver {
    public StoreConfig generateStoreConfig(Annotation storeAnnotation) throws DesignGenerationException {
        String type = null;
        List <String> options = new ArrayList<>();
        for (Element element : storeAnnotation.getElements()) {
            if (element.getKey().equalsIgnoreCase("TYPE")) {
                type = element.getValue();
            } else {
                options.add(element.toString());
            }
        }
        if (type == null) {
            throw new DesignGenerationException("Can not find type for the Store");
        }
        StoreConfig storeConfig = new StoreConfig(type, options);
        preserveAndBindCodeSegment(storeAnnotation, storeConfig);

        return storeConfig;
    }
}
