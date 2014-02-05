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

package org.wso2.carbon.event.formatter.core.config;


import org.wso2.carbon.databridge.commons.AttributeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class EventFormatterConstants {

    public static final String NORMALIZATION_STRING = "_";
    public static final String EF_CONF_NS = "http://wso2.org/carbon/eventformatter";
    public static final String EF_ELE_ROOT_ELEMENT = "eventFormatter";
    public static final String TM_ELE_DIRECTORY = "eventformatters";
    public static final String EF_ELE_PROPERTY = "property";
    public static final String EF_ATTR_NAME = "name";
    public static final String EF_ATTR_VERSION = "version";
    public static final String EF_ATTR_STREAM_NAME = "streamName";
    public static final String EF_ELE_PROPERTY_STREAM_NAME = "stream";
    public static final String EF_ELE_FROM_PROPERTY = "from";
    public static final String EF_ELE_TO_METADATA_PROPERTY = "metaData";
    public static final String EF_ELE_TO_CORRELATION_PROPERTY = "correlationData";
    public static final String EF_ELE_TO_PAYLOAD_PROPERTY = "payloadData";
    public static final String EF_ELE_TO_PROPERTY = "to";
    public static final String EF_ELE_MAPPING_PROPERTY = "mapping";
    public static final String EF_ATTR_TA_TYPE = "eventAdaptorType";
    public static final String EF_ATTR_TA_NAME = "eventAdaptorName";
    public static final String EF_ATTR_TYPE = "type";
    public static final String EF_ATTR_FACTORY_CLASS = "factoryClass";
    public static final String EF_WSO2EVENT_MAPPING_TYPE = "wso2event";
    public static final String EF_TEXT_MAPPING_TYPE = "text";
    public static final String EF_MAP_MAPPING_TYPE = "map";
    public static final String EF_XML_MAPPING_TYPE = "xml";
    public static final String EF_JSON_MAPPING_TYPE = "json";
    public static final String EF_ELE_MAPPING_INLINE = "inline";
    public static final String EF_ELE_MAPPING_REGISTRY = "registry";
    public static final String ATTR_TYPE_FLOAT = "float";
    public static final String ATTR_TYPE_DOUBLE = "double";
    public static final String ATTR_TYPE_INTEGER = "int";
    public static final String ATTR_TYPE_LONG = "long";
    public static final String ATTR_TYPE_STRING = "string";
    public static final String ATTR_TYPE_BOOL = "boolean";
    public static final String PROPERTY_META_PREFIX = "meta_";
    public static final String PROPERTY_CORRELATION_PREFIX = "correlation_";

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
    public static final String TM_ATTR_TRACING = "trace";
    public static final String TM_ATTR_STATISTICS = "statistics";
    public static final String TM_VALUE_ENABLE = "enable";
    public static final String TM_VALUE_DISABLE = "disable";
    public static final String EVENT_FORMATTER = "Event Formatter";
    public static final String STREAM_ID_SEPERATOR = ":";
    public static final String EF_CONFIG_FILE_EXTENSION_WITH_DOT = ".xml";
    public static final String EF_ATTR_CUSTOM_MAPPING = "customMapping";
    public static final String DEFAULT_EVENT_FORMATTER_POSTFIX = "_formatter";

    public static final String ADAPTOR_MESSAGE_STREAM_NAME = "stream";
    public static final String ADAPTOR_MESSAGE_STREAM_VERSION = "version";
    public static final String ADAPTOR_TYPE_WSO2EVENT = "wso2event";

    public static final String NO_DEPENDENCY_INFO_MSG = "No dependency information available for this event formatter";

    private EventFormatterConstants() {
    }
}