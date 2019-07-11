/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.streaming.integrator.common.utils.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Map;

/**
 * A second level configuration bean class for siddhi extension config.
 */
@Configuration(description = "Extensions configuration")
public class ExtensionChildConfiguration {

    @Element(description = "Extension name")
    private String name = "";

    @Element(description = "Extension namespace")
    private String namespace = "";

    @Element(description = "Extension properties")
    private Map<String, String> properties;

    public String getName() {
        return name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public String toString() {
        return "name : " + name + ", namespace : " + namespace + ", properties : " + properties.toString();
    }
}

