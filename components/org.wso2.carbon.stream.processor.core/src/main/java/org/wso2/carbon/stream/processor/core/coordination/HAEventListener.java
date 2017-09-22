/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stream.processor.core.coordination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;

import java.util.HashMap;

public class HAEventListener extends MemberEventListener{

    private static final Logger log = LoggerFactory.getLogger(HAEventListener.class);

    private HACoordinationSinkHandlerManager haCoordinationSinkHandlerManager;

    public HAEventListener(HACoordinationSinkHandlerManager sinkHandlerManager) {
        this.haCoordinationSinkHandlerManager = sinkHandlerManager;
    }

    @Override
    public void memberAdded(NodeDetail nodeDetail) {
        //Since only two nodes. This callback means that the new node is the passive node.
        log.info("Member Added " + nodeDetail.getNodeId());
    }

    @Override
    public void memberRemoved(NodeDetail nodeDetail) {
        log.info("Member Removed " + nodeDetail.getNodeId());
    }

    @Override
    public void coordinatorChanged(NodeDetail nodeDetail) {
        //Since only two nodes. This callback means this node becomes the active node.
        boolean isLeader = StreamProcessorDataHolder.getInstance().getClusterCoordinator().isLeaderNode();
        if (isLeader) {
            log.info("Hi I am the Leader");
            if (haCoordinationSinkHandlerManager != null) {
                HashMap<String, SinkHandler> registeredSinkHandlers = haCoordinationSinkHandlerManager.
                        getRegsiteredSinkHandlers();
                for (SinkHandler sinkHandler: registeredSinkHandlers.values()) {
                    ((HACoordinationSinkHandler)sinkHandler).setAsActive();
                }
            }
        } else {
            log.info("Hi I am the Passive");
        }
        log.info("Coordinator Changed " + nodeDetail.getNodeId());
    }
}
