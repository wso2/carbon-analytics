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

package org.wso2.carbon.business.rules.core.internal.bean.businessRulesFromTemplate;

import org.wso2.carbon.business.rules.core.internal.bean.BusinessRule;

import java.util.Map;

/**
 * Represents a Business Rule, created from Template
 */
public class BusinessRuleFromTemplate extends BusinessRule {
    private String ruleTemplateName;
    private Map<String, String> properties;

    public BusinessRuleFromTemplate(String uuid, String name, String templateGroupName, String type, String ruleTemplateName, Map<String, String> properties) {
        super(uuid, name, templateGroupName, type);
        this.ruleTemplateName = ruleTemplateName;
        this.properties = properties;
    }

    public String getRuleTemplateName() {
        return ruleTemplateName;
    }

    public void setRuleTemplateName(String ruleTemplateName) {
        this.ruleTemplateName = ruleTemplateName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "BusinessRuleFromTemplate{" +
                "uuid='" + super.getUuid() + '\'' +
                ", name='" + super.getName() + '\'' +
                ", templateGroupName='" + super.getTemplateGroupName() + '\'' +
                ", type='" + super.getType() + '\'' +
                "ruleTemplateName='" + ruleTemplateName + '\'' +
                ", properties=" + properties +
                '}';
    }
}
