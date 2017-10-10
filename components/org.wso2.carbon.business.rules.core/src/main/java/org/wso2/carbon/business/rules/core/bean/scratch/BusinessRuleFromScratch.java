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
package org.wso2.carbon.business.rules.core.bean.scratch;

import org.wso2.carbon.business.rules.core.bean.BusinessRule;

/**
 * Represents a Business Rule, created from scratch
 */
public class BusinessRuleFromScratch extends BusinessRule {
    private String inputRuleTemplateUUID;
    private String outputRuleTemplateUUID;
    private BusinessRuleFromScratchProperty properties;

    public BusinessRuleFromScratch(String uuid, String name, String templateGroupUUID, String type,
                                   String inputRuleTemplateUUID, String outputRuleTemplateUUID,
                                   BusinessRuleFromScratchProperty properties) {
        super(uuid, name, templateGroupUUID, type);
        this.inputRuleTemplateUUID = inputRuleTemplateUUID;
        this.outputRuleTemplateUUID = outputRuleTemplateUUID;
        this.properties = properties;
    }

    public String getInputRuleTemplateUUID() {
        return inputRuleTemplateUUID;
    }

    public void setInputRuleTemplateUUID(String inputRuleTemplateUUID) {
        this.inputRuleTemplateUUID = inputRuleTemplateUUID;
    }

    public String getOutputRuleTemplateUUID() {
        return outputRuleTemplateUUID;
    }

    public void setOutputRuleTemplateUUID(String outputRuleTemplateUUID) {
        this.outputRuleTemplateUUID = outputRuleTemplateUUID;
    }

    public BusinessRuleFromScratchProperty getProperties() {
        return properties;
    }

    public void setProperties(BusinessRuleFromScratchProperty properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "BusinessRuleFromScratch{" +
                "\n" + super.toString() +
                "\ninputRuleTemplateUUID='" + inputRuleTemplateUUID + '\'' +
                ", \noutputRuleTemplateUUID='" + outputRuleTemplateUUID + '\'' +
                ", \nproperties=" + properties +
                "\n} ";
    }
}
