/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.rules.core.bean.template;

import org.wso2.carbon.business.rules.core.bean.BusinessRule;

import java.util.Map;

/**
 * Represents a Business Rule, created from Template
 */
public class BusinessRuleFromTemplate extends BusinessRule {
    private String ruleTemplateUUID;
    private Map<String, String> properties;

    public BusinessRuleFromTemplate(String uuid, String name, String templateGroupUUID, String type,
                                    String ruleTemplateUUID, Map<String, String> properties) {
        super(uuid, name, templateGroupUUID, type);
        this.ruleTemplateUUID = ruleTemplateUUID;
        this.properties = properties;
    }

    public String getRuleTemplateUUID() {
        return ruleTemplateUUID;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "BusinessRuleFromTemplate{" +
                "\n" + super.toString() +
                "\nruleTemplateUUID='" + ruleTemplateUUID + '\'' +
                ", \nproperties=" + properties +
                "\n} ";
    }
}
