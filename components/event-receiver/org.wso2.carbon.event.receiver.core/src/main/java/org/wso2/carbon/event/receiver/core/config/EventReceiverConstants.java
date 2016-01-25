/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.receiver.core.config;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.input.adapter.core.MessageType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class EventReceiverConstants {

    public static final String ER_CONF_NS = "http://wso2.org/carbon/eventreceiver";
    public static final String ER_ELEMENT_ROOT_ELEMENT = "eventReceiver";
    public static final String ER_CONFIG_DIRECTORY = "eventreceivers";
    public static final String ER_ELEMENT_PROPERTY = "property";
    public static final String ER_ELEMENT_XPATH_DEFINITION = "xpathDefinition";
    public static final String ER_ELEMENT_FROM = "from";
    public static final String ER_ELEMENT_MAPPING = "mapping";
    public static final String ER_ELEMENT_TO = "to";
    public static final String ER_ATTR_NAME = "name";
    public static final String ER_ATTR_TYPE = "type";
    public static final String ER_ATTR_DEFAULT_VALUE = "default";
    public static final String ER_ATTR_XPATH = "xpath";
    public static final String ER_ATTR_JSONPATH = "jsonPath";
    public static final String ER_ATTR_PREFIX = "prefix";
    public static final String ER_ATTR_NAMESPACE = "namespace";
    public static final String ER_ATTR_DATA_TYPE = "dataType";
    public static final String ER_ATTR_REGEX = "regex";
    public static final String ER_ATTR_TA_NAME = "eventAdapterName";
    public static final String ER_ATTR_TA_TYPE = "eventAdapterType";
    public static final String ER_ATTR_STREAM_NAME = "streamName";
    public static final String ER_ATTR_VERSION = "version";
    public static final String ER_ATTR_FACTORY_CLASS = "factoryClass";
    public static final String META_DATA_VAL = "meta";
    public static final String CORRELATION_DATA_VAL = "correlation";
    public static final String PAYLOAD_DATA_VAL = "payload";
    public static final String ATTR_TYPE_FLOAT = "float";
    public static final String ATTR_TYPE_DOUBLE = "double";
    public static final String ATTR_TYPE_INT = "int";
    public static final String ATTR_TYPE_LONG = "long";
    public static final String ATTR_TYPE_STRING = "string";
    public static final String ATTR_TYPE_BOOL = "bool";
    public static final String ER_WSO2EVENT_MAPPING_TYPE = "wso2event";
    public static final String ER_TEXT_MAPPING_TYPE = "text";
    public static final String ER_MAP_MAPPING_TYPE = "map";
    public static final String ER_XML_MAPPING_TYPE = "xml";
    public static final String ER_JSON_MAPPING_TYPE = "json";
    public static final String ER_ATTR_PARENT_XPATH = "parentXpath";
    public static final String STREAM_NAME_VER_DELIMITER = ":";
    public static final String META_DATA_PREFIX = "meta_";
    public static final String CORRELATION_DATA_PREFIX = "correlation_";
    public static final String CLASS_FOR_BOOLEAN = "java.lang.Boolean";
    public static final String CLASS_FOR_INTEGER = "java.lang.Integer";
    public static final String CLASS_FOR_LONG = "java.lang.Long";
    public static final String CLASS_FOR_FLOAT = "java.lang.Float";
    public static final String CLASS_FOR_DOUBLE = "java.lang.Double";
    public static final String CLASS_FOR_STRING = "java.lang.String";
    public static final Map<AttributeType, String> ATTRIBUTE_TYPE_CLASS_TYPE_MAP = Collections.unmodifiableMap(new HashMap<AttributeType, String>() {{
        put(AttributeType.BOOL, CLASS_FOR_BOOLEAN);
        put(AttributeType.DOUBLE, CLASS_FOR_DOUBLE);
        put(AttributeType.FLOAT, CLASS_FOR_FLOAT);
        put(AttributeType.LONG, CLASS_FOR_LONG);
        put(AttributeType.INT, CLASS_FOR_INTEGER);
        put(AttributeType.STRING, CLASS_FOR_STRING);
    }});
    public static final Map<String, AttributeType> STRING_ATTRIBUTE_TYPE_MAP = Collections.unmodifiableMap(new HashMap<String, AttributeType>() {{
        put(ATTR_TYPE_BOOL, AttributeType.BOOL);
        put(ATTR_TYPE_STRING, AttributeType.STRING);
        put(ATTR_TYPE_DOUBLE, AttributeType.DOUBLE);
        put(ATTR_TYPE_FLOAT, AttributeType.FLOAT);
        put(ATTR_TYPE_INT, AttributeType.INT);
        put(ATTR_TYPE_LONG, AttributeType.LONG);
    }});
    public static final Map<String, String> MESSAGE_TYPE_STRING_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put(MessageType.WSO2EVENT, ER_WSO2EVENT_MAPPING_TYPE);
        put(MessageType.XML, ER_XML_MAPPING_TYPE);
        put(MessageType.MAP, ER_MAP_MAPPING_TYPE);
        put(MessageType.TEXT, ER_TEXT_MAPPING_TYPE);
        put(MessageType.JSON, ER_JSON_MAPPING_TYPE);
    }});
    public static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    public static final String ER_ATTR_TRACE_ENABLED = "trace";
    public static final String ER_ATTR_STATISTICS_ENABLED = "statistics";
    public static final String ER_ATTR_CUSTOM_MAPPING_ENABLED = "customMapping";
    public static final String ENABLE_CONST = "enable";
    public static final String DISABLE_CONST = "disable";
    public static final String DEFAULT_STREAM_VERSION = "1.0.0";
    public static final String DEFAULT_STREAM_DEFINITION_STORE_LOCATION = "/StreamDefinitions";
    public static final String ER_CONFIG_FILE_EXTENSION_WITH_DOT = ".xml";
    public static final String ER_CONFIG_FILE_EXTENSION = "xml";
    public static final String EVENT_RECEIVER = "Event Receiver";
    public static final String EVENT_STREAM = "Event Stream";
    public static final String JSON_ARRAY_START_CHAR = "[";
    public static final String REGISTRY_PATH_SEPARATOR = "/";
    public static final String ADAPTER_MESSAGE_STREAM_NAME = "stream";
    public static final String ADAPTER_MESSAGE_STREAM_VERSION = "version";
    public static final String ADAPTER_TYPE_WSO2EVENT = "wso2event";
    public static final String DEFAULT_EVENT_RECEIVER_POSTFIX = "_receiver";
    public static final String NO_DEPENDENCY_INFO_MSG = "No dependency information available for this event receiver";

    public static final String EVENT_ATTRIBUTE_SEPARATOR = ":";

    public static final String MULTIPLE_EVENTS_PARENT_TAG = "events";
    public static final String EVENT_PARENT_TAG = "event";
    public static final String EVENT_META_TAG = "metaData";
    public static final String EVENT_CORRELATION_TAG = "correlationData";
    public static final String EVENT_PAYLOAD_TAG = "payloadData";

    public static final String ER_ATTR_ENCRYPTED = "encrypted";
    public static final String ER_ATTR_ARBITRARY_MAPS_ENABLED = "arbitraryMaps";

    private EventReceiverConstants() {

    }

    public static final String HTTP_RECEIVER_ENDPOINT_PREFIX = "endpoints";

}
