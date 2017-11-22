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
import org.wso2.carbon.stream.processor.core.ha.HACoordinationRecordTableHandler;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSinkHandler;
import org.wso2.carbon.stream.processor.core.ha.util.CompressionUtil;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.model.HAStateSyncObject;
import org.wso2.carbon.stream.processor.core.model.OutputSyncTimestamps;
import org.wso2.carbon.stream.processor.core.model.OutputSyncTimestampCollection;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerManager;
import org.wso2.siddhi.core.table.record.RecordTableHandler;
import org.wso2.siddhi.core.table.record.RecordTableHandlerManager;

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

    public Response haOutputSyncTimestampGet() throws NotFoundException {

        SinkHandlerManager sinkHandlerManager = StreamProcessorDataHolder.getSinkHandlerManager();
        Map<String, SinkHandler> registeredSinkHandlers = sinkHandlerManager.getRegisteredSinkHandlers();
        List<OutputSyncTimestamps> lastPublishedTimestamps = new ArrayList<>();

        if (registeredSinkHandlers.size() > 0) {
            for (Map.Entry<String, SinkHandler> sinkHandlerMap : registeredSinkHandlers.entrySet()) {
                long timestamp = ((HACoordinationSinkHandler) sinkHandlerMap.getValue()).
                        getActiveNodeLastPublishedTimestamp();
                lastPublishedTimestamps.add(new OutputSyncTimestamps(sinkHandlerMap.getKey(),
                        Long.toString(timestamp)));
            }
        }

        RecordTableHandlerManager recordTableHandlerManager = StreamProcessorDataHolder.getRecordTableHandlerManager();
        Map<String, RecordTableHandler> registeredRecordTableHandlers = recordTableHandlerManager.
                getRegisteredRecordTableHandlers();
        List<OutputSyncTimestamps> lastRecordTableOperationTimestamp = new ArrayList<>();

        if (registeredRecordTableHandlers.size() > 0) {
            for (Map.Entry<String, RecordTableHandler> recordTableHandlerMap : registeredRecordTableHandlers.
                    entrySet()) {
                long timestamp = ((HACoordinationRecordTableHandler) recordTableHandlerMap.getValue()).
                        getActiveNodeLastOperationTimestamp();
                lastRecordTableOperationTimestamp.add(new OutputSyncTimestamps(recordTableHandlerMap.
                        getKey(), Long.toString(timestamp)));

            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Active Node: Sending back last published event's timestamp of " + registeredSinkHandlers.size()
                    + " sinks and timestamps of last operation's of " + registeredRecordTableHandlers.size() +
                    " record tables");
        }
        return Response.ok().entity(new OutputSyncTimestampCollection(lastPublishedTimestamps,
                lastRecordTableOperationTimestamp)).build();
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
