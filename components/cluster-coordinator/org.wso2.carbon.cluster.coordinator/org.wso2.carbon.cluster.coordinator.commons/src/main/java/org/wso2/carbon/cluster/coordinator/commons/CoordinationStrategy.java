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

package org.wso2.carbon.cluster.coordinator.commons;

import org.wso2.carbon.cluster.coordinator.commons.exception.ClusterCoordinationException;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;

import java.util.List;
import java.util.Map;

/**
 * This interface is common for all the coordination strategies. All the coordination strategy classes
 * should implement this interface.
 */
public interface CoordinationStrategy {

    /**
     * Get the node details of the current cluster group.
     *
     * @param groupId the group Id of the required node details
     * @return the node details of the current group
     */
    List<NodeDetail> getAllNodeDetails(String groupId) throws ClusterCoordinationException;

    /**
     * Get the leader ID of the current cluster group.
     *
     * @param groupId the group Id of current cluster group
     * @return the  leader node ID of the current cluster group
     */
    NodeDetail getLeaderNode(String groupId);

    /**
     * Register an event listener as an instance of the MemberEventListener class. Therefore the node
     * events can be notified via the listener class.
     *
     * @param memberEventListener The class instance for listening member events
     */
    void registerEventListener(MemberEventListener memberEventListener);

    /**
     * Join the node with a specific group.
     *
     * @param groupId the group Id of the needed cluster group
     */
    void joinGroup(String groupId, Map<String, Object> propertiesMap);
}

