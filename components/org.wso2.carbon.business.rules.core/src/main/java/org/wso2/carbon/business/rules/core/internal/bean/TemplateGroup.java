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

package org.wso2.carbon.business.rules.core.internal.bean;

import java.util.Collection;

/**
 * Represents a Template Group, which consists of one or more RuleTemplates
 */
public class TemplateGroup {
    private String uuid;
    private String name;
    private String description;
    private Collection<RuleTemplate> ruleTemplates;

    public TemplateGroup(String uuid, String name, String description, Collection<RuleTemplate> ruleTemplates) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.ruleTemplates = ruleTemplates;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<RuleTemplate> getRuleTemplates() {
        return ruleTemplates;
    }

    public void setRuleTemplates(Collection<RuleTemplate> ruleTemplates) {
        this.ruleTemplates = ruleTemplates;
    }

    @Override
    public String toString() {
        return "TemplateGroup{" +
                "\nuuid='" + uuid + '\'' +
                ", \nname='" + name + '\'' +
                ", \ndescription='" + description + '\'' +
                ", \nruleTemplates=" + ruleTemplates +
                "\n}";
    }
}
