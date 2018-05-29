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
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create a list of AttributeConfigs, for a list of Siddhi Attributes
 */
public class AttributeConfigListGenerator {
    /**
     * Generates list of AttributeConfigs, with given List of Siddhi Attributes
     * @param attributes    List of Siddhi Attribute objects
     * @return              List of AttributeConfig objects
     */
    public List<AttributeConfig> generateAttributeConfigList(List<Attribute> attributes) {
        List<AttributeConfig> attributeConfigs = new ArrayList<>();
        for (Attribute attribute : attributes) {
            attributeConfigs.add(new AttributeConfig(attribute.getName(), attribute.getType().name().toLowerCase()));
        }
        return attributeConfigs;
    }
}
