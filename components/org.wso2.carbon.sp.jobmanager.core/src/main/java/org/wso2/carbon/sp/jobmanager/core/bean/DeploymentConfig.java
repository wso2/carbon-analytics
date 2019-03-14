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
import org.wso2.carbon.database.query.manager.config.Queries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    /**
     * @deprecated zooKeeperURLs is moved to {@link ZooKeeperConfig} bean
     */
    @Deprecated
    @Element(description = "ZooKeeper urls of Kafka cluster")
    private String zooKeeperURLs;
    @Element(description = "ZooKeeper configurations")
    private ZooKeeperConfig zooKeeperConfig;
    @Element(description = "JMS Factory initial configuration")
    private String factoryInitial;
    @Element(description = "provider url configuration for siddhi-jms-io")
    private String providerUrl;
    @Element(description = "Database queries template array list.")
    private List<Queries> queries = new ArrayList<>();
    @Element(description = "Nats based url of nats cluster")
    private String natsServerUrl;
    @Element(description = "Id of the created nats cluster")
    private String clusterId;
    @Element(description = "Siddhi App Creator based on the distribute implementation")
    private String appCreatorClass = "org.wso2.carbon.sp.jobmanager.core.appcreator.KafkaSiddhiAppCreator";

    public String getNatsServerUrl() {
        return natsServerUrl;
    }

    public void setNatsServerUrl(String natsServerUrl) {
        this.natsServerUrl = natsServerUrl;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getProviderUrl() {

        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getFactoryInitial() {
        return factoryInitial;
    }

    public void setFactoryInitial(String factoryInitial) {
        this.factoryInitial = factoryInitial;
    }

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

    public void setAllocationAlgorithm(String allocationAlgorithm) {
        this.allocationAlgorithm = allocationAlgorithm;
    }

    public List<Queries> getQueries() {
        return queries;
    }

    public void setQueries(List<Queries> queries) {
        this.queries = queries;
    }

    public String getAppCreatorClass() {
        return appCreatorClass;
    }

    public void setAppCreatorClass(String appCreatorClass) {
        this.appCreatorClass = appCreatorClass;
    }
}
