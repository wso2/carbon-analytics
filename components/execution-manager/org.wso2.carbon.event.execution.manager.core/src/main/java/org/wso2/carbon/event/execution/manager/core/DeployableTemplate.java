/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.execution.manager.core;

import org.wso2.carbon.event.execution.manager.core.structure.configuration.TemplateConfiguration;

public class DeployableTemplate {

    private TemplateConfiguration configuration;
    private String script;
    private String[] streams;
    private String[] executionPlans;
    private String sparkScript;
    private String cronExpression;  //cron expression for the Spark script.


    public TemplateConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TemplateConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String[] getStreams() {
        return streams;
    }

    public void setStreams(String[] streams) {
        this.streams = streams;
    }

    public String[] getExecutionPlans() {
        return executionPlans;
    }

    public void setExecutionPlans(String[] executionPlans) {
        this.executionPlans = executionPlans;
    }

    public String getSparkScript() {
        return sparkScript;
    }

    public void setSparkScript(String sparkScript) {
        this.sparkScript = sparkScript;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
