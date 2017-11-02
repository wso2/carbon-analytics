/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.status.dashboard.core.bean.table;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Siddhi app Component Bean Class.
 */
public class MetricElement {
    private String type;
    private List<Attribute> attributes = new ArrayList<>();

    public MetricElement() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = WordUtils.capitalize(type);
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void addAttributes(Attribute attribute) {
        attributes.add(attribute);
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
}
