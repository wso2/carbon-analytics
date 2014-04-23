package org.wso2.carbon.databridge.persistence.cassandra.datastore;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cassandraunit.AbstractCassandraUnit4TestCase;
import org.cassandraunit.dataset.DataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.BeforeClass;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.exception.EventProcessingException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.persistence.cassandra.CassandraTestConstants;
import org.wso2.carbon.databridge.persistence.cassandra.Utils.CassandraSDSUtils;
import org.wso2.carbon.databridge.persistence.cassandra.Utils.StreamDefinitionUtils;
import org.wso2.carbon.databridge.persistence.cassandra.internal.util.ServiceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.fail;

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
public class BaseCassandraSDSTest extends AbstractCassandraUnit4TestCase {

    static Log log = LogFactory.getLog(BaseCassandraSDSTest.class);

    protected static CassandraConnector cassandraConnector;

    private final static StringSerializer stringSerializer = StringSerializer.get();
    private final static LongSerializer longSerializer = LongSerializer.get();
    private final static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();

    protected static Cluster cluster;
    protected static StreamDefinition streamDefinition1;
    protected static StreamDefinition streamDefinition2;
    protected static StreamDefinition tooLongStreamDefinition;
    protected static StreamDefinition streamDefinition3;

    private static final String STREAM_NAME_KEY = "Name";

    private static final String STREAM_VERSION_KEY = "Version";
    private static final String STREAM_NICK_NAME_KEY = "Nick_Name";
    private static final String STREAM_TIMESTAMP_KEY = "Timestamp";
    private static final String STREAM_DESCRIPTION_KEY = "Description";

    private static final String STREAM_ID_KEY = "StreamId";
    public static final String BAM_META_STREAM_DEF_CF = "STREAM_DEFINITION";

    public static final String BAM_META_KEYSPACE = "META_KS";

    public static final String BAM_EVENT_DATA_KEYSPACE = "EVENT_KS";

    private int port = 9443;
    private String localAddress = "localhost";

    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra.yaml");

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("admin", "admin");

        CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator("localhost:9171");
        cluster = HFactory.getOrCreateCluster("TestCluster", hostConfigurator, credentials);
        ServiceHolder.setCassandraConnector(new CassandraConnector());
        cassandraConnector = ServiceHolder.getCassandraConnector();
        StreamDefinitionUtils.readConfigFile();
        ClusterFactory.initCassandraKeySpaces(cluster);

