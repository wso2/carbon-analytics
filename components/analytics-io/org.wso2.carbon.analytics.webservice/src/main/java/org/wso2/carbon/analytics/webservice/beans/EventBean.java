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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * This class represents the bean class for Event.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EventBean implements Serializable {

    private static final long serialVersionUID = -8366231301934561565L;
    private String streamName;
    private String streamVersion;
    private long timeStamp;
    private RecordValueEntryBean[] metaData;
    private RecordValueEntryBean[] correlationData;
    private RecordValueEntryBean[] payloadData;
    private RecordValueEntryBean[] arbitraryData = null;

    public EventBean() {

    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getStreamVersion() {
        return streamVersion;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public RecordValueEntryBean[] getMetaData() {
        return metaData;
    }

    public void setMetaData(RecordValueEntryBean[] metaData) {
        this.metaData = metaData;
    }

    public RecordValueEntryBean[] getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(RecordValueEntryBean[] correlationData) {
        this.correlationData = correlationData;
    }

    public RecordValueEntryBean[] getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(RecordValueEntryBean[] payloadData) {
        this.payloadData = payloadData;
    }

    public RecordValueEntryBean[] getArbitraryData() {
        return arbitraryData;
    }

    public void setArbitraryData(RecordValueEntryBean[] arbitraryData) {
        this.arbitraryData = arbitraryData;
    }
}
