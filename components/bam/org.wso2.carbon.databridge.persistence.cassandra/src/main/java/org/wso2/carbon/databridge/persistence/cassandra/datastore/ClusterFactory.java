package org.wso2.carbon.databridge.persistence.cassandra.datastore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.prettyprint.hector.api.Cluster;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.persistence.cassandra.Utils.StreamDefinitionUtils;
import org.wso2.carbon.databridge.persistence.cassandra.internal.util.ServiceHolder;

import java.util.concurrent.TimeUnit;

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
public class ClusterFactory {

    private static volatile LoadingCache<Credentials, Cluster> clusterLoadingCache;

    private static Log log = LogFactory.getLog(ClusterFactory.class);



    private ClusterFactory() {

    }

    private static void init() {
        if (clusterLoadingCache != null) {
                return;
        }
        synchronized (ClusterFactory.class) {
            if (clusterLoadingCache != null) {
                return;
            }
            clusterLoadingCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build(new CacheLoader<Credentials, Cluster>() {

                        @Override
                        public Cluster load(Credentials credentials) throws Exception {
                            if (log.isTraceEnabled()) {
                                log.trace("Cache not hit. Loading cluster for user : " + credentials.getUsername());
                            }

//                            ClusterConfiguration configuration = ClusterConfigurationFactory.create(loadConfigXML());
//                            CassandraHostConfigurator cassandraHostConfigurator = createCassandraHostConfigurator();
//                            Map<String, String> creds = new HashMap<String, String>();
//                            creds.put(USERNAME_KEY, credentials.getUsername());
//                            creds.put(PASSWORD_KEY, credentials.getPassword());
//                            Cluster cluster =
//                                    HFactory.createCluster(configuration.getClusterName(), cassandraHostConfigurator,
//                                            creds);
                            ClusterInformation clusterInformation = new ClusterInformation(credentials.getUsername(),
                                    credentials.getPassword());
                            Cluster cluster = ServiceHolder.getDataAccessService().getCluster(clusterInformation);
                            initCassandraKeySpaces(cluster);
                            return cluster;
                        }
                    });
        }
    }

    public static void initCassandraKeySpaces(Cluster cluster) throws StreamDefinitionStoreException {
        log.info("Initializing cluster");
        CassandraConnector connector = ServiceHolder.getCassandraConnector();
        connector.createKeySpaceIfNotExisting(cluster, CassandraConnector.BAM_META_KEYSPACE);

        connector.createKeySpaceIfNotExisting(cluster, StreamDefinitionUtils.getKeySpaceName());
        connector.createKeySpaceIfNotExisting(cluster, StreamDefinitionUtils.getIndexKeySpaceName());

        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAM_DEF_CF, null);
        connector.createColumnFamily(cluster, StreamDefinitionUtils.getIndexKeySpaceName(),
                CassandraConnector.INDEX_DEF_CF, null);
        connector.createColumnFamily(cluster, StreamDefinitionUtils.getIndexKeySpaceName(),
                CassandraConnector.GLOBAL_ACTIVITY_MONITORING_INDEX_CF, null);
    }

    public static Cluster getCluster(Credentials credentials) {
        init();
        return clusterLoadingCache.getUnchecked(credentials);
    }
}
