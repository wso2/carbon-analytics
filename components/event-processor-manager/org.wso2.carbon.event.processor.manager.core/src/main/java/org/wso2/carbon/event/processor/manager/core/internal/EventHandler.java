/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.manager.core.internal;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisherConfig;
import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.core.EventManagementUtil;
import org.wso2.carbon.event.processor.manager.core.EventSync;
import org.wso2.carbon.event.processor.manager.core.internal.ds.EventManagementServiceValueHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandler {
    private static Logger log = Logger.getLogger(EventHandler.class);

    private TCPEventServer tcpEventServer = null;

    private IMap<String, HostAndPort> members = null;
    private ConcurrentHashMap<HostAndPort, TCPEventPublisher> tcpEventPublisherPool = new ConcurrentHashMap<HostAndPort, TCPEventPublisher>();
    private ConcurrentHashMap<String, EventSync> eventSyncMap = new ConcurrentHashMap<String, EventSync>();
    private HostAndPort localMember;
    private TCPEventPublisherConfig localEventPublisherConfiguration;
    private boolean allowEventSync = true;
    private boolean isMemberNode;

    public void init(String memberType, HostAndPort localMember,
                     TCPEventPublisherConfig localEventPublisherConfiguration, boolean isMemberNode) {
        this.isMemberNode = isMemberNode;
        HazelcastInstance hazelcastInstance = EventManagementServiceValueHolder.getHazelcastInstance();
        this.members = hazelcastInstance.getMap(memberType);
        this.localMember = localMember;
        registerLocalMember();
        this.localEventPublisherConfiguration = localEventPublisherConfiguration;
    }

    public void registerLocalMember() {
        if (isMemberNode && members != null) {
            this.members.set(EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getLocalMember().getUuid(), localMember);
        }
    }

    public void removeMember(String uuid) {
        if (members != null) {
            members.remove(uuid);
        }
    }

    public void shutdown() {
        if (members != null) {
            members.remove(EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getLocalMember().getUuid());
        }
        for (TCPEventPublisher publisher : tcpEventPublisherPool.values()) {
            publisher.shutdown();
        }
        if (tcpEventServer != null) {
            tcpEventServer.shutdown();
        }
    }

    public void syncEvent(String syncId, Event event) {
        if (allowEventSync) {
            for (TCPEventPublisher publisher : tcpEventPublisherPool.values()) {
                if (publisher != null) {
                    try {
                        Object[] eventData = ArrayUtils.addAll(ArrayUtils.addAll(event.getMetaData(), event.getCorrelationData()), event.getPayloadData());
                        publisher.sendEvent(syncId, event.getTimeStamp(), eventData, event.getArbitraryDataMap(), true);
                    } catch (IOException e) {
                        log.error("Error sending sync events to " + syncId, e);
                    }
                }
            }
        }
    }

    public void registerEventSync(EventSync eventSync) {
        eventSyncMap.putIfAbsent(EventManagementUtil.getSyncIdFromDatabridgeStream(eventSync.getStreamDefinition()), eventSync);
        for (TCPEventPublisher tcpEventPublisher : tcpEventPublisherPool.values()) {
            tcpEventPublisher.addStreamDefinition(EventManagementUtil.constructStreamDefinition(
                    EventManagementUtil.getSyncIdFromDatabridgeStream(eventSync.getStreamDefinition()),
                    eventSync.getStreamDefinition()));
        }
        if (tcpEventServer != null) {
            tcpEventServer.addStreamDefinition(EventManagementUtil.constructStreamDefinition(
                    EventManagementUtil.getSyncIdFromDatabridgeStream(eventSync.getStreamDefinition()),
                    eventSync.getStreamDefinition()));
        }
    }

    public void unregisterEventSync(String syncId) {
        EventSync eventSync = eventSyncMap.remove(syncId);
        if (eventSync != null) {
            for (TCPEventPublisher tcpEventPublisher : tcpEventPublisherPool.values()) {
                tcpEventPublisher.removeStreamDefinition(EventManagementUtil.constructStreamDefinition(
                        EventManagementUtil.getSyncIdFromDatabridgeStream(eventSync.getStreamDefinition()),
                        eventSync.getStreamDefinition()));
            }
            if (tcpEventServer != null) {
                tcpEventServer.removeStreamDefinition(EventManagementUtil.getSyncIdFromDatabridgeStream(eventSync.getStreamDefinition()));
            }
        }
    }

    public void startServer(HostAndPort member) {
        if (tcpEventServer == null) {
            TCPEventServerConfig tcpEventServerConfig = new TCPEventServerConfig(member.getHostName(), member.getPort());
            tcpEventServer = new TCPEventServer(tcpEventServerConfig, new StreamCallback() {
                @Override
                public void receive(String streamId, long timestamp, Object[] event, Map<String, String> arbitraryMapData) {
                    int index = streamId.indexOf("/");
                    if (index != -1) {
                        int tenantId = Integer.parseInt(streamId.substring(0, index));
                        try {
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);

                            EventSync eventSync = eventSyncMap.get(streamId);

                            if (log.isDebugEnabled()) {
                                log.debug("Event Received to :" + streamId);
                            }
                            if (eventSync != null) {
                                eventSync.process(EventManagementUtil.getWso2Event(eventSync.getStreamDefinition(), timestamp, event));
                            }

                        } catch (Exception e) {
                            log.error("Unable to process events for tenant :" + tenantId + " on stream:" + streamId.substring(index), e);
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }
                    }
                }
            }, null);
            for (EventSync eventSync : eventSyncMap.values()) {
                tcpEventServer.addStreamDefinition(EventManagementUtil.constructStreamDefinition(
                        EventManagementUtil.getSyncIdFromDatabridgeStream(eventSync.getStreamDefinition()),
                        eventSync.getStreamDefinition()));
            }
            try {
                tcpEventServer.start();
                log.info("Event Management TCPEventServer for EventReceiver started on port " + member.getPort());
            } catch (IOException e) {
                log.error("Unable to start TCPEventServer for EventReceiver started on port " + member.getPort());
            }
        }
    }

    public void checkMemberUpdate() {
        cleanupMembers();
        updateEventPublishers();
    }

    private synchronized void updateEventPublishers() {
        if (members != null) {
            List<HostAndPort> memberList = new ArrayList<HostAndPort>(members.values());
            memberList.remove(localMember);
            List<HostAndPort> currentMembers = new ArrayList<>(tcpEventPublisherPool.keySet());
            for (HostAndPort member : memberList) {
                if (!currentMembers.remove(member)) {
                    addEventPublisher(member);
                }
            }
            for (HostAndPort member : currentMembers) {
                removeEventPublisher(member);
            }
        }
    }

    public synchronized void addEventPublisher(HostAndPort member) {
        try {
            if (!tcpEventPublisherPool.containsKey(member)) {
                TCPEventPublisher tcpEventPublisher = new TCPEventPublisher(member.getHostName() + ":" + member.getPort(),
                        localEventPublisherConfiguration, false, null);
                for (EventSync eventSync : eventSyncMap.values()) {
                    tcpEventPublisher.addStreamDefinition(EventManagementUtil.constructStreamDefinition(
                            EventManagementUtil.getSyncIdFromDatabridgeStream(eventSync.getStreamDefinition()),
                            eventSync.getStreamDefinition()));
                }
                tcpEventPublisherPool.putIfAbsent(member, tcpEventPublisher);
                log.info("CEP sync publisher initiated to Member '" + member.getHostName() + ":" + member.getPort() + "'");
            }
        } catch (IOException e) {
            log.error("Error occurred while trying to start the publisher: " + e.getMessage(), e);
        }
    }

    private synchronized void removeEventPublisher(HostAndPort member) {
        TCPEventPublisher tcpEventPublisher = tcpEventPublisherPool.remove(member);
        if (tcpEventPublisher != null) {
            tcpEventPublisher.shutdown();
            log.info("CEP sync publisher disconnected from Member '" + member.getHostName() + ":" + member.getPort() + "'");
        }
    }

    private void cleanupMembers() {
        if (members != null) {
            Set<String> activeMemberUuidSet = new HashSet<String>();

            for (Member member : EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getMembers()) {
                activeMemberUuidSet.add(member.getUuid());
            }

            List<String> currentMemberUuidList = new ArrayList<String>(members.keySet());
            for (String memberUuid : currentMemberUuidList) {
                if (!activeMemberUuidSet.contains(memberUuid)) {
                    members.remove(memberUuid);
                }
            }
        }
    }

    public void allowEventSync(boolean allowEventSync) {
        this.allowEventSync = allowEventSync;
    }
}
