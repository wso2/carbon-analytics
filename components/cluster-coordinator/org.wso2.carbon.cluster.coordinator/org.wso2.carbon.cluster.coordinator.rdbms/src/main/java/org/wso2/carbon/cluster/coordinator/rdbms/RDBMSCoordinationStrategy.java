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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cluster.coordinator.commons.CoordinationStrategy;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.configs.CoordinationPropertyNames;
import org.wso2.carbon.cluster.coordinator.commons.configs.CoordinationStrategyConfiguration;
import org.wso2.carbon.cluster.coordinator.commons.exception.ClusterCoordinationException;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.commons.util.MemberEventType;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * This class controls the overall process of RDBMS coordination.
 */
public class RDBMSCoordinationStrategy implements CoordinationStrategy {

    /**
     * Heartbeat interval in seconds.
     */
    private final int heartBeatInterval;
    /**
     * After this much of time the node is assumed to have left the cluster.
     */
    private final int heartbeatMaxAge;
    /**
     * Thread executor used to run the coordination algorithm.
     */
    private final ScheduledExecutorService threadExecutor;
    /**
     * Class logger.
     */
    private Log logger = LogFactory.getLog(RDBMSCoordinationStrategy.class);
    /**
     * Used to send and receive cluster notifications.
     */
    private RDBMSMemberEventProcessor rdbmsMemberEventProcessor;
    /**
     * Used to communicate with the communication bus context.
     */
    private RDBMSCommunicationBusContextImpl communicationBusContext;

    private String localNodeId;

