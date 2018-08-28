///*
// * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  WSO2 Inc. licenses this file to you under the Apache License,
// *  Version 2.0 (the "License"); you may not use this file except
// *  in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an
// *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// *  KIND, either express or implied. See the License for the
// *  specific language governing permissions and limitations
// *  under the License.
// */
//
//package org.wso2.carbon.stream.processor.core.event.queue;
//
//import org.apache.log4j.Logger;
//import org.wso2.carbon.stream.processor.core.ha.transport.TCPNettyClient;
//import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
//import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
//import org.wso2.siddhi.core.SiddhiAppRuntime;
//import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
//import org.wso2.siddhi.core.util.snapshot.PersistenceReference;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Map;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//
//public class  EventQueueObserver implements Runnable {
//
//    private static final Logger log = Logger.getLogger(EventQueueObserver.class);
//    private TCPNettyClient tcpNettyClient = new TCPNettyClient();
//
//    @Override
//    public void run() {
//        if (EventTreeMapManager.getEventQueue().getRemainingCapacity() < 18000) {
//            log.error("Event queue threshold is reached. Hence persisting the Siddhi Apps");
//            persistSiddhiApps();
//        }
//    }
//
//    private void persistSiddhiApps() {
//        try {
//            Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
//                    getSiddhiAppMap();
//            String[] siddhiAppRevisionArray = new String[siddhiAppMap.size()];
//            int siddhiAppCount = 0;
//            for (Map.Entry<String, SiddhiAppData> siddhiAppMapEntry : siddhiAppMap.entrySet()) {
//                SiddhiAppRuntime siddhiAppRuntime = siddhiAppMapEntry.getValue().getSiddhiAppRuntime();
//                if (siddhiAppRuntime != null) {
//                    PersistenceReference persistenceReference = siddhiAppRuntime.persist();
//                    siddhiAppRevisionArray[siddhiAppCount] = persistenceReference.getRevision();
//                    siddhiAppCount++;
//                    Future fullStateFuture = persistenceReference.getFullStateFuture();
//                    if (fullStateFuture != null) {
//                        fullStateFuture.get(StreamProcessorDataHolder.getDeploymentConfig().getLiveSync().getStateSyncTimeout(),
//                                TimeUnit.MILLISECONDS);
//                    } else {
//                        for (Future future : persistenceReference.getIncrementalStateFuture()) {
//                            future.get(StreamProcessorDataHolder.getDeploymentConfig().getLiveSync().getStateSyncTimeout(),
//                                    TimeUnit.MILLISECONDS);
//                        }
//                    }
//                } else {
//                    log.error("Active Node: Persisting of Siddhi app " + siddhiAppMapEntry.getValue() +
//                            " not successful. Check if app deployed properly");
//                }
//            }
//            if (!tcpNettyClient.isActive()) {
//                tcpNettyClient.connect("localhost", 9893);
//            }
//            String siddhiAppRevisions = Arrays.toString(siddhiAppRevisionArray);
//            tcpNettyClient.send("controlMessage", siddhiAppRevisions.getBytes());
//            log.info("SiddhiApp Revisions :     " + siddhiAppRevisions);
//            log.info("Oldest Revision Timestamp :   " + getOldestRevisionTime(siddhiAppRevisionArray));
//            EventTreeMapManager.getEventQueue().trimBasedOnTimestamp(getOldestRevisionTime(siddhiAppRevisionArray));
//        } catch (ConnectionUnavailableException e) {
//            log.error("Error in connecting to 'localhost' and '9892' of the Passive node");
//        } catch (Exception e) {
//            log.error("Error while persisting siddhi applications. " + e.getMessage(), e);
//        }
//        log.info("Active Node: Persisting of all Siddhi Applications on request of passive node successful");
//    }
//
//    private long getOldestRevisionTime(String[] siddhiAppRevisionArray) {
//        ArrayList<Long> timestampList = new ArrayList<>();
//        for (String aSiddhiAppRevisionArray : siddhiAppRevisionArray) {
//            timestampList.add(Long.parseLong(aSiddhiAppRevisionArray.split("_")[0]));
//        }
//        timestampList.sort(Long::compareTo);
//        return timestampList.get(0);
//    }
//}
