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
package org.wso2.carbon.event.publisher.core.config;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;

import java.util.Map;

public class EventPublisherConfiguration {

    private String eventPublisherName;
    private String fromStreamName;
    private String fromStreamVersion;
    private OutputEventAdapterConfiguration toAdapterConfiguration;
    private Map<String, String> toAdapterDynamicProperties;
    private OutputMapping outputMapping;
    private boolean enableTracing;
    private boolean enableStatistics;
    private boolean editable;
    private boolean enableProcessing;


    public String getEventPublisherName() {
        return eventPublisherName;
    }

    public void setEventPublisherName(String eventPublisherName) {
        this.eventPublisherName = eventPublisherName;
    }

    public String getFromStreamName() {
        return fromStreamName;
    }

    public void setFromStreamName(String fromStreamName) {
        this.fromStreamName = fromStreamName;
    }

    public String getFromStreamVersion() {
        return fromStreamVersion;
    }

    public void setFromStreamVersion(String fromStreamVersion) {
        this.fromStreamVersion = fromStreamVersion;
    }

    public OutputEventAdapterConfiguration getToAdapterConfiguration() {
        return toAdapterConfiguration;
    }

    public void setToAdapterConfiguration(OutputEventAdapterConfiguration toAdapterConfiguration) {
        this.toAdapterConfiguration = toAdapterConfiguration;
    }

    public OutputMapping getOutputMapping() {
        return outputMapping;
    }

    public void setOutputMapping(
            OutputMapping outputMapping) {
        this.outputMapping = outputMapping;
    }

    public boolean isTracingEnabled() {
        return enableTracing;
    }

    public void setTraceEnabled(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public boolean isStatisticsEnabled() {
        return enableStatistics;
    }

    public void setStatisticsEnabled(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }

    public boolean isProcessingEnabled() {
        return enableProcessing;
    }

    public void setProcessEnabled(boolean processing) {
        this.enableProcessing = processing;
    }

    public Map<String, String> getToAdapterDynamicProperties() {
        return toAdapterDynamicProperties;
    }

    public void setToAdapterDynamicProperties(Map<String, String> toAdapterDynamicProperties) {
        this.toAdapterDynamicProperties = toAdapterDynamicProperties;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }
}
