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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements;

import java.util.List;
import java.util.Map;

/**
 * Represents configuration of a Siddhi Stream
 */
public class StreamConfig extends SiddhiElementConfig {
    private String name;
    private List<AttributeConfig> attributeList;
    private List<String> annotationList;
    private String partitionId;
    private Map<String, String> connectorsAndStreams;

    public StreamConfig(String id,
                        String name,
                        List<AttributeConfig> attributeList,
                        List<String> annotationList) {
        super(id);
        this.name = name;
        this.attributeList = attributeList;
        this.annotationList = annotationList;
    }

    public String getName() {
        return name;
    }

    public List<AttributeConfig> getAttributeList() {
        return attributeList;
    }

    public List<String> getAnnotationList() {
        return annotationList;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public Map<String, String> getConnectorsAndStreams() {
        return connectorsAndStreams;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttributeList(List<AttributeConfig> attributeList) {
        this.attributeList = attributeList;
    }

    public void setAnnotationList(List<String> annotationList) {
        this.annotationList = annotationList;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public void setConnectorsAndStreams(Map<String, String> connectorsAndStreams) {
        this.connectorsAndStreams = connectorsAndStreams;
    }
}
