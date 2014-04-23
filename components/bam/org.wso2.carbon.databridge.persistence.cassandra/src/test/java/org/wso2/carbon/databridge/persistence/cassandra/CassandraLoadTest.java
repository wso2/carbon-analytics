package org.wso2.carbon.databridge.persistence.cassandra;

import org.junit.Test;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.BaseCassandraSDSTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CassandraLoadTest extends BaseCassandraSDSTest {


    private final Map<String, Event> insertedEvents = new HashMap<String, Event>();
    @Test
    public void loadTest() {
//        // save stream defn 1
//        String streamIdKey1 = DataBridgeUtils
//                .constructStreamKey(streamDefinition1.getName(), streamDefinition1.getVersion());
//        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
//        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);
//
//        // save stream defn 2
//        String streamIdKey2 = DataBridgeUtils
//                .constructStreamKey(streamDefinition2.getName(), streamDefinition2.getVersion());
//        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition2);
//        cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition2);
//
//
//        final List<Event> eventList = new ArrayList<Event>();
//        // retrieve stream id 1
//        String retrievedStreamId1 = cassandraConnector.getStreamIdFromStore(getCluster(), streamIdKey1);
//        eventList.addAll(
//                EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent1, retrievedStreamId1));
//
//        // retrieve stream id 2
//        String retrievedStreamId2 = cassandraConnector.getStreamIdFromStore(getCluster(), streamIdKey2);
//        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent2, retrievedStreamId2));
//
//
//
//        ExecutorService executorService = Executors.newFixedThreadPool(500);
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//
//                for (Event event : eventList) {
//                    try {
//                        String rowKey = cassandraConnector.insertEvent(cluster, event);
//                        // inserts row key only if event is valid, i.e. only proper events will add a row key
//                        insertedEvents.put(rowKey, event);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        // ignore
//                    }
//                }
//            }
//        });
//
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        assertEquals(2000, insertedEvents.size());
//
//        Map<String, Event> retrievedEvents = new HashMap<String, Event>();
//
//        for (Map.Entry<String, Event> eventProps : insertedEvents.entrySet()) {
//            try {
//                retrievedEvents.put(eventProps.getKey(),
//                        cassandraConnector.getEvent(cluster, eventProps.getValue().getStreamId(), eventProps.getKey()));
//            } catch (EventProcessingException e) {
//                e.printStackTrace();
//                fail();
//            }
//        }
//
//
//        for (Map.Entry<String, Event> rowKeyAndEvent : retrievedEvents.entrySet()) {
//            Event retrievedEvent = rowKeyAndEvent.getValue();
//            Event originialEvent = insertedEvents.get(rowKeyAndEvent.getKey());
//            System.out.println("Retrieved Event : " + retrievedEvent + "\n Original Event : " + originialEvent + "\n\n");
//            if (streamDefinition1.getStreamId().equals(originialEvent.getStreamId())) {
//                assertTrue(DataBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition1));
//            } else if (streamDefinition2.getStreamId().equals(originialEvent.getStreamId())) {
//                assertTrue(DataBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition2));
//
//
//            }
//        }
//
    }
}
