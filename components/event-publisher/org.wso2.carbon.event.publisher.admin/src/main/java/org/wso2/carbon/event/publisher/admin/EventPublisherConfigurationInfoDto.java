/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.publisher.admin;

/**
 * Event Builder Configuration Details are stored in this class
 */

public class EventPublisherConfigurationInfoDto {

    private String eventPublisherName;
    private String messageFormat;
    private String outputAdapterType;
    private String inputStreamId;
    private boolean enableTracing;
    private boolean enableStats;
    private boolean editable;
    private boolean enableProcessing;

    public String getEventPublisherName() {
        return eventPublisherName;
    }

    public void setEventPublisherName(String eventPublisherName) {
        this.eventPublisherName = eventPublisherName;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public String getOutputAdapterType() {
        return outputAdapterType;
    }

    public void setOutputAdapterType(String outputAdapterType) {
        this.outputAdapterType = outputAdapterType;
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

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isEnableProcessing() {
        return enableProcessing;
    }

    public void setEnableProcessing(boolean enableProcessing) {
        this.enableProcessing = enableProcessing;
    }


}
