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

/**
 * Represents a Templated item with placeholders
 * i.e : SiddhiApp with placeholders (Not considering Gadget & Dashboard for now)
 */
public class Template {
    private String type;
    private String content;
    private String exposedStreamDefinition;

    public Template(String type, String content, String exposedStreamDefinition) {
        this.type = type;
        this.content = content;
        this.exposedStreamDefinition = exposedStreamDefinition;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExposedStreamDefinition() {
        return exposedStreamDefinition;
    }

    public void setExposedStreamDefinition(String exposedStreamDefinition) {
        this.exposedStreamDefinition = exposedStreamDefinition;
    }
}
