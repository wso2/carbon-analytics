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
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;
import org.wso2.carbon.event.processor.manager.core.EventReceiverManagementService;
import org.wso2.carbon.event.processor.manager.core.EventSync;
import org.wso2.carbon.event.processor.manager.core.Manager;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.HAConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;
import org.wso2.carbon.event.processor.manager.core.config.Mode;
import org.wso2.carbon.event.processor.manager.core.config.PersistenceConfiguration;
import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;
import org.wso2.carbon.event.processor.manager.core.exception.ManagementConfigurationException;
import org.wso2.carbon.event.processor.manager.core.internal.ds.EventManagementServiceValueHolder;
import org.wso2.carbon.event.processor.manager.core.internal.util.ConfigurationConstants;
import org.wso2.carbon.event.processor.manager.core.internal.util.ManagementModeConfigurationLoader;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CarbonEventManagementService implements EventManagementService {

    private static Logger log = Logger.getLogger(CarbonEventManagementService.class);

    private Mode mode = Mode.SingleNode;
    private ManagementModeInfo managementModeInfo;

    private EventProcessorManagementService processorManager;
    private EventReceiverManagementService receiverManager;
    private List<EventPublisherManagementService> publisherManager;

    private EventHandler receiverEventHandler = new EventHandler();
    private EventHandler presenterEventHandler = new EventHandler();

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
            publisherManager = new CopyOnWriteArrayList<>();
        } catch (ManagementConfigurationException e) {
            throw new EventManagementException("Error getting management mode information", e);
        }
        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
            isWorkerNode = haConfiguration.isWorkerNode();
            isPresenterNode = haConfiguration.isPresenterNode();
            if (isWorkerNode) {
                receiverEventHandler.startServer(haConfiguration.getEventSyncConfig());
            }
            if (isPresenterNode && !isWorkerNode) {
                presenterEventHandler.startServer(haConfiguration.getLocalPresenterConfig());
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
                presenterEventHandler.startServer(distributedConfiguration.getLocalPresenterConfig());
            }
        }
    }

    public void init(HazelcastInstance hazelcastInstance) {
        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
            if (isWorkerNode) {
                receiverEventHandler.init(ConfigurationConstants.RECEIVERS, haConfiguration.getEventSyncConfig(),
                        haConfiguration.constructEventSyncPublisherConfig(), isWorkerNode);
                haManager = new HAManager(hazelcastInstance, haConfiguration, executorService, receiverEventHandler, presenterEventHandler);
                haManager.init();
                if (haEventPublisherTimeSyncMap == null) {
                    haEventPublisherTimeSyncMap = EventManagementServiceValueHolder.getHazelcastInstance().getMap(ConfigurationConstants.HA_EVENT_PUBLISHER_TIME_SYNC_MAP);
                }
            }
            presenterEventHandler.init(ConfigurationConstants.PRESENTERS, haConfiguration.getLocalPresenterConfig(),
                    haConfiguration.constructPresenterPublisherConfig(), isPresenterNode && !isWorkerNode);
            checkMemberUpdate();
        } else if (mode == Mode.Distributed) {
            if (stormReceiverCoordinator != null) {
                stormReceiverCoordinator.tryBecomeCoordinator();
            }
            DistributedConfiguration distributedConfiguration = managementModeInfo.getDistributedConfiguration();
            presenterEventHandler.init(ConfigurationConstants.PRESENTERS, distributedConfiguration
                    .getLocalPresenterConfig(), distributedConfiguration.constructPresenterPublisherConfig(), isPresenterNode);
            checkMemberUpdate();
        } else if (mode == Mode.SingleNode) {
            log.warn("CEP started with clustering enabled, but SingleNode configuration given.");
        }

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                presenterEventHandler.registerLocalMember();
                receiverEventHandler.registerLocalMember();
                checkMemberUpdate();
                if (mode == Mode.HA) {
                    if (isWorkerNode && haManager != null) {
                        haManager.verifyState();
                    }
                }

            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                receiverEventHandler.removeMember(membershipEvent.getMember().getUuid());
                presenterEventHandler.removeMember(membershipEvent.getMember().getUuid());
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
            checkMemberUpdateInterval = distributedConfiguration.getMemberUpdateCheckInterval();
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
        receiverEventHandler.shutdown();
        presenterEventHandler.shutdown();
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
            this.publisherManager.add((EventPublisherManagementService) manager);
        }
    }

    @Override
    public void unsubscribe(Manager manager) {
        if (manager.getType() == Manager.ManagerType.Processor) {
            this.processorManager = null;
        } else if (manager.getType() == Manager.ManagerType.Receiver) {
            this.receiverManager = null;
        } else if (manager.getType() == Manager.ManagerType.Publisher) {
            this.publisherManager.remove(manager);
        }
    }

    @Override
    public void syncEvent(String syncId, Manager.ManagerType type, Event event) {
        if (type == Manager.ManagerType.Receiver) {
            receiverEventHandler.syncEvent(syncId, event);
        } else {
            presenterEventHandler.syncEvent(syncId, event);
        }
    }

    @Override
    public void registerEventSync(EventSync eventSync, Manager.ManagerType type) {
        if (type == Manager.ManagerType.Receiver) {
            receiverEventHandler.registerEventSync(eventSync);
        } else {
            presenterEventHandler.registerEventSync(eventSync);
        }
    }

    @Override
    public void unregisterEventSync(String syncId, Manager.ManagerType type) {
        if (type == Manager.ManagerType.Receiver) {
            receiverEventHandler.unregisterEventSync(syncId);
        } else {
            presenterEventHandler.unregisterEventSync(syncId);
        }
    }

    public EventProcessorManagementService getEventProcessorManagementService() {
        return processorManager;
    }

    public EventReceiverManagementService getEventReceiverManagementService() {
        return receiverManager;
    }

    public List<EventPublisherManagementService> getEventPublisherManagementService() {
        return publisherManager;
    }

    private void checkMemberUpdate() {

        if (isWorkerNode) {
            if (mode == Mode.HA) {
                receiverEventHandler.checkMemberUpdate();
                presenterEventHandler.checkMemberUpdate();
            } else if (mode == Mode.Distributed) {
                presenterEventHandler.checkMemberUpdate();
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
