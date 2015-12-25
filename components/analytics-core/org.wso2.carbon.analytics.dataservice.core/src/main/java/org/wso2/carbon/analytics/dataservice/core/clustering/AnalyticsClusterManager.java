/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.dataservice.core.clustering;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * This interface represents the analytics clustering related operations.
 */
public interface AnalyticsClusterManager {

    /**
     * Joins a given group.
     * @param groupId The group id of the group
     * @param groupEventListener The listener class to listen for group events, this can be null, if you 
     * do not want to receive group events
     * @throws AnalyticsClusterException
     */
    void joinGroup(String groupId, GroupEventListener groupEventListener) throws AnalyticsClusterException;
    
    /**
     * Checks if the current node is the leader of the given node.
     * @param groupId The group id of the group
     * @return True if the current node is the leader
     */
    boolean isLeader(String groupId);
    
    /**
     * Returns the leader member of the given group.
     * @param groupId The group id of the group
     * @return The member object of the leaders
     * @throws AnalyticsClusterException
     */
    Object getLeader(String groupId);
    
    /**
     * Returns a list of members of the given group. The member object returned from this can be 
     * used in other methods which expects this member object as a parameter, e.g. 'executeOne'
     * @param groupId The group id of the group
     * @return The list of object representing each member of the group.
     * @throws AnalyticsClusterException
     */
    List<Object> getMembers(String groupId) throws AnalyticsClusterException;
    
    /**
     * Executes the given {@link Callable} in all the nodes in the group, including the current one.
     * @param groupId The group id of the group
     * @param callable The implementation to be executed
     * @return The aggregation of results from all the nodes
     * @throws AnalyticsClusterException
     */
    <T> List<T> executeAll(String groupId, Callable<T> callable) throws AnalyticsClusterException;
    
    /**
     * Executes the given {@link Callable} in the given member.
     * @param groupId The group id of the group 
     * @param member The target member, must be retrieved from a call such as 'getMembers'
     * @param callable The implementation to be executed
     * @return The result from the target member
     * @throws AnalyticsClusterException
     */
    <T> T executeOne(String groupId, Object member, Callable<T> callable) throws AnalyticsClusterException;
    
    /**
     * Executes the given {@link Callable} in the given member asynchronously.
     * @param groupId The group id of the group 
     * @param member The target member, must be retrieved from a call such as 'getMembers'
     * @param callable The implementation to be executed
     * @return The result from the target member as a {@link Future} object
     * @throws AnalyticsClusterException
     */
    <T> Future<T> executeOneFuture(String groupId, Object member, Callable<T> callable) throws AnalyticsClusterException;

    /**
     * Sets a property local to the given group.
     * @param groupId The group id of the group
     * @param name The name of the property
     * @param value The property value
     */
    void setProperty(String groupId, String name, Serializable value);
    
    /**
     * Retrieves a property local to a given group.
     * @param groupId The group id of the group
     * @param name THe name of the property
     * @return The property value
     */
    Serializable getProperty(String groupId, String name);
    
    /**
     * Checks if the server is in clustered mode.
     * @return True if in clustered mode
     */
    boolean isClusteringEnabled();

    Object getLocalMember();
    
}
