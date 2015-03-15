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
package org.wso2.carbon.event.stream.core.internal.util;

import org.wso2.carbon.databridge.commons.AttributeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface EventStreamConstants {


    public static final String EVENT_STREAMS = "eventstreams";
    public static final String STREAM_DEFINITION_FILE_EXTENSION = ".json";
    public static final String STREAM_DEFINITION_FILE_EXTENSION_TYPE = "json";
    public static final String STREAM_DEFINITION_FILE_DELIMITER = "_";
    public static final String STREAM_DEFINITION_DELIMITER = ":";

    public static final String ATTR_TYPE_FLOAT = "float";
    public static final String ATTR_TYPE_DOUBLE = "double";
    public static final String ATTR_TYPE_INTEGER = "int";
    public static final String ATTR_TYPE_LONG = "long";
    public static final String ATTR_TYPE_STRING = "string";
    public static final String ATTR_TYPE_BOOLEAN = "boolean";
    public static final String ATTR_TYPE_BOOL = "bool";


    public static final Map<String, AttributeType> STRING_ATTRIBUTE_TYPE_MAP = Collections.unmodifiableMap(new HashMap<String, AttributeType>() {{
        put(ATTR_TYPE_BOOLEAN, AttributeType.BOOL);
        put(ATTR_TYPE_STRING, AttributeType.STRING);
        put(ATTR_TYPE_DOUBLE, AttributeType.DOUBLE);
        put(ATTR_TYPE_FLOAT, AttributeType.FLOAT);
        put(ATTR_TYPE_INTEGER, AttributeType.INT);
        put(ATTR_TYPE_LONG, AttributeType.LONG);
    }});

    public static final String XML_EVENT = "xml";
    public static final String JSON_EVENT = "json";
    public static final String TEXT_EVENT = "text";

    public static final String SAMPLE_EVENTS_PARENT_TAG = "events";
    public static final String SAMPLE_EVENT_PARENT_TAG = "event";
    public static final String SAMPLE_EVENT_META_TAG = "metaData";
    public static final String SAMPLE_EVENT_CORRELATION_TAG = "correlationData";
    public static final String SAMPLE_EVENT_PAYLOAD_TAG = "payloadData";

    public static final String META_PREFIX = "meta_";
    public static final String CORRELATION_PREFIX = "correlation_";
    public static final String EVENT_ATTRIBUTE_VALUE_SEPARATOR = ":";
    public static final String EVENT_ATTRIBUTE_SEPARATOR = ",";


}
