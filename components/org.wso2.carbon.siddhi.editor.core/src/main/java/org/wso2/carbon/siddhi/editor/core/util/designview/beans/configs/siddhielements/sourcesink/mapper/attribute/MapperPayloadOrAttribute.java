/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute;

/**
 * Represents @attribute or @payload of a Siddhi Source/Sink mapper
 */
public abstract class MapperPayloadOrAttribute {
    private String payloadOrAttribute;
    private String type;

    public MapperPayloadOrAttribute(String payloadOrAttribute, String type) {
        this.payloadOrAttribute = payloadOrAttribute;
        this.type = type;
    }

    public String getPayloadOrAttribute() {
        return payloadOrAttribute;
    }

    public void setPayloadOrAttribute(String payloadOrAttribute) {
        this.payloadOrAttribute = payloadOrAttribute;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
