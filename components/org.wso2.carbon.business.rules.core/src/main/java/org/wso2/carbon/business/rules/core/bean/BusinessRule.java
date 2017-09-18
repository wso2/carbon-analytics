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
 * Represents the parent of Business Rules
 * created from Template
 * and created from scratch
 */
public abstract class BusinessRule {
    private String uuid;
    private String name;
    private String templateGroupName;
    private String type; // "template" or "scratch"

    public BusinessRule(String uuid, String name, String templateGroupName, String type) {
        this.uuid = uuid;
        this.name = name;
        this.templateGroupName = templateGroupName;
        this.type = type;
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

    public String getTemplateGroupName() {
        return templateGroupName;
    }

    public void setTemplateGroupName(String templateGroupName) {
        this.templateGroupName = templateGroupName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BusinessRule{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", templateGroupName='" + templateGroupName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
