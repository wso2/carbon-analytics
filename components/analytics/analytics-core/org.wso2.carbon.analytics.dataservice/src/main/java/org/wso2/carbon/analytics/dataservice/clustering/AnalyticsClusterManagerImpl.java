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
package org.wso2.carbon.analytics.dataservice.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

/**
 * This class represents an {@link AnalyticsClusterManager} implementation.
 */
public class AnalyticsClusterManagerImpl implements AnalyticsClusterManager, MembershipListener {

    private static final String LEADER_INIT_DONE_FLAG = "LEADER_INIT_FLAG";

    private static final String ANALYTICS_GROUP_EXECUTOR_SERVICE_PREFIX = "_ANALYTICS_GROUP_EXECUTOR_SERVICE_";

    private static final String ANALYTICS_CLUSTER_GROUP_MEMBERS_PREFIX = "_ANALYTICS_CLUSTER_GROUP_MEMBERS_";
    
    private static final String ANALYTICS_CLUSTER_GROUP_DATA_PREFIX = "_ANALYTICS_CLUSTER_GROUP_DATA_";
    
    private static final Log log = LogFactory.getLog(AnalyticsClusterManagerImpl.class);
        
    private HazelcastInstance hz;
    
    private Map<String, GroupEventListener> groups = new HashMap<String, GroupEventListener>();
    
    private Map<String, Member> leaders = new HashMap<String, Member>();
        
    public AnalyticsClusterManagerImpl() {
        this.hz = AnalyticsServiceHolder.getHazelcastInstance();
        if (this.isClusteringEnabled()) {
            this.hz.getCluster().addMembershipListener(this);
        }
    }

    @Override
    public void joinGroup(String groupId, GroupEventListener groupEventListener) throws AnalyticsClusterException {
        if (!this.isClusteringEnabled()) {
            throw new AnalyticsClusterException("Clustering is not enabled");
        }
        if (this.groups.containsKey(groupId)) {
            throw new AnalyticsClusterException("This node has already joined the group: " + groupId);
        }
        this.checkAndCleanupGroups(groupId);
        this.groups.put(groupId, groupEventListener);
        List<Member> groupMembers = this.getGroupMembers(groupId);
        Member myself = this.hz.getCluster().getLocalMember();
        groupMembers.add(myself);
        if (this.checkLeader(myself, groupId)) {
            this.leaders.put(groupId, myself);
            if (groupEventListener != null) {
                groupEventListener.onBecomingLeader();
            }
            this.setLeaderInitDoneFlag(groupId);
            if (groupEventListener != null) {
                groupEventListener.onLeaderUpdate();
            }
        } else {
            this.waitForInitialLeader(groupId);
            if (groupEventListener != null) {
                groupEventListener.onLeaderUpdate();
            }
            this.sendMemberAddedNotificationToLeader(groupId);
        }
    }
    
    private boolean checkLeader(Member member, String groupId) {
        return member.equals(this.getLeader(groupId));
    }
    
    private String generateLeaderInitDoneFlagName(String groupId) {
        return groupId + "_" + LEADER_INIT_DONE_FLAG;
    }
    
    private void setLeaderInitDoneFlag(String groupId) {
        this.hz.getAtomicLong(this.generateLeaderInitDoneFlagName(groupId)).set(1);
    }
    
    private void resetLeaderInitDoneFlag(String groupId) {
        this.hz.getAtomicLong(this.generateLeaderInitDoneFlagName(groupId)).set(0);
    }
    
    private void waitForInitialLeader(String groupId) {
        if (log.isDebugEnabled()) {
            log.debug("Waiting for initial leader...");
        }
        while (this.hz.getAtomicLong(this.generateLeaderInitDoneFlagName(groupId)).get() <= 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Done waiting for initial leader.");
        }
    }
    
    private void sendMemberAddedNotificationToLeader(String groupId) throws AnalyticsClusterException {
        Member member = this.getLeader(groupId);
        this.executeOne(groupId, member, new LeaderMemberAddedNotification(groupId));
    }
    
    @Override
    public Member getLeader(String groupId) {
        return this.getGroupMembers(groupId).get(0);
    }
    
    @Override
    public boolean isLeader(String groupId) {
        return this.hz.getCluster().getLocalMember().equals(this.leaders.get(groupId));
    }
    
    private void executeMyselfBecomingLeader(String groupId) throws AnalyticsClusterException {
        this.leaders.put(groupId, this.hz.getCluster().getLocalMember());
        this.groups.get(groupId).onBecomingLeader();
        this.executeAll(groupId, new LeaderUpdateNotification(groupId));
    }
    
    private String generateGroupListId(String groupId) {
        return ANALYTICS_CLUSTER_GROUP_MEMBERS_PREFIX + groupId;
    }
    
    private String generateGroupExecutorId(String groupId) {
        return ANALYTICS_GROUP_EXECUTOR_SERVICE_PREFIX + groupId;
    }
    
    private String generateGroupDataMapId(String groupId) {
        return ANALYTICS_CLUSTER_GROUP_DATA_PREFIX + groupId;
    }
    
    private List<Member> getGroupMembers(String groupId) {
        return this.hz.getList(this.generateGroupListId(groupId));
    }
    
