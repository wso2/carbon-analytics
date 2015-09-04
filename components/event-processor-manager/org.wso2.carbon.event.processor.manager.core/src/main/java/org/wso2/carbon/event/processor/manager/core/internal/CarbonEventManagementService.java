/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.manager.core.internal;

import com.hazelcast.core.*;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisherConfig;
import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.core.*;
import org.wso2.carbon.event.processor.manager.core.config.*;
import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;
import org.wso2.carbon.event.processor.manager.core.exception.ManagementConfigurationException;
import org.wso2.carbon.event.processor.manager.core.internal.ds.EventManagementServiceValueHolder;
import org.wso2.carbon.event.processor.manager.core.internal.util.ConfigurationConstants;
import org.wso2.carbon.event.processor.manager.core.internal.util.ManagementModeConfigurationLoader;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.siddhi.core.event.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class CarbonEventManagementService implements EventManagementService {

    private static Logger log = Logger.getLogger(CarbonEventManagementService.class);

    private Mode mode = Mode.SingleNode;
    private ManagementModeInfo managementModeInfo;

    private EventProcessorManagementService processorManager;
    private EventReceiverManagementService receiverManager;
    private EventPublisherManagementService publisherManager;

    private IMap<String, HostAndPort> receivers;
    private IMap<String, HostAndPort> presenters;
    private CopyOnWriteArrayList<HostAndPort> syncReceivers = new CopyOnWriteArrayList<HostAndPort>();
    private CopyOnWriteArrayList<HostAndPort> syncPresenters = new CopyOnWriteArrayList<HostAndPort>();
    private ConcurrentHashMap<String, EventSync> eventSyncMap = new ConcurrentHashMap<String, EventSync>();
    private TCPEventServer tcpEventServer;
    private ConcurrentHashMap<HostAndPort, TCPEventPublisher> tcpEventPublisherPool = new ConcurrentHashMap<HostAndPort, TCPEventPublisher>();

    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(3);

    private HAManager haManager = null;
    private IMap<String, Long> haEventPublisherTimeSyncMap = null;

    private PersistenceManager persistenceManager = null;

    private StormReceiverCoordinator stormReceiverCoordinator = null;

    private boolean isManagerNode = false;
    private boolean isWorkerNode = false;
    private boolean isPresenterNode = false;


    public CarbonEventManagementService() {
        try {
            managementModeInfo = ManagementModeConfigurationLoader.loadManagementModeInfo();
            mode = managementModeInfo.getMode();
        } catch (ManagementConfigurationException e) {
            throw new EventManagementException("Error getting management mode information", e);
        }
        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
            isWorkerNode = haConfiguration.isWorkerNode();
            isPresenterNode = haConfiguration.isPresenterNode();
            if (isWorkerNode) {
                startServer(haConfiguration.getEventSyncConfig(), haConfiguration.getEventSyncServerThreads());
            }
            if (isPresenterNode) {
                startServer(haConfiguration.getLocalPresenterConfig(), haConfiguration.getPresenterServerThreads());
            }
        } else if (mode == Mode.SingleNode) {
            PersistenceConfiguration persistConfig = managementModeInfo.getPersistenceConfiguration();
            if (persistConfig != null) {
                ScheduledExecutorService scheduledExecutorService = Executors
                        .newScheduledThreadPool(persistConfig.getThreadPoolSize());
                long persistenceTimeInterval = persistConfig.getPersistenceTimeInterval();
                if (persistenceTimeInterval > 0) {
                    persistenceManager = new PersistenceManager(scheduledExecutorService, persistenceTimeInterval);
                    persistenceManager.init();
                }
            }
        } else if (mode == Mode.Distributed) {
            DistributedConfiguration distributedConfiguration = managementModeInfo.getDistributedConfiguration();
            isManagerNode = distributedConfiguration.isManagerNode();
            isWorkerNode = distributedConfiguration.isWorkerNode();
            if (isWorkerNode) {
                stormReceiverCoordinator = new StormReceiverCoordinator();
            }
            isPresenterNode = distributedConfiguration.isPresenterNode();
            if (isPresenterNode) {
                startServer(distributedConfiguration.getLocalPresenterConfig(), distributedConfiguration.getPresentationReceiverThreads());
            }
        }
    }

    public void init(HazelcastInstance hazelcastInstance) {

        receivers = hazelcastInstance.getMap(ConfigurationConstants.RECEIVERS);
        presenters = hazelcastInstance.getMap(ConfigurationConstants.PRESENTERS);

        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
            if (isWorkerNode) {
                haManager = new HAManager(hazelcastInstance, haConfiguration, executorService);
                haManager.init();

                receivers.set(hazelcastInstance.getCluster().getLocalMember().getUuid(), haConfiguration.getEventSyncConfig());

                if (haEventPublisherTimeSyncMap == null) {
                    haEventPublisherTimeSyncMap = EventManagementServiceValueHolder.getHazelcastInstance().getMap(ConfigurationConstants.HA_EVENT_PUBLISHER_TIME_SYNC_MAP);
                }
            }
            if (isPresenterNode) {
                presenters.set(hazelcastInstance.getCluster().getLocalMember().getUuid(), haConfiguration.getLocalPresenterConfig());
            }
            checkMemberUpdate();
        } else if (mode == Mode.Distributed) {
            if (stormReceiverCoordinator != null) {
                stormReceiverCoordinator.tryBecomeCoordinator();
            }
            DistributedConfiguration distributedConfiguration = managementModeInfo.getDistributedConfiguration();
            if (isPresenterNode) {
                presenters.set(hazelcastInstance.getCluster().getLocalMember().getUuid(), distributedConfiguration.getLocalPresenterConfig());
            }
            checkMemberUpdate();
        } else if (mode == Mode.SingleNode) {
            log.warn("CEP started with clustering enabled, but SingleNode configuration given.");
        }

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                checkMemberUpdate();
                if (mode == Mode.HA) {
                    if (isWorkerNode && haManager != null) {
                        haManager.verifyState();
                    }
                }
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                receivers.remove(membershipEvent.getMember().getUuid());
                presenters.remove(membershipEvent.getMember().getUuid());
                checkMemberUpdate();
                if (mode == Mode.HA) {
                    if (isWorkerNode && haManager != null) {
                        haManager.tryChangeState();
                    }
                } else if (mode == Mode.Distributed) {
                    if (stormReceiverCoordinator != null) {
                        stormReceiverCoordinator.tryBecomeCoordinator();
                    }
                }
            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            }

        });

    }

    public void init(ConfigurationContextService configurationContextService) {
        if (mode == Mode.SingleNode || isWorkerNode) {
            receiverManager.start();
        }
        if ((mode == Mode.Distributed || mode == Mode.HA) && isWorkerNode || mode == Mode.SingleNode) {
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("Starting polling event receivers");
                        EventReceiverManagementService eventReceiverManagementService = getEventReceiverManagementService();
                        if (eventReceiverManagementService != null) {
                            eventReceiverManagementService.startPolling();
                        } else {
                            log.error("Adapter polling failed as EventReceiverManagementService not available");
                        }
                    } catch (Exception e) {
                        log.error("Unexpected error occurred when start polling event adapters", e);
                    }
                }
            }, ConfigurationConstants.AXIS_TIME_INTERVAL_IN_MILLISECONDS * 4, TimeUnit.MILLISECONDS);
        }

        int checkMemberUpdateInterval = 10 * 1000;
        if (mode == Mode.Distributed) {
            DistributedConfiguration distributedConfiguration = managementModeInfo.getDistributedConfiguration();
            checkMemberUpdateInterval = distributedConfiguration.getCheckMemberUpdateInterval();
        } else if (mode == Mode.HA) {
            HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
            checkMemberUpdateInterval = haConfiguration.getCheckMemberUpdateInterval();
        }
        if (mode == Mode.Distributed || mode == Mode.HA) {
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    checkMemberUpdate();
                }
            }, checkMemberUpdateInterval, checkMemberUpdateInterval, TimeUnit.MILLISECONDS);
        }
    }

    public void shutdown() {
        if (haManager != null) {
            haManager.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        if (persistenceManager != null) {
            persistenceManager.shutdown();
        }
        if (receivers != null) {
            receivers.remove(EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getLocalMember().getUuid());
        }
        if (presenters != null) {
            presenters.remove(EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getLocalMember().getUuid());
        }
        syncReceivers.clear();
        syncPresenters.clear();
        if (tcpEventServer != null) {
            tcpEventServer.shutdown();
        }
    }

    public byte[] getState() {
        if (mode == Mode.HA) {
            if (isWorkerNode) {
                return haManager.getState();
            }
        }
        return null;
    }

    public ManagementModeInfo getManagementModeInfo() {
        return managementModeInfo;
    }

    public void subscribe(Manager manager) {
        if (manager.getType() == Manager.ManagerType.Processor) {
            this.processorManager = (EventProcessorManagementService) manager;
        } else if (manager.getType() == Manager.ManagerType.Receiver) {
            this.receiverManager = (EventReceiverManagementService) manager;
        } else if (manager.getType() == Manager.ManagerType.Publisher) {
            this.publisherManager = (EventPublisherManagementService) manager;
        }
    }

    @Override
    public void unsubscribe(Manager manager) {
        if (manager.getType() == Manager.ManagerType.Processor) {
            this.processorManager = null;
        } else if (manager.getType() == Manager.ManagerType.Receiver) {
            this.receiverManager = null;
        } else if (manager.getType() == Manager.ManagerType.Publisher) {
            this.publisherManager = null;
        }
    }

    @Override
    public void syncEvent(String syncId, Manager.ManagerType type, Event event) {
        List<HostAndPort> members = null;
        if (type == Manager.ManagerType.Receiver) {
            members = syncReceivers;
        } else if (type == Manager.ManagerType.Publisher) {
            members = syncPresenters;
        }
        if (members != null) {
            for (HostAndPort member : members) {
                TCPEventPublisher publisher = tcpEventPublisherPool.get(member);
                if (publisher != null) {
                    try {
                        publisher.sendEvent(syncId, event.getTimestamp(), event.getData(), true);
                    } catch (IOException e) {
                        log.error("Error sending sync events to " + syncId, e);
                    }
                }
            }
        }

    }

    @Override
    public void registerEventSync(EventSync eventSync) {
        eventSyncMap.putIfAbsent(eventSync.getStreamDefinition().getId(), eventSync);
        for (TCPEventPublisher tcpEventPublisher : tcpEventPublisherPool.values()) {
            tcpEventPublisher.addStreamDefinition(eventSync.getStreamDefinition());
        }
        if (tcpEventServer != null) {
            tcpEventServer.addStreamDefinition(eventSync.getStreamDefinition());
        }
    }

    @Override
    public void unregisterEventSync(String syncId) {
        EventSync eventSync = eventSyncMap.remove(syncId);
        if (eventSync != null) {
            for (TCPEventPublisher tcpEventPublisher : tcpEventPublisherPool.values()) {
                tcpEventPublisher.removeStreamDefinition(eventSync.getStreamDefinition());
            }
            if (tcpEventServer != null) {
                tcpEventServer.removeStreamDefinition(eventSync.getStreamDefinition().getId());
            }
        }

    }

    public EventProcessorManagementService getEventProcessorManagementService() {
        return processorManager;
    }

    public EventReceiverManagementService getEventReceiverManagementService() {
        return receiverManager;
    }

    public EventPublisherManagementService getEventPublisherManagementService() {
        return publisherManager;
    }

    private void startServer(HostAndPort member, int threads) {
        if (tcpEventServer == null) {
            TCPEventServerConfig tcpEventServerConfig = new TCPEventServerConfig(member.getPort());
            tcpEventServerConfig.setNumberOfThreads(threads);
            tcpEventServer = new TCPEventServer(tcpEventServerConfig, new StreamCallback() {
                @Override
                public void receive(String streamId, long timestamp, Object[] data) {
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
                                eventSync.process(new Event(timestamp, data));
                            }

                        } catch (Exception e) {
                            log.error("Unable to start event adaptors for tenant :" + tenantId, e);
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }
                    }
                }
            }, null);
            for (EventSync eventSync : eventSyncMap.values()) {
                tcpEventServer.addStreamDefinition(eventSync.getStreamDefinition());
            }
            tcpEventServer.start();
            log.info("Event Management TCPEventServer for EventReceiver started on port " + member.getPort());
        }
    }

    public synchronized void updateMembers(List<HostAndPort> members, TCPEventPublisherConfig tcpEventPublisherConfig) {
        List<HostAndPort> currentMembers = new ArrayList<>(tcpEventPublisherPool.keySet());

        for (HostAndPort member : members) {
            if (!currentMembers.remove(member)) {
                addMember(member, tcpEventPublisherConfig);
            }
        }
        for (HostAndPort member : currentMembers) {
            removeMember(member);
        }
    }

    public synchronized void removeMember(HostAndPort member) {
        TCPEventPublisher tcpEventPublisher = tcpEventPublisherPool.remove(member);
        if (tcpEventPublisher != null) {
            tcpEventPublisher.shutdown();
            log.info("CEP sync publisher disconnected from Member '" + member.getHostName() + ":" + member.getPort() + "'");
        }
    }

    public synchronized void addMember(HostAndPort member, TCPEventPublisherConfig tcpEventPublisherConfig) {
        try {
            if (!tcpEventPublisherPool.containsKey(member)) {
                TCPEventPublisher tcpEventPublisher = new TCPEventPublisher(member.getHostName() + ":" + member.getPort(), tcpEventPublisherConfig, false, null);
                for (EventSync eventSync : eventSyncMap.values()) {
                    tcpEventPublisher.addStreamDefinition(eventSync.getStreamDefinition());
                }
                tcpEventPublisherPool.putIfAbsent(member, tcpEventPublisher);
                log.info("CEP sync publisher initiated to Member '" + member.getHostName() + ":" + member.getPort() + "'");
            }
        } catch (IOException e) {
            log.error("Error occurred while trying to start the publisher: " + e.getMessage(), e);
        }
    }

    public void setSyncReceivers(List<HostAndPort> members) {
        syncReceivers.clear();
        syncReceivers.addAll(members);
    }

    public void setSyncPresenters(List<HostAndPort> members) {
        syncPresenters.clear();
        syncPresenters.addAll(members);
    }

    private void checkMemberUpdate() {

        if (isWorkerNode) {
            cleanupMembers();

            if (receivers != null) {
                if (mode == Mode.HA) {
                    List<HostAndPort> memberList = new ArrayList<HostAndPort>(receivers.values());
                    memberList.remove(managementModeInfo.getHaConfiguration().getEventSyncConfig());
                    updateMembers(memberList, managementModeInfo.getHaConfiguration().constructEventSyncPublisherConfig());
                    setSyncReceivers(memberList);
                }
            }
            if (presenters != null) {
                if (mode == Mode.Distributed) {
                    List<HostAndPort> memberList = new ArrayList<HostAndPort>(presenters.values());
                    memberList.remove(managementModeInfo.getDistributedConfiguration().getLocalPresenterConfig());
                    updateMembers(memberList, managementModeInfo.getDistributedConfiguration().constructPresenterPublisherConfig());
                    setSyncPresenters(memberList);
                } else if (mode == Mode.HA) {
                    List<HostAndPort> memberList = new ArrayList<HostAndPort>(presenters.values());
                    memberList.remove(managementModeInfo.getHaConfiguration().getEventSyncConfig());
                    updateMembers(memberList, managementModeInfo.getHaConfiguration().constructPresenterPublisherConfig());
                    setSyncPresenters(memberList);
                }
            }
        }
    }

    private void cleanupMembers() {
        HazelcastInstance hazelcastInstance = EventManagementServiceValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null) {
            Set<String> activeMemberUuidSet = new HashSet<String>();

            for (Member member : hazelcastInstance.getCluster().getMembers()) {
                activeMemberUuidSet.add(member.getUuid());
            }

            if (receivers != null) {
                List<String> currentMemberUuidList = new ArrayList<String>(receivers.keySet());
                for (String memberUuid : currentMemberUuidList) {
                    if (!activeMemberUuidSet.contains(memberUuid)) {
                        receivers.remove(memberUuid);
                    }
                }
            }
            if (presenters != null) {
                List<String> currentMemberUuidList = new ArrayList<String>(presenters.keySet());
                for (String memberUuid : currentMemberUuidList) {
                    if (!activeMemberUuidSet.contains(memberUuid)) {
                        presenters.remove(memberUuid);
                    }
                }
            }
        }
    }

    @Override
    public void updateLatestEventSentTime(String publisherName, int tenantId, long timestamp) {
        haEventPublisherTimeSyncMap.putAsync(tenantId + "-" + publisherName, EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getClusterTime());
    }

    @Override
    public long getLatestEventSentTime(String publisherName, int tenantId) {
        if (haEventPublisherTimeSyncMap == null) {
            haEventPublisherTimeSyncMap = EventManagementServiceValueHolder.getHazelcastInstance()
                    .getMap(ConfigurationConstants.HA_EVENT_PUBLISHER_TIME_SYNC_MAP);
        }
        Long latestTimePublished = haEventPublisherTimeSyncMap.get(tenantId + "-" + publisherName);
        if (latestTimePublished != null) {
            return latestTimePublished;
        }
        return 0;
    }

    @Override
    public long getClusterTimeInMillis() {
        return EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getClusterTime();
    }
}