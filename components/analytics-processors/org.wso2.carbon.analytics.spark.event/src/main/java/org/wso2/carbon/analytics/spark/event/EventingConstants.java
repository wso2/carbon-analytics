/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.analytics.spark.event;

/**
 * Event stream relation provider related constants.
 */
public class EventingConstants {

    public static final String TENANT_ID = "tenantId";
    public static final String STREAM_NAME = "streamName";
    public static final String VERSION = "version";
    public static final String PAYLOAD = "payload";
    public static final String META_DATA = "meta";
    public static final String CORRELATION_DATA = "correlation";
    public static final String STREAM_ID = "streamId";
    public static final String ANALYTICS_SPARK_EVENTING_TASK_TYPE = "ANALYTICS_SPARK_EVENTING";
    public static final String ANALYTICS_SPARK_EVENTING_TASK_NAME = "STORE_EVENT_ROUTER_TASK";
    public static final String DISABLE_EVENT_SINK_SYS_PROP = "disableEventSink";
    public static final String DISABLE_SPARK_EVENTING_TASK_SYS_PROP = "disableSparkEventingTask";
    public static final int SPARK_EVENTING_TASK_RUN_INTERVAL_MS = 10000;
    public static final String EVENT_META_DATA_PREFIX = "meta_";
    public static final String EVENT_CORRELATION_DATA_PREFIX = "correlation_";
    
}
