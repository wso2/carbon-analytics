/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.data.provider.rdbms.utils;

/**
 * RDBMS data provider constant class.
 */
public final class RDBMSProviderConstants {
    public static final String TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}";
    public static final String INCREMENTAL_COLUMN_PLACEHOLDER = "{{INCREMENTAL_COLUMN}}";
    public static final String LIMIT_VALUE_PLACEHOLDER = "{{LIMIT_VALUE}}";
    public static final String LAST_RECORD_VALUE_PLACEHOLDER = "{{LAST_RECORD_VALUE}}";
    public static final String RECORD_DELETE_QUERY = "record_delete";
    public static final String TOTAL_RECORD_COUNT_QUERY = "total_record_count";
    public static final String RECORD_LIMIT_QUERY = "record_limit";
    public static final String RECORD_GREATER_THAN_QUERY = "record_greater_than";
}
