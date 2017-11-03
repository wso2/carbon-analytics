/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sp.distributed.resource.core.bean;

import java.util.List;

/**
 * Response for joining the resource pool or heartbeat update.
 */
public class HeartbeatResponse {
    /**
     * Configurations of the current leader node.
     */
    private ManagerNodeConfig leader;
    /**
     * List of managers that are connected to the leader node's cluster.
     */
    private List<HTTPInterfaceConfig> connectedManagers;
    /**
     * Current nodes joined state. (whether NEW or EXISTS)
     */
    private String joinedState;

    /**
     * Getter for the leader.
     *
     * @return leader.
     */
    public ManagerNodeConfig getLeader() {
        return leader;
    }

    /**
     * Setter for the leader
     *
     * @param leader node's config
     */
    public void setLeader(ManagerNodeConfig leader) {
        this.leader = leader;
    }

    /**
     * Getter for the list of connected managers.
     *
     * @return {@link List<HTTPInterfaceConfig>} of connected managers.
     */
    public List<HTTPInterfaceConfig> getConnectedManagers() {
        return connectedManagers;
    }

    /**
     * Setter for the list of connected managers.
     *
     * @param connectedManagers {@link List<HTTPInterfaceConfig>} of connected managers.
     */
    public void setConnectedManagers(List<HTTPInterfaceConfig> connectedManagers) {
        this.connectedManagers = connectedManagers;
    }

    /**
     * Getter for the joinedState.
     *
     * @return joinedState
     */
    public String getJoinedState() {
        return joinedState;
    }

    /**
     * Setter for the joined state.
     *
     * @param joinedState state of the current node.
     */
    public void setJoinedState(String joinedState) {
        this.joinedState = joinedState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HeartbeatResponse that = (HeartbeatResponse) o;
        if (getLeader() != null
                ? !getLeader().equals(that.getLeader())
                : that.getLeader() != null) {
            return false;
        }
        if (getConnectedManagers() != null
                ? !getConnectedManagers().equals(that.getConnectedManagers())
                : that.getConnectedManagers() != null) {
            return false;
        }
        return getJoinedState() != null
                ? getJoinedState().equals(that.getJoinedState())
                : that.getJoinedState() == null;
    }

    @Override
    public int hashCode() {
        int result = getLeader() != null ? getLeader().hashCode() : 0;
        result = 31 * result + (getConnectedManagers() != null ? getConnectedManagers().hashCode() : 0);
        result = 31 * result + (getJoinedState() != null ? getJoinedState().hashCode() : 0);
        return result;
    }
}

