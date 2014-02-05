package org.wso2.carbon.analytics.hive.incremental.metadb;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.HiveIncrementalProcessException;
import org.wso2.carbon.analytics.hive.incremental.util.IncrementalProcessingConstants;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class MetaStore {
    private static Log log = LogFactory.getLog(MetaStore.class);
    private static final StringSerializer stringSerializer = new StringSerializer();
    private static final ByteBufferSerializer byteBufferSerializer = new ByteBufferSerializer();
    private static final LongSerializer longSerializer = new LongSerializer();

    private String emptyStringBuffer = "";

    public static String DEFAULT_STRATEGY_CLASS = "org.apache.cassandra.locator.SimpleStrategy";
    public static String DEFAULT_CONSISTENCY_LEVEL = "QUORUM";
    public static int DEFAULT_REPLICATION_FACTOR = 1;
    public static String DEFAULT_KS_NAME = "HIVE_INCREMENTAL_KS";

    public Cluster getCluster(String username, String password) {
        ClusterInformation clusterInformation = new ClusterInformation(username, password);
        return ServiceHolder.getCassandraDataAccessService().getCluster(clusterInformation);
    }

    public void createCFIfNotExists(Cluster cluster,String ksName,  String cfName, HashMap<String, String> props) {
        String strategyClass = props.get(IncrementalProcessingConstants.DATASOURCE_PROPS_STRATEGY_CLASS);
        if (null == strategyClass) {
            strategyClass = DEFAULT_STRATEGY_CLASS;
        }

        int replicationFactor;
        if (null == props.get(IncrementalProcessingConstants.DATASOURCE_PROPS_REPLICATION_FACTOR)) {
            replicationFactor = DEFAULT_REPLICATION_FACTOR;
        } else {
            replicationFactor = Integer.parseInt(props.get(IncrementalProcessingConstants.DATASOURCE_PROPS_REPLICATION_FACTOR));
        }

        createKeySpaceIfNotExisting(cluster, ksName, strategyClass, replicationFactor);

        String readConsistency = props.get(IncrementalProcessingConstants.DATASOURCE_PROPS_READ_CONSISTENCY);
        if (null == readConsistency) {
            readConsistency = DEFAULT_CONSISTENCY_LEVEL;
        }

        String writeConsistency = props.get(IncrementalProcessingConstants.DATASOURCE_PROPS_WRITE_CONSISTENCY);
        if (null == writeConsistency) {
            writeConsistency = DEFAULT_CONSISTENCY_LEVEL;
        }

        ConsistencyLevelPolicy policy = new MetaStoreConsistencyLevelPolicy(readConsistency, writeConsistency);
        createColumnFamily(cluster, ksName, cfName, policy);
    }

    public boolean createKeySpaceIfNotExisting(Cluster cluster, String keySpaceName,
                                               String strategyClass, int replicationFactor) {

        KeyspaceDefinition keySpaceDef = cluster.describeKeyspace(keySpaceName);

        if (keySpaceDef == null) {
            cluster.addKeyspace(HFactory.createKeyspaceDefinition(
                    keySpaceName, strategyClass, replicationFactor
                    , null));

            keySpaceDef = cluster.describeKeyspace(keySpaceName);
            //Sometimes it takes some time to make keySpaceDef!=null
            int retryCount = 0;
            while (keySpaceDef == null && retryCount < 100) {
                try {
                    Thread.sleep(100);
                    keySpaceDef = cluster.describeKeyspace(keySpaceName);
                    if (keySpaceDef != null) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    public ColumnFamilyDefinition createColumnFamily(Cluster cluster, String keyspaceName,
                                                     String columnFamilyName, ConsistencyLevelPolicy policy) {
        Keyspace keyspace = getKeyspace(keyspaceName, cluster, policy);
        KeyspaceDefinition keyspaceDef =
                cluster.describeKeyspace(keyspace.getKeyspaceName());
        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfdef : cfDef) {
            if (cfdef.getName().equals(columnFamilyName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Column Family " + columnFamilyName + " already exists.");
                }
                return cfdef;
            }
        }
        ColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
        columnFamilyDefinition.setKeyspaceName(keyspaceName);
        columnFamilyDefinition.setName(columnFamilyName);
        columnFamilyDefinition.setKeyValidationClass(ComparatorType.UTF8TYPE.getClassName());
        columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);

        BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
        columnDefinition.setName(stringSerializer.
                toByteBuffer(IncrementalProcessingConstants.LAST_ACCESSED_TIME_COLUMN_NAME));

        columnDefinition.setValidationClass(ComparatorType.LONGTYPE.getClassName());

        columnFamilyDefinition.addColumnDefinition(columnDefinition);

        Map<String, String> compressionOptions = new HashMap<String, String>();
        compressionOptions.put("sstable_compression", "SnappyCompressor");
        compressionOptions.put("chunk_length_kb", "128");
        columnFamilyDefinition.setCompressionOptions(compressionOptions);


        cluster.addColumnFamily(new ThriftCfDef(columnFamilyDefinition), true);

        // give some time to propogate changes
        keyspaceDef =
                cluster.describeKeyspace(keyspace.getKeyspaceName());
        int retryCount = 0;
        while (retryCount < 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }

            for (ColumnFamilyDefinition cfdef : keyspaceDef.getCfDefs()) {
                if (cfdef.getName().equals(columnFamilyName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Column Family " + columnFamilyName + " already exists.");
                    }
                    return cfdef;
                }
            }
            retryCount++;
        }

        throw new RuntimeException("The column family " + columnFamilyName + " was  not created");
    }

    private Keyspace getKeyspace(String keyspace, Cluster cluster, ConsistencyLevelPolicy consistencyLevelPolicy) {
        return HFactory.createKeyspace(keyspace, cluster, consistencyLevelPolicy);
    }

    private Keyspace getKeyspace(String keyspace, Cluster cluster) {
        return HFactory.createKeyspace(keyspace, cluster);
    }

    public List<HColumn<String, Long>> getColumnsOfRow(Cluster cluster, String keyspaceName, String cfName, String rowName)
            throws HiveIncrementalProcessException {
        if (null == keyspaceName) {
            keyspaceName = DEFAULT_KS_NAME;
        }
        Keyspace keyspace = getKeyspace(keyspaceName, cluster);

        SliceQuery<String, String, Long> sliceQuery =
                HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer,
                        longSerializer);
        sliceQuery.setColumnFamily(cfName);

        sliceQuery.setKey(rowName);
        sliceQuery.setRange(emptyStringBuffer, emptyStringBuffer, false, 100);

        QueryResult<ColumnSlice<String, Long>> result;
        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new HiveIncrementalProcessException(exception.getMessage(), exception);
        }

        List<HColumn<String, Long>> hColumnsList;
        hColumnsList = result.get().getColumns();

        return hColumnsList;

    }


    public static String getStringDeserialization(ByteBuffer data) {
        Serializer serializer = stringSerializer;
        Object columnName = serializer.fromByteBuffer(data);

        return columnName.toString();
    }

    public static String getStringDeserialization(Serializer serializer, ByteBuffer data) {
        if (serializer instanceof ByteBufferSerializer) {
            serializer = new StringSerializer();
        }
        Object columnName = serializer.fromByteBuffer(data);
        return columnName.toString();
    }


    public void updateColumn(Cluster cluster, String ksName, String cfName, String rowKey, String colKey, long colValue) {
        Keyspace keyspace = getKeyspace(ksName, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
        mutator.addInsertion(rowKey, cfName,
                HFactory.createColumn(colKey, colValue, stringSerializer, longSerializer));
        mutator.execute();
    }

}
