/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.coordination.listener;

import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.sp.coordination.listener.internal.CoordinationListenerDataHolder;

public class CoordinationEventListener extends MemberEventListener {
    @Override
    public void memberAdded(NodeDetail nodeDetail) {
        //No action needed.
    }

    @Override
    public void memberRemoved(NodeDetail nodeDetail) {
        //No action needed.
    }

    @Override
    public void coordinatorChanged(NodeDetail nodeDetail) {
        ClusterCoordinator clusterCoordinator = CoordinationListenerDataHolder.getClusterCoordinator();
        if (clusterCoordinator != null) {
            CoordinationListenerDataHolder.setIsLeader(clusterCoordinator.isLeaderNode());
        }
    }

    @Override
    public void becameUnresponsive(String s) {
        //// TODO: 3/26/19  
    }
}
