/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.manager.core.config;

import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DistributedConfiguration implements Serializable {

    private boolean workerNode = false;
    private boolean managerNode = false;
    private HostAndPort localManagerConfig = new HostAndPort("localhost", 8904);

    private List<HostAndPort> managers = new ArrayList<HostAndPort>();


    private int transportMaxPort = 15100;
    private int transportMinPort = 15000;
    private int transportReconnectInterval = 15000;
    private String jar;

    private int topologySubmitRetryInterval = 10000;
    private int heartbeatInterval = 5000;
    private int managementReconnectInterval = 10000;

    private String distributedUIUrl;

    /**
     * Status monitor configs
     */
    private int lockTimeout = 60;   //Lock timeout in seconds.
    private int updateRate = 60000; //Rate in milliseconds at which the hazelcast map will be updated by each worker.


    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getTopologySubmitRetryInterval() {
        return topologySubmitRetryInterval;
    }

    public void setTopologySubmitRetryInterval(int topologySubmitRetryInterval) {
        this.topologySubmitRetryInterval = topologySubmitRetryInterval;
    }

    public boolean isWorkerNode() {
        return workerNode;
    }

    public void setWorkerNode(boolean workerNode) {
        this.workerNode = workerNode;
    }

    public boolean isManagerNode() {
        return managerNode;
    }

    public void setManagerNode(boolean managerNode) {
        this.managerNode = managerNode;
    }

    public HostAndPort getLocalManagerConfig() {
        return localManagerConfig;
    }

    public void setLocalManagerConfig(String hostName, int port) {
        this.localManagerConfig = new HostAndPort(hostName, port);
    }

    public List<HostAndPort> getManagers() {
        return managers;
    }

    public void addManager(String hostName, int port) {
        this.managers.add(new HostAndPort(hostName, port));
    }

    public int getManagementReconnectInterval() {
        return managementReconnectInterval;
    }

    public void setManagementReconnectInterval(int managementReconnectInterval) {
        this.managementReconnectInterval = managementReconnectInterval;
    }

    public int getTransportMaxPort() {
        return transportMaxPort;
    }

    public void setTransportMaxPort(int transportMaxPort) {
        this.transportMaxPort = transportMaxPort;
    }

    public int getTransportMinPort() {
        return transportMinPort;
    }

    public void setTransportMinPort(int transportMinPort) {
        this.transportMinPort = transportMinPort;
    }

    public int getTransportReconnectInterval() {
        return transportReconnectInterval;
    }

    public void setTransportReconnectInterval(int transportReconnectInterval) {
        this.transportReconnectInterval = transportReconnectInterval;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String getJar() {
        return jar;
    }

    public String getDistributedUIUrl() {
        return distributedUIUrl;
    }

    public void setDistributedUIUrl(String distributedUIUrl) {
        this.distributedUIUrl = distributedUIUrl;
    }

    public int getLockTimeout() {
        return lockTimeout;
    }

    public void setLockTimeout(int lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public int getUpdateRate() {
        return updateRate;
    }

    public void setUpdateRate(int updateRate) {
        this.updateRate = updateRate;
    }
}
