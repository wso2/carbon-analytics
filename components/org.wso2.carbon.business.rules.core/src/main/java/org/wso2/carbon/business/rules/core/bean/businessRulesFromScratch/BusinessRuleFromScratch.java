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
package org.wso2.carbon.business.rules.core.bean.businessRulesFromScratch;

import org.wso2.carbon.business.rules.core.bean.BusinessRule;

import java.util.Collection;

/**
 * Represents a Business Rule, created from scratch
 */
public class BusinessRuleFromScratch extends BusinessRule {
    private Collection<BusinessRulePropertyFromScratch> ruleTemplateProperties;

    public BusinessRuleFromScratch(String uuid, String name, String templateGroupName, String type,
                                   Collection<BusinessRulePropertyFromScratch> ruleTemplateProperties) {
        super(uuid, name, templateGroupName, type);
        this.ruleTemplateProperties = ruleTemplateProperties;
    }

    public Collection<BusinessRulePropertyFromScratch> getRuleTemplateProperties() {
        return ruleTemplateProperties;
    }

    public void setRuleTemplateProperties(Collection<BusinessRulePropertyFromScratch> ruleTemplateProperties) {
        this.ruleTemplateProperties = ruleTemplateProperties;
    }

    @Override
    public String toString() {
        return "BusinessRuleFromScratch{" +
                "uuid='" + super.getUuid() + '\'' +
                ", name='" + super.getName() + '\'' +
                ", templateGroupName='" + super.getTemplateGroupName() + '\'' +
                ", type='" + super.getType() + '\'' +
                ", ruleTemplateProperties=" + ruleTemplateProperties +
                '}';
    }
}
