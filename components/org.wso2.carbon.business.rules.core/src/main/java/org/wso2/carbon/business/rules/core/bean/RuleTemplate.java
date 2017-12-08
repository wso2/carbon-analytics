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

package org.wso2.carbon.business.rules.core.bean;

import java.util.List;
import java.util.Map;

/**
 * Represents a Rule Template, which consists of one or more Templates
 */
public class RuleTemplate {
    private String uuid;
    private String name;
    private String type;
    private String instanceCount;
    private String script;
    private String description;
    private List<Template> templates;
    private Map<String, RuleTemplateProperty> properties; // Name, RuleTemplateProperty object

    /**
     *
     * @param uuid uuid of the rule template
     * @param name name of the rule template
     * @param type type of the rule template
     * @param instanceCount how many times can this rule template be instantiated
     * @param script java script provided in the rule template
     * @param description description of the rule template
     * @param templates list of templated artifacts in rule template
     * @param properties map of possible options for the templated fields.
     */
    public RuleTemplate(String uuid, String name, String type, String instanceCount, String script, String description,
                        List<Template> templates, Map<String, RuleTemplateProperty> properties) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
        this.instanceCount = instanceCount;
        this.script = script;
        this.description = description;
        this.templates = templates;
        this.properties = properties;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getInstanceCount() {
        return instanceCount;
    }

    public String getScript() {
        return script;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public Map<String, RuleTemplateProperty> getProperties() {
        return properties;
    }

    public String getDescription() {
        return description;
    }
}
