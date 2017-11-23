/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.stream.processor.common.utils.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * A second level configuration bean class for siddhi reference config.
 */
@Configuration(description = "Reference configuration")
public class ReferenceChildConfiguration {

    @Element(description = "Reference name")
    private String name = "";

    @Element(description = "Reference type")
    private String type = "";

    @Element(description = "Reference properties")
    private Map<String, String> properties = new HashMap<>();

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "name : " + name + ", properties : " + properties.toString();
    }
}
