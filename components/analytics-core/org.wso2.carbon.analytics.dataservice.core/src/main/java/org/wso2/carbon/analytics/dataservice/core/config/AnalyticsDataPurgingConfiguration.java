/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.dataservice.core.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the configuration for analytics data purging.
 */
@XmlRootElement(name = "analytics-data-purging")
public class AnalyticsDataPurgingConfiguration {
    private boolean enable;
    private String cronExpression;
    private int retentionDays;
    private AnalyticsDataPurgingIncludeTable[] purgingIncludeTables;

    @XmlElement(name = "purging-enable", required=false)
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @XmlElement(name = "cron-expression", nillable = false)
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @XmlElement(name = "data-retention-days", nillable = false)
    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    @XmlElementWrapper(name = "purge-include-tables")
    @XmlElement(name = "table")
    public AnalyticsDataPurgingIncludeTable[] getPurgingIncludeTables() {
        return purgingIncludeTables;
    }

    public void setPurgingIncludeTables(AnalyticsDataPurgingIncludeTable[] excludeTables) {
        this.purgingIncludeTables = excludeTables;
    }
    
}
