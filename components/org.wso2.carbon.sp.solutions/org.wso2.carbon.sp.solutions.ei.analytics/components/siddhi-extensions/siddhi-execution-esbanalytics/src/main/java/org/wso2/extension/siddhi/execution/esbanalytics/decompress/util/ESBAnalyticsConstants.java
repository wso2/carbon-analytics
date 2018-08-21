/*
 *  Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.execution.esbanalytics.decompress.util;

/**
 * Contain constants required for the CompressedEventUtils class
 */
public class ESBAnalyticsConstants {

    // Attribute for the tenant ID
    public static final String META_TENANT_ID_ATTRIBUTE = "metaTenantId";

    // Uncompressed message data types
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_INTEGER = "integer";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_BOOL = "boolean";
    public static final String TYPE_STRING = "string";
}
