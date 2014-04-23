package org.wso2.carbon.databridge.persistence.cassandra;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.EventConverterUtils;
import org.wso2.carbon.databridge.core.Utils.DataBridgeUtils;
import org.wso2.carbon.databridge.core.exception.EventProcessingException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.persistence.cassandra.Utils.CassandraSDSUtils;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.BaseCassandraSDSTest;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

import static junit.framework.Assert.*;

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
public class CassandraDefnStoreTest extends BaseCassandraSDSTest {


    @Test(expected = Exception.class)
    public void createCFDuringStreamDefn() {
        try {
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }
        ColumnFamilyDefinition columnFamilyDefinition =
                HFactory.createColumnFamilyDefinition(CassandraTestConstants.BAM_EVENT_DATA_KEYSPACE,
                                                      CassandraSDSUtils.convertStreamNameToCFName(streamDefinition1.getName()));
        cluster.addColumnFamily(columnFamilyDefinition);
    }

    @Test(expected = Exception.class)
    public void tooLongStreamName() {
        try {
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), tooLongStreamDefinition);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void checkEqualityofEventIdAndStreamDefnId() {

        try {
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }


        List<Event> eventList = EventConverterUtils
                .convertFromJson(CassandraTestConstants.properEvent, streamDefinition1.getStreamId());

        for (Event event : eventList) {
            assertEquals(event.getStreamId(), streamDefinition1.getStreamId());
        }
    }

    @Test
    public void saveSameStreamMultipleTimes() {
        try {
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }

        String secondTimeStreamId1 = null;
        Credentials credentials = getCredentials(getCluster());

        try {
            secondTimeStreamId1 = cassandraConnector.getStreamDefinitionFromCassandra(
                    cluster, streamDefinition1.getStreamId()).getStreamId();
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }


        assertEquals(streamDefinition1.getStreamId(), secondTimeStreamId1);
    }


    @Test
    public void checkHappyPathStreamStoreOperations() {

        try {
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }

        StreamDefinition streamDefinitionFromStore = null;
        try {
            Credentials credentials = getCredentials(cluster);
            streamDefinitionFromStore =
                    cassandraConnector.getStreamDefinitionFromCassandra(
                            getCluster(), streamDefinition1.getStreamId());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(streamDefinition1, streamDefinitionFromStore);
        List<Event> eventList = EventConverterUtils.convertFromJson(
                CassandraTestConstants.properEvent, streamDefinition1.getStreamId());
        try {
            int eventCounter = 0;
            for (Event event : eventList) {
                insertEvent(cluster, event, eventCounter++);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void insertMixOfWronglyFormedAndCorrectlyFormedEvents() {
        try {
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }

        List<Event> eventList = EventConverterUtils.convertFromJson(CassandraTestConstants.properandImproperEvent, streamDefinition1.getStreamId());
        List<String> insertedEvents = new ArrayList<String>();
        int eventCounter = 0;
        for (Event event : eventList) {
            try {
                String rowKey = insertEvent(cluster, event, eventCounter++);
                // inserts row key only if event is valid, i.e. only proper events will add a row key
                insertedEvents.add(rowKey);
            } catch (Exception e) {
                // ignore
            }
        }
        assertEquals(2, insertedEvents.size());

    }

    @Test
    public void insertEventsFromTwoVersions() {
        try {
            // save stream defn 1
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

            // save stream defn 2
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition2);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }


        List<Event> eventList = new ArrayList<Event>();
        // retrieve stream id 1
        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent1, streamDefinition1.getStreamId()));

        // retrieve stream id 2
        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent2, streamDefinition2.getStreamId()));


        Map<String, Event> insertedEvents = new HashMap<String, Event>();
        int eventCounter = 0;
        for (Event event : eventList) {
            try {
                String rowKey = insertEvent(cluster, event, eventCounter++);
                // inserts row key only if event is valid, i.e. only proper events will add a row key
                insertedEvents.put(rowKey, event);
            } catch (Exception e) {
                e.printStackTrace();
                // ignore
            }
        }
        assertEquals(4, insertedEvents.size());

        Map<String, Event> retrievedEvents = new HashMap<String, Event>();

        for (Map.Entry<String, Event> eventProps : insertedEvents.entrySet()) {
            try {
                retrievedEvents.put(eventProps.getKey(),getEvent(
                        cluster, eventProps.getValue().getStreamId(), eventProps.getKey()));
            } catch (EventProcessingException e) {
                e.printStackTrace();
                fail();
            }
        }


