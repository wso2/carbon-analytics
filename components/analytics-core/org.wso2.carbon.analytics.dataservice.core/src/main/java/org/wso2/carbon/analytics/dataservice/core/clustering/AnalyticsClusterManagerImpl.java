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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.spi.exception.TargetNotMemberException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;

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

/**
 * This class represents an {@link AnalyticsClusterManager} implementation.
 */
public class AnalyticsClusterManagerImpl implements AnalyticsClusterManager, MembershipListener {

    private static final String LEADER_INIT_DONE_FLAG = "LEADER_INIT_FLAG";

    private static final String ANALYTICS_GROUP_EXECUTOR_SERVICE_PREFIX = "_ANALYTICS_GROUP_EXECUTOR_SERVICE_";

    private static final String ANALYTICS_CLUSTER_GROUP_MEMBERS_PREFIX = "_ANALYTICS_CLUSTER_GROUP_MEMBERS_";

    private static final String ANALYTICS_CLUSTER_GROUP_DATA_PREFIX = "_ANALYTICS_CLUSTER_GROUP_DATA_";

    private static final String MEMBERSHIP_NUMBER_STRING = "MEMBERSHIP_NUMBER";

    private static final Log log = LogFactory.getLog(AnalyticsClusterManagerImpl.class);

    private static final int MAX_RETRIES = 15;

    private static final long MAX_RETRY_WAIT_INTERVAL = 60000L;

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
    public void joinGroup(String groupId, GroupEventListener groupEventListener)
            throws AnalyticsClusterException {
        if (!this.isClusteringEnabled()) {
            throw new AnalyticsClusterException("Clustering is not enabled");
        }
        if (this.groups.containsKey(groupId)) {
            throw new AnalyticsClusterException("This node has already joined the group: " + groupId);
        }

        log.info("Local member joining the group - " + groupId);

        int retries = 0;
        boolean isFailed = !this.executeJoinGroupFlow(groupId, groupEventListener);
        while (isFailed && (retries < MAX_RETRIES)) {
            log.info("Retrying executing Join Group Flow for " + groupId + ". Retry count = " + retries);
            long waitTime = Math.min(getWaitTimeExp(retries), MAX_RETRY_WAIT_INTERVAL);
            retryWait(waitTime);
            isFailed = !this.executeJoinGroupFlow(groupId, groupEventListener);
            retries++;
        }
    }

