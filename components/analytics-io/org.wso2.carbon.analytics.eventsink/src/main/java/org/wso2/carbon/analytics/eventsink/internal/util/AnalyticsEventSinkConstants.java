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
package org.wso2.carbon.analytics.eventsink.internal.util;

/**
 * This class holds the constant used in the component.
 */
public class AnalyticsEventSinkConstants {
    public static final String ANALYTICS_CONF_DIR = "analytics";
    public static final String EVENT_SINK_CONFIGURATION_FILE_NAME = "analytics-eventsink-config.xml";
    public static final int DEFAULT_EVENT_QUEUE_SIZE = 2048; // This value need to be power of 2
    public static final int DEFAULT_BATCH_SIZE = 10;
    public static final int DEFAULT_WORKER_POOL_SIZE = 10;
    public static final int DEFAULT_MAX_QUEUE_CAPACITY = 50;

    public static final String EVENT_META_DATA_TYPE = "meta";
    public static final String EVENT_CORRELATION_DATA_TYPE = "correlation";
    public static final String STREAM_VERSION_KEY = "_version";
    public static final String PAYLOAD_TIMESTAMP = "_timestamp";

    public static final String DEPLOYMENT_DIR_NAME = "eventsink";
    public static final String DEPLOYMENT_FILE_EXT = ".xml";
    public static final String DISABLE_EVENT_SINK_JVM_OPTION = "disableEventSink";
}
