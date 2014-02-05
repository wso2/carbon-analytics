/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.admin.internal.util;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EventBuilderAdminConstants {
    public static final String XPATH_PREFIX_NS_PREFIX = "prefix_";
    public static final String SPECIFIC_ATTR_PREFIX = "specific_";
    public static final String FROM_SUFFIX = "_from";
    public static final String MAPPING_SUFFIX = "_mapping";
    public static final String META_DATA_PREFIX = "meta_";
    public static final String CORRELATION_DATA_PREFIX = "correlation_";
    public static final Map<AttributeType, String> ATTRIBUTE_TYPE_STRING_MAP = Collections.unmodifiableMap(new HashMap<AttributeType, String>() {{
        put(AttributeType.BOOL, EventBuilderConstants.ATTR_TYPE_BOOL);
        put(AttributeType.STRING, EventBuilderConstants.ATTR_TYPE_STRING);
        put(AttributeType.DOUBLE, EventBuilderConstants.ATTR_TYPE_DOUBLE);
        put(AttributeType.FLOAT, EventBuilderConstants.ATTR_TYPE_FLOAT);
        put(AttributeType.INT, EventBuilderConstants.ATTR_TYPE_INT);
        put(AttributeType.LONG, EventBuilderConstants.ATTR_TYPE_LONG);
    }});
    public static final String PARENT_SELECTOR_XPATH_KEY = "parentSelectorXpath";
    public static final String CUSTOM_MAPPING_VALUE_KEY = "customMappingValue";

    public static final String ENABLE_CONST = "enable";
    public static final String DISABLE_CONST = "disable";
}