    public RDBMSCoordinationStrategy() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("RDBMSCoordinationStrategy-%d").build();
        this.threadExecutor = Executors.newScheduledThreadPool(
                CoordinationStrategyConfiguration.getInstance().getRdbmsConfigs()
                        .get(CoordinationPropertyNames.RDBMS_BASED_PERFORM_TASK_THREAD_COUNT),
                namedThreadFactory);
        this.heartBeatInterval = CoordinationStrategyConfiguration.getInstance().getRdbmsConfigs()
                .get(CoordinationPropertyNames.RDBMS_BASED_COORDINATION_HEARTBEAT_INTERVAL);
        // Maximum age of a heartbeat. After this much of time, the heartbeat is considered invalid and node is
        // considered to have left the cluster.
        this.heartbeatMaxAge = heartBeatInterval * 2;
        this.localNodeId = generateRandomId();
        this.rdbmsMemberEventProcessor = new RDBMSMemberEventProcessor(localNodeId);
        this.communicationBusContext = new RDBMSCommunicationBusContextImpl();
    }

    public RDBMSCoordinationStrategy(DataSource datasource) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("RDBMSCoordinationStrategy-%d").build();
        this.threadExecutor = Executors.newScheduledThreadPool(
                CoordinationStrategyConfiguration.getInstance().getRdbmsConfigs()
                        .get("heartbeatInterval"), namedThreadFactory);
        this.heartBeatInterval = CoordinationStrategyConfiguration.getInstance().getRdbmsConfigs()
                .get(CoordinationPropertyNames.RDBMS_BASED_COORDINATION_HEARTBEAT_INTERVAL);
        // Maximum age of a heartbeat. After this much of time, the heartbeat is considered invalid and node is
        // considered to have left the cluster.
        this.heartbeatMaxAge = heartBeatInterval * 2;
        this.localNodeId = generateRandomId();
        this.rdbmsMemberEventProcessor = new RDBMSMemberEventProcessor(localNodeId, datasource);
        this.communicationBusContext = new RDBMSCommunicationBusContextImpl(datasource);
    }

    @Override public List<NodeDetail> getAllNodeDetails(String groupId)
            throws ClusterCoordinationException {
        return communicationBusContext.getAllNodeData(groupId);
    }

    @Override public NodeDetail getLeaderNode(String groupID) {
        List<NodeDetail> nodeDetails = communicationBusContext.getAllNodeData(groupID);
        for (NodeDetail nodeDetail : nodeDetails) {
            if (nodeDetail.isCoordinator()) {
                return nodeDetail;
            }
        }
        return null;
    }

    @Override public void joinGroup(String groupId, Map<String, Object> propertiesMap) {
        CoordinatorElectionTask coordinatorElectionTask = new CoordinatorElectionTask(localNodeId,
                groupId, propertiesMap);
        threadExecutor.scheduleWithFixedDelay(coordinatorElectionTask, heartBeatInterval,
                heartBeatInterval, TimeUnit.MILLISECONDS);
        //clear old membership events for the node
        communicationBusContext.clearMembershipEvents(localNodeId, groupId);
    }

    public String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    @Override public void registerEventListener(MemberEventListener memberEventListener) {
        // Register listener for membership changes
        rdbmsMemberEventProcessor.addEventListener(memberEventListener);
    }

    public void stop() {
        this.threadExecutor.shutdown();
        this.rdbmsMemberEventProcessor.stop();
    }

    /**
     * Possible node states
     * <p>
     * +----------+
     * +-------->+ Election +<---------+
     * |         +----------+          |
     * |            |    |             |
     * |            |    |             |
     * +-----------+   |    |   +-------------+
     * | Candidate +<--+    +-->+ Coordinator |
     * +-----------+            +-------------+
     */
    private enum NodeState {
        COORDINATOR, MEMBER
    }

    /**
     * For each member, this class will run in a separate thread.
     */
    private class CoordinatorElectionTask implements Runnable {

        /**
         * Current state of the node.
         */
        private NodeState currentNodeState;
        /**
         * Used to uniquely identify a node in the cluster.
         */
        private String localNodeId;

        /**
         * Used to uniquely identify the group ID in the cluster.
         */
        private String localGroupId;
        /**
         * The unique property map of the node.
         */
        private Map<String, Object> localpropertiesMap;

        /**
         * Constructor.
         *
         * @param nodeId  node ID of the current node
         * @param groupId group ID of the current group
         */
        private CoordinatorElectionTask(String nodeId, String groupId,
                Map<String, Object> propertiesMap) {
            this.localGroupId = groupId;
            this.localNodeId = nodeId;
            this.localpropertiesMap = propertiesMap;
            this.currentNodeState = NodeState.MEMBER;
            try {
                communicationBusContext.clearMembershipEvents(nodeId, groupId);
            } catch (ClusterCoordinationException e) {
                logger.warn("Error while clearing old membership events for local node (" + nodeId
                        + ") and group (" + groupId + ")", e);
            }
        }

        @Override public void run() {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Current node state: " + currentNodeState);
                }
                switch (currentNodeState) {
                case MEMBER:
                    performMemberTask();
                    break;
                case COORDINATOR:
                    performCoordinatorTask();
                    break;
                }
            } catch (Throwable e) {
                logger.error("Error detected while running coordination algorithm. Node became a "
                        + NodeState.MEMBER + " node in group " + localGroupId, e);
                currentNodeState = NodeState.MEMBER;
            }
        }

        /**
         * Perform periodic task that should be done by a MEMBER node.
         *
         * @return next NodeStatus
         * @throws ClusterCoordinationException
         * @throws InterruptedException
         */
        private void performMemberTask() throws ClusterCoordinationException, InterruptedException {
            updateNodeHeartBeat();
            boolean coordinatorValid = communicationBusContext
                    .checkIfCoordinatorValid(localGroupId, heartbeatMaxAge);
            if (!coordinatorValid) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Node ID :" + localNodeId
                            + " Going for election since the Coordinator is invalid for group ID: "
                            + localGroupId);
                }
                communicationBusContext.removeCoordinator(localGroupId, heartbeatMaxAge);
                performElectionTask();
            }
        }

        /**
         * Try to update the heart beat entry for local node in the DB. If the entry is deleted by the coordinator,
         * this will recreate the entry.
         *
         * @throws ClusterCoordinationException
         */
        private void updateNodeHeartBeat() throws ClusterCoordinationException {
            boolean heartbeatEntryExists = communicationBusContext
                    .updateNodeHeartbeat(localNodeId, localGroupId);
            if (!heartbeatEntryExists) {
                communicationBusContext
                        .createNodeHeartbeatEntry(localNodeId, localGroupId, localpropertiesMap);
            }
        }

        /**
         * Perform periodic task that should be done by a Coordinating node.
         *
         * @return next NodeState
         * @throws ClusterCoordinationException
         * @throws InterruptedException
         */
        private void performCoordinatorTask()
                throws ClusterCoordinationException, InterruptedException {
            // Try to update the coordinator heartbeat
            boolean stillCoordinator = communicationBusContext
                    .updateCoordinatorHeartbeat(localNodeId, localGroupId);
            if (stillCoordinator) {
                updateNodeHeartBeat();
                long currentTimeMillis = System.currentTimeMillis();
                List<NodeDetail> allNodeInformation = communicationBusContext
                        .getAllNodeData(localGroupId);
                findAddedRemovedMembers(allNodeInformation, currentTimeMillis);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Going for election since Coordinator state is lost in group"
                            + localGroupId);
                }
                performElectionTask();
            }
        }

        /**
         * Finds the newly added and removed nodes to the group.
         *
         * @param allNodeInformation all the node information of the group
         * @param currentTimeMillis  current timestamp
         */
        private void findAddedRemovedMembers(List<NodeDetail> allNodeInformation,
                long currentTimeMillis) {
            List<String> allActiveNodeIds = getNodeIds(allNodeInformation);
            List<NodeDetail> removedNodeDetails = new ArrayList<>();
            List<String> newNodes = new ArrayList<String>();
            List<String> removedNodes = new ArrayList<String>();
            for (NodeDetail nodeDetail : allNodeInformation) {
                long heartbeatAge = currentTimeMillis - nodeDetail.getLastHeartbeat();
                String nodeId = nodeDetail.getNodeId();
                if (heartbeatAge >= heartbeatMaxAge) {
                    removedNodes.add(nodeId);
                    allActiveNodeIds.remove(nodeId);
                    removedNodeDetails.add(nodeDetail);
                    communicationBusContext.removeNode(nodeId, localGroupId);
                } else if (nodeDetail.isNewNode()) {
                    newNodes.add(nodeId);
                    communicationBusContext.markNodeAsNotNew(nodeId, localGroupId);
                }
            }
            notifyAddedMembers(newNodes, allActiveNodeIds);
            notifyRemovedMembers(removedNodes, allActiveNodeIds, removedNodeDetails);
        }

        /**
         * Notifies the members in the group about the newly added nodes.
         *
         * @param newNodes         The list of newly added members to the group
         * @param allActiveNodeIds all the node IDs of the current group
         */
        private void notifyAddedMembers(List<String> newNodes, List<String> allActiveNodeIds) {
            for (String newNode : newNodes) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Member added " + newNode + "to group " + localGroupId);
                }
                rdbmsMemberEventProcessor
                        .notifyMembershipEvent(newNode, localGroupId, allActiveNodeIds,
                                MemberEventType.MEMBER_ADDED);
            }
        }

        /**
         * Stores the removed member detail in the database.
         *
         * @param allActiveNodeIds   all the node IDs of the current group
         * @param removedNodeDetails node details of the removed nodes
         */
        private void storeRemovedMemberDetails(List<String> allActiveNodeIds,
                List<NodeDetail> removedNodeDetails) {
            for (NodeDetail nodeDetail : removedNodeDetails) {
                communicationBusContext
                        .insertRemovedNodeDetails(nodeDetail.getNodeId(), nodeDetail.getGroupId(),
                                allActiveNodeIds, nodeDetail.getpropertiesMap());
            }
        }

        /**
         * Notifies the members in the group about the removed nodes from the group.
         *
         * @param removedNodes     The list of removed membwes from the group
         * @param allActiveNodeIds all the node IDs of the current group
         */
        private void notifyRemovedMembers(List<String> removedNodes, List<String> allActiveNodeIds,
                List<NodeDetail> removedNodeDetails) {
            storeRemovedMemberDetails(allActiveNodeIds, removedNodeDetails);
            for (String removedNode : removedNodes) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Member removed " + removedNode + "from group " + localGroupId);
                }
                rdbmsMemberEventProcessor
                        .notifyMembershipEvent(removedNode, localGroupId, allActiveNodeIds,
                                MemberEventType.MEMBER_REMOVED);
            }
        }

        /**
         * Perform new coordinator election task.
         *
         * @return next NodeState
         * @throws InterruptedException
         */
        private void performElectionTask() throws InterruptedException {
            try {
                this.currentNodeState = tryToElectSelfAsCoordinator();
            } catch (ClusterCoordinationException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Current node became a " + NodeState.MEMBER + " node in group "
                            + localGroupId, e);
                }
                this.currentNodeState = NodeState.MEMBER;
            }
        }

        /**
         * Try to elect local node as the coordinator by creating the coordinator entry.
         *
         * @return next NodeState
         * @throws ClusterCoordinationException
         * @throws InterruptedException
         */
        private NodeState tryToElectSelfAsCoordinator()
                throws ClusterCoordinationException, InterruptedException {
            NodeState nextState;
            boolean electedAsCoordinator = communicationBusContext
                    .createCoordinatorEntry(localNodeId, localGroupId);
            if (electedAsCoordinator) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Elected current node as the coordinator in group " + localGroupId);
                }
                nextState = NodeState.COORDINATOR;
                // notify nodes about coordinator change
                rdbmsMemberEventProcessor
                        .notifyMembershipEvent(localNodeId, localGroupId, getAllNodeIdentifiers(),
                                MemberEventType.COORDINATOR_CHANGED);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Election resulted in current node becoming a " + NodeState.MEMBER
                            + " node in group " + localGroupId);
                }
                nextState = NodeState.MEMBER;
            }
            return nextState;
        }

        /**
         * Get the node IDs of the current group.
         *
         * @return node IDs of the current group
         * @throws ClusterCoordinationException
         */
        public List<String> getAllNodeIdentifiers() throws ClusterCoordinationException {
            List<NodeDetail> allNodeInformation = communicationBusContext
                    .getAllNodeData(localGroupId);
            return getNodeIds(allNodeInformation);
        }

        /**
         * Return a list of node ids from the heartbeat data list.
         *
         * @param allHeartbeatData list of heartbeat data
         * @return list of node IDs
         */
        private List<String> getNodeIds(List<NodeDetail> allHeartbeatData) {
            List<String> allNodeIds = new ArrayList<String>(allHeartbeatData.size());
            for (NodeDetail nodeDetail : allHeartbeatData) {
                allNodeIds.add(nodeDetail.getNodeId());
            }
            return allNodeIds;
        }
    }
}
