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
import org.wso2.carbon.utils.CarbonUtils;

import java.io.Serializable;


public class HAConfiguration implements Serializable {

    //nodeType
    private boolean workerNode = false;

    private boolean presenterNode = false;
    private HostAndPort localPresenterConfig = new HostAndPort("localhost", 11000);

    //management
    private HostAndPort managementConfig = new HostAndPort("localhost", 10005);
    private int managementStateSyncRetryInterval = 10000;
    private long managementTryStateChangeInterval = 15000;

    //eventSync
    private HostAndPort eventSyncConfig = new HostAndPort("localhost", 11224);
    private int eventSyncPublisherTcpSendBufferSize = 5242880;
    private String eventSyncPublisherCharSet = "UTF-8";
    private int eventSyncPublisherBufferSize = 1024;
    private long eventSyncPublisherConnectionStatusCheckInterval = 30000;
    private int eventSyncReceiverQueueSize = 1000000;
    private int eventSyncPublisherQueueSize = 1000000;
    private int eventSyncReceiverMaxQueueSizeInMb = 50;
    private int eventSyncPublisherMaxQueueSizeInMb = 50;

    //presentation
    private int presentationPublisherTcpSendBufferSize = 5242880;
    private String presentationPublisherCharSet = "UTF-8";
    private int presentationPublisherBufferSize = 1024;
    private long presentationPublisherConnectionStatusCheckInterval = 30000;

    private int checkMemberUpdateInterval = 10000;
    private String memberUuid;
    private boolean isActive = false;

    private static final int carbonXmlportOffset = Integer.parseInt(CarbonUtils.getServerConfiguration().getFirstProperty("Ports.Offset"));

    // Port offset from -DportOffset option
    private static final int commandLinePortOffset = Integer.parseInt(System.getProperty("portOffset") == null ? "0" : System.getProperty("portOffset"));

    // Priority should be given to the offset in -DportOffset
    private static int portOffset = 0;
    static {
        if (commandLinePortOffset > 0)
            portOffset = commandLinePortOffset;
        else if (carbonXmlportOffset > 0 ){
            portOffset = carbonXmlportOffset;
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

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
        this.localPresenterConfig = new HostAndPort(host, port + portOffset);
    }

    public HostAndPort getManagementConfig() {
        return managementConfig;
    }

    public HostAndPort getEventSyncConfig() {
        return eventSyncConfig;
    }

    public void setManagement(String host, int port) {
        this.managementConfig = new HostAndPort(host, port + portOffset);
    }

    public void setEventSyncConfig(String host, int port) {
        this.eventSyncConfig = new HostAndPort(host, port + portOffset);
    }

    public String getMemberUuid() {
        return memberUuid;
    }

    public void setMemberUuid(String memberUuid) {
        this.memberUuid = memberUuid;
    }

    public int getCheckMemberUpdateInterval() {
        return checkMemberUpdateInterval;
    }

    public void setCheckMemberUpdateInterval(int checkMemberUpdateInterval) {
        this.checkMemberUpdateInterval = checkMemberUpdateInterval;
    }

    public int getManagementStateSyncRetryInterval() {
        return managementStateSyncRetryInterval;
    }

    public void setManagementStateSyncRetryInterval(int managementStateSyncRetryInterval) {
        this.managementStateSyncRetryInterval = managementStateSyncRetryInterval;
    }

    public long getManagementTryStateChangeInterval() {
        return managementTryStateChangeInterval;
    }

    public void setManagementTryStateChangeInterval(long managementTryStateChangeInterval) {
        this.managementTryStateChangeInterval = managementTryStateChangeInterval;
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
        tcpEventPublisherConfig.setBufferSize(getPresentationPublisherBufferSize());
        tcpEventPublisherConfig.setConnectionStatusCheckInterval(getPresentationPublisherConnectionStatusCheckInterval());
        tcpEventPublisherConfig.setCharset(getPresentationPublisherCharSet());
        tcpEventPublisherConfig.setTcpSendBufferSize(getPresentationPublisherTcpSendBufferSize());
        return tcpEventPublisherConfig;
    }

    public int getEventSyncReceiverQueueSize() {
        return eventSyncReceiverQueueSize;
    }

    public void setEventSyncReceiverQueueSize(int eventSyncReceiverQueueSize) {
        this.eventSyncReceiverQueueSize = eventSyncReceiverQueueSize;
    }

    public int getEventSyncPublisherQueueSize() {
        return eventSyncPublisherQueueSize;
    }

    public void setEventSyncPublisherQueueSize(int eventSyncPublisherQueueSize) {
        this.eventSyncPublisherQueueSize = eventSyncPublisherQueueSize;
    }

    public int getEventSyncReceiverMaxQueueSizeInMb() {
        return eventSyncReceiverMaxQueueSizeInMb;
    }

    public void setEventSyncReceiverMaxQueueSizeInMb(int eventSyncReceiverMaxQueueSizeInMb) {
        this.eventSyncReceiverMaxQueueSizeInMb = eventSyncReceiverMaxQueueSizeInMb;
    }

    public int getEventSyncPublisherMaxQueueSizeInMb() {
        return eventSyncPublisherMaxQueueSizeInMb;
    }

    public void setEventSyncPublisherMaxQueueSizeInMb(int eventSyncPublisherMaxQueueSizeInMb) {
        this.eventSyncPublisherMaxQueueSizeInMb = eventSyncPublisherMaxQueueSizeInMb;
    }
}
