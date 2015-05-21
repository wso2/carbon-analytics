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
package org.wso2.carbon.event.output.adapter.cassandra.internal.util;

public class CassandraEventAdapterConstants {

    private CassandraEventAdapterConstants() {
    }

    public static final String ADAPTER_TYPE_CASSANDRA = "cassandra";

    public static final String ADAPTER_CASSANDRA_HOSTS = "hosts";
    public static final String ADAPTER_CASSANDRA_HOSTS_HINT = "hosts.hint";

    public static final String ADAPTER_CASSANDRA_PORT = "port";
    public static final String ADAPTER_CASSANDRA_PORT_HINT = "port.hint";

    public static final String ADAPTER_CASSANDRA_USER_NAME = "user.name";

    public static final String ADAPTER_CASSANDRA_PASSWORD = "password";

    public static final String ADAPTER_CASSANDRA_STRATEGY_CLASS = "strategy.class";
    public static final String ADAPTER_CASSANDRA_STRATEGY_CLASS_HINT = "strategy.class.hint";
    public static final String ADAPTER_CASSANDRA_DEFAULT_STRATEGY_CLASS = "org.apache.cassandra.locator.SimpleStrategy";

    public static final String ADAPTER_CASSANDRA_REPLICATION_FACTOR = "replication.factor";
    public static final String ADAPTER_CASSANDRA_REPLICATION_FACTOR_HINT = "replication.factor.hint";
    public static final int ADAPTER_CASSANDRA_DEFAULT_REPLICATION_FACTOR = 1;

    public static final String ADAPTER_CASSANDRA_INDEXED_COLUMNS = "indexed.columns";
    public static final String ADAPTER_CASSANDRA_INDEXED_COLUMNS_HINT = "indexed.columns.hint";

    public static final String ADAPTER_CASSANDRA_KEY_SPACE_NAME = "key.space.name";

    public static final String ADAPTER_CASSANDRA_COLUMN_FAMILY_NAME = "column.family.name";

    public static final String CASSANDRA_CLUSTER_NAME_PREFIX = "EventPublisher_";



}
