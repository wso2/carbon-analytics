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

package org.wso2.carbon.event.receiver.core.internal;


import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.core.EventReceiverManagementService;
import org.wso2.carbon.event.processor.manager.core.config.ManagementConfigurationException;
import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.siddhi.core.util.snapshot.ByteSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarbonEventReceiverManagementService extends EventReceiverManagementService {

    private Logger log = Logger.getLogger(CarbonEventReceiverManagementService.class);
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private HostAndPort otherMember;
    private TCPEventPublisher tcpEventPublisher;
    private TCPEventServer tcpEventServer;

    public CarbonEventReceiverManagementService() {
            tcpEventServer = null;
            tcpEventPublisher = null;
        try {
            EventReceiverServiceValueHolder.getCarbonEventReceiverService().setManagementModeInfo(ManagementModeInfo.getInstance());
        } catch (ManagementConfigurationException e) {
            log.error("Error while reading CEP configuration XML", e);
        }
        EventReceiverServiceValueHolder.getEventManagementService().subscribe(this);
    }

    @Override
    public byte[] getState() {
        Map<Integer, Map<String, EventReceiver>> tenantSpecificEventAdapters = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
        Map<Integer, HashMap<String, byte[]>> data = new HashMap<Integer, HashMap<String, byte[]>>();
        for (Map.Entry<Integer, Map<String, EventReceiver>> pair : tenantSpecificEventAdapters.entrySet()) {
            Map<String, EventReceiver> map = pair.getValue();
            int tenantId = pair.getKey();
            HashMap<String, byte[]> tenantData = new HashMap<String, byte[]>();

            for (Map.Entry<String, EventReceiver> receiverEntry : map.entrySet()) {
                if (receiverEntry.getValue().getInputAdapterRuntime().isEventDuplicatedInCluster()) {
                    byte[] state = receiverEntry.getValue().getInputEventDispatcher().getState();
                    tenantData.put(receiverEntry.getKey(), state);
                }
            }
            data.put(tenantId, tenantData);
        }
        return ByteSerializer.OToB(data);
    }

    @Override
    public void syncState(byte[] bytes) {
        Map<Integer, Map<String, EventReceiver>> tenantSpecificEventAdapters = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
        Map<Integer, HashMap<String, byte[]>> data = new HashMap<Integer, HashMap<String, byte[]>>();
        for (Map.Entry<Integer, HashMap<String, byte[]>> pair : data.entrySet()) {
            Map<String, byte[]> map = pair.getValue();
            int tenantId = pair.getKey();
            for (Map.Entry<String, byte[]> receiverEntry : map.entrySet()) {
                tenantSpecificEventAdapters.get(tenantId).get(receiverEntry.getKey())
                        .getInputEventDispatcher().syncState(receiverEntry.getValue());
            }
        }
    }

    @Override
    public void pause() {
        readWriteLock.writeLock().lock();
    }

    @Override
    public void resume() {
        readWriteLock.writeLock().unlock();
    }

    @Override
    public void start() {
        EventReceiverServiceValueHolder.getCarbonEventReceiverService().startInputAdapterRuntimes();
    }

    @Override
    public void startPolling() {
        EventReceiverServiceValueHolder.getCarbonEventReceiverService().startPollingInputAdapterRuntimes();
    }

    @Override
    public void setOtherMember(HostAndPort otherMember) {
        if ((this.otherMember == null || !this.otherMember.equals(otherMember)) &&
                tcpEventPublisher == null) {
            try {
                tcpEventPublisher = new TCPEventPublisher(otherMember.getHostName() + ":" + otherMember.getPort(),
                        false);
                Map<Integer, Map<String, EventReceiver>> map = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
                for (Map.Entry<Integer, Map<String, EventReceiver>> tenantEntry : map.entrySet()) {
                    int tenantId = tenantEntry.getKey();
                    for (Map.Entry<String, EventReceiver> receiverEntry : tenantEntry.getValue().entrySet()) {
                        String receiverName = receiverEntry.getKey();
                        addToPublisherAndServer(tenantId, receiverName, receiverEntry.getValue().getExportedStreamDefinition());
                    }
                }

            } catch (IOException e) {
                log.error("Error occurred while trying to start the publisher: " + e.getMessage(), e);
            }
        }
        this.otherMember = otherMember;
    }

    public void addToPublisherAndServer(int tenantId, String receiverName, StreamDefinition dataBridgeStreamDefinition) {

        org.wso2.siddhi.query.api.definition.StreamDefinition streamDefinition = new org.wso2.siddhi.query.api.definition.StreamDefinition();
        streamDefinition.setId(tenantId + "/" + receiverName);

        List<Attribute> attributes = new ArrayList<Attribute>();
        if(dataBridgeStreamDefinition.getMetaData() != null) {
            attributes.addAll(dataBridgeStreamDefinition.getMetaData());
        }
        if(dataBridgeStreamDefinition.getCorrelationData() != null) {
            attributes.addAll(dataBridgeStreamDefinition.getCorrelationData());
        }
        if(dataBridgeStreamDefinition.getPayloadData() != null) {
            attributes.addAll(dataBridgeStreamDefinition.getPayloadData());
        }
        for (Attribute attr : attributes) {
            streamDefinition.attribute(attr.getName(), org.wso2.siddhi.query.api.definition.Attribute.Type.valueOf(attr.getType().toString()));
        }

        if (tcpEventPublisher != null) {
            tcpEventPublisher.addStreamDefinition(streamDefinition);
        }
        if (tcpEventServer != null) {
            tcpEventServer.subscribe(streamDefinition);
        }
    }

    public void sendToOther(int tenantId, String eventReceiverName, Object[] data) {
        if (tcpEventPublisher != null) {
            try {
                tcpEventPublisher.sendEvent(tenantId + "/" + eventReceiverName, data, true);
            } catch (Throwable e) {
                log.error(e);
            }
        }
    }

    @Override
    public void startServer(HostAndPort member) {
        if (tcpEventServer == null) {
            TCPEventServerConfig tcpEventServerConfig = new TCPEventServerConfig(member.getPort());
            tcpEventServerConfig.setNumberOfThreads(1);
            tcpEventServer = new TCPEventServer(tcpEventServerConfig, new StreamCallback() {
                @Override
                public void receive(String streamId, Object[] event) {
                    int index = streamId.indexOf("/");
                    String eventReceiverName = streamId.substring(index + 1);
                    if (index != -1) {
                        int tenantId = Integer.parseInt(streamId.substring(0, index));
                        try {
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                            EventReceiver eventReceiver =
                                    EventReceiverServiceValueHolder.getCarbonEventReceiverService().getEventReceiver(tenantId, eventReceiverName);
//                            getReadLock().lock();   //TODo check
//                            getReadLock().unlock();
                            eventReceiver.getInputEventDispatcher().getCallBack().sendEventData(event);

                        } catch (Exception e) {
                            log.error("Unable to start event adpaters for tenant :" + tenantId, e);
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }
                    }
                }
            });
            Map<Integer, Map<String, EventReceiver>> map = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
            for (Map.Entry<Integer, Map<String, EventReceiver>> tenantEntry : map.entrySet()) {
                int tenantId = tenantEntry.getKey();
                for (Map.Entry<String, EventReceiver> receiverEntry : tenantEntry.getValue().entrySet()) {
                    String receiverName = receiverEntry.getKey();
                    addToPublisherAndServer(tenantId, receiverName, receiverEntry.getValue().getExportedStreamDefinition());
                }
            }
            tcpEventServer.start();
            log.info("CEP Management TCPEventServer for EventReceiver started on port " + member.getPort());
        }
    }

    public Lock getReadLock() {
        return readWriteLock.readLock();
    }

    public void shutdown() {
        if (tcpEventPublisher != null) {
            tcpEventPublisher.shutdown();
        }
        if (tcpEventServer != null) {
            tcpEventServer.shutdown();
        }
    }

    @Override
    public ManagerType getType() {
        return ManagerType.Receiver;
    }
}
