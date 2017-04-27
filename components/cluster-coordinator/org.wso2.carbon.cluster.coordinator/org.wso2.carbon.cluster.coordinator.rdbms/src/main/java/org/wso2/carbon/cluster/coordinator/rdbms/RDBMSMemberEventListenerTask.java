/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cluster.coordinator.rdbms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.exception.ClusterCoordinationException;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.commons.util.MemberEvent;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * The task that runs periodically to detect membership change events.
 */
class RDBMSMemberEventListenerTask implements Runnable {

    /**
     * Class logger.
     */
    private static final Log logger = LogFactory.getLog(RDBMSMemberEventListenerTask.class);
    /**
     * Node id of the node for which the reader reads member changes.
     */
    public String nodeID;
    /**
     * Communication bus object to communicate with the database for the context store.
     */
    private RDBMSCommunicationBusContextImpl communicationBusContext;
    /**
     * List used to hold all the registered subscribers.
     */
    private List<MemberEventListener> listeners;

    /**
     * Default Constructor.
     *
     * @param nodeId Local node ID used to uniquely identify the node within cluster
     */
    RDBMSMemberEventListenerTask(String nodeId) {
        this.nodeID = nodeId;
        this.listeners = new ArrayList<MemberEventListener>();
        this.communicationBusContext = new RDBMSCommunicationBusContextImpl();
    }

    RDBMSMemberEventListenerTask(String nodeId, DataSource dataSource) {
        this.nodeID = nodeId;
        this.listeners = new ArrayList<MemberEventListener>();
        this.communicationBusContext = new RDBMSCommunicationBusContextImpl(dataSource);
    }

    /**
     * The task that is periodically run to read membership events and to notify the listeners.
     */
    @Override public void run() {
        try {
            List<MemberEvent> membershipEvents = readMembershipEvents();
            if (!membershipEvents.isEmpty()) {
                for (MemberEvent event : membershipEvents) {
                    switch (event.getMembershipEventType()) {
                    case MEMBER_ADDED:
                        //todo pass nodedetail object
                        notifyMemberAdditionEvent(event.getTargetNodeId(),
                                event.getTargetGroupId());
                        break;
                    case MEMBER_REMOVED:
                        notifyMemberRemovalEvent(event.getTargetNodeId(), event.getTargetGroupId());
                        break;
                    case COORDINATOR_CHANGED:
                        notifyCoordinatorChangeEvent(event.getTargetNodeId(),
                                event.getTargetGroupId());
                        break;
                    default:
                        logger.error(
                                "Unknown cluster event type: " + event.getMembershipEventType());
                        break;
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No membership events to sync");
                }
            }
        } catch (Throwable e) {
            logger.warn("Error occurred while reading membership events.", e);
        }
    }

    /**
     * Notifies the coordinator change event to the registered listeners.
     *
     * @param member The node ID of the event occured
     */
    private void notifyCoordinatorChangeEvent(String member, String groupId) {
        for (MemberEventListener listener : listeners) {
            if (listener.getGroupId().equals(groupId)) {
                NodeDetail nodeDetail = communicationBusContext.getNodeData(member, groupId);
                if (nodeDetail != null) {
                    listener.coordinatorChanged(nodeDetail);
                }
            }
        }
    }

    /**
     * Notifies the member removal  event to the registered listeners.
     *
     * @param member The node ID of the event occured
     */
    private void notifyMemberRemovalEvent(String member, String groupId) {
        for (MemberEventListener listener : listeners) {
            if (listener.getGroupId().equals(groupId)) {
                NodeDetail nodeDetail = communicationBusContext
                        .getRemovedNodeData(nodeID, groupId, member);
                if (nodeDetail != null) {
                    listener.memberRemoved(nodeDetail);
                }
            }
        }
    }

    /**
     * Notifies the member added  event to the registered listeners.
     *
     * @param member The node ID of the event occured
     */
    private void notifyMemberAdditionEvent(String member, String groupId) {
        for (MemberEventListener listener : listeners) {
            if (listener.getGroupId().equals(groupId)) {
                NodeDetail nodeDetail = communicationBusContext.getNodeData(member, groupId);
                if (nodeDetail != null) {
                    listener.memberAdded(nodeDetail);
                }
            }
        }
    }

    /**
     * Method to read membership events.
     * <p>This will read all membership events that a are recorded for a particular node and clear all of those once
     * read.
     *
     * @return list membership events
     * @throws ClusterCoordinationException
     */
    private List<MemberEvent> readMembershipEvents() throws ClusterCoordinationException {
        return communicationBusContext.readMemberShipEvents(nodeID);
    }

    /**
     * Add a listener to be notified of the cluster membership events.
     *
     * @param membershipListener membership listener object
     */
    public void addEventListener(MemberEventListener membershipListener) {
        listeners.add(membershipListener);
    }

    /**
     * Remove a previously added listener.
     *
     * @param membershipListener membership listener object
     */
    public void removeEventListener(MemberEventListener membershipListener) {
        listeners.remove(membershipListener);
    }
}