    private boolean executeJoinGroupFlow(String groupId, GroupEventListener groupEventListener) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing Join Group Flow for : " + groupId);
            }
            this.checkAndCleanupGroups(groupId);
            this.groups.put(groupId, groupEventListener);
            List<Member> groupMembers = this.getGroupMembers(groupId);
            Member myself = this.hz.getCluster().getLocalMember();
            if (groupMembers.contains(myself)) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing myself from HZ Group Members list : " + myself);
                }
                groupMembers.remove(myself);
            }
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
            return true;
        } catch (Exception e) {
            String msg = "Exception while executing the join group flow .. " + e.getMessage();
            log.warn(msg, e);
            return false;
        }
    }

    private boolean checkLeader(Member member, String groupId) {
        Member leader = this.getLeader(groupId);
        log.info("Checking for leader of the Group : " + groupId + ". This member = " + member
                + " , current leader = " + leader);
        return member.equals(leader);
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

    private void sendMemberAddedNotificationToLeader(String groupId)
            throws AnalyticsClusterException {
        Member member = this.getLeader(groupId);
        this.executeOne(groupId, member, new LeaderMemberAddedNotification(groupId));
    }

    @Override
    public Member getLeader(String groupId) {
        return this.getGroupMembers(groupId).get(0);
    }

    @Override
    public boolean isLeader(String groupId) {
        Member localMember = this.hz.getCluster().getLocalMember();
        Member groupLeader = this.leaders.get(groupId);

        if (log.isDebugEnabled()) {
            log.debug("Checking whether local member is the leader of the Group : " + groupId + ". This member = " + localMember
                    + " , current leader = " + groupLeader);
        }
        return localMember.equals(groupLeader);
    }

    private void executeMyselfBecomingLeader(String groupId) throws AnalyticsClusterException {
        this.leaders.put(groupId, this.hz.getCluster().getLocalMember());
        GroupEventListener listener = this.groups.get(groupId);
        if (listener != null) {
            listener.onBecomingLeader();
        }
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<Member> getGroupMembers(String groupId) {
        return new HzDistributedList(this.hz.getMap(this.generateGroupListId(groupId)));
    }

    /**
     * This method checks the current active members to see if there any members left in the member list,
     * that is actually not in the cluster and clean it up. This can happen, when the last member of the group
     * also goes away, but the distributed memory is retained from other nodes in the cluster.
     *
     * @param groupId The group id
     */
    private void checkAndCleanupGroups(String groupId) {
        List<Member> groupMembers = this.getGroupMembers(groupId);
        Set<Member> existingMembers = this.hz.getCluster().getMembers();
        Iterator<Member> memberItr = groupMembers.iterator();
        List<Member> removeList = new ArrayList<>();
        Member currentMember;
        while (memberItr.hasNext()) {
            currentMember = memberItr.next();
            if (!existingMembers.contains(currentMember)) {
                removeList.add(currentMember);
            }
        }
        for (Member member : removeList) {
            groupMembers.remove(member);
        }
        if (this.getGroupMembers(groupId).size() == 0) {
            this.resetLeaderInitDoneFlag(groupId);
            //clean up the membership number
            this.hz.getAtomicLong(MEMBERSHIP_NUMBER_STRING).set(0);
        }
    }

    @Override
    public List<Object> getMembers(String groupId) throws AnalyticsClusterException {
        return new ArrayList<Object>(this.getGroupMembers(groupId));
    }

    @Override
    public <T> T executeOne(String groupId, Object member, Callable<T> callable)
            throws AnalyticsClusterException {
        Future<T> result = this.executeOneFuture(groupId, member, callable);
        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AnalyticsClusterException("Error in cluster execute one: " + e.getMessage(), e);
        }
    }
    
    @Override
    public <T> Future<T> executeOneFuture(String groupId, Object member, Callable<T> callable)
            throws AnalyticsClusterException {
        return this.hz.getExecutorService(this.generateGroupExecutorId(groupId)).submitToMember(
                callable, (Member) member);
    }

    @Override
    public <T> List<T> executeAll(String groupId, Callable<T> callable)
            throws AnalyticsClusterException {
        List<Member> members = this.getGroupMembers(groupId);
        List<T> result = new ArrayList<T>();
        Map<Member, Future<T>> executionResult = this.hz.getExecutorService(
                this.generateGroupExecutorId(groupId)).submitToMembers(callable, members);
        List<Member> invalidMembers = new ArrayList<>();
        for (Map.Entry<Member, Future<T>> entry : executionResult.entrySet()) {
            try {
                result.add(entry.getValue().get());
            } catch (TargetNotMemberException e) {
                invalidMembers.add(entry.getKey());
                log.warn("Invalid target member: " + entry.getKey() + " removed from group: " + groupId);
            } catch (InterruptedException | ExecutionException e) {
                throw new AnalyticsClusterException("Error in cluster execute all: " + e.getMessage(), e);
            }
        }
        /* process invalid members */
        if (!invalidMembers.isEmpty()) {
            members.removeAll(invalidMembers);
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
    public Object getLocalMember() {
        return this.hz.getCluster().getLocalMember();
    }

    @Override
    public void memberAdded(MembershipEvent event) {
        /* nothing to do */
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent event) {
        /* nothing to do */
    }

    private boolean executeCheckGroupMemberRemovalFlow(String groupId, Member member) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing Check Group Member Removal : " + groupId + " , " + member);
            }
		    List<Member> groupMembers = this.getGroupMembers(groupId);
		    if (groupMembers.contains(member)) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing member from HZ Group Members list : " + member);
                }
                groupMembers.remove(member);
            }
            if (this.isLeader(groupId)) {
                log.info("Local Member is already the leader of the Group : " + groupId);
		            /* if I'm already the leader, notify of the membership change */
                GroupEventListener listener = this.groups.get(groupId);
                if (listener != null) {
                    listener.onMembersChangeForLeader(true);
                }
            } else if (this.checkLeader(this.hz.getCluster().getLocalMember(), groupId)) {
                log.info("Local member is the new leader of the Group") ;
		            /* check if I'm already not the leader, and if I just became the leader */
                this.executeMyselfBecomingLeader(groupId);
            }  else {
                log.info("Local Member is neither current leader nor new leader.") ;
            }
            GroupEventListener listener = this.groups.get(groupId);
            if (listener != null) {
                listener.onMemberRemoved();
            }
            log.info("Returning from executeCheckGroupMemberRemovalFlow.") ;
            return true;
        } catch (Exception e) {
            String msg = "Exception while executing the check Group Member Removal flow .. " + e.getMessage();
            log.warn(msg, e);
            return false;
        }      
    }

    private void leaderUpdateNotificationReceived(String groupId) {
        if (log.isDebugEnabled()) {
            log.debug("Leader Update Notification Received : " + groupId);
        }
        GroupEventListener listener = this.groups.get(groupId);
        if (listener != null) {
            listener.onLeaderUpdate();
        }
    }

    private void leaderMemberAdditionNotificationReceived(String groupId) {
        if (log.isDebugEnabled()) {
            log.debug("Leader Member Addition Notification Received : " + groupId);
        }
        GroupEventListener listener = this.groups.get(groupId);
        if (listener != null) {
            listener.onMembersChangeForLeader(false);
        }
    }

    @Override
    public void memberRemoved(MembershipEvent event) {
        Member member = event.getMember();
        log.info("Member Removed Event : " + member);
        Set<String> groupIds = this.groups.keySet();

        log.info("Group IDs : " + groupIds.size() + " --> " + groupIds);
        for (String groupId : groupIds) {
            int retries = 0;
            boolean isFailed = !this.executeCheckGroupMemberRemovalFlow(groupId, member);
            while (isFailed && (retries < MAX_RETRIES)) {
                log.info("Retrying executing Check Group Member Removal Flow for : " + groupId
                        + ". Retry count : " + retries + ", Member : " + member);
                long waitTime = Math.min(getWaitTimeExp(retries), MAX_RETRY_WAIT_INTERVAL);
                retryWait(waitTime);
                isFailed = !this.executeCheckGroupMemberRemovalFlow(groupId, member);
                retries++;
            }
        }
    }

    private long getWaitTimeExp(int retryCount) {
        return ((long) Math.pow(2, retryCount) * 100L);
    }

    private void retryWait(long waitTime)  {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ignored) {
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
