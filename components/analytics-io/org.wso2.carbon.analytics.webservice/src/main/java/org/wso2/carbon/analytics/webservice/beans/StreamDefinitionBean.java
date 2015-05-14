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

package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class represents the bean class for the stream definitions
 */
public class StreamDefinitionBean implements Serializable{


    private static final long serialVersionUID = -1950031600313258519L;
    private String name;
    private String version;
    private String nickName;
    private String description;
    private String[] tags;
    private StreamDefAttributeBean[] metaData;
    private StreamDefAttributeBean[] correlationData;
    private StreamDefAttributeBean[] payloadData;

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

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public StreamDefAttributeBean[] getMetaData() {
        return metaData;
    }

    public void setMetaData(StreamDefAttributeBean[] metaData) {
        this.metaData = metaData;
    }

    public StreamDefAttributeBean[] getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(StreamDefAttributeBean[] correlationData) {
        this.correlationData = correlationData;
    }

    public StreamDefAttributeBean[] getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(StreamDefAttributeBean[] payloadData) {
        this.payloadData = payloadData;
    }
}
