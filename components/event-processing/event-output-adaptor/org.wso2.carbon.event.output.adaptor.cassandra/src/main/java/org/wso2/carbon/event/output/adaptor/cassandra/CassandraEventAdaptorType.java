/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.cassandra;

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
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adaptor.cassandra.internal.util.CassandraEventAdaptorConstants;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorConfigException;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CassandraEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(CassandraEventAdaptorType.class);
    private StringSerializer sser = new StringSerializer();
    private static CassandraEventAdaptorType cassandraEventAdaptor = new CassandraEventAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputEventAdaptorConfiguration, EventAdaptorInfo>> tenantedCassandraClusterCache = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputEventAdaptorConfiguration, EventAdaptorInfo>>();

    private CassandraEventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.MAP);
        return supportOutputMessageTypes;
    }

    /**
     * @return cassandra event adaptor instance
     */
    public static CassandraEventAdaptorType getInstance() {

        return cassandraEventAdaptor;
    }

    /**
     * @return name of the cassandra event adaptor
     */
    @Override
    protected String getName() {
        return CassandraEventAdaptorConstants.ADAPTOR_TYPE_CASSANDRA;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        //To change body of implemented methods use File | Settings | File Templates.
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.cassandra.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set cluster name
        Property clusterName = new Property(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_CLUSTER_NAME);
        clusterName.setDisplayName(
                resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_CLUSTER_NAME));
        clusterName.setRequired(true);
        clusterName.setHint(resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_CLUSTER_NAME_HINT));
        propertyList.add(clusterName);

        // set host name
        Property hostName = new Property(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_HOSTNAME);
        hostName.setDisplayName(
                resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_HOSTNAME));
        hostName.setRequired(true);
        propertyList.add(hostName);


        // set port
        Property port = new Property(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_PORT);
        port.setDisplayName(
                resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_PORT));
        port.setRequired(true);
        propertyList.add(port);


        // set user name
        Property userName = new Property(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_USER_NAME);
        userName.setDisplayName(
                resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_USER_NAME));
        userName.setRequired(true);
        propertyList.add(userName);


        // set password
        Property password = new Property(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_PASSWORD);
        password.setDisplayName(
                resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_PASSWORD));
        password.setRequired(true);
        password.setSecured(true);
        propertyList.add(password);


        // set index all columns
        Property indexAllColumns = new Property(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_INDEX_ALL_COLUMNS);
        indexAllColumns.setDisplayName(
                resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_INDEX_ALL_COLUMNS));
        indexAllColumns.setOptions(new String[]{"true", "false"});
        indexAllColumns.setDefaultValue("false");
        indexAllColumns.setHint(resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_INDEX_ALL_COLUMNS_HINT));
        propertyList.add(indexAllColumns);

        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // key space
        Property keySpace = new Property(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_KEY_SPACE_NAME);
        keySpace.setDisplayName(
                resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_KEY_SPACE_NAME));
        keySpace.setRequired(true);
        propertyList.add(keySpace);

        // column family
        Property columnFamily = new Property(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_COLUMN_FAMILY_NAME);
        columnFamily.setDisplayName(
                resourceBundle.getString(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_COLUMN_FAMILY_NAME));
        columnFamily.setRequired(true);
        propertyList.add(columnFamily);

        return propertyList;
    }

    /**
     * @param outputEventMessageConfiguration
     *                 - topic name to publish messages
     * @param message  - is and Object[]{Event, EventDefinition}
     * @param outputEventAdaptorConfiguration
     *
     * @param tenantId
     */
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        ConcurrentHashMap<OutputEventAdaptorConfiguration, EventAdaptorInfo> cassandraClusterCache = null;
        if (message instanceof Map) {
            try {

                cassandraClusterCache = tenantedCassandraClusterCache.get(tenantId);
                if (null == cassandraClusterCache) {
                    cassandraClusterCache = new ConcurrentHashMap<OutputEventAdaptorConfiguration, EventAdaptorInfo>();
                    if (null != tenantedCassandraClusterCache.putIfAbsent(tenantId, cassandraClusterCache)) {
                        cassandraClusterCache = tenantedCassandraClusterCache.get(tenantId);
                    }
                }

                EventAdaptorInfo eventAdaptorInfo = cassandraClusterCache.get(outputEventAdaptorConfiguration);
                if (null == eventAdaptorInfo) {
                    Map<String, String> properties = outputEventAdaptorConfiguration.getOutputProperties();

                    Map<String, String> credentials = new HashMap<String, String>();
                    credentials.put("username", properties.get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_USER_NAME));
                    credentials.put("password", properties.get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_PASSWORD));

                    // cleaning existing cached copies.
                    Cluster cluster = HFactory.getCluster(properties.get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_CLUSTER_NAME));
                    if (cluster != null) {
                        HFactory.shutdownCluster(cluster);
                    }

                    cluster = HFactory.createCluster(properties.get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_CLUSTER_NAME),
                                                             new CassandraHostConfigurator(
                                                                     properties.get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_HOSTNAME) + ":" +
                                                                     properties.get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_PORT)), credentials);


                    if (cluster.getKnownPoolHosts(true).size() < 1) {
                        // not properly connected.
                        log.error("Cannot connect to the Cassandra cluster: " + cluster.getName() + ". Please check the configuration.");
                        HFactory.shutdownCluster(cluster);
                        return;
                    }
                    String indexAllColumnsString = properties.get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_INDEX_ALL_COLUMNS);
                    boolean indexAllColumns = false;
                    if (indexAllColumnsString != null && indexAllColumnsString.equals("true")) {
                        indexAllColumns = true;
                    }
                    eventAdaptorInfo = new EventAdaptorInfo(cluster, indexAllColumns);
                    if (null != cassandraClusterCache.putIfAbsent(outputEventAdaptorConfiguration, eventAdaptorInfo)) {
                        eventAdaptorInfo = cassandraClusterCache.get(outputEventAdaptorConfiguration);
                    } else {
                        log.info("Initiated Cassandra Writer " + outputEventAdaptorConfiguration.getName());
                    }
                }


                String keySpaceName = outputEventMessageConfiguration.getOutputMessageProperties().get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_KEY_SPACE_NAME);
                String columnFamilyName = outputEventMessageConfiguration.getOutputMessageProperties().get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_COLUMN_FAMILY_NAME);
                MessageInfo messageInfo = eventAdaptorInfo.getMessageInfoMap().get(outputEventMessageConfiguration);
                if (null == messageInfo) {
                    Keyspace keyspace = HFactory.createKeyspace(keySpaceName, eventAdaptorInfo.getCluster());
                    messageInfo = new MessageInfo(keyspace);
                    if (null != eventAdaptorInfo.getMessageInfoMap().putIfAbsent(outputEventMessageConfiguration, messageInfo)) {
                        messageInfo = eventAdaptorInfo.getMessageInfoMap().get(outputEventMessageConfiguration);
                    }
                }

                if (eventAdaptorInfo.getCluster().describeKeyspace(keySpaceName) == null) {
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
                    eventAdaptorInfo.getCluster().addKeyspace(keyspaceDefinition);
                    KeyspaceDefinition fromCluster = eventAdaptorInfo.getCluster().describeKeyspace(keySpaceName);
                    messageInfo.setColumnFamilyDefinition(new BasicColumnFamilyDefinition(fromCluster.getCfDefs().get(0)));
                } else {
                    KeyspaceDefinition fromCluster = eventAdaptorInfo.getCluster().describeKeyspace(keySpaceName);
                    for (ColumnFamilyDefinition columnFamilyDefinition : fromCluster.getCfDefs()) {
                        if (columnFamilyDefinition.getName().equals(columnFamilyName)) {
                            messageInfo.setColumnFamilyDefinition(new BasicColumnFamilyDefinition(columnFamilyDefinition));
                            break;
                        }
                    }
                }


                Mutator<String> mutator = HFactory.createMutator(messageInfo.getKeyspace(), sser);
                String uuid = UUID.randomUUID().toString();
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) message).entrySet()) {

                    if (eventAdaptorInfo.isIndexAllColumns() && !messageInfo.getColumnNames().contains(entry.getKey())) {
                        BasicColumnFamilyDefinition columnFamilyDefinition = messageInfo.getColumnFamilyDefinition();
                        BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
                        columnDefinition.setName(StringSerializer.get().toByteBuffer(
                                entry.getKey()));
                        columnDefinition.setIndexType(ColumnIndexType.KEYS);
                        columnDefinition.setIndexName(keySpaceName + "_" + columnFamilyName + "_" + entry.getKey() + "_Index");
                        columnDefinition.setValidationClass(ComparatorType.UTF8TYPE
                                                                    .getClassName());
                        columnFamilyDefinition.addColumnDefinition(columnDefinition);
                        eventAdaptorInfo.getCluster().updateColumnFamily(new ThriftCfDef(columnFamilyDefinition));
                        messageInfo.getColumnNames().add(entry.getKey());
                    }
                    mutator.insert(uuid, columnFamilyName, HFactory.createStringColumn(entry.getKey(), entry.getValue().toString()));
                }

                mutator.execute();
            }catch (Throwable t){
                if (cassandraClusterCache != null) {
                    cassandraClusterCache.remove(outputEventAdaptorConfiguration);
                }
                log.error("Cannot connect to Cassandra "+t.getMessage());
            }
        }
    }


    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        String cassandraHosts = outputEventAdaptorConfiguration.getOutputProperties().get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_HOSTNAME) + ":" + outputEventAdaptorConfiguration.getOutputProperties().get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_PORT);

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", outputEventAdaptorConfiguration.getOutputProperties().get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_USER_NAME));
        credentials.put("password", outputEventAdaptorConfiguration.getOutputProperties().get(CassandraEventAdaptorConstants.ADAPTOR_CASSANDRA_PASSWORD));


        CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(cassandraHosts);
        hostConfigurator.setRetryDownedHosts(false);
        // this.cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator, credentials);
        Cluster cluster = new ThriftCluster("test-cluster", hostConfigurator, credentials);
        Set knownPools = cluster.getKnownPoolHosts(true);
        if (knownPools == null || knownPools.size() == 0) {
            throw new OutputEventAdaptorEventProcessingException("Couldn't connect to Cassandra cluster");
        }
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

        public void setColumnFamilyDefinition(BasicColumnFamilyDefinition columnFamilyDefinition) {
            this.columnFamilyDefinition = columnFamilyDefinition;
        }

        public List<String> getColumnNames() {
            return columnNames;
        }
    }

    class EventAdaptorInfo {
        private Cluster cluster;
        private boolean indexAllColumns;
        private ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, MessageInfo> messageInfoMap = new ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, MessageInfo>();

        EventAdaptorInfo(Cluster cluster, boolean indexAllColumns) {
            this.cluster = cluster;
            this.indexAllColumns = indexAllColumns;
        }

        public Cluster getCluster() {
            return cluster;
        }

        public ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, MessageInfo> getMessageInfoMap() {
            return messageInfoMap;
        }

        public boolean isIndexAllColumns() {
            return indexAllColumns;
        }
    }
}
