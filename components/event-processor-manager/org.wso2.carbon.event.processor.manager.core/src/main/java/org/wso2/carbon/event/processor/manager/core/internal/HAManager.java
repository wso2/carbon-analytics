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
import org.wso2.carbon.event.processor.manager.commons.utils.ByteSerializer;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;
import org.wso2.carbon.event.processor.manager.core.EventReceiverManagementService;
import org.wso2.carbon.event.processor.manager.core.Manager;
import org.wso2.carbon.event.processor.manager.core.config.HAConfiguration;
import org.wso2.carbon.event.processor.manager.core.internal.ds.EventManagementServiceValueHolder;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.ManagementServiceClientThriftImpl;
import org.wso2.carbon.event.processor.manager.core.internal.util.ConfigurationConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class HAManager {
    private static final Log log = LogFactory.getLog(HAManager.class);

    private final HazelcastInstance hazelcastInstance;
    private HAConfiguration haConfiguration;
    private boolean activeLockAcquired;
    private boolean passiveLockAcquired;
    private ILock activeLock;
    private ILock passiveLock;
    private IMap<HAConfiguration, Boolean> members;
    private IMap<String, HAConfiguration> roleToMembershipMap;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    private Future stateChanger = null;
    private String activeId;
    private String passiveId;


    public HAManager(HazelcastInstance hazelcastInstance, HAConfiguration haConfiguration) {
        this.hazelcastInstance = hazelcastInstance;
        this.haConfiguration = haConfiguration;
        activeId = ConfigurationConstants.ACTIVEID;
        passiveId = ConfigurationConstants.PASSIVEID;
        activeLock = hazelcastInstance.getLock(activeId);
        passiveLock = hazelcastInstance.getLock(passiveId);

        members = hazelcastInstance.getMap(ConfigurationConstants.MEMBERS);
        members.set(haConfiguration, true);

        SnapshotServer.start(haConfiguration);
        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                if (!activeLockAcquired) {
                    tryChangeState();
                }
            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            }
        });

        try {
            roleToMembershipMap = hazelcastInstance.getMap(ConfigurationConstants.ROLE_MEMBERSHIP_MAP);
        } catch (Exception e) {
            log.error(e);
        }
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
            scheduledThreadPoolExecutor.execute(new PeriodicStateChanger());
        }
    }

    private void tryChangeState() {
        if (!passiveLockAcquired) {
            if (passiveLock.tryLock()) {
                passiveLockAcquired = true;
                if (activeLock.tryLock()) {
                    activeLockAcquired = true;
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
                becomeActive();
                passiveLockAcquired = false;
                passiveLock.forceUnlock();

            }
        }
    }

    private void becomePassive() {
        roleToMembershipMap.set(passiveId, haConfiguration);
        HAConfiguration activeMember = null;

            activeMember = roleToMembershipMap.get(activeId);

        HAConfiguration passiveMember = roleToMembershipMap.get(passiveId);
        // Send non-duplicate events to active member
        CarbonEventManagementService eventManagementService = EventManagementServiceValueHolder.getCarbonEventManagementService();
        List<HostAndPort> receiverList=new ArrayList<HostAndPort>();
        receiverList.add(activeMember.getTransport());
        eventManagementService.setReceiverEndpoints(receiverList);

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
        if (eventProcessorManagementService != null) {
            eventProcessorManagementService.restoreState(stateMap.get(Manager.ManagerType.Processor));
            eventProcessorManagementService.resume();
        }
        if (eventReceiverManagementService != null) {
            eventReceiverManagementService.syncState(stateMap.get(Manager.ManagerType.Receiver));
            eventReceiverManagementService.resume();
        }

    }

    private void becomeActive() {
        CarbonEventManagementService eventManagementService = EventManagementServiceValueHolder.getCarbonEventManagementService();
        EventReceiverManagementService eventReceiverManagementService = eventManagementService.getEventReceiverManagementService();
        EventPublisherManagementService eventPublisherManagementService = eventManagementService.getEventPublisherManagementService();

        roleToMembershipMap.set(activeId, haConfiguration);

        if (eventPublisherManagementService != null) {
            eventPublisherManagementService.setDrop(false);
        }
        if (eventReceiverManagementService != null) {
            eventReceiverManagementService.start();
        }
    }

    public byte[] getState() {
        CarbonEventManagementService eventManagementService = EventManagementServiceValueHolder.getCarbonEventManagementService();
        EventReceiverManagementService eventReceiverManagementService = eventManagementService.getEventReceiverManagementService();
        EventProcessorManagementService eventProcessorManagementService = eventManagementService.getEventProcessorManagementService();
        HAConfiguration passiveMember = roleToMembershipMap.get(passiveId);
        HashMap<Manager.ManagerType, byte[]> stateMap = new HashMap<Manager.ManagerType, byte[]>();

        List<HostAndPort> receiverList=new ArrayList<HostAndPort>();
        receiverList.add(passiveMember.getTransport());
        eventManagementService.setReceiverEndpoints(receiverList);

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
        SnapshotServer.shutDown();
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
                stateChanger = scheduledThreadPoolExecutor.schedule(this, 15, TimeUnit.SECONDS);
            }
        }
    }
}
