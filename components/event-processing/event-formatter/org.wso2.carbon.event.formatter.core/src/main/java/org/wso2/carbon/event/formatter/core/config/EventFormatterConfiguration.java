/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.event.formatter.core.config;

import org.wso2.carbon.event.formatter.core.internal.config.ToPropertyConfiguration;

public class EventFormatterConfiguration {

    private String eventFormatterName;

    private String fromStreamName;

    private String fromStreamVersion;

    private ToPropertyConfiguration toPropertyConfiguration;

    private OutputMapping outputMapping;

    private boolean enableTracing;

    private boolean enableStatistics;

    public String getEventFormatterName() {
        return eventFormatterName;
    }

    public void setEventFormatterName(String eventFormatterName) {
        this.eventFormatterName = eventFormatterName;
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

    public ToPropertyConfiguration getToPropertyConfiguration() {
        return toPropertyConfiguration;
    }

    public void setToPropertyConfiguration(ToPropertyConfiguration toPropertyConfiguration) {
        this.toPropertyConfiguration = toPropertyConfiguration;
    }

    public OutputMapping getOutputMapping() {
        return outputMapping;
    }

    public void setOutputMapping(
            OutputMapping outputMapping) {
        this.outputMapping = outputMapping;
    }

    public boolean isEnableTracing() {
        return enableTracing;
    }

    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }
}
