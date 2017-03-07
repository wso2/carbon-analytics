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
package org.wso2.carbon.event.template.manager.core.structure.configuration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * JAXB Class of ScenarioConfiguration element
 */
@XmlRootElement
public class ScenarioConfiguration {

    private String name;
    private String scenario;
    private String domain;
    private String description;
    private Map<String,String> parameterMap = new HashMap<>();
    private StreamMappings streamMappings;

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public String getScenario() {
        return scenario;
    }

    @XmlAttribute
    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getDomain() {
        return domain;
    }

    @XmlAttribute
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description;
    }

    @XmlElement
    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, String> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public StreamMappings getStreamMappings() {
        return streamMappings;
    }

    public void setStreamMappings(StreamMappings streamMappings) {
        this.streamMappings = streamMappings;
    }
}
