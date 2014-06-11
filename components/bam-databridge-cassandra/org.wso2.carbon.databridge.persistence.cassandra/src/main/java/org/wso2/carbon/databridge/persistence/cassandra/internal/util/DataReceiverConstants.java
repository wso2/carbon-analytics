/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.databridge.persistence.cassandra.internal.util;


/**
 * BAM event receiver constants
 */
public final class DataReceiverConstants {

    public static final String CLUSTER_NAME = "Test Cluster";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String RPC_PORT = "9160";

    public static final String USERNAME_VALUE = "admin";
    public static final String PASSWORD_VALUE = "admin";

    public static final String LOCAL_NODE = "localhost";
    public static final String CASSANDRA_STREAM_DEF_AUTH_CONF = "event-stream-definition-auth.xml";

    public static final String CASSANDRA_STREAM_DEF_AUTH_PATH = "/components/org.wso2.carbon.bam.datareceiver/streamdef/auth";
    public static final String PASSWORD_PROPERTY = "CASSANDRA_STREAM_DEF_USER_PASSWORD";
    public static final String USERNAME_PROPERTY = "CASSANDRA_STREAM_DEF_USER_NAME";
    public static final String CASSANDRA_STREAM_DEF_HOSTPOOL_PATH = "/components/org.wso2.carbon.bam.datareceiver/streamdef/hostpool";
    public static final String HOSTPOOL_PROPERTY = "CASSANDRA_STREAM_DEF_HOST_POOL";

    public static final int DEFAULT_RECEIVER_NODE_ID = 0;
    public static final String DEFAULT_KEY_SPACE_NAME = "EVENT_KS";
    public static final String DEFAULT_INDEX_KEYSPACE_NAME = "EVENT_INDEX_KS";

}
