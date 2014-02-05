/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.formatter.admin.internal;

/**
 * Event Builder Configuration Details are stored in this class
 */

public class EventFormatterConfigurationInfoDto {

    private String eventFormatterName;
    private String mappingType;
    private String outEventAdaptorName;
    private String inputStreamId;
    private boolean enableTracing;
    private boolean enableStats;

    public String getEventFormatterName() {
        return eventFormatterName;
    }

    public void setEventFormatterName(String eventFormatterName) {
        this.eventFormatterName = eventFormatterName;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public String getOutEventAdaptorName() {
        return outEventAdaptorName;
    }

    public void setOutEventAdaptorName(String outEventAdaptorName) {
        this.outEventAdaptorName = outEventAdaptorName;
    }

    public String getInputStreamId() {
        return inputStreamId;
    }

    public void setInputStreamId(String inputStreamId) {
        this.inputStreamId = inputStreamId;
    }

    public boolean isEnableTracing() {
        return enableTracing;
    }

    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }
}
