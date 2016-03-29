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
package org.wso2.carbon.event.execution.manager.core.structure.configuration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB Class of TemplateConfiguration element
 */
@XmlRootElement
public class TemplateConfiguration {

    private String name;
    private String type;
    private String from;
    private String description;
    private Parameter[] parameters;
    private String executionParameters;

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    @XmlAttribute
    public void setFrom(String from) {
        this.from = from;
    }

    public String getDescription() {
        return description;
    }

    @XmlElement
    public void setDescription(String description) {
        this.description = description;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }


    public String getExecutionParameters() {
        return executionParameters;
    }

    @XmlElement
    public void setExecutionParameters(String executionParameters) {
        this.executionParameters = executionParameters;
    }


}
