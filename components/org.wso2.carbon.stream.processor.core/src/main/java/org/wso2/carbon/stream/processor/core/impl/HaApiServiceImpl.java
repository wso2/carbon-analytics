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

package org.wso2.carbon.stream.processor.core.impl;

import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.api.*;
import org.wso2.carbon.stream.processor.core.api.NotFoundException;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSinkHandler;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSinkHandlerManager;
import org.wso2.carbon.stream.processor.core.internal.beans.ActiveNodeLastPublishedEventTimeStamp;
import org.wso2.carbon.stream.processor.core.ha.util.CompressionUtil;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.model.HAStateSyncObject;
import org.wso2.carbon.stream.processor.core.model.LastPublishedTimestamp;
import org.wso2.carbon.stream.processor.core.model.LastPublishedTimestampCollection;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * Implementation of HA Api Service
 */
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-21T09:31:22.101Z")
public class HaApiServiceImpl extends HaApiService {

    private static final Logger log = Logger.getLogger(HaApiServiceImpl.class);

    @Override
    public Response haPublishedTimestampGet() throws NotFoundException {

        HACoordinationSinkHandlerManager sinkHandlerManager = (HACoordinationSinkHandlerManager)
                StreamProcessorDataHolder.getSinkHandlerManager();
        Map<String, SinkHandler> registeredSinkHandlers = sinkHandlerManager.getRegisteredSinkHandlers();
        List<LastPublishedTimestamp> lastPublishedTimestamps = new ArrayList<>();

        if (registeredSinkHandlers.size() > 0) {
            for (SinkHandler sinkHandler : registeredSinkHandlers.values()) {
                ActiveNodeLastPublishedEventTimeStamp activeNodeLastPublishedTimestamp =
                        ((HACoordinationSinkHandler) sinkHandler).getActiveNodeLastPublishedTimestamp();
                long timestamp = activeNodeLastPublishedTimestamp.getTimestamp();
                lastPublishedTimestamps.add(new LastPublishedTimestamp(activeNodeLastPublishedTimestamp.
                        getSinkHandlerElementId(), Long.toString(timestamp)));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Active Node: Sending back last published event's timestamp of " + registeredSinkHandlers.size()
                    + " sinks.");
        }
        return Response.ok().entity(new LastPublishedTimestampCollection(lastPublishedTimestamps)).build();
    }

    @Override
    public Response haStateGet() throws NotFoundException, IOException {

        Map<String, Long> sourceTimestamps = new HashMap<>();
        Map<String, byte[]> snapshotMap = new HashMap<>();
        try {
            Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                    getSiddhiAppMap();
            for (Map.Entry<String, SiddhiAppData> siddhiAppMapEntry : siddhiAppMap.entrySet()) {
                byte[] compressedArray = CompressionUtil.compressGZIP(siddhiAppMapEntry.getValue().
                        getSiddhiAppRuntime().snapshot());
                snapshotMap.put(siddhiAppMapEntry.getKey(), compressedArray);
            }
        } catch (Exception e) {
            log.error("Error while snapshoting all siddhi applications to send to passive node. " + e.getMessage(), e);
            return Response.status(500).build();
        }

        log.info("Active Node: Snapshoting of all Siddhi Applications on request of passive node successful");
        return Response.ok().entity(new HAStateSyncObject(snapshotMap)).build();
    }

    @Override
    public Response haStateGet(String siddhiAppName) throws NotFoundException, IOException {

        Map<String, byte[]> snapshotMap = new HashMap<>();
        try {
            SiddhiAppData siddhiAppData = StreamProcessorDataHolder.getStreamProcessorService().getSiddhiAppMap().
                    get(siddhiAppName);
            if (siddhiAppData != null) {
                byte[] snapshot = siddhiAppData.getSiddhiAppRuntime().snapshot();
                byte[] compressedArray = CompressionUtil.compressGZIP(snapshot);
                snapshotMap.put(siddhiAppName, compressedArray);
            } else {
                log.warn("Siddhi application " + siddhiAppName + " may not be deployed in active node yet but " +
                        "requested for snapshot from passive node");
                return Response.ok().entity(new HAStateSyncObject(false)).build();
            }

        } catch (Exception e) {
            log.error("Error while snapshoting " + siddhiAppName + " to send to passive node. " + e.getMessage(), e);
            return Response.status(500).build();
        }

        log.info("Active Node: Snapshoting of " + siddhiAppName + " on request of passive node successfull");
        return Response.ok().entity(new HAStateSyncObject(snapshotMap)).build();
    }
}
