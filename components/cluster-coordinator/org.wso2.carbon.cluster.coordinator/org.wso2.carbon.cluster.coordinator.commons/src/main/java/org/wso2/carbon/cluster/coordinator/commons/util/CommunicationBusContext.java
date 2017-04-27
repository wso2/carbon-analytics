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

package org.wso2.carbon.cluster.coordinator.commons.util;

import org.wso2.carbon.cluster.coordinator.commons.exception.ClusterCoordinationException;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;

import java.util.List;
import java.util.Map;

/**
 * This interface contains required methods for communication bus layer.
 */
public interface CommunicationBusContext {

    /**
     * Try to create the coordinator entry using local node information.
     *
     * @param nodeId  local nodes ID
     * @param groupId local group ID
     * @return True if row was created, false otherwise
     */
    boolean createCoordinatorEntry(String nodeId, String groupId)
            throws ClusterCoordinationException;

    /**
     * Check if the given node is the coordinator
     *
     * @param groupId local group ID
     * @param nodeId  local node ID
     * @return True if the given node is the coordinator, false otherwise
     */
    boolean checkIsCoordinator(String nodeId, String groupId) throws ClusterCoordinationException;

    /**
     * Update coordinator heartbeat value to current time
     *
     * @param groupId local group ID
     * @param nodeId  local node ID
     */
    boolean updateCoordinatorHeartbeat(String nodeId, String groupId)
            throws ClusterCoordinationException;

    /**
     * Check if the coordinator is timed out using the heart beat value
     *
     * @param groupId local group ID
     * @param age     maximum relative age with respect to current time in milliseconds
     * @return True if timed out, False otherwise
     */
    boolean checkIfCoordinatorValid(String groupId, int age) throws ClusterCoordinationException;

    /**
     * Remove current Coordinator entry from database
     *
     * @param groupId local group ID
     */
    void removeCoordinator(String groupId, int age) throws ClusterCoordinationException;

    /**
     * Update Node heartbeat value to current time
     *
     * @param groupId local group ID
     * @param nodeId  local node ID
     */
    boolean updateNodeHeartbeat(String nodeId, String groupId) throws ClusterCoordinationException;

    /**
     * Update Node heartbeat value to current time
     *
     * @param nodeId local node ID
     * @throws ClusterCoordinationException when an error is detected while calling the store (mostly due to a DB error)
     */
    void createNodeHeartbeatEntry(String nodeId, String groupId, Map<String, Object> propertiesMap)
            throws ClusterCoordinationException;

    /**
     * Get node heart beat status for all existing nodes
     *
     * @param groupId local group ID
     * @return list of all the nodes in the group
     * @throws ClusterCoordinationException when an error is detected while calling the store (mostly due to a DB error)
     */
    List<NodeDetail> getAllNodeData(String groupId) throws ClusterCoordinationException;

    /**
     * Remove heartbeat entry for the given node. This is normally done when the coordinator detects that the node
     * has left the
     *
     * @param nodeId  local node ID
     * @param groupId local group ID
     * @throws ClusterCoordinationException when an error is detected while calling the store (mostly due to a DB error)
     */
    void removeNode(String nodeId, String groupId) throws ClusterCoordinationException;

    void insertRemovedNodeDetails(String removedMember, String groupId, List<String> clusterNodes,
            Map<String, Object> removedPropertiesMap) throws ClusterCoordinationException;

    /**
     * Use this method to indicate that the coordinator detected the node addition to cluster.
     *
     * @param groupId local group ID
     * @param nodeId  local node ID
     * @throws ClusterCoordinationException when an error is detected while calling the store (mostly due to a DB error)
     */
    void markNodeAsNotNew(String nodeId, String groupId) throws ClusterCoordinationException;

    /**
     * Get current coordinator's node ID
     *
     * @param groupId local group ID
     * @return node ID of the current coordinator
     * @throws ClusterCoordinationException when an error is detected while calling the store (mostly due to a DB error)
     */
    String getCoordinatorNodeId(String groupId) throws ClusterCoordinationException;

    /**
     * Clear all heartbeat data present in the database. This is normally done when the cluster is restarted
     *
     * @throws ClusterCoordinationException when an error is detected while calling the store (mostly due to a DB error)
     */
    void clearHeartBeatData() throws ClusterCoordinationException;

    /*
     * ============================ Membership related methods =======================================
     */

    /**
     * Method to store cluster membership based event.
     *
     * @param clusterNodes        nodes by which the event is destined to be read
     * @param membershipEventType the membership change type
     * @param changedMember       member for which the event was triggered
     * @param groupId             local group ID
     */
    void storeMembershipEvent(String changedMember, String groupId, List<String> clusterNodes,
            int membershipEventType) throws ClusterCoordinationException;

    /**
     * Method to read cluster membership changed events for a nodeID.
     *
     * @param nodeID local node ID used to read event for current node
     */
    List<MemberEvent> readMemberShipEvents(String nodeID) throws ClusterCoordinationException;

    /**
     * Method to remove all membership events from the store for a particular node.
     *
     * @param groupID local group ID
     */
    void clearMembershipEvents(String nodeID, String groupID) throws ClusterCoordinationException;

    /**
     * Get the nodedetails of the specified node.
     *
     * @param nodeId  local nodeId
     * @param groupId local groupId
     * @return nodedetail object of the node.
     * @throws ClusterCoordinationException
     */
    NodeDetail getNodeData(String nodeId, String groupId) throws ClusterCoordinationException;

    /**
     * Get the node details of the removed node
     *
     * @param nodeId        local nodeId
     * @param groupId       local groupId
     * @param removedNodeId removed node Id
     * @return node detail object of the removed node
     * @throws ClusterCoordinationException
     */
    NodeDetail getRemovedNodeData(String nodeId, String groupId, String removedNodeId)
            throws ClusterCoordinationException;

}
