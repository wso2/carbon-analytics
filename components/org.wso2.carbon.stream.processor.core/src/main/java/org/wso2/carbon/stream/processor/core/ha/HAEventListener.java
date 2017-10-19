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

package org.wso2.carbon.stream.processor.core.ha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;

import java.util.Map;

/**
 * Event listener implementation that listens for changes that happen within the cluster used for 2 node minimum HA
 */
public class HAEventListener extends MemberEventListener {

    private static final Logger log = LoggerFactory.getLogger(HAEventListener.class);

    @Override
    public void memberAdded(NodeDetail nodeDetail) {
        // Do Nothing
    }

    @Override
    public void memberRemoved(NodeDetail nodeDetail) {
        // Do Nothing
    }

    @Override
    public void coordinatorChanged(NodeDetail nodeDetail) {
        boolean isLeader = StreamProcessorDataHolder.getClusterCoordinator().isLeaderNode();
        if (isLeader) {
            log.info("HA Deployment: Changing from Passive State to Active State");
            StreamProcessorDataHolder.getHAManager().changeToActive();
            HACoordinationSinkHandlerManager haCoordinationSinkHandlerManager = (HACoordinationSinkHandlerManager)
                    StreamProcessorDataHolder.getSinkHandlerManager();
            Map<String, SinkHandler> registeredSinkHandlers = haCoordinationSinkHandlerManager.
                    getRegisteredSinkHandlers();
            for (SinkHandler sinkHandler : registeredSinkHandlers.values()) {
                ((HACoordinationSinkHandler) sinkHandler).setAsActive();
            }
            HACoordinationSourceHandlerManager haCoordinationSourceHandlerManager = (HACoordinationSourceHandlerManager)
                    StreamProcessorDataHolder.getSourceHandlerManager();
            Map<String, SourceHandler> registeredSourceHandlers = haCoordinationSourceHandlerManager.
                    getRegsiteredSourceHandlers();
            for (SourceHandler sourceHandler : registeredSourceHandlers.values()) {
                ((HACoordinationSourceHandler) sourceHandler).setAsActive();
            }
        }
    }
}