    /**
     * This method checks the current active members to see if there any members left in the member list,
     * that is actually not in the cluster and clean it up. This can happen, when the last member of the group
     * also goes away, but the distributed memory is retained from other nodes in the cluster.
     * @param groupId The group id
     */
    private void checkAndCleanupGroups(String groupId) {
        List<Member> groupMembers = this.getGroupMembers(groupId);
        Set<Member> existingMembers = this.hz.getCluster().getMembers();
        Iterator<Member> memberItr = groupMembers.iterator();
        while (memberItr.hasNext()) {
            if (!existingMembers.contains(memberItr.next())) {
                memberItr.remove();
            }
        }
        if (this.getGroupMembers(groupId).size() == 0) {
            this.resetLeaderInitDoneFlag(groupId); 
        }
    }
    
    @Override
    public List<Object> getMembers(String groupId) throws AnalyticsClusterException {
        return new ArrayList<Object>(this.getGroupMembers(groupId));
    }

    @Override
    public <T> T executeOne(String groupId, Object member, Callable<T> callable) throws AnalyticsClusterException {
        Future<T> result = this.hz.getExecutorService(
                this.generateGroupExecutorId(groupId)).submitToMember(callable, (Member) member);
        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AnalyticsClusterException("Error in cluster execute one: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> List<T> executeAll(String groupId, Callable<T> callable) throws AnalyticsClusterException {
        if (!this.groups.containsKey(groupId)) {
            throw new AnalyticsClusterException("The node is required to join the group (" + 
                    groupId + ") before sending cluster messages");
        }
        List<Member> members = this.getGroupMembers(groupId);
        List<T> result = new ArrayList<T>();
        Map<Member, Future<T>> executionResult = this.hz.getExecutorService(
                this.generateGroupExecutorId(groupId)).submitToMembers(callable, members);
        for (Map.Entry<Member, Future<T>> entry : executionResult.entrySet()) {
            try {
                result.add(entry.getValue().get());
            } catch (InterruptedException | ExecutionException e) {
                throw new AnalyticsClusterException("Error in cluster execute all: " + e.getMessage(), e);
            }
        }
        return result;
    }
    
    @Override
    public void setProperty(String groupId, String name, Serializable value) {
        Map<String, Serializable> data = this.hz.getMap(this.generateGroupDataMapId(groupId));
        data.put(name, value);
    }

    @Override
    public Serializable getProperty(String groupId, String name) {
        Map<String, Serializable> data = this.hz.getMap(this.generateGroupDataMapId(groupId));
        return data.get(name);
    }

    @Override
    public boolean isClusteringEnabled() {
        return this.hz != null;
    }

    @Override
    public void memberAdded(MembershipEvent event) {
        /* nothing to do */
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent event) {
        /* nothing to do */
    }

    private void checkGroupMemberRemoval(String groupId, Member member) throws AnalyticsClusterException {
        List<Member> groupMembers = this.getGroupMembers(groupId);
        if (groupMembers.contains(member)) {
            groupMembers.remove(member);
        }
        if (this.isLeader(groupId)) {
            /* if I'm already the leader, notify of the membership change */
            this.groups.get(groupId).onMembersChangeForLeader();
        } else if (this.checkLeader(this.hz.getCluster().getLocalMember(), groupId)) {
            /* check if I'm already not the leader, and if I just became the leader */
            this.executeMyselfBecomingLeader(groupId);
        }
    }
        
    private void leaderUpdateNotificationReceived(String groupId) {
        GroupEventListener listener = this.groups.get(groupId);
        if (listener != null) {
            listener.onLeaderUpdate();
        }
    }
    
    private void leaderMemberAdditionNotificationReceived(String groupId) {
        GroupEventListener listener = this.groups.get(groupId);
        if (listener != null) {
            listener.onMembersChangeForLeader();
        }
    }
    
    @Override
    public void memberRemoved(MembershipEvent event) {
        Set<String> groupIds = this.groups.keySet();
        for (String groupId : groupIds) {
            try {
                this.checkGroupMemberRemoval(groupId, event.getMember());
            } catch (AnalyticsClusterException e) {
                log.error("Error in member removal: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * This class represents the cluster message that notifies the cluster of a new member to the group.
     */
    public static class LeaderMemberAddedNotification implements Callable<String>, Serializable {
        
        private static final long serialVersionUID = -3363760290841109792L;
        
        private String groupId;
                
        public LeaderMemberAddedNotification(String groupId) {
            this.groupId = groupId;
        }
        
        public String getGroupId() {
            return groupId;
        }

        @Override
        public String call() throws Exception {
            AnalyticsClusterManager cm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            if (cm instanceof AnalyticsClusterManagerImpl) {
                AnalyticsClusterManagerImpl cmImpl = (AnalyticsClusterManagerImpl) cm;
                cmImpl.leaderMemberAdditionNotificationReceived(this.getGroupId());
            }
            return "OK";
        }
        
    }
    
    /**
     * This class represents the cluster message that notifies the cluster of a new leader.
     */
    public static class LeaderUpdateNotification implements Callable<String>, Serializable {

        private static final long serialVersionUID = -8378187556136928045L;
        
        private String groupId;
                
        public LeaderUpdateNotification(String groupId) {
            this.groupId = groupId;
        }
        
        public String getGroupId() {
            return groupId;
        }
        
        @Override
        public String call() throws Exception {
            AnalyticsClusterManager cm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            if (cm instanceof AnalyticsClusterManagerImpl) {
                AnalyticsClusterManagerImpl cmImpl = (AnalyticsClusterManagerImpl) cm;
                cmImpl.leaderUpdateNotificationReceived(this.getGroupId());
            }
            return "OK";
        }

    }
    
}
