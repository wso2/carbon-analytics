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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.core.coordination.util.RequestUtil;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.model.LastPublishedTimestamp;
import org.wso2.carbon.stream.processor.core.model.LastPublishedTimestampCollection;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;

import java.net.URI;
import java.util.Map;

/**
 * Class that manages the periodic calls the passive node makes to the active node to sync the publishers
 */
public class PassivePublisherSyncManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PassivePublisherSyncManager.class);

    private String activeNodeHost;
    private String activeNodePort;

    public PassivePublisherSyncManager(String host, String port) {
        this.activeNodeHost = host;
        this.activeNodePort = port;
    }

    @Override
    public void run() {

        String url = "http://%s:%d/ha/publishedts";
        URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
        String httpResponseMessage = RequestUtil.sendRequest(baseURI);
        if (log.isDebugEnabled()) {
            log.debug("Passive Node accessed Active node to retrieve last published timestamps");
        }
        HACoordinationSinkHandlerManager haCoordinationSinkHandlerManager =
                (HACoordinationSinkHandlerManager) StreamProcessorDataHolder.getSinkHandlerManager();
        Map<String, SinkHandler> sinkHandlerMap = haCoordinationSinkHandlerManager.getRegisteredSinkHandlers();

        LastPublishedTimestampCollection timestampCollection = new Gson().fromJson(httpResponseMessage,
                LastPublishedTimestampCollection.class);

        if (sinkHandlerMap.size() != timestampCollection.size()) {
            //This might happen due to delays in deploying siddhi applications to both nodes
            log.warn("Active node and Passive node do not have same amount of sink handlers.");
        }
        
        for (LastPublishedTimestamp publisherSyncTimestamp : timestampCollection.getLastPublishedTimestamps()) {
            if (log.isDebugEnabled()) {
                log.debug("Updating Passive Node Publisher Queue for Sink " + publisherSyncTimestamp.getId()
                        + " with timestamp " + publisherSyncTimestamp.getTimestamp());
            }
            HACoordinationSinkHandler sinkHandler = (HACoordinationSinkHandler) sinkHandlerMap.
                    get(publisherSyncTimestamp.getId());
            sinkHandler.trimPassiveNodeEventQueue(publisherSyncTimestamp.getTimestamp());
        }
    }
}
