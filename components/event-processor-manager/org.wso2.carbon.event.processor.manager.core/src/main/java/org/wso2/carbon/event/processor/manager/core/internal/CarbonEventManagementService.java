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
package org.wso2.carbon.event.processor.manager.core.internal;

import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.core.*;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.HAConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;
import org.wso2.carbon.event.processor.manager.core.config.Mode;
import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;
import org.wso2.carbon.event.processor.manager.core.exception.ManagementConfigurationException;
import org.wso2.carbon.event.processor.manager.core.internal.util.ConfigurationConstants;
import org.wso2.carbon.event.processor.manager.core.internal.util.ManagementModeConfigurationLoader;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CarbonEventManagementService implements EventManagementService {

    private static Logger log = Logger.getLogger(CarbonEventManagementService.class);

    private Mode mode = Mode.SingleNode;
    private ManagementModeInfo managementModeInfo;

    private HAManager haManager = null;
    private ScheduledExecutorService executorService;

    private EventProcessorManagementService processorManager;
    private EventReceiverManagementService receiverManager;
    private EventPublisherManagementService publisherManager;

    private ConcurrentHashMap<String, EventSync> eventSyncMap = new ConcurrentHashMap<String, EventSync>();
    private TCPEventServer tcpEventServer;
    private ConcurrentHashMap<HostAndPort, TCPEventPublisher> tcpEventReceiverPublishers = new ConcurrentHashMap<HostAndPort, TCPEventPublisher>();
    private ConcurrentHashMap<HostAndPort, TCPEventPublisher> tcpEventPublisherPublishers = new ConcurrentHashMap<HostAndPort, TCPEventPublisher>();


    public CarbonEventManagementService() {
        try {
            managementModeInfo = ManagementModeConfigurationLoader.loadManagementModeInfo();
            mode = managementModeInfo.getMode();
        } catch (ManagementConfigurationException e) {
            throw new EventManagementException("Error getting management mode information", e);
        }
        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
            startServer(haConfiguration.getTransport());
        } else if (mode == Mode.Distributed) {
            DistributedConfiguration distributedConfiguration = managementModeInfo.getDistributedConfiguration();
//            startServer(distributedConfiguration.getTransport()); //Todo
        }
    }

    public void init(HazelcastInstance hazelcastInstance) {
        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
            haManager = new HAManager(hazelcastInstance, haConfiguration);
            haManager.init();
        } else if (mode == Mode.SingleNode) {
            log.warn("CEP started with clustering enabled, but SingleNode configuration given.");
        }
    }

    public void init(ConfigurationContextService configurationContextService) {
        if (mode != Mode.HA) {
            receiverManager.start();
        }
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                log.info("Starting polling event adapters");
                getEventReceiverManagementService().startPolling();
            }
        }, ConfigurationConstants.AXIS_TIME_INTERVAL_IN_MILLISECONDS * 4, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if (haManager != null) {
            haManager.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        setReceiverEndpoints(new ArrayList<HostAndPort>());
        setPublisherEndpoints(new ArrayList<HostAndPort>());

        if (tcpEventServer != null) {
            tcpEventServer.shutdown();
        }
    }

    public byte[] getState() {
        if (mode == Mode.HA) {
            return haManager.getState();
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
    public void syncEvent(String syncId, Manager.ManagerType type, Object[] data) {
        if (type == Manager.ManagerType.Receiver) {
            for (TCPEventPublisher publisher : tcpEventReceiverPublishers.values()) {
                try {
                    publisher.sendEvent(syncId, data, true);
                } catch (IOException e) {
                    log.error("Error sending sync events to " + syncId, e);
                }
            }
        } else if (type == Manager.ManagerType.Publisher) {
            for (TCPEventPublisher publisher : tcpEventPublisherPublishers.values()) {
                try {
                    publisher.sendEvent(syncId, data, true);
                } catch (IOException e) {
                    log.error("Error sending sync events to " + syncId, e);
                }
            }
        }
    }

    @Override
    public void registerEventSync(EventSync eventSync) {
        eventSyncMap.putIfAbsent(eventSync.getStreamDefinition().getId(), eventSync);
        for (TCPEventPublisher tcpEventPublisher : tcpEventPublisherPublishers.values()) {
            tcpEventPublisher.addStreamDefinition(eventSync.getStreamDefinition());
        }
        for (TCPEventPublisher tcpEventPublisher : tcpEventReceiverPublishers.values()) {
            tcpEventPublisher.addStreamDefinition(eventSync.getStreamDefinition());
        }
        if (tcpEventServer != null) {
            tcpEventServer.subscribe(eventSync.getStreamDefinition());
        }
    }

    @Override
    public void unregisterEventSync(String syncId) {
        EventSync eventSync = eventSyncMap.remove(syncId);
        if (eventSync != null) {
            for (TCPEventPublisher tcpEventPublisher : tcpEventPublisherPublishers.values()) {
                tcpEventPublisher.removeStreamDefinition(eventSync.getStreamDefinition());
            }
            for (TCPEventPublisher tcpEventPublisher : tcpEventReceiverPublishers.values()) {
                tcpEventPublisher.removeStreamDefinition(eventSync.getStreamDefinition());
            }
            if (tcpEventServer != null) {
                tcpEventServer.unsubscribe(eventSync.getStreamDefinition().getId());
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


    private void startServer(HostAndPort member) {
        if (tcpEventServer == null) {
            TCPEventServerConfig tcpEventServerConfig = new TCPEventServerConfig(member.getPort());
            tcpEventServerConfig.setNumberOfThreads(1); //todo fix
            tcpEventServer = new TCPEventServer(tcpEventServerConfig, new StreamCallback() {
                @Override
                public void receive(String streamId, Object[] data) {
                    int index = streamId.indexOf("/");
                    if (index != -1) {
                        int tenantId = Integer.parseInt(streamId.substring(0, index));
                        try {
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);

                            EventSync eventSync = eventSyncMap.get(streamId);

                            if (eventSync != null) {
                                eventSync.process(data);
                            }

                        } catch (Exception e) {
                            log.error("Unable to start event adpaters for tenant :" + tenantId, e);
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }
                    }
                }
            });
            for (EventSync eventSync : eventSyncMap.values()) {
                tcpEventServer.subscribe(eventSync.getStreamDefinition());
            }
            tcpEventServer.start();
            log.info("Event Management TCPEventServer for EventReceiver started on port " + member.getPort());
        }
    }

    public void setReceiverEndpoints(List<HostAndPort> members) {
        List<HostAndPort> currentReceivers = new ArrayList<>(tcpEventReceiverPublishers.keySet());

        for (HostAndPort member : members) {
            if (!currentReceivers.remove(member)) {
                if (!tcpEventPublisherPublishers.containsKey(member)) {
                    startClient(member, tcpEventReceiverPublishers);
                } else {
                    tcpEventReceiverPublishers.put(member, tcpEventPublisherPublishers.get(member));
                }
            }
        }

        for (HostAndPort member : currentReceivers) {
            if (!tcpEventPublisherPublishers.containsKey(member)) {
                stopClient(member, tcpEventReceiverPublishers);
            } else {
                tcpEventReceiverPublishers.remove(member);
            }
        }

    }

    public void setPublisherEndpoints(List<HostAndPort> members) {
        List<HostAndPort> currentReceivers = new ArrayList<>(tcpEventPublisherPublishers.keySet());

        for (HostAndPort member : members) {
            if (!currentReceivers.remove(member)) {
                if (!tcpEventReceiverPublishers.containsKey(member)) {
                    startClient(member, tcpEventPublisherPublishers);
                } else {
                    tcpEventPublisherPublishers.put(member, tcpEventReceiverPublishers.get(member));
                }
            }
        }

        for (HostAndPort member : currentReceivers) {
            if (!tcpEventReceiverPublishers.containsKey(member)) {
                stopClient(member, tcpEventPublisherPublishers);
            } else {
                tcpEventPublisherPublishers.remove(member);
            }
        }

    }

    private void startClient(HostAndPort member, ConcurrentHashMap<HostAndPort, TCPEventPublisher> tcpEventPublishers) {

        TCPEventPublisher tcpEventPublisher;
        try {
            tcpEventPublisher = new TCPEventPublisher(member.getHostName() + ":" + member.getPort(), false);
            for (EventSync eventSync : eventSyncMap.values()) {
                tcpEventPublisher.addStreamDefinition(eventSync.getStreamDefinition());
            }
            tcpEventPublishers.putIfAbsent(member, tcpEventPublisher);
        } catch (IOException e) {
            log.error("Error occurred while trying to start the publisher: " + e.getMessage(), e);
        }

    }

    private void stopClient(HostAndPort member, ConcurrentHashMap<HostAndPort, TCPEventPublisher> tcpEventPublishers) {

        TCPEventPublisher tcpEventPublisher = tcpEventPublishers.remove(member);
        if (tcpEventPublisher != null) {
            tcpEventPublisher.shutdown();
        }
    }

}
