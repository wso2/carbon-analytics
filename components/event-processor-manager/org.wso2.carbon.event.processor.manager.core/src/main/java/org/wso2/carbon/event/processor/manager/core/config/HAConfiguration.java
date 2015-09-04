/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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


public class HAConfiguration implements Serializable {

    private boolean workerNode = false;
    private boolean presenterNode = false;
    private HostAndPort localPresenterConfig = new HostAndPort("localhost", 11000);

    private HostAndPort managementConfig;
    private HostAndPort eventSyncConfig;
    private int eventSyncReconnectionInterval;
    private int eventSyncServerThreads;
    private int presenterServerThreads;
    private String memberUuid;
    private int checkMemberUpdateInterval;
    private int syncStateRetryInterval = 10000;
    private long tryStateChangeInterval = 15000;
    private int presenterPublisherBufferSize;
    private long presenterPublisherConnectionStatusCheckInterval;
    private String presenterPublisherCharSet;
    private int presenterPublisherTcpSendBufferSize;
    private int eventSyncPublisherBufferSize;
    private long eventSyncPublisherConnectionStatusCheckInterval;
    private String eventSyncPublisherCharSet;
    private int eventSyncPublisherTcpSendBufferSize;

    public boolean isWorkerNode() {
        return workerNode;
    }

    public void setWorkerNode(boolean workerNode) {
        this.workerNode = workerNode;
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

    public int getEventSyncReconnectionInterval() {
        return eventSyncReconnectionInterval;
    }

    public int getEventSyncServerThreads() {
        return eventSyncServerThreads;
    }

    public void setEventSyncServerThreads(int eventSyncServerThreads) {
        this.eventSyncServerThreads = eventSyncServerThreads;
    }

    public HostAndPort getManagementConfig() {
        return managementConfig;
    }

    public HostAndPort getEventSyncConfig() {
        return eventSyncConfig;
    }

    public void setManagement(String host, int port) {
        this.managementConfig = new HostAndPort(host, port);
    }

    public void setTransport(String host, int port, int reconnectionInterval) {
        this.eventSyncConfig = new HostAndPort(host, port);
        this.eventSyncReconnectionInterval = reconnectionInterval;
    }

    public String getMemberUuid() {
        return memberUuid;
    }

    public void setMemberUuid(String memberUuid) {
        this.memberUuid = memberUuid;
    }

    public void setPresenterServerThreads(int presenterServerThreads) {
        this.presenterServerThreads = presenterServerThreads;
    }

    public int getPresenterServerThreads() {
        return presenterServerThreads;
    }

    public int getCheckMemberUpdateInterval() {
        return checkMemberUpdateInterval;
    }

    public void setCheckMemberUpdateInterval(int checkMemberUpdateInterval) {
        this.checkMemberUpdateInterval = checkMemberUpdateInterval;
    }

    public int getSyncStateRetryInterval() {
        return syncStateRetryInterval;
    }

    public void setSyncStateRetryInterval(int syncStateRetryInterval) {
        this.syncStateRetryInterval = syncStateRetryInterval;
    }

    public long getTryStateChangeInterval() {
        return tryStateChangeInterval;
    }

    public void setTryStateChangeInterval(long tryStateChangeInterval) {
        this.tryStateChangeInterval = tryStateChangeInterval;
    }

    public int getPresenterPublisherBufferSize() {
        return presenterPublisherBufferSize;
    }

    public void setPresenterPublisherBufferSize(int presenterPublisherBufferSize) {
        this.presenterPublisherBufferSize = presenterPublisherBufferSize;
    }

    public long getPresenterPublisherConnectionStatusCheckInterval() {

        return presenterPublisherConnectionStatusCheckInterval;
    }

    public void setPresenterPublisherConnectionStatusCheckInterval(long presenterPublisherConnectionStatusCheckInterval) {
        this.presenterPublisherConnectionStatusCheckInterval = presenterPublisherConnectionStatusCheckInterval;
    }

    public String getPresenterPublisherCharSet() {

        return presenterPublisherCharSet;
    }

    public void setPresenterPublisherCharSet(String presenterPublisherCharSet) {
        this.presenterPublisherCharSet = presenterPublisherCharSet;
    }

    public int getPresenterPublisherTcpSendBufferSize() {
        return presenterPublisherTcpSendBufferSize;
    }

    public void setPresenterPublisherTcpSendBufferSize(int presenterPublisherTcpSendBufferSize) {
        this.presenterPublisherTcpSendBufferSize = presenterPublisherTcpSendBufferSize;
    }

    public int getEventSyncPublisherBufferSize() {
        return eventSyncPublisherBufferSize;
    }

    public void setEventSyncPublisherBufferSize(int eventSyncPublisherBufferSize) {
        this.eventSyncPublisherBufferSize = eventSyncPublisherBufferSize;
    }

    public long getEventSyncPublisherConnectionStatusCheckInterval() {
        return eventSyncPublisherConnectionStatusCheckInterval;
    }

    public void setEventSyncPublisherConnectionStatusCheckInterval(long eventSyncPublisherConnectionStatusCheckInterval) {
        this.eventSyncPublisherConnectionStatusCheckInterval = eventSyncPublisherConnectionStatusCheckInterval;
    }

    public String getEventSyncPublisherCharSet() {
        return eventSyncPublisherCharSet;
    }

    public void setEventSyncPublisherCharSet(String eventSyncPublisherCharSet) {
        this.eventSyncPublisherCharSet = eventSyncPublisherCharSet;
    }

    public int getEventSyncPublisherTcpSendBufferSize() {
        return eventSyncPublisherTcpSendBufferSize;
    }


    public void setEventSyncPublisherTcpSendBufferSize(int eventSyncPublisherTcpSendBufferSize) {
        this.eventSyncPublisherTcpSendBufferSize = eventSyncPublisherTcpSendBufferSize;
    }

    public TCPEventPublisherConfig constructEventSyncPublisherConfig() {

        TCPEventPublisherConfig tcpEventPublisherConfig = new TCPEventPublisherConfig();
        tcpEventPublisherConfig.setBufferSize(getEventSyncPublisherBufferSize());
        tcpEventPublisherConfig.setConnectionStatusCheckInterval(getEventSyncPublisherConnectionStatusCheckInterval());
        tcpEventPublisherConfig.setCharset(getEventSyncPublisherCharSet());
        tcpEventPublisherConfig.setTcpSendBufferSize(getEventSyncPublisherTcpSendBufferSize());
        return tcpEventPublisherConfig;
    }

    public TCPEventPublisherConfig constructPresenterPublisherConfig() {

        TCPEventPublisherConfig tcpEventPublisherConfig = new TCPEventPublisherConfig();
        tcpEventPublisherConfig.setBufferSize(getPresenterPublisherBufferSize());
        tcpEventPublisherConfig.setConnectionStatusCheckInterval(getPresenterPublisherConnectionStatusCheckInterval());
        tcpEventPublisherConfig.setCharset(getPresenterPublisherCharSet());
        tcpEventPublisherConfig.setTcpSendBufferSize(getPresenterPublisherTcpSendBufferSize());
        return tcpEventPublisherConfig;
    }

}
