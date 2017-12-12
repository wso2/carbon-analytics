package org.wso2.carbon.status.dashboard.core.bean;

import org.wso2.carbon.config.annotation.Element;

/**
 * Bean class contains the configuration details of the fign client.
 */
public class WorkerConnectionConfigurations {
    @Element(description = "Connection timeout of a feign client")
    private Integer clientConnectionTimeOut;

    @Element(description = "Connection timeout of a feign client")
    private Integer clientReadTimeOut;

    public WorkerConnectionConfigurations() {
    }

    public WorkerConnectionConfigurations(Integer clientConnectionTimeOut, Integer clientReadTimeOut) {
        this.clientConnectionTimeOut = clientConnectionTimeOut;
        this.clientReadTimeOut = clientReadTimeOut;
    }

    public Integer getConnectionTimeOut() {
        return clientConnectionTimeOut;
    }

    public void setConnectionTimeOut(Integer connectionTimeOut) {
        this.clientConnectionTimeOut = connectionTimeOut;
    }

    public Integer getReadTimeOut() {
        return clientReadTimeOut;
    }

    public void setReadTimeOut(Integer readTimeOut) {
        this.clientReadTimeOut = readTimeOut;
    }
}