        try {
            streamDefinition1 = EventDefinitionConverterUtils.convertFromJson(CassandraTestConstants.definition);
            streamDefinition2 = EventDefinitionConverterUtils.convertFromJson(CassandraTestConstants.definition2);
            tooLongStreamDefinition = EventDefinitionConverterUtils.convertFromJson(CassandraTestConstants.tooLongdefinition);
            streamDefinition3 = EventDefinitionConverterUtils.convertFromJson(CassandraTestConstants.definition3);
        } catch (MalformedStreamDefinitionException e) {
            fail();
        }
    }

    @Override
    public void before() throws Exception {


    }

    @Override
    public Cluster getCluster() {
        return cluster;
    }

    @Override
    public DataSet getDataSet() {
        return null;
    }

    protected String insertEvent(Cluster cluster, Event eventData, int eventCounter)
            throws MalformedStreamDefinitionException, StreamDefinitionStoreException {

        StreamDefinition streamDef = cassandraConnector.getStreamDefinitionFromCassandra(
                cluster, eventData.getStreamId());
        String streamColumnFamily = CassandraSDSUtils.convertStreamNameToCFName(
                DataBridgeCommonsUtils.getStreamNameFromStreamId(eventData.getStreamId()));

        if ((streamDef == null) || (streamColumnFamily == null)) {
            String errorMsg = "Event stream definition or column family cannot be null";
            log.error(errorMsg);
            throw new StreamDefinitionStoreException(errorMsg);
        }

        Mutator<String> mutator = cassandraConnector.getMutator(cluster);

        // / add  current server time as time stamp if time stamp is not set
        long timestamp;
        if (eventData.getTimeStamp() != 0L) {
            timestamp = eventData.getTimeStamp();
        } else {
            timestamp = System.currentTimeMillis();
        }

        String rowKey = CassandraSDSUtils.createRowKey(timestamp, localAddress, port,
                                                       eventCounter);

        String streamDefDescription = streamDef.getDescription();
        String streamDefNickName = streamDef.getNickName();

        mutator.addInsertion(rowKey, streamColumnFamily,
                             HFactory.createStringColumn(STREAM_ID_KEY, streamDef.getStreamId()));
        mutator.addInsertion(rowKey, streamColumnFamily,
                             HFactory.createStringColumn(STREAM_NAME_KEY, streamDef.getName()));
        mutator.addInsertion(rowKey, streamColumnFamily,
                             HFactory.createStringColumn(STREAM_VERSION_KEY, streamDef.getVersion()));

        if (streamDefDescription != null) {
            mutator.addInsertion(rowKey, streamColumnFamily,
                                 HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, streamDefDescription));
        }
        if (streamDefNickName != null) {
            mutator.addInsertion(rowKey, streamColumnFamily,
                                 HFactory.createStringColumn(STREAM_NICK_NAME_KEY, streamDefNickName));
        }

        mutator.addInsertion(rowKey, streamColumnFamily,
                             HFactory.createColumn(STREAM_TIMESTAMP_KEY, timestamp, stringSerializer,
                                                   longSerializer));

        if (eventData.getArbitraryDataMap() != null) {
            cassandraConnector.insertVariableFields(streamColumnFamily, rowKey, mutator,
                                                    eventData.getArbitraryDataMap());
        }

        if (streamDef.getMetaData() != null) {
            cassandraConnector.prepareDataForInsertion(
                    eventData.getMetaData(), streamDef.getMetaData(), DataType.meta, rowKey,
                                    streamColumnFamily, mutator);

        }
        //Iterate for correlation  data
        if (eventData.getCorrelationData() != null) {
            cassandraConnector.prepareDataForInsertion(
                    eventData.getCorrelationData(), streamDef.getCorrelationData(),
                    DataType.correlation, rowKey, streamColumnFamily, mutator);
        }

        //Iterate for payload data
        if (eventData.getPayloadData() != null) {
            cassandraConnector.prepareDataForInsertion(eventData.getPayloadData(),
                                                       streamDef.getPayloadData(), DataType.payload,
                                                       rowKey, streamColumnFamily, mutator);
        }

        cassandraConnector.commit(mutator);

        return rowKey;
    }


    protected Event getEvent(Cluster cluster, String streamId, String rowKey)
            throws EventProcessingException {

        // get Event definition

        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = cassandraConnector.
                    getStreamDefinitionFromCassandra(cluster, streamId);
        } catch (StreamDefinitionStoreException e) {
            log.error("Error while fetching stream definition from Cassandra..");
        }

        if ((streamDefinition == null)) {
            String errorMsg = "Event stream definition cannot be null";
            log.error(errorMsg);
            throw new EventProcessingException(errorMsg);
        }

        List<Attribute> payloadDefinitions = streamDefinition.getPayloadData();
        List<Attribute> correlationDefinitions = streamDefinition.getCorrelationData();
        List<Attribute> metaDefinitions = streamDefinition.getMetaData();


        // start conversion

        SliceQuery<String, String, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(cassandraConnector.getKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster), stringSerializer, stringSerializer,
                                          byteBufferSerializer);
        String cfName = CassandraSDSUtils.convertStreamNameToCFName(
                DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId));
        sliceQuery.setKey(rowKey).setRange("", "", true, Integer.MAX_VALUE).setColumnFamily(
                cfName);
        ColumnSlice<String, ByteBuffer> columnSlice = sliceQuery.execute().get();

        Event event = new Event();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> correlationData = new ArrayList<Object>();
        List<Object> payloadData = new ArrayList<Object>();

        try {
            event.setStreamId(CassandraSDSUtils.getString(
                    columnSlice.getColumnByName(STREAM_ID_KEY).getValue()));
            event.setTimeStamp(CassandraSDSUtils.getLong(
                    columnSlice.getColumnByName(STREAM_TIMESTAMP_KEY).getValue()));

            if (payloadDefinitions != null) {
                for (Attribute payloadDefinition : payloadDefinitions) {
                    payloadData.add(cassandraConnector.
                            getValueForDataTypeList(columnSlice, payloadDefinition, DataType.payload));
                }
            }

            if (metaDefinitions != null) {
                for (Attribute payloadDefinition : metaDefinitions) {
                    metaData.add(cassandraConnector.
                            getValueForDataTypeList(columnSlice, payloadDefinition, DataType.meta));
                }
            }

            if (correlationDefinitions != null) {
                for (Attribute payloadDefinition : correlationDefinitions) {
                    correlationData.add(correlationData
                            .add(cassandraConnector.
                            getValueForDataTypeList(columnSlice, payloadDefinition, DataType.correlation)));
                }
            }
        } catch (IOException e) {
            String errorMsg = "Error during event data conversions.";
            log.error(errorMsg, e);
            throw new EventProcessingException(errorMsg, e);
        }

        Object[] metas = metaDefinitions == null ? null : metaData.toArray();
        Object[] correlations = correlationDefinitions == null ? null : correlationData.toArray();
        Object[] payloads = payloadDefinitions == null ? null : payloadData.toArray();
        event.setMetaData(metas);
        event.setCorrelationData(correlations);
        event.setPayloadData(payloads);

        return event;
    }

}
