/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.stream.admin.internal;

import java.util.Arrays;

public class EventStreamDefinitionDto {

    private String name;
    private String version;
    private String description;
    private String nickName;
    private EventStreamAttributeDto[] metaAttributes;
    private EventStreamAttributeDto[] correlationAttributes;
    private EventStreamAttributeDto[] payloadAttributes;
    private String streamDefinitionString;
    private boolean editable;

    public EventStreamAttributeDto[] getMetaData() {
        return metaAttributes;
    }

    public void setMetaData(EventStreamAttributeDto[] metaData) {
        this.metaAttributes = metaData;
    }

    public EventStreamAttributeDto[] getCorrelationData() {
        return correlationAttributes;
    }

    public void setCorrelationData(EventStreamAttributeDto[] correlationData) {
        this.correlationAttributes = correlationData;
    }

    public EventStreamAttributeDto[] getPayloadData() {
        return payloadAttributes;
    }

    public void setPayloadData(EventStreamAttributeDto[] payloadData) {
        this.payloadAttributes = payloadData;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setStreamDefinitionString(String streamDefinitionString){
        this.streamDefinitionString = streamDefinitionString;
    }

    public String getStreamDefinitionString(){
        return streamDefinitionString;
    }


    public String convertToJsonString() {
        final StringBuffer sb = new StringBuffer('{');
        sb.append("\"name\":\"").append(name).append("\",");
        sb.append("\"version\":\"").append(version).append("\",");
        sb.append("\"description\":\"").append(description).append("\",");
        sb.append("\"nickName\":\"").append(nickName).append("\",");
        sb.append("\"metaAttributes\":[").append(convertAttributeArrayToString(metaAttributes)).append("],");
        sb.append("\"correlationAttributes\":[").append(convertAttributeArrayToString(correlationAttributes)).append("],");
        sb.append("\"payloadAttributes\":[").append(convertAttributeArrayToString(payloadAttributes)).append("]");
        sb.append('}');
        return sb.toString();
    }

    private String convertAttributeArrayToString(EventStreamAttributeDto[] attributes){
        String attributeString = "";

        if(0<attributes.length){
            final StringBuffer sb = new StringBuffer();
            for(int i=0; i<attributes.length;i++){
                if(i!=0){
                    sb.append(',');
                }
                sb.append("{\"name\":\"").append(attributes[i].getAttributeName()).append("\",");
                sb.append("\"type\":\"").append(attributes[i].getAttributeType()).append("\"}");
            }
            return sb.toString();
        }
        return attributeString;

    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }
}
