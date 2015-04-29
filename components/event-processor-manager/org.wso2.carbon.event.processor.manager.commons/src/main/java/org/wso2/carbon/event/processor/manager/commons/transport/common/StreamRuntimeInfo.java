/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.processor.manager.commons.transport.common;

import org.wso2.siddhi.query.api.definition.Attribute;

public class StreamRuntimeInfo {

    private String streamId;
    private byte streamIdSize;
    private int fixedMessageSize;
    private int noOfStringAttributes;
    private int noOfAttributes;
    private Attribute.Type[] attributes;

    public StreamRuntimeInfo(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
    }

    public int getFixedMessageSize() {
        return fixedMessageSize;
    }

    public void setFixedMessageSize(int fixedMessageSize) {
        this.fixedMessageSize = fixedMessageSize;
    }

    public int getNoOfStringAttributes() {
        return noOfStringAttributes;
    }

    public void setNoOfStringAttributes(int noOfStringAttributes) {
        this.noOfStringAttributes = noOfStringAttributes;
    }

    public int getNoOfAttributes() {
        return noOfAttributes;
    }

    public void setNoOfAttributes(int noOfAttributes) {
        this.noOfAttributes = noOfAttributes;
    }

    public Attribute.Type[] getAttributeTypes() {
        return attributes;
    }

    public void setAttributeTypes(Attribute.Type[] attributes) {
        this.attributes = attributes;
    }

    public byte getStreamIdSize() {
        return streamIdSize;
    }

    public void setStreamIdSize(byte streamIdSize) {
        this.streamIdSize = streamIdSize;
    }
}
