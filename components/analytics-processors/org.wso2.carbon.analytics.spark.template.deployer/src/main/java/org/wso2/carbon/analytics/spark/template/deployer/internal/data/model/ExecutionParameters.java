package org.wso2.carbon.analytics.spark.template.deployer.internal.data.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "executionParameters")
public class ExecutionParameters {

    private String cron;
    private String sparkScript;

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getSparkScript() {
        return sparkScript;
    }

    public void setSparkScript(String sparkScript) {
        this.sparkScript = sparkScript;
    }
}
