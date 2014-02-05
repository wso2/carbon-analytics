/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.processor.core.internal.util;


public interface EventProcessorConstants {

    String EP_CONF_NS = "http://wso2.org/carbon/eventprocessor";

    String EP_PREFIX = "ep";
    String EP_ELE_ROOT_ELEMENT = "executionPlan";

    String EP_ELE_DESC = "description";
    String EP_ELE_SIDDHI_CONFIG = "siddhiConfiguration";
    String EP_ELE_IMP_STREAMS = "importedStreams";
    String EP_ELE_EXP_STREAMS = "exportedStreams";
    String EP_ELE_QUERIES = "queryExpressions";
    String EP_ELE_STREAM = "stream";
    String EP_ELE_PROPERTY = "property";
    String EP_ATTR_STATISTICS = "statistics";
    String EP_ATTR_TRACING = "trace";
    String EP_ENABLE = "enable";
    String EP_DISABLE = "disable";
    String EP_ATTR_PASSTHROUGH_FLOW = "passthroughFlow";


    // for inputs  - siddhi stream
    String EP_ATTR_AS = "as";
    // for outputs - siddhi stream
    String EP_ATTR_VALUEOF = "valueOf";

    String EP_ATTR_NAME = "name";
    String EP_ATTR_VERSION = "version";


    String STREAM_SEPARATOR = ":";
    String ATTRIBUTE_SEPARATOR = "_";

    String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";

    String EP_ELE_DIRECTORY = "executionplans";
    String XML_EXTENSION = ".xml";

    String EVENT_PROCESSOR = "Event Processor";
    String SIDDHI_DISTRIBUTED_PROCESSING = "siddhi.enable.distributed.processing";
    String SIDDHI_SNAPSHOT_INTERVAL = "siddhi.persistence.snapshot.time.interval.minutes";
    String EP_CONFIG_FILE_EXTENSION_WITH_DOT = ".xml";

    String META = "meta";
    String CORRELATION = "correlation";

    String HAZELCAST_INSTANCE = "hazelcast.instance";

    String NO_DEPENDENCY_INFO_MSG = "No dependency information available for this event formatter";
}
