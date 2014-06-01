/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.streamdefn.cassandra.internal.util;


/**
 * Data Sink Constants
 */
public final class DataSinkConstants {

    private DataSinkConstants() { }

    public static final String TRANSPORT_ADAPTOR_NAME = "ThriftEventReceiver";
    public static final String TRANSPORT_ADAPTOR_TYPE = "wso2event";
    public static final String INPUT_EVENT_ADAPTOR_STREAM = "stream";
    public static final String INPUT_EVENT_ADAPTOR_VERSION = "version";
    public static final String EVENT_BUILDER_NAME_PREPEND = "cassandra-stream-";
    public static final String DATA_SINK_CONFIG_XML = "cassandra-datasink-config.xml";
    public static final String DATA_SINK_NAMESPACE = "http://wso2.org/carbon/cassandraDataSink";
    public static final String CASSANDRA_DATA_SINK_CONFIGURATION = "CassandraDataSinkConfiguration";
    public static final String DATA_SINK_PERSISTED_STREAMS = "PersistedStreams";
    public static final String INCLUDE_STREAMS = "Include";
    public static final String EXCLUDE_STREAMS = "Exclude";
    public static final String CASSANDRA_STREAM_LISTENER_PREFIX = "Cassandra_Stream_Listener";
}
