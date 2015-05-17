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
import org.wso2.carbon.event.output.adapter.cassandra.internal.util.CassandraEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.*;

public class CassandraEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(CassandraEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;

    private Map<String, String> credentials = null;
    private String columnFamilyName;
    private Mutator<String> mutator;
    private Cluster cluster;

    public CassandraEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                 Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        Map<String, String> staticProperties = eventAdapterConfiguration.getStaticProperties();

        String username = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_USER_NAME);
        String password = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PASSWORD);
        if (username != null && password != null) {
            credentials = new HashMap<String, String>();
            credentials.put("username", username);
            credentials.put("password", password);
        } else if (username != null || password != null) {
            throw new OutputEventAdapterException("Both username & password properties should be null or not null for Cassandra Output Adapter '" + eventAdapterConfiguration.getName() + "'");
        }

    }

    @Override
    public void testConnect() {
        Map<String, String> staticProperties = eventAdapterConfiguration.getStaticProperties();

        String clusterName = CassandraEventAdapterConstants.CASSANDRA_CLUSTER_NAME_PREFIX + eventAdapterConfiguration.getName();

        CassandraHostConfigurator chc = new CassandraHostConfigurator();
        chc.setHosts(staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTS));
        if (staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT) != null) {
            chc.setPort(Integer.parseInt(staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT)));
        }

        Cluster cluster = HFactory.createCluster(clusterName, chc, credentials);
        try {
            Set knownPools = cluster.getKnownPoolHosts(true);
            if (knownPools == null || knownPools.size() == 0) {
                throw new ConnectionUnavailableException("Couldn't connect to Cassandra cluster no known hosts found");
            }
        } finally {
            HFactory.shutdownCluster(cluster);
        }

    }

    @Override
    public void connect() {
        Map<String, String> staticProperties = eventAdapterConfiguration.getStaticProperties();

        String clusterName = CassandraEventAdapterConstants.CASSANDRA_CLUSTER_NAME_PREFIX + eventAdapterConfiguration.getName();

        CassandraHostConfigurator chc = new CassandraHostConfigurator();
        chc.setHosts(staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTS));
        if (staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT) != null) {
            chc.setPort(Integer.parseInt(staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT)));
        }
        cluster = HFactory.createCluster(clusterName, chc, credentials);

        if (cluster.getKnownPoolHosts(true).size() < 1) {
            // not properly connected.
            throw new ConnectionUnavailableException("Cannot connect to the Cassandra cluster '" + cluster.getName() +
                    "' from Output Cassandra Adapter '" + eventAdapterConfiguration.getName() + "'");
        }

        String keySpaceName = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_KEY_SPACE_NAME);
        Keyspace keyspace = HFactory.createKeyspace(keySpaceName, cluster);

        columnFamilyName = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_COLUMN_FAMILY_NAME);

        BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
        columnFamilyDefinition.setKeyspaceName(keySpaceName);
        columnFamilyDefinition.setName(columnFamilyName);
        columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
        columnFamilyDefinition.setDefaultValidationClass(ComparatorType.UTF8TYPE.getClassName());
        columnFamilyDefinition.setKeyValidationClass(ComparatorType.UTF8TYPE.getClassName());

        String strategy = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_STRATEGY_CLASS);
        if (strategy == null) {
            strategy = CassandraEventAdapterConstants.ADAPTER_CASSANDRA_DEFAULT_STRATEGY_CLASS;
        }

        String indexColumnsString = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_INDEXED_COLUMNS);
        if (indexColumnsString != null && !indexColumnsString.isEmpty()) {
            String[] indexedColumns = indexColumnsString.split(",");
            for (String key : indexedColumns) {
                BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
                columnDefinition.setName(StringSerializer.get().toByteBuffer(key));
                columnDefinition.setIndexType(ColumnIndexType.KEYS);
                columnDefinition.setIndexName(keySpaceName + "_" + columnFamilyName + "_" + key + "_Index");
                columnDefinition.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
                columnFamilyDefinition.addColumnDefinition(columnDefinition);
            }
        }

        int replicationFactor = CassandraEventAdapterConstants.ADAPTER_CASSANDRA_DEFAULT_REPLICATION_FACTOR;
        String replicationFactorString = staticProperties.get(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_REPLICATION_FACTOR);
        if (replicationFactorString != null) {
            replicationFactor = Integer.parseInt(replicationFactorString);
        }

        ColumnFamilyDefinition cfDef = new ThriftCfDef(columnFamilyDefinition);

        KeyspaceDefinition existingKeyspaceDefinition = cluster.describeKeyspace(keySpaceName);
        if (existingKeyspaceDefinition == null) {
            cluster.addKeyspace(HFactory.createKeyspaceDefinition(keySpaceName, strategy, replicationFactor, Arrays.asList(cfDef)));
        } else {
            ColumnFamilyDefinition existingColumnFamilyDefinition = null;
            for (ColumnFamilyDefinition cfd : existingKeyspaceDefinition.getCfDefs()) {
                if (cfd.getName().equals(cfDef.getName())) {
                    existingColumnFamilyDefinition = cfd;
                    break;
                }
            }
            if (existingColumnFamilyDefinition == null) {
                cluster.addColumnFamily(cfDef);
            } else {
                cluster.updateColumnFamily(cfDef);
            }
        }
        mutator = HFactory.createMutator(keyspace, new StringSerializer());

    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        if (message instanceof Map) {
            String uuid = UUID.randomUUID().toString();
            try {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) message).entrySet()) {
                    mutator.insert(uuid, columnFamilyName, HFactory.createStringColumn(entry.getKey(), entry.getValue().toString()));
                }
                mutator.execute();
            } catch (Throwable t) {
                log.error("Cannot publish message to Cassandra: " + t.getMessage(), t);
            }
        } else {
            log.error("Event cannot be published as it's not type of Map, hence dropping the Event: " + message);
        }

    }

    @Override
    public void disconnect() {
        if (cluster != null) {
            HFactory.shutdownCluster(cluster);
            cluster = null;
        }

    }

    @Override
    public void destroy() {
        credentials = null;
    }


}
