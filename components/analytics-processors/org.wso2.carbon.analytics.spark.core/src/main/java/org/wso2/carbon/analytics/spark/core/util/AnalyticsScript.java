/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.spark.core.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "Analytics")
public class AnalyticsScript {
    private String name;
    private String scriptContent;
    private String cronExpression;
    private boolean editable;
    private String carbonApplicationFileName;

    public AnalyticsScript(String name) {
        this.name = name;
    }

    public AnalyticsScript(){}

    @XmlElement (name = "Name")
    public void setName(String name) {
        this.name = name;
    }

    @XmlElement (name = "CronExpression")
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @XmlElement (name = "Script")
    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public String getName() {
        return name;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    @XmlElement (name = "Editable")
    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @XmlElement (name = "CarbonAppName")
    public String getCarbonApplicationFileName() {
        return carbonApplicationFileName;
    }

    public void setCarbonApplicationFileName(String carbonApplicationFileName) {
        this.carbonApplicationFileName = carbonApplicationFileName;
    }
}
