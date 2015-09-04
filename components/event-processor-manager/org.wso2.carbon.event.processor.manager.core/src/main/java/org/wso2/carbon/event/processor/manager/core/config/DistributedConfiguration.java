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

import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisherConfig;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DistributedConfiguration implements Serializable {

    //nodeType
    private boolean workerNode = false;

    private boolean managerNode = false;
    private HostAndPort localManagerConfig = new HostAndPort("localhost", 8904);

    private boolean presenterNode = false;
    private HostAndPort localPresenterConfig = new HostAndPort("localhost", -1);

    //management
    private List<HostAndPort> managers = new ArrayList<HostAndPort>();
    private int managementReconnectInterval = 10000;
    private int managementHeartbeatInterval = 5000;
    private int topologySubmitRetryInterval = 10000;

    //transport
    private int transportMinPort = 15000;
    private int transportMaxPort = 15100;
    private int transportReconnectInterval = 15000;
    private int cepReceiverOutputQueueSize = 8192;
    private int stormPublisherOutputQueueSize = 8192;

    private int checkMemberUpdateInterval = 10000;

    private int transportPublisherTcpSendBufferSize = 5242880;
    private String transportPublisherCharSet = "UTF-8";
    private int transportPublisherBufferSize = 1024;
    private long transportPublisherConnectionStatusCheckInterval = 30000;
    private int transportReceiverThreads = 10;

    //presentation
    private int presentationPublisherTcpSendBufferSize = 5242880;
    private String presentationPublisherCharSet = "UTF-8";
    private int presentationPublisherBufferSize = 1024;
    private long presentationPublisherConnectionStatusCheckInterval = 30000;
    private int presentationReceiverThreads = 10;

    //status
    private int statusLockTimeout = 60000;   //Lock timeout in milliseconds.
    private int statusUpdateInterval = 60000; //Rate in milliseconds at which the hazelcast map will be updated by each worker.

    private String jar;
    private String distributedUIUrl;

    public int getTransportReceiverThreads() {
        return transportReceiverThreads;
    }

    public void setTransportReceiverThreads(int transportReceiverThreads) {
        this.transportReceiverThreads = transportReceiverThreads;
    }

    public String getTransportPublisherCharSet() {
        return transportPublisherCharSet;
    }

    public void setTransportPublisherCharSet(String transportPublisherCharSet) {
        this.transportPublisherCharSet = transportPublisherCharSet;
    }

    public int getTransportPublisherTcpSendBufferSize() {
        return transportPublisherTcpSendBufferSize;
    }

    public void setTransportPublisherTcpSendBufferSize(int transportPublisherTcpSendBufferSize) {
        this.transportPublisherTcpSendBufferSize = transportPublisherTcpSendBufferSize;
    }

    public int getCepReceiverOutputQueueSize() {
        return cepReceiverOutputQueueSize;
    }

    public void setCepReceiverOutputQueueSize(int cepReceiverOutputQueueSize) {

        this.cepReceiverOutputQueueSize = cepReceiverOutputQueueSize;
    }

    public int getStormPublisherOutputQueueSize() {
        return stormPublisherOutputQueueSize;
    }

    public void setStormPublisherOutputQueueSize(int stormPublisherOutputQueueSize) {
        this.stormPublisherOutputQueueSize = stormPublisherOutputQueueSize;
    }

    public int getManagementHeartbeatInterval() {
        return managementHeartbeatInterval;
    }

    public void setManagementHeartbeatInterval(int managementHeartbeatInterval) {
        this.managementHeartbeatInterval = managementHeartbeatInterval;
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

    public int getStatusLockTimeout() {
        return statusLockTimeout;
    }

    public void setStatusLockTimeout(int statusLockTimeout) {
        this.statusLockTimeout = statusLockTimeout;
    }

    public int getStatusUpdateInterval() {
        return statusUpdateInterval;
    }

    public void setStatusUpdateInterval(int statusUpdateInterval) {
        this.statusUpdateInterval = statusUpdateInterval;
    }

    public boolean isPresenterNode() {
        return presenterNode;
    }

    public void setPresenterNode(boolean presenterNode) {
        this.presenterNode = presenterNode;
    }

    public HostAndPort getLocalPresenterConfig() {
        return localPresenterConfig;
    }

    public void setLocalPresenterConfig(String host, int port) {
        this.localPresenterConfig = new HostAndPort(host, port);
    }

    public int getCheckMemberUpdateInterval() {
        return checkMemberUpdateInterval;
    }

    public void setCheckMemberUpdateInterval(int checkMemberUpdateInterval) {
        this.checkMemberUpdateInterval = checkMemberUpdateInterval;
    }

    public int getTransportPublisherBufferSize() {
        return transportPublisherBufferSize;
    }

    public long getTransportPublisherConnectionStatusCheckInterval() {
        return transportPublisherConnectionStatusCheckInterval;
    }

    public int getPresentationPublisherTcpSendBufferSize() {
        return presentationPublisherTcpSendBufferSize;
    }

    public String getPresentationPublisherCharSet() {
        return presentationPublisherCharSet;
    }

    public int getPresentationPublisherBufferSize() {
        return presentationPublisherBufferSize;
    }

    public long getPresentationPublisherConnectionStatusCheckInterval() {
        return presentationPublisherConnectionStatusCheckInterval;
    }

    public int getPresentationReceiverThreads() {
        return presentationReceiverThreads;
    }

    public void setLocalManagerConfig(HostAndPort localManagerConfig) {
        this.localManagerConfig = localManagerConfig;
    }

    public void setLocalPresenterConfig(HostAndPort localPresenterConfig) {
        this.localPresenterConfig = localPresenterConfig;
    }

    public void setManagers(List<HostAndPort> managers) {
        this.managers = managers;
    }

    public void setTransportPublisherBufferSize(int transportPublisherBufferSize) {
        this.transportPublisherBufferSize = transportPublisherBufferSize;
    }

    public void setTransportPublisherConnectionStatusCheckInterval(long transportPublisherConnectionStatusCheckInterval) {
        this.transportPublisherConnectionStatusCheckInterval = transportPublisherConnectionStatusCheckInterval;
    }

    public void setPresentationPublisherTcpSendBufferSize(int presentationPublisherTcpSendBufferSize) {
        this.presentationPublisherTcpSendBufferSize = presentationPublisherTcpSendBufferSize;
    }

    public void setPresentationPublisherCharSet(String presentationPublisherCharSet) {
        this.presentationPublisherCharSet = presentationPublisherCharSet;
    }

    public void setPresentationPublisherBufferSize(int presentationPublisherBufferSize) {
        this.presentationPublisherBufferSize = presentationPublisherBufferSize;
    }

    public void setPresentationPublisherConnectionStatusCheckInterval(long presentationPublisherConnectionStatusCheckInterval) {
        this.presentationPublisherConnectionStatusCheckInterval = presentationPublisherConnectionStatusCheckInterval;
    }

    public void setPresentationReceiverThreads(int presentationReceiverThreads) {
        this.presentationReceiverThreads = presentationReceiverThreads;
    }

    public TCPEventPublisherConfig constructTransportPublisherConfig() {

        TCPEventPublisherConfig tcpEventPublisherConfig = new TCPEventPublisherConfig();
        tcpEventPublisherConfig.setBufferSize(getTransportPublisherBufferSize());
        tcpEventPublisherConfig.setConnectionStatusCheckInterval(getTransportPublisherConnectionStatusCheckInterval());
        tcpEventPublisherConfig.setCharset(getTransportPublisherCharSet());
        tcpEventPublisherConfig.setTcpSendBufferSize(getTransportPublisherTcpSendBufferSize());
        return tcpEventPublisherConfig;
    }

    public TCPEventPublisherConfig constructPresenterPublisherConfig() {

        TCPEventPublisherConfig tcpEventPublisherConfig = new TCPEventPublisherConfig();
        tcpEventPublisherConfig.setBufferSize(getPresentationPublisherBufferSize());
        tcpEventPublisherConfig.setConnectionStatusCheckInterval(getPresentationPublisherConnectionStatusCheckInterval());
        tcpEventPublisherConfig.setCharset(getPresentationPublisherCharSet());
        tcpEventPublisherConfig.setTcpSendBufferSize(getPresentationPublisherTcpSendBufferSize());
        return tcpEventPublisherConfig;
    }
}
