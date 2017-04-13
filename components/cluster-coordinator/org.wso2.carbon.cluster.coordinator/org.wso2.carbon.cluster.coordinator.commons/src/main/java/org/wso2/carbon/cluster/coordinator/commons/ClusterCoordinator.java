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

import org.wso2.carbon.cluster.coordinator.commons.configs.CoordinationPropertyNames;
import org.wso2.carbon.cluster.coordinator.commons.configs.CoordinationStrategyConfiguration;
import org.wso2.carbon.cluster.coordinator.commons.exception.ClusterCoordinationException;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;

import java.util.List;
import java.util.Map;

/**
 * The OSGI service class for the coordinator algorithm.
 */
public class ClusterCoordinator {

    /**
     * Creates a new instance of the ClusterCoordinator class.
     */
    private static ClusterCoordinator instance = new ClusterCoordinator();
    private CoordinationStrategy coordinationStrategy;

    private ClusterCoordinator() {
        CoordinationStrategyConfiguration coordinationStrategyConfiguration = CoordinationStrategyConfiguration
                .getInstance();
        ClusterCoordinator.class.getClassLoader();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            if (coordinationStrategyConfiguration.getConfiguration().equals("zookeeper")) {
                this.coordinationStrategy = (CoordinationStrategy) classLoader
                        .loadClass(CoordinationPropertyNames.ZOOKEEPER_COORDINATION_STRATERGY)
                        .newInstance();
            } else {
                this.coordinationStrategy = (CoordinationStrategy) classLoader
                        .loadClass(CoordinationPropertyNames.RDBMS_COORDINATION_STRATERGY)
                        .newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw new ClusterCoordinationException("Error when initializing coordinator strategy",
                    e);
        } catch (InstantiationException e) {
            throw new ClusterCoordinationException("Error when initializing coordinator strategy",
                    e);
        } catch (IllegalAccessException e) {
            throw new ClusterCoordinationException("Error when initializing coordinator strategy",
                    e);
        }
    }

    /**
     * Get the created instance of the current class.
     *
     * @return the created instance of the Clustercoordinator class
     */
    public static ClusterCoordinator getInstance() {
        return instance;
    }

    /**
     * Get the node details of the current cluster group.
     *
     * @param groupId the group Id of the required node details
     * @return the node details of the current group
     */
    public List<NodeDetail> getAllNodeDetails(String groupId) {
        return coordinationStrategy.getAllNodeDetails(groupId);
    }

    /**
     * Get the leader ID of the current group.
     *
     * @param groupId the group Id of current cluster group
     *                return the  leader node ID of the current cluster group
     * @return The leader node object of the current group
     */
    public NodeDetail getLeaderNode(String groupId) {
        return coordinationStrategy.getLeaderNode(groupId);
    }

    /**
     * Register an event listener as an instance of the MemberEventListener class. Therefore the node
     * events can be notified via the listener class.
     *
     * @param groupId             Group ID of the group
     * @param memberEventListener The listener which should listen to the group events
     */
    public void registerEventListener(String groupId, MemberEventListener memberEventListener) {
        this.coordinationStrategy.registerEventListener(memberEventListener);
    }

    /**
     * Join the node with a specific group.
     *
     * @param groupId the group Id of the needed cluster group
     */
    public void joinGroup(String groupId, Map<String, Object> propertiesMap) {
        this.coordinationStrategy.joinGroup(groupId, propertiesMap);
    }
}
