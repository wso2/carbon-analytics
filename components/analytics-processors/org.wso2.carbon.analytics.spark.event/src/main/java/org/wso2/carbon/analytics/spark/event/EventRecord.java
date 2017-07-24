/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.spark.event;

import java.util.List;

public class EventRecord {
    private int tenantId;
    private String streamId;
    private List<Object> payloadEntries;
    private List<Object> metaEntries;
    private List<Object> correlationEntries;

    public EventRecord(int tenantId, String streamId, List<Object> payloadEntries, List<Object> metaEntries, List<Object> correlationEntries) {
        this.tenantId = tenantId;
        this.streamId = streamId;
        this.payloadEntries = payloadEntries;
        this.metaEntries = metaEntries;
        this.correlationEntries = correlationEntries;
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getStreamId() {
        return streamId;
    }

    public List<Object> getPayloadEntries() {
        return payloadEntries;
    }

    public List<Object> getMetaEntries() {
        return metaEntries;
    }

    public List<Object> getCorrelationEntries() {
        return correlationEntries;
    }
}
