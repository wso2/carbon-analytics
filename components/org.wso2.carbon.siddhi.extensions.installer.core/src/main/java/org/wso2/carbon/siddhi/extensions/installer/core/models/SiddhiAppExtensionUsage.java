/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.extensions.installer.core.models;

import io.siddhi.query.api.SiddhiElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Denotes the usage of an extension in a Siddhi app.
 * This carries information about a Siddhi element, which is the user of an extension,
 * along with its extracted properties that will be helpful in identifying the usage.
 */
public class SiddhiAppExtensionUsage {
    private Map<String, String> properties = new HashMap<>();
    private SiddhiElement usingSiddhiElement;

    public SiddhiAppExtensionUsage(Map<String, String> properties, SiddhiElement usingSiddhiElement) {
        this.properties = properties;
        this.usingSiddhiElement = usingSiddhiElement;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
