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
/**
 * Temporary class for enable mapping
 */
package org.wso2.carbon.status.dashboard.core.eventFlow;

import org.wso2.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.List;

public class ComponentInfoDataHolder {
    private ComponentHolderType componentHolderTypeType;
    private List<Element> elements = new ArrayList();
private String streamId;
    public ComponentInfoDataHolder() {
    }

    public ComponentInfoDataHolder(String streamId, ComponentHolderType componentHolderType) {
        this.streamId = streamId;
        this.componentHolderTypeType = componentHolderType;
    }

    public String getElementId() {
        String id= "";
        if(!elements.isEmpty()) {
            for (Element element : elements) {
                id = id.concat(element.getKey() + ":" + element.getValue() + "\\n");
            }
            id = id.substring(0,id.length()-"\\n".length());
        } else {
            id=streamId;
        }
        return id;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public ComponentHolderType getComponentHolderType() {
        return componentHolderTypeType;
    }

    public void setComponentHolderTypeType(ComponentHolderType componentHolderTypeType) {
        this.componentHolderTypeType = componentHolderTypeType;
    }
}
