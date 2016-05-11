/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.execution.manager.core.structure.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB Class of Template element
 */
@XmlRootElement
public class Template {
    private String name;
    private String description;
    private String templateType;
    private String script;
    private String[] executionPlans;
    private String executionType;
    private Parameter[] parameters;
    private String cronExpression;
    private String sparkScript;

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @XmlAttribute
    public void setTemplateType(String type) {
        this.templateType = type;
    }

    public String getTemplateType() {
        return this.templateType;
    }

    @XmlElement
    public void setDescription(String description) {
        this.description = description;
    }

    public String getScript() {
        return script;
    }

    @XmlElement
    public void setScript(String script) {
        this.script = script;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    public String[] getExecutionPlans() {
        return executionPlans;
    }

    @XmlElementWrapper(name = "executionPlans")
    @XmlElement(name = "executionPlan")
    public void setExecutionPlans(String[] executionPlans) {
        this.executionPlans = executionPlans;
    }

    public String getSparkScript() {
        return sparkScript;
    }

    @XmlElement(name = "sparkScript")
    public void setSparkScript(String sparkScript) {
        this.sparkScript = sparkScript;
    }

    @XmlAttribute
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return this.cronExpression;
    }

    public String getExecutionType() {
        return executionType;
    }

    @XmlElement
    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }
}
