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

/**
 * This interface represents the listener for analytics cluster group related events.
 */
public interface GroupEventListener {

    /**
     * Called when the current node becomes the leader of the given group.
     */
    void onBecomingLeader();
    
    /**
     * Called when the group leadership updates, this is called even when the new leader is the current node,
     * and this call is made after the leader's onBecomingLeader has fully executed, so the leader can do any
     * initializations before telling the cluster that he is the leader.
     */
    void onLeaderUpdate();
    
    /**
     * Called when the group members change by either a new member arriving or departing. This is executed
     * only for the group leader. This is not called in the instance of myself becoming the leader, because
     * of another node has departed.
     *
     * @param removed True if the change was a member being removed, false if a new member arrives
     */
    void onMembersChangeForLeader(boolean removed);
    
    /**
     * Called when any member has left the group.
     */
    void onMemberRemoved();
    
}
