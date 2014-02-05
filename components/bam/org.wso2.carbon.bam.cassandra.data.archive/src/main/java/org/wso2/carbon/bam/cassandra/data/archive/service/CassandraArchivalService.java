package org.wso2.carbon.bam.cassandra.data.archive.service;


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

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.analytics.hive.web.HiveScriptStoreService;
import org.wso2.carbon.bam.cassandra.data.archive.exception.CassandraArchiveException;
import org.wso2.carbon.bam.cassandra.data.archive.exception.InvalidCronExpressionException;
import org.wso2.carbon.bam.cassandra.data.archive.util.ArchiveConfiguration;
import org.wso2.carbon.bam.cassandra.data.archive.util.CassandraArchiveUtil;
import org.wso2.carbon.bam.cassandra.data.archive.util.GenerateHiveScript;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.HashMap;
import java.util.Map;


public class CassandraArchivalService {

    private static final Log log = LogFactory.getLog(CassandraArchivalService.class);

    public static final String BAM_META_KEYSPACE = "META_KS";
    public static final String BAM_META_STREAM_DEF_CF = "STREAM_DEFINITION";
    private static final String STREAM_DEF = "STREAM_DEFINITION";

    Cluster cluster;

    public void archiveCassandraData(ArchiveConfiguration archiveConfiguration) throws Exception {

        if ((archiveConfiguration != null)) {
            if ( !archiveConfiguration.isSchedulingOn() || CronExpression.isValidExpression(
                    archiveConfiguration.getCronExpression())) {

                if (archiveConfiguration.getConnectionURL() == null) {
                    ClusterInformation clusterInformation = new ClusterInformation(archiveConfiguration.getUserName(),
                                                                                   archiveConfiguration.getPassword());
                    cluster = CassandraArchiveUtil.getDataAccessService().getCluster(clusterInformation);
                } else {
                    String connectionUrl = archiveConfiguration.getConnectionURL();
                    CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(connectionUrl);
                    Map<String, String> credentials = new HashMap<String, String>();
                    credentials.put("username", archiveConfiguration.getUserName());
                    credentials.put("password", archiveConfiguration.getPassword());
                    cluster = new ThriftCluster(CassandraArchiveUtil.DEFAULT_CASSANDRA_CLUSTER, hostConfigurator, credentials);
                }
                CassandraArchiveUtil.setCluster(cluster);

                try {

                    StreamDefinition streamDefinition = getStreamDefinition(cluster, archiveConfiguration);

                    if (streamDefinition != null) {
                        GenerateHiveScript generateHiveScript = new GenerateHiveScript(cluster, archiveConfiguration);
                        String hiveQuery = generateHiveScript.generateMappingForReadingCassandraOriginalCF(streamDefinition);
                        hiveQuery = hiveQuery + generateHiveScript.createUDF();
                        hiveQuery = hiveQuery + generateHiveScript.generateMappingForWritingToArchivalCF(streamDefinition) + "\n";
                        hiveQuery = hiveQuery + generateHiveScript.hiveQueryForWritingDataToArchivalCF(streamDefinition, archiveConfiguration) + "\n";
                        hiveQuery = hiveQuery + generateHiveScript.generateMappingForWritingToTmpCF(streamDefinition) + "\n";
                        hiveQuery = hiveQuery + generateHiveScript.hiveQueryForWritingDataToTmpCF(streamDefinition, archiveConfiguration) + "\n";
                        hiveQuery = hiveQuery + generateHiveScript.mapReduceJobAsHiveQuery();
                        if (archiveConfiguration.isSchedulingOn()) {
                            HiveScriptStoreService hiveScriptStoreService = CassandraArchiveUtil.getHiveScriptStoreService();
                            String scriptName = streamDefinition.getName() + streamDefinition.getVersion() + "_archiveScript";
                            hiveScriptStoreService.saveHiveScript(scriptName, hiveQuery, archiveConfiguration.getCronExpression());
                        } else {
                            HiveExecutorService hiveExecutorService = CassandraArchiveUtil.getHiveExecutorService();
                            if (log.isDebugEnabled()) {
                                log.debug(hiveQuery);
                            }
                            hiveExecutorService.execute(null, hiveQuery);
                        }
                    }else {
                        String message = "Unable to find stream definition " + archiveConfiguration.getStreamName() +
                                " with version " + archiveConfiguration.getVersion();
                        log.error(message);
                        throw new CassandraArchiveException(message);
                    }

                } catch (StreamDefinitionStoreException e) {
                    log.error("Failed to get stream definition from Cassandra", e);
                    throw new CassandraArchiveException("Failed to get stream definition");
                }
            } else {
                log.error("Invalid cron expression: " + archiveConfiguration.getCronExpression());
                throw new InvalidCronExpressionException("Invalid cron expression: " + archiveConfiguration.getCronExpression());
            }
        }else {
            log.error("UI doesn't pass the configuration to backend");
            throw new CassandraArchiveException("UI doesn't pass the configuration to backend");
        }
    }


    private StreamDefinition getStreamDefinition(Cluster cluster, ArchiveConfiguration archiveConfiguration)
            throws StreamDefinitionStoreException {
        StreamDefinition streamDef = null;
        Keyspace keyspace =
                HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF)
                .setKey(getStreamKey(archiveConfiguration)).setName(STREAM_DEF);
        QueryResult<HColumn<String, String>> result = columnQuery.execute();
        HColumn<String, String> hColumn = result.get();
        try {
            if (hColumn != null) {
                streamDef = EventDefinitionConverterUtils.convertFromJson(hColumn.getValue());
            }
        } catch (MalformedStreamDefinitionException e) {
            throw new StreamDefinitionStoreException(
                    "Retrieved definition from Cassandra store is malformed. Retrieved "
                            +
                            "value : " + hColumn.getValue());
        }
        return streamDef;
    }

    private String getStreamKey(ArchiveConfiguration archiveConfiguration) {
        return archiveConfiguration.getStreamName() + ":" + archiveConfiguration.getVersion();
    }
}
