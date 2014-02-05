/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.utils.persistence;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.utils.config.CFConfigBean;
import org.wso2.carbon.bam.utils.config.ConfigurationException;
import org.wso2.carbon.bam.utils.config.KeyPart;
import org.wso2.carbon.bam.utils.internal.UtilsServiceComponent;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoSQLDataStore implements DataStore {

    private static final Log log = LogFactory.getLog(NoSQLDataStore.class);

    private static StringSerializer stringSerializer = StringSerializer.get();
    private static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();

    private Cluster cluster = null;
    private Keyspace keySpace = null;
    private KeyspaceDefinition keySpaceDef = null;

    private static NoSQLDataStore instance = null;

    public static final String BAM_KEYSPACE = "BAMKeyspace";
    public static final String META_COLUMN_FAMILY_NAME = "CFInfo";
    public static final String INDEX_COLUMN_FAMILY_NAME = "CFIndexes";
    public static final String CURSORS_COLUMN_FAMILY_NAME = "CFCursors";
    public static final String INDEX_ROW_KEY = "indexRowKey";
    public static final String GRANULARITY = "granularity";
    public static final String DEFAULT_COLUMN_FAMILY = "defaultCF";
    public static final String SECONDARY_COLUMN_FAMILY = "secondaryCF";
    public static final String ROW_KEY = "rowKey";

    public static synchronized NoSQLDataStore getNoSQLDataStore() throws ConfigurationException {
        if (instance == null) {
            instance = new NoSQLDataStore();
        }
        return instance;

    }

    public void initializeStore(Map<String, String> credentials, boolean force)
            throws InitializationException {
        String userName = credentials.get(PersistencyConstants.USER_NAME);
        String password = credentials.get(PersistencyConstants.PASSWORD);

        if (!force) {
            if (!isInitialized()) {
                try {
                    initializeBAMNoSQLDataStore(userName, password);
                } catch (ConfigurationException e) {
                    throw new InitializationException("Unable to initialize store..", e);
                }
            }
        } else {
            try {
                initializeBAMNoSQLDataStore(userName, password);
            } catch (ConfigurationException e) {
                throw new InitializationException("Unable to initialize store..", e);
            }
        }

    }

    public NoSQLDataStore() {
        //initializeBAMNoSQLDataStore();
    }

    public Keyspace getBamKeyspace() {
        return keySpace;
    }

    private synchronized Cluster initializeBAMNoSQLDataStore(String userName, String password)
            throws ConfigurationException {

//                Todo: Use the data access service, once the related bug is fixed in Cassandra component
//            cluster = ReceiverUtils.getDataAccessService().getCluster(new ClusterInformation("admin", "admin"));
//                Todo : Get rid of hard coded username/ password when implementing MT
        ClusterInformation clusterInfo = new ClusterInformation(userName, password);
        clusterInfo.setClusterName(userName);

        cluster = CassandraUtils.createCluster(clusterInfo);
        keySpaceDef = cluster.describeKeyspace(BAM_KEYSPACE);
        if (keySpaceDef == null) {
            cluster.addKeyspace(HFactory.createKeyspaceDefinition(BAM_KEYSPACE));
            keySpaceDef = cluster.describeKeyspace(BAM_KEYSPACE);
        }

        keySpace = HFactory.createKeyspace(BAM_KEYSPACE, cluster);

/*        keySpaceDefPool.put(userName, bamKeyspaceDefinition);
        keySpacePool.put(userName, HFactory.createKeyspace(BAM_KEYSPACE, cluster));*/
        //setBamKeyspace(HFactory.createKeyspace(BAM_KEYSPACE, cluster));

        String domain = MultitenantUtils.getTenantDomain(userName);

        int tenantId;
        try {
            tenantId = UtilsServiceComponent.getRealmService().getTenantManager().getTenantId(domain);
        } catch (UserStoreException e) {
            throw new ConfigurationException("Unable to get tenant information..", e);
        }

//        List<CFConfigBean> cfConfigurations = ConfigurationHolder.getInstance().
//                getIndexConfigurations(tenantId);
//        for (CFConfigBean cfConfiguration : cfConfigurations) {
//            createCF(cfConfiguration.getCfName());
//        }

        // Create meta data column families
        createCF(META_COLUMN_FAMILY_NAME);
        createCF(INDEX_COLUMN_FAMILY_NAME);
        createCF(CURSORS_COLUMN_FAMILY_NAME);

        // Create base event tables
        createCF(PersistencyConstants.EVENT_TABLE);
        createCF(PersistencyConstants.META_TABLE);
        createCF(PersistencyConstants.CORRELATION_TABLE);

//        persistColumnFamilyInformation(cfConfigurations);

        //clusterPool.put(userName, cluster);

        return cluster;

    }

    public boolean isInitialized() {

        if (cluster != null) {
            return true;
        }

        return false;
    }


    public void persistColumnFamilyConfiguration(CFConfigBean cfConfig) {
        List<CFConfigBean> config = new ArrayList<CFConfigBean>();
        config.add(cfConfig);

        persistColumnFamilyInformation(config);
    }

    public void persistIndexes(String cfName, List<KeyPart> indexes, Map<String, String> cfData) {

        for (KeyPart index : indexes) {
            if (index.isIndexStored()) {
                String indexValue = cfData.get(index.getName());

                if (indexValue != null) {
                    String rowKey = cfName + "---" + index.getName();

                    Map<String, String> data = new HashMap<String, String>();
                    data.put(indexValue, "");

                    persistData(INDEX_COLUMN_FAMILY_NAME, rowKey, data);

/*                    data = new HashMap<String, String>();
                    data.put(rowKey, "");

                    persistData(INDEX_COLUMN_FAMILY_NAME, DEFAULT_INDEX_ROW_KEY, data);*/
                }
            }
        }

    }

    private void persistColumnFamilyInformation(List<CFConfigBean> cfConfigurations) {
        for (CFConfigBean cfConfig : cfConfigurations) {
            String cfName = cfConfig.getCfName();

            String indexRowKey;
            if (cfConfig.getIndexRowKey() != null) {
                indexRowKey = cfConfig.getIndexRowKey();
            } else {
                indexRowKey = "";
            }

            String granularity;
            if (cfConfig.getGranularity() != null) {
                granularity = cfConfig.getGranularity();
            } else {
                granularity = "";
            }

            Map<String, String> cfData = new HashMap<String, String>();
            cfData.put(INDEX_ROW_KEY, indexRowKey);
            cfData.put(GRANULARITY, granularity);
            cfData.put(DEFAULT_COLUMN_FAMILY, Boolean.valueOf(cfConfig.isDefaultCF()).toString());
            cfData.put(SECONDARY_COLUMN_FAMILY, Boolean.valueOf(cfConfig.isPrimaryCF()).toString());

            int counter = 0;
            if (cfConfig.getRowKeyParts() != null) {
                for (KeyPart part : cfConfig.getRowKeyParts()) {
                    cfData.put(ROW_KEY + (counter++), part.getName() + ":" + part.isIndexStored()
                                                      + ":" + part.getType());
                }
            }

            persistData(META_COLUMN_FAMILY_NAME, cfName, cfData);

        }

    }

    public void setLastCursorForColumnFamily(String cfName, String sequenceName,
                                             int analyzerIndex, String lastCursor) {

        String key = sequenceName + analyzerIndex;
        Map<String, String> cfData = new HashMap<String, String>();
        cfData.put(key, lastCursor);

        persistData(CURSORS_COLUMN_FAMILY_NAME, cfName, cfData);

    }

    private ThreadLocal<Boolean> startBatchCommit = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private ThreadLocal<Mutator<String>> mutatorThreadLocal = new ThreadLocal<Mutator<String>>();

    public void startBatchCommit() {
        startBatchCommit.set(true);
        mutatorThreadLocal.set(HFactory.createMutator(getBamKeyspace(), stringSerializer));
    }

    public void endBatchCommit() {
        mutatorThreadLocal.get().execute();
        mutatorThreadLocal.set(null);
        startBatchCommit.set(false);

    }

    public boolean persistData(String CFName, String rowKey, Map<String, String> data) {
        if (!cfExists(CFName)) {
            createCF(CFName);
        }

        Mutator<String> mutator;
        if (startBatchCommit.get()) {
            mutator = mutatorThreadLocal.get();
        } else {
            mutator = HFactory.createMutator(getBamKeyspace(), stringSerializer);
        }

        for (Map.Entry<String, String> column : data.entrySet()) {
            mutator.addInsertion(rowKey, CFName, HFactory.createStringColumn(column.getKey(), column.getValue()));
        }

        //mutator.addInsertion(PersistencyConstants.ROW_INDEX, CFName, HFactory.createStringColumn(rowKey, ""));

        if (!startBatchCommit.get()) {
            mutator.execute();
        }
        return true;
    }

    public boolean persistBinaryData(String cfName, String rowKey, Map<String, ByteBuffer> data) {
        if (!cfExists(cfName)) {
            createCF(cfName);
        }

        Mutator<String> mutator;
        if (startBatchCommit.get()) {
            mutator = mutatorThreadLocal.get();
        } else {
            mutator = HFactory.createMutator(getBamKeyspace(), stringSerializer);
        }

        for (Map.Entry<String, ByteBuffer> column : data.entrySet()) {
            mutator.addInsertion(rowKey, cfName, HFactory.createColumn(column.getKey(),
                                                                       column.getValue(),
                                                                       stringSerializer,
                                                                       byteBufferSerializer));
        }

        if (!startBatchCommit.get()) {
            mutator.execute();
        }
        return true;

    }

    private List<String> cfList = new ArrayList<String>();

    private boolean cfExists(String CFName) {
        return cfList.contains(CFName);
    }

/*    public boolean isColumnFamilyPresent(String cfName) {
        BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
        columnFamilyDefinition.setColumnType(ColumnType.STANDARD);
        columnFamilyDefinition.setName(cfName);
        columnFamilyDefinition.setKeyspaceName(BAM_KEYSPACE);

        synchronized (this) {
            if (keySpaceDef != null) {
                for (ColumnFamilyDefinition cfDef : keySpaceDef.getCfDefs()) {
                    if (cfDef.getName().equals(cfName)) {
                        return true;
                    }
                }
            }
        }

        return false;

    }*/

    // This is a hacky way to detect whether the column family is present in Cassandra.
    // (Method using KeyspaceDef doesn't work reliably. Gives false negatives).
    // This should be replaced with a proper way to detect a column family if such method is found
    public boolean isColumnFamilyPresent(String cfName) {
        MultigetSliceQuery<String, String, String> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(getBamKeyspace(), stringSerializer,
                                                  stringSerializer, stringSerializer);
        multigetSliceQuery.setColumnFamily(cfName);
        multigetSliceQuery.setKeys("test");
        multigetSliceQuery.setRange("", "", false, 1);

        try {
            multigetSliceQuery.execute();
        } catch (HInvalidRequestException e) {
            return false;
        }

        return true;
    }

    public boolean createCF(String CFName) {
        BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
        columnFamilyDefinition.setColumnType(ColumnType.STANDARD);
        columnFamilyDefinition.setName(CFName);
        columnFamilyDefinition.setKeyspaceName(BAM_KEYSPACE);

        synchronized (this) {
            boolean cfDefFound = false;

            if (keySpaceDef != null) {
                for (ColumnFamilyDefinition cfDef : keySpaceDef.getCfDefs()) {
                    if (cfDef.getName().equals(CFName)) {
                        cfDefFound = true;
                        break;
                    }
                }
                // Column Family not found, so create it
                if (!cfDefFound) {
                    ThriftCfDef cfDef = new ThriftCfDef(columnFamilyDefinition);
                    cluster.addColumnFamily(cfDef);
                }
                cfList.add(CFName);
            } else {
                return false;
            }
        }
        return true;
    }

}


