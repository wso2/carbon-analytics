package org.wso2.carbon.analytics.hive.cassandra;

import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.extension.AbstractHiveAnalyzer;

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
public class CassandraConfigurationAnalyzer extends AbstractHiveAnalyzer {

    @Override
    public void execute() throws HiveExecutionException {
        String config = getProperty(CassandraConfigurationConstants.CASSANDRA_CONFIG);
        if (config == null || config.equalsIgnoreCase(Boolean.TRUE.toString())) {
            setProperty(CassandraConfigurationConstants.STREAM_EVENTS_KEYSPACE,
                    CassandraStreamDefnConfigReader.getKeySpaceName());
            setProperty(CassandraConfigurationConstants.STREAM_INDEX_EVENTS_KEYSPACE,
                    CassandraStreamDefnConfigReader.getIndexKeySpaceName());
            setProperty(CassandraConfigurationConstants.CASSANDRA_REPLICATION_FACTOR,
                    String.valueOf(CassandraStreamDefnConfigReader.getReplicationFactor()));
            setProperty(CassandraConfigurationConstants.CASSANDRA_WRITE_CONSISTENCY,
                    CassandraStreamDefnConfigReader.getWriteConsistencyLevel());
            setProperty(CassandraConfigurationConstants.CASSANDRA_READ_CONSISTENCY,
                    CassandraStreamDefnConfigReader.getReadConsistencyLevel());
            setProperty(CassandraConfigurationConstants.CASSANDRA_STRATEGY_CLASS,
                    CassandraStreamDefnConfigReader.getStrategyClass());
            setProperty(CassandraConfigurationConstants.CASSANDRA_CONFIG,
                    Boolean.TRUE.toString());
        }
    }
}
