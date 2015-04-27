/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.output.adapter.cassandra;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.cassandra.internal.util.CassandraEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CassandraEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(CassandraEventAdapter.class);
    private static ThreadPoolExecutor threadPoolExecutor;
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputEventAdapterConfiguration, EventAdapterInfo>> tenantedCassandraClusterCache = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputEventAdapterConfiguration, EventAdapterInfo>>();
    private StringSerializer sser = new StringSerializer();
    private int tenantId;
    private String keySpaceName;
    private String columnFamilyName;
    private Mutator<String> mutator;
    private String uuid;
    private EventAdapterInfo eventAdapterInfo;
    private MessageInfo messageInfo;


    public CassandraEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                 Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        //ThreadPoolExecutor will be assigned  if it is null
        if (threadPoolExecutor == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(CassandraEventAdapterConstants.MIN_THREAD_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(CassandraEventAdapterConstants.MIN_THREAD_NAME));
            } else {
                minThread = CassandraEventAdapterConstants.MIN_THREAD;
            }

            if (globalProperties.get(CassandraEventAdapterConstants.MAX_THREAD_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(CassandraEventAdapterConstants.MAX_THREAD_NAME));
            } else {
                maxThread = CassandraEventAdapterConstants.MAX_THREAD;
            }

            if (globalProperties.get(CassandraEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        CassandraEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = CassandraEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME;
            }

            threadPoolExecutor = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
        }

    }

    @Override

    public void testConnect() {
        String cassandraHosts = eventAdapterConfiguration.getStaticProperties().get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTNAME) + ":" + eventAdapterConfiguration.getStaticProperties().get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT);

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", eventAdapterConfiguration.getStaticProperties().get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_USER_NAME));
        credentials.put("password", eventAdapterConfiguration.getStaticProperties().get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PASSWORD));


        CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(cassandraHosts);
        hostConfigurator.setRetryDownedHosts(false);
        // this.cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator, credentials);
        Cluster cluster = new ThriftCluster("test-cluster", hostConfigurator, credentials);
        Set knownPools = cluster.getKnownPoolHosts(true);
        if (knownPools == null || knownPools.size() == 0) {
            log.error("Couldn't connect to Cassandra cluster");
        }
    }

    @Override
    public void connect() {
        ConcurrentHashMap<OutputEventAdapterConfiguration, EventAdapterInfo> cassandraClusterCache = null;
        Map<String, String> staticProperties = eventAdapterConfiguration.getStaticProperties();

        try{
                cassandraClusterCache = tenantedCassandraClusterCache.get(tenantId);
                if (null == cassandraClusterCache) {
                    cassandraClusterCache = new ConcurrentHashMap<OutputEventAdapterConfiguration, EventAdapterInfo>();
                    if (null != tenantedCassandraClusterCache.putIfAbsent(tenantId, cassandraClusterCache)) {
                        cassandraClusterCache = tenantedCassandraClusterCache.get(tenantId);
                    }
                }

                eventAdapterInfo = cassandraClusterCache.get(eventAdapterConfiguration);
                if (null == eventAdapterInfo) {


                    Map<String, String> credentials = new HashMap<String, String>();
                    credentials.put("username", staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_USER_NAME));
                    credentials.put("password", staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PASSWORD));

                    // cleaning existing cached copies.
                    Cluster cluster = HFactory.getCluster(staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_CLUSTER_NAME));
                    if (cluster != null) {
                        HFactory.shutdownCluster(cluster);
                    }

                    CassandraHostConfigurator chc = new CassandraHostConfigurator(
                            staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTNAME) + ":" +
                                    staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT));
                    String clusterName = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_CLUSTER_NAME);

                    cluster = HFactory.createCluster(clusterName, chc, credentials);


                    if (cluster.getKnownPoolHosts(true).size() < 1) {
                        // not properly connected.
                        log.error("Cannot connect to the Cassandra cluster: " + cluster.getName() + ". Please check the configuration.");
                        HFactory.shutdownCluster(cluster);
                        return;
                    }
                    String indexAllColumnsString = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_INDEX_ALL_COLUMNS);
                    boolean indexAllColumns = false;
                    if (indexAllColumnsString != null && indexAllColumnsString.equals("true")) {
                        indexAllColumns = true;
                    }
                    eventAdapterInfo = new EventAdapterInfo(cluster, indexAllColumns);
                    if (null != cassandraClusterCache.putIfAbsent(eventAdapterConfiguration, eventAdapterInfo)) {
                        eventAdapterInfo = cassandraClusterCache.get(eventAdapterConfiguration);
                    } else {
                        log.info("Initiated Cassandra Writer " + eventAdapterConfiguration.getName());
                    }
                }

                keySpaceName = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_KEY_SPACE_NAME);
                columnFamilyName = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_COLUMN_FAMILY_NAME);

                messageInfo = eventAdapterInfo.getMessageInfoMap().get(eventAdapterConfiguration);
                if (null == messageInfo) {
                    Keyspace keyspace = HFactory.createKeyspace(keySpaceName, eventAdapterInfo.getCluster());
                    messageInfo = new MessageInfo(keyspace);
                    if (null != eventAdapterInfo.getMessageInfoMap().putIfAbsent(eventAdapterConfiguration, messageInfo)) {
                        messageInfo = eventAdapterInfo.getMessageInfoMap().get(eventAdapterConfiguration);
                    }
                }

                if (eventAdapterInfo.getCluster().describeKeyspace(keySpaceName) == null) {
                    BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
                    columnFamilyDefinition.setKeyspaceName(keySpaceName);
                    columnFamilyDefinition.setName(columnFamilyName);
                    columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
                    columnFamilyDefinition
                            .setDefaultValidationClass(ComparatorType.UTF8TYPE
                                    .getClassName());
                    columnFamilyDefinition
                            .setKeyValidationClass(ComparatorType.UTF8TYPE
                                    .getClassName());

                    ColumnFamilyDefinition cfDef = new ThriftCfDef(
                            columnFamilyDefinition);

                    KeyspaceDefinition keyspaceDefinition = HFactory
                            .createKeyspaceDefinition(keySpaceName,
                                    "org.apache.cassandra.locator.SimpleStrategy", 1,
                                    Arrays.asList(cfDef));
                    eventAdapterInfo.getCluster().addKeyspace(keyspaceDefinition);
                    KeyspaceDefinition fromCluster = eventAdapterInfo.getCluster().describeKeyspace(keySpaceName);
                    messageInfo.setColumnFamilyDefinition(new BasicColumnFamilyDefinition(fromCluster.getCfDefs().get(0)));
                } else {
                    KeyspaceDefinition fromCluster = eventAdapterInfo.getCluster().describeKeyspace(keySpaceName);
                    for (ColumnFamilyDefinition columnFamilyDefinition : fromCluster.getCfDefs()) {
                        if (columnFamilyDefinition.getName().equals(columnFamilyName)) {
                            messageInfo.setColumnFamilyDefinition(new BasicColumnFamilyDefinition(columnFamilyDefinition));
                            break;
                        }
                    }
                }


                mutator = HFactory.createMutator(messageInfo.getKeyspace(), sser);
                uuid = UUID.randomUUID().toString();
            }catch (Throwable t) {
                if (cassandraClusterCache != null) {
                    cassandraClusterCache.remove(eventAdapterConfiguration);
                }
                log.error("Cannot connect to Cassandra: " + t.getMessage(), t);
            }
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        ConcurrentHashMap<OutputEventAdapterConfiguration, EventAdapterInfo> cassandraClusterCache = null;
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (message instanceof Map) {
            try{
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) message).entrySet()) {

                    if (eventAdapterInfo.isIndexAllColumns() && !messageInfo.getColumnNames().contains(entry.getKey())) {
                        BasicColumnFamilyDefinition columnFamilyDefinition = messageInfo.getColumnFamilyDefinition();
                        BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
                        columnDefinition.setName(StringSerializer.get().toByteBuffer(
                                entry.getKey()));
                        columnDefinition.setIndexType(ColumnIndexType.KEYS);
                        columnDefinition.setIndexName(keySpaceName + "_" + columnFamilyName + "_" + entry.getKey() + "_Index");
                        columnDefinition.setValidationClass(ComparatorType.UTF8TYPE
                                .getClassName());
                        columnFamilyDefinition.addColumnDefinition(columnDefinition);
                        eventAdapterInfo.getCluster().updateColumnFamily(new ThriftCfDef(columnFamilyDefinition));
                        messageInfo.getColumnNames().add(entry.getKey());
                    }
                    mutator.insert(uuid, columnFamilyName, HFactory.createStringColumn(entry.getKey(), entry.getValue().toString()));
                }

                mutator.execute();
            } catch (Throwable t) {
                if (cassandraClusterCache != null) {
                    cassandraClusterCache.remove(eventAdapterConfiguration);
                }
                log.error("Cannot publish message to Cassandra: " + t.getMessage(), t);
            }
        }

    }

    @Override
    public void disconnect() {
        //close producer
        ConcurrentHashMap<OutputEventAdapterConfiguration, EventAdapterInfo> cassandraClusterCache = tenantedCassandraClusterCache.get(tenantId);
        if (cassandraClusterCache != null) {
            cassandraClusterCache.remove(eventAdapterConfiguration);
        }
    }

    @Override
    public void destroy() {
        //not required
    }



    class MessageInfo {
        private Keyspace keyspace;
        private BasicColumnFamilyDefinition columnFamilyDefinition;
        private List<String> columnNames = new ArrayList<String>();


        MessageInfo(Keyspace keyspace) {
            this.keyspace = keyspace;
        }

        public Keyspace getKeyspace() {
            return keyspace;
        }

        public BasicColumnFamilyDefinition getColumnFamilyDefinition() {
            return columnFamilyDefinition;
        }

        public void setColumnFamilyDefinition(
                BasicColumnFamilyDefinition columnFamilyDefinition) {
            this.columnFamilyDefinition = columnFamilyDefinition;
        }

        public List<String> getColumnNames() {
            return columnNames;
        }
    }

    class EventAdapterInfo {
        private Cluster cluster;
        private boolean indexAllColumns;
        private ConcurrentHashMap<OutputEventAdapterConfiguration, MessageInfo> messageInfoMap = new ConcurrentHashMap<OutputEventAdapterConfiguration, MessageInfo>();

        EventAdapterInfo(Cluster cluster, boolean indexAllColumns) {
            this.cluster = cluster;
            this.indexAllColumns = indexAllColumns;
        }

        public Cluster getCluster() {
            return cluster;
        }

        public ConcurrentHashMap<OutputEventAdapterConfiguration, MessageInfo> getMessageInfoMap() {
            return messageInfoMap;
        }

        public boolean isIndexAllColumns() {
            return indexAllColumns;
        }
    }

}