        for (Map.Entry<String, Event> rowKeyAndEvent : retrievedEvents.entrySet()) {
            Event retrievedEvent = rowKeyAndEvent.getValue();
            Event originialEvent = insertedEvents.get(rowKeyAndEvent.getKey());
            System.out.println("Retrieved Event : " + retrievedEvent + "\n Original Event : " + originialEvent + "\n\n");
            if (streamDefinition1.getStreamId().equals(originialEvent.getStreamId())) {
                assertTrue(DataBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition1));
            } else if (streamDefinition2.getStreamId().equals(originialEvent.getStreamId())) {
                assertTrue(DataBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition2));


            }
        }
    }


    @Test
    public void insertEventsFromMultipleStreams() {
        try {
            // save stream defn 1
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition1);

            // save stream defn 3
            cassandraConnector.saveStreamDefinitionToStore(getCluster(), streamDefinition3);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            fail();
        }


        List<Event> eventList = new ArrayList<Event>();
        // retrieve stream id 1
        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent1, streamDefinition1.getStreamId()));

        // retrieve stream id 3
        eventList.addAll(EventConverterUtils.convertFromJson(CassandraTestConstants.multipleProperEvent3, streamDefinition3.getStreamId()));


        Map<String, Event> insertedEvents = new HashMap<String, Event>();
        int eventCounter = 0;
        for (Event event : eventList) {
            try {
                String rowKey = insertEvent(cluster, event, eventCounter++);
                // inserts row key only if event is valid, i.e. only proper events will add a row key
                insertedEvents.put(rowKey, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assertEquals(4, insertedEvents.size());

        Map<String, Event> retrievedEvents = new HashMap<String, Event>();

        for (Map.Entry<String, Event> eventProps : insertedEvents.entrySet()) {
            try {
                retrievedEvents.put(
                        eventProps.getKey(),getEvent(
                                cluster, eventProps.getValue().getStreamId(), eventProps.getKey()));
            } catch (EventProcessingException e) {
                e.printStackTrace();
                fail();
            }
        }


        for (Map.Entry<String, Event> rowKeyAndEvent : retrievedEvents.entrySet()) {
            Event retrievedEvent = rowKeyAndEvent.getValue();
            Event originialEvent = insertedEvents.get(rowKeyAndEvent.getKey());
            System.out.println("Retrieved Event : " + retrievedEvent + "\n Original Event : " +
                               originialEvent + "\n\n");
            if (streamDefinition1.getStreamId().equals(originialEvent.getStreamId())) {
                assertTrue(DataBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition1));
            } else if (streamDefinition2.getStreamId().equals(originialEvent.getStreamId())) {
                assertTrue(DataBridgeUtils.equals(originialEvent, retrievedEvent, streamDefinition3));


            }
        }

    }

    @Test
    public void getAllStreamDefns() throws StreamDefinitionStoreException {
        Collection<StreamDefinition> allStreamDefinitionFromStore =
                cassandraConnector.getAllStreamDefinitionFromStore(getCluster());
        assertNotNull(allStreamDefinitionFromStore);
    }

    @Test
    public void checkForNullDefnsWhenRetrievingAllStreamDefns()
            throws StreamDefinitionStoreException {
        Collection<StreamDefinition> expectedAllStreamDefinitionFromStore =
                cassandraConnector.getAllStreamDefinitionFromStore(getCluster());

        Collection<StreamDefinition> actualAllStreamDefinitionFromStore =
                cassandraConnector.getAllStreamDefinitionFromStore(getCluster());

        assertEquals(expectedAllStreamDefinitionFromStore.size(), actualAllStreamDefinitionFromStore.size());

    }


    @Test
    public void deleteStreamDefinitionAndId() throws StreamDefinitionStoreException {
        insertEventsFromMultipleStreams();
        insertEventsFromTwoVersions();
        Collection<StreamDefinition> allStreamDefinitionFromStore =
                cassandraConnector.getAllStreamDefinitionFromStore(getCluster());
        int i = 0;
        for (StreamDefinition streamDefinition : allStreamDefinitionFromStore) {
            cassandraConnector.deleteStreamDefinitionFromCassandra(
                    getCluster(), streamDefinition.getStreamId());
            i++;
            Collection<StreamDefinition> newAllStreamDefns =
                    cassandraConnector.getAllStreamDefinitionFromStore(getCluster());
            assertEquals(allStreamDefinitionFromStore.size() - i, newAllStreamDefns.size());

        }


    }

    private static Credentials getCredentials(Cluster cluster) {
        Map<String, String> credentials = cluster.getCredentials();

        Credentials creds = null;
        for (Map.Entry<String, String> entry : credentials.entrySet()) {
            String userName = entry.getKey();
            String password = entry.getValue();
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);

            creds = new Credentials(userName, password, tenantDomain);
        }

        return creds;
    }

}
