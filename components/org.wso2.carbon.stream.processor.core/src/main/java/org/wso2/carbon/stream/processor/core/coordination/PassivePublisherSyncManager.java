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

package org.wso2.carbon.stream.processor.core.coordination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;

import java.util.Map;

/**
 * Class that manages the periodic calls the passive node makes to the active node
 */
public class PassivePublisherSyncManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PassivePublisherSyncManager.class);

    private HACoordinationSinkHandlerManager haCoordinationSinkHandlerManager;

    public PassivePublisherSyncManager(HACoordinationSinkHandlerManager haCoordinationSinkHandlerManager) {
        this.haCoordinationSinkHandlerManager = haCoordinationSinkHandlerManager;
    }

    @Override
    public void run() {
        long activeNodePublishedLastTs = 0L;
        //TODO API call to Active Node and get the timestamp

        Map<String, SinkHandler> sinkHandlerMap = haCoordinationSinkHandlerManager.getRegsiteredSinkHandlers();
        for (SinkHandler sinkHandler : sinkHandlerMap.values()) {
            ((HACoordinationSinkHandler) sinkHandler).updatePassiveNodeEventQueue(activeNodePublishedLastTs);
        }
    }
}
