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

import com.hazelcast.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.event.processor.manager.commons.utils.ByteSerializer;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;
import org.wso2.carbon.event.processor.manager.core.EventReceiverManagementService;
import org.wso2.carbon.event.processor.manager.core.Manager;
import org.wso2.carbon.event.processor.manager.core.config.HAConfiguration;
import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;
import org.wso2.carbon.event.processor.manager.core.internal.ds.EventManagementServiceValueHolder;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.ManagementServiceClientThriftImpl;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.ManagementServiceImpl;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.service.ManagementService;
import org.wso2.carbon.event.processor.manager.core.internal.util.ConfigurationConstants;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class HAManager {
    private static final Log log = LogFactory.getLog(HAManager.class);

    private HAConfiguration haConfiguration;
    private final ScheduledExecutorService executorService;
    private boolean activeLockAcquired;
    private boolean passiveLockAcquired;
    private ILock activeLock;
    private ILock passiveLock;
    private IMap<String, HAConfiguration> roleToMembershipMap;
    private final SnapshotServer snapshotServer;

    private Future stateChanger = null;
    private String activeId;
    private String passiveId;

    private HAConfiguration otherMember;


    public HAManager(HazelcastInstance hazelcastInstance, HAConfiguration haConfiguration, ScheduledExecutorService executorService) {
        this.haConfiguration = haConfiguration;
        this.executorService = executorService;
        activeId = ConfigurationConstants.ACTIVEID;
        passiveId = ConfigurationConstants.PASSIVEID;
        activeLock = hazelcastInstance.getLock(activeId);
        passiveLock = hazelcastInstance.getLock(passiveId);

        snapshotServer = new SnapshotServer();
        snapshotServer.start(haConfiguration);

        roleToMembershipMap = hazelcastInstance.getMap(ConfigurationConstants.ROLE_MEMBERSHIP_MAP);
        roleToMembershipMap.addEntryListener(new EntryAdapter<String, HAConfiguration>() {

            @Override
            public void entryRemoved(EntryEvent<String, HAConfiguration> stringCEPMembershipEntryEvent) {
                tryChangeState();
            }

        }, activeId, false);

    }

    public void init() {
        tryChangeState();
        if (!activeLockAcquired) {
            executorService.execute(new PeriodicStateChanger());
        }
    }

    public void tryChangeState() {
        if (!activeLockAcquired && !passiveLockAcquired) {
            if (passiveLock.tryLock()) {
                passiveLockAcquired = true;
                //adding a attribute to the local member so the the other member can identify in which state this member is in
                EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getLocalMember().setBooleanAttribute("active", false);
                if (activeLock.tryLock()) {
                    activeLockAcquired = true;
                    EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getLocalMember().setBooleanAttribute("active", true);
                    becomeActive();
                    passiveLockAcquired = false;
                    passiveLock.forceUnlock();
                } else {
                    becomePassive();
                }
            }
        } else if (!activeLockAcquired) {
            if (activeLock.tryLock()) {
                activeLockAcquired = true;
                EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getLocalMember().setBooleanAttribute("active", true);
                becomeActive();
                passiveLockAcquired = false;
                passiveLock.forceUnlock();
            }
        }
    }

    public void bePassive(){
        if(passiveLock.tryLock()){
            passiveLockAcquired = true;
            activeLockAcquired = false;
            becomePassive();
        }
    }

    public byte[] getState() {
        CarbonEventManagementService eventManagementService = EventManagementServiceValueHolder.getCarbonEventManagementService();
        EventReceiverManagementService eventReceiverManagementService = eventManagementService.getEventReceiverManagementService();
        EventProcessorManagementService eventProcessorManagementService = eventManagementService.getEventProcessorManagementService();
        HAConfiguration passiveMember = roleToMembershipMap.get(passiveId);
        otherMember = passiveMember;
        HashMap<Manager.ManagerType, byte[]> stateMap = new HashMap<Manager.ManagerType, byte[]>();

        List<HostAndPort> receiverList = new ArrayList<HostAndPort>();
        receiverList.add(passiveMember.getTransport());
        eventManagementService.setReceiverMembers(receiverList);
        eventManagementService.addMember(otherMember.getTransport());

        if (eventProcessorManagementService != null) {
            eventProcessorManagementService.pause();
        }

        if (eventReceiverManagementService != null) {
            eventReceiverManagementService.pause();
            byte[] receiverState = eventReceiverManagementService.getState();
            stateMap.put(Manager.ManagerType.Receiver, receiverState);
        }

        if (eventProcessorManagementService != null) {
            byte[] processorState = eventProcessorManagementService.getState();
            stateMap.put(Manager.ManagerType.Processor, processorState);
        }

        byte[] state = ByteSerializer.OToB(stateMap);

        if (eventProcessorManagementService != null) {
            eventProcessorManagementService.resume();
        }
        if (eventReceiverManagementService != null) {
            eventReceiverManagementService.resume();
        }

        return state;
    }

    public void shutdown() {
        if (passiveLockAcquired) {
            roleToMembershipMap.remove(passiveId);
            passiveLock.forceUnlock();
        }
        if (activeLockAcquired) {
            roleToMembershipMap.remove(activeId);
            activeLock.forceUnlock();

        }
        stateChanger.cancel(false);

        if (snapshotServer != null) {
            snapshotServer.shutDown();
        }
    }

    private void becomeActive() {
        CarbonEventManagementService eventManagementService = EventManagementServiceValueHolder.getCarbonEventManagementService();
        EventReceiverManagementService eventReceiverManagementService = eventManagementService.getEventReceiverManagementService();
        EventPublisherManagementService eventPublisherManagementService = eventManagementService.getEventPublisherManagementService();

        roleToMembershipMap.set(activeId, haConfiguration);
        eventManagementService.setReceiverMembers(new ArrayList<HostAndPort>());
        if (otherMember != null) {
            eventManagementService.removeMember(otherMember.getTransport());
        }
        otherMember = null;

        if (eventPublisherManagementService != null) {
            eventPublisherManagementService.setDrop(false);
        }
        if (eventReceiverManagementService != null) {
            eventReceiverManagementService.start();
        }
        log.info("Became CEP HA Active Member");
    }

    private void becomePassive() {
        roleToMembershipMap.set(passiveId, haConfiguration);

        final HAConfiguration activeMember = roleToMembershipMap.get(activeId);
        otherMember = activeMember;

        // Send non-duplicate events to active member
        final CarbonEventManagementService eventManagementService = EventManagementServiceValueHolder.getCarbonEventManagementService();
        List<HostAndPort> receiverList = new ArrayList<HostAndPort>();
        receiverList.add(otherMember.getTransport());
        eventManagementService.setReceiverMembers(receiverList);
        eventManagementService.addMember(otherMember.getTransport());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        syncState(activeMember, eventManagementService);
                        log.info("CEP HA State successfully synced.");
                        return;
                    } catch (EventManagementException e) {
                        log.error("CEP HA State syncing failed, " + e.getMessage(), e);
                    }
                    try {
                        Thread.sleep(10000); //Todo move to config file
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        log.info("Became CEP HA Passive Member");
    }

    private void syncState(HAConfiguration activeMember, CarbonEventManagementService eventManagementService) {
        EventReceiverManagementService eventReceiverManagementService = eventManagementService.getEventReceiverManagementService();
        EventProcessorManagementService eventProcessorManagementService = eventManagementService.getEventProcessorManagementService();
        EventPublisherManagementService eventPublisherManagementService = eventManagementService.getEventPublisherManagementService();
        if (eventReceiverManagementService != null) {
            eventReceiverManagementService.start();
            eventReceiverManagementService.pause();
        }
        if (eventProcessorManagementService != null) {
            eventProcessorManagementService.pause();
        }
        if (eventPublisherManagementService != null) {
            eventPublisherManagementService.setDrop(true);
        }
        ManagementServiceClient client = new ManagementServiceClientThriftImpl();
        byte[] state = null;
        try {
            state = client.getSnapshot(activeMember.getManagement());
        } catch (Throwable e) {
            log.error(e);
        }
        HashMap<Manager.ManagerType, byte[]> stateMap = (HashMap<Manager.ManagerType, byte[]>) ByteSerializer.BToO(state);
        // Synchronize the duplicate events with active member
        try {
            if (eventProcessorManagementService != null) {
                eventProcessorManagementService.restoreState(stateMap.get(Manager.ManagerType.Processor));
            }
            if (eventReceiverManagementService != null) {
                eventReceiverManagementService.syncState(stateMap.get(Manager.ManagerType.Receiver));
            }

        } finally {
            if (eventProcessorManagementService != null) {
                eventProcessorManagementService.resume();
            }
            if (eventReceiverManagementService != null) {
                eventReceiverManagementService.resume();
            }

        }
    }

    class PeriodicStateChanger implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            tryChangeState();
            if (!activeLockAcquired) {
                stateChanger = executorService.schedule(this, 15, TimeUnit.SECONDS);
            }
        }
    }

    class SnapshotServer {
        private TServer dataReceiverServer;

        public void start(HAConfiguration config) {

            HostAndPort management = config.getManagement();
            try {
                TServerSocket serverTransport = new TServerSocket(
                        new InetSocketAddress(management.getHostName(), management.getPort()));
                ManagementService.Processor<ManagementServiceImpl> processor =
                        new ManagementService.Processor<ManagementServiceImpl>(new ManagementServiceImpl());
                dataReceiverServer = new TThreadPoolServer(
                        new TThreadPoolServer.Args(serverTransport).processor(processor));
                Thread thread = new Thread(new ServerThread(dataReceiverServer));
                log.info("CEP HA Snapshot Server started on " + management.getHostName() + ":" + management.getPort());
                thread.start();
            } catch (TTransportException e) {
                log.error("Cannot start CEP HA Snapshot Server on port " + management.getHostName() + ":" + management.getPort(), e);
            } catch (Throwable e) {
                log.error("Error in starting CEP HA Snapshot Server ", e);
            }
        }

        public void shutDown() {
            dataReceiverServer.stop();
        }

        class ServerThread implements Runnable {
            private TServer server;

            ServerThread(TServer server) {
                this.server = server;
            }

            public void run() {
                this.server.serve();
            }
        }

    }
}