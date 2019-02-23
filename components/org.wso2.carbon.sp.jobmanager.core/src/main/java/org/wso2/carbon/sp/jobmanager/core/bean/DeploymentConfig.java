/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sp.jobmanager.core.bean;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.io.Serializable;

/**
 * This class represents the deployment configuration for distributed deployment.
 */
@Configuration(namespace = "deployment.config", description = "Distributed deployment configuration")
public class DeploymentConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    @Element(description = "deployment type (distributed/ha)", required = true)
    private String type;
    @Element(description = "Allocation algorithm", required = true)
    private String allocationAlgorithm = "org.wso2.carbon.sp.jobmanager.core.allocation.RoundRobinAllocationAlgorithm";
    @Element(description = "HTTPS host:port configurations", required = true)
    private InterfaceConfig httpsInterface;
    private int heartbeatInterval = 10000;
    private int heartbeatMaxRetry = 2;
    private int minResourceCount = 1;
    @Element(description = "datasource to persist resource mappings", required = true)
    private String datasource;
    @Element(description = "bootstrap urls for Kafka", required = true)
    private String bootstrapURLs;
    private String dbUsername;
    private String dbPassword;
    @Element(description = "Enable Metricscheduling or not", required = true)
    private boolean isMetricScheduling = true;
    private int waitingTime = 15;

    /**
     * @deprecated zooKeeperURLs is moved to {@link ZooKeeperConfig} bean
     */
    @Deprecated
    @Element(description = "ZooKeeper urls of Kafka cluster", required = true)
    private String zooKeeperURLs;
    @Element(description = "ZooKeeper configurations", required = true)
    private ZooKeeperConfig zooKeeperConfig;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public InterfaceConfig getHttpsInterface() {
        return httpsInterface;
    }

    public void setHttpsInterface(InterfaceConfig httpsInterface) {
        this.httpsInterface = httpsInterface;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getHeartbeatMaxRetry() {
        return heartbeatMaxRetry;
    }

    public  boolean getMetricScheduling() {
        return isMetricScheduling;
    }

    public void setMetricScheduling(boolean isMetricScheduling){ this.isMetricScheduling = isMetricScheduling;}

    public void setHeartbeatMaxRetry(int heartbeatMaxRetry) {
        this.heartbeatMaxRetry = heartbeatMaxRetry;
    }

    public int getMinResourceCount() {
        return minResourceCount;
    }

    public void setMinResourceCount(int minResourceCount) {
        this.minResourceCount = minResourceCount;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getBootstrapURLs() {
        return bootstrapURLs;
    }

    public void setBootstrapURLs(String bootstrapURLs) {
        this.bootstrapURLs = bootstrapURLs;
    }

    public String getZooKeeperURLs() {
        return zooKeeperURLs;
    }

    public void setZooKeeperURLs(String zooKeeperURLs) {
        this.zooKeeperURLs = zooKeeperURLs;
    }

    public ZooKeeperConfig getZooKeeperConfig() {
        return zooKeeperConfig;
    }

    public void setZooKeeperConfig(ZooKeeperConfig zooKeeperConfig) {
        this.zooKeeperConfig = zooKeeperConfig;
    }

    public String getAllocationAlgorithm() {
        return allocationAlgorithm;
    }

    public  String getDbUsername() {
        return  dbUsername;
    }

    public  String getDbPassword() {
        return  dbPassword;
    }

    public int getWaitingTime(){ return  waitingTime;}

    public void setAllocationAlgorithm(String allocationAlgorithm) {
        this.allocationAlgorithm = allocationAlgorithm;
    }
}
