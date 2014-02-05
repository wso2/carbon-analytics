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

package org.wso2.carbon.event.output.adaptor.cassandra.internal.util;


public final class CassandraEventAdaptorConstants {

    private CassandraEventAdaptorConstants() {
    }

    public static final String ADAPTOR_CASSANDRA_USER_NAME = "user.name";

    public static final String ADAPTOR_CASSANDRA_PASSWORD = "password";

    public static final String ADAPTOR_CASSANDRA_CLUSTER_NAME = "cluster.name";
    public static final String ADAPTOR_CASSANDRA_CLUSTER_NAME_HINT = "cluster.name.hint";

    public static final String ADAPTOR_CASSANDRA_HOSTNAME = "hostname";

    public static final String ADAPTOR_CASSANDRA_PORT = "port";

    public static final String ADAPTOR_CASSANDRA_INDEX_ALL_COLUMNS = "index.all.columns";
    public static final String ADAPTOR_CASSANDRA_INDEX_ALL_COLUMNS_HINT = "index.all.columns.hint";

    public static final String ADAPTOR_TYPE_CASSANDRA = "cassandra";

    public static final String ADAPTOR_CASSANDRA_KEY_SPACE_NAME = "key.space.name";

    public static final String ADAPTOR_CASSANDRA_COLUMN_FAMILY_NAME = "column.family.name";

}
