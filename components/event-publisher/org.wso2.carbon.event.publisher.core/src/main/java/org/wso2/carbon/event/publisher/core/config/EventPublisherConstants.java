/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.publisher.core.config;


import org.wso2.carbon.databridge.commons.AttributeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class EventPublisherConstants {
    private EventPublisherConstants() {

    }

    public static final String NORMALIZATION_STRING = "_";
    public static final String EF_CONF_NS = "http://wso2.org/carbon/eventpublisher";
    public static final String EF_ELEMENT_ROOT_ELEMENT = "eventPublisher";
    public static final String EF_CONFIG_DIRECTORY = "eventpublishers";
    public static final String EF_ELE_PROPERTY = "property";
    public static final String EF_ATTR_NAME = "name";
    public static final String EF_ATTR_VERSION = "version";
    public static final String EF_ATTR_STREAM_NAME = "streamName";
    public static final String EF_ELE_PROPERTY_STREAM_NAME = "stream";
    public static final String EF_ELEMENT_FROM = "from";
    public static final String EF_ELE_TO_METADATA_PROPERTY = "metaData";
    public static final String EF_ELE_TO_CORRELATION_PROPERTY = "correlationData";
    public static final String EF_ELE_TO_PAYLOAD_PROPERTY = "payloadData";
    public static final String EF_ELEMENT_TO = "to";
    public static final String EF_ELEMENT_MAPPING = "mapping";
    public static final String EF_ATTR_TA_TYPE = "eventAdapterType";
    public static final String EF_ATTR_TYPE = "type";
    public static final String EF_ATTR_FACTORY_CLASS = "factoryClass";
    public static final String EF_WSO2EVENT_MAPPING_TYPE = "wso2event";
    public static final String EF_TEXT_MAPPING_TYPE = "text";
    public static final String EF_MAP_MAPPING_TYPE = "map";
    public static final String EF_XML_MAPPING_TYPE = "xml";
    public static final String EF_JSON_MAPPING_TYPE = "json";
    public static final String EF_ELE_MAPPING_INLINE = "inline";
    public static final String EF_ELE_MAPPING_REGISTRY = "registry";
    public static final String EF_ELE_CACHE_TIMEOUT_DURATION = "cacheTimeoutDuration";
    public static final String ATTR_TYPE_FLOAT = "float";
    public static final String ATTR_TYPE_DOUBLE = "double";
    public static final String ATTR_TYPE_INTEGER = "int";
    public static final String ATTR_TYPE_LONG = "long";
    public static final String ATTR_TYPE_STRING = "string";
    public static final String ATTR_TYPE_BOOL = "bool";
    public static final String PROPERTY_META_PREFIX = "meta_";
    public static final String PROPERTY_CORRELATION_PREFIX = "correlation_";
    public static final String PROPERTY_ARBITRARY_DATA_MAP_PREFIX = "arbitrary_";

    public static final Map<String, AttributeType> STRING_ATTRIBUTE_TYPE_MAP = Collections.unmodifiableMap(new HashMap<String, AttributeType>() {{
        put(ATTR_TYPE_BOOL, AttributeType.BOOL);
        put(ATTR_TYPE_STRING, AttributeType.STRING);
        put(ATTR_TYPE_DOUBLE, AttributeType.DOUBLE);
        put(ATTR_TYPE_FLOAT, AttributeType.FLOAT);
        put(ATTR_TYPE_INTEGER, AttributeType.INT);
        put(ATTR_TYPE_LONG, AttributeType.LONG);
    }});

    public static final String REGISTRY_CONF_PREFIX = "conf:/";
    public static final String REGISTRY_GOVERNANCE_PREFIX = "gov:/";
    public static final String EF_ATTR_TRACE_ENABLED = "trace";
    public static final String EF_ATTR_PROCESSING = "processing";
    public static final String EF_ATTR_STATISTICS_ENABLED = "statistics";
    public static final String ENABLE_CONST = "enable";
    public static final String TM_VALUE_DISABLE = "disable";
    public static final String EVENT_PUBLISHER = "Event Publisher";
    public static final String EVENT_STREAM = "Event Stream";
    public static final String STREAM_ID_SEPERATOR = ":";
    public static final String EF_CONFIG_FILE_EXTENSION_WITH_DOT = ".xml";
    public static final String EF_ATTR_CUSTOM_MAPPING = "customMapping";
    public static final String DEFAULT_EVENT_PUBLISHER_POSTFIX = "_publisher";

    public static final String ADAPTER_MESSAGE_UNIQUE_ID = "uniqueId";
    public static final String ADAPTER_MESSAGE_UNIQUE_ID_VALUE = "event";
    public static final String ADAPTER_TYPE_LOGGER = "logger";

    public static final String NO_DEPENDENCY_INFO_MSG = "No dependency information available for this event publisher";

    public static final String MULTIPLE_EVENTS_PARENT_TAG = "events";
    public static final String EVENT_PARENT_TAG = "event";
    public static final String EVENT_META_TAG = "metaData";
    public static final String EVENT_CORRELATION_TAG = "correlationData";
    public static final String EVENT_PAYLOAD_TAG = "payloadData";
    public static final String EVENT_ARBITRARY_DATA_MAP_TAG = "arbitraryDataMap";
    public static final String EVENT_DEFAULT_NAMESPACE = "http://wso2.org/carbon/events";
    public static final String TEMPLATE_EVENT_ATTRIBUTE_PREFIX = "{{";
    public static final String TEMPLATE_EVENT_ATTRIBUTE_POSTFIX = "}}";
    public static final String EVENT_ATTRIBUTE_VALUE_SEPARATOR = ":";
    public static final String EVENT_ATTRIBUTE_SEPARATOR = ",";
    public static final String DEFAULT_STREAM_VERSION = "1.0.0";

    public static final String EF_ATTR_ENCRYPTED = "encrypted";
    public static final String DOUBLE_QUOTE = "\"";

    public static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";

    public static final String METRICS_ROOT = "WSO2_CEP";
    public static final String METRICS_EVENT_PUBLISHERS = "EventPublishers";
    public static final String METRICS_PUBLISHED_EVENTS = "PublishedEvents";
    public static final String METRIC_DELIMITER = ".";
    public static final String METRIC_AGGREGATE_ANNOTATION = "[+]";
    public static final String TEMP_CARBON_APPS_DIRECTORY = "carbonapps";
}