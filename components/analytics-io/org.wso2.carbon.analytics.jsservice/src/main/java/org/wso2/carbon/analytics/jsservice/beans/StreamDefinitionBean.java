/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.analytics.jsservice.beans;

import java.util.List;
import java.util.Map;

/**
 * This class represents the stream definition bean class
 */
public class StreamDefinitionBean {
    private String name;
    private String version;
    private String nickName;
    private String description;
    private List<String> tags;
    private Map<String, String> metaData;
    private Map<String, String> correlationData;
    private Map<String, String> payloadData;

    public StreamDefinitionBean() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public Map<String, String> getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(Map<String, String> correlationData) {
        this.correlationData = correlationData;
    }

    public Map<String, String> getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(Map<String, String> payloadData) {
        this.payloadData = payloadData;
    }
}
