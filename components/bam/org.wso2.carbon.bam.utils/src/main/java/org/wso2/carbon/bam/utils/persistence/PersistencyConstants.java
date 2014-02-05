/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.utils.persistence;

public class PersistencyConstants {

    public static final String BAM_KEYSPACE = "BAMKeyspace";

    public static final String INDEX_ROW_KEY = "indexRowKey";
    public static final String GRANULARITY = "granularity";
    public static final String DEFAULT_COLUMN_FAMILY = "defaultCF";
    public static final String SECONDARY_COLUMN_FAMILY = "secondaryCF";
    public static final String ROW_KEY = "rowKey";
    public static final String ROW_INDEX = "meta:rowIndex";
    public static final String BASE_COLUMN_FAMILY = "EVENT";
    public static final String DEFAULT_SEQUENCE_NAME = "default";
    public static final int DEFAULT_SEQUENCE_INDEX = 0;
    public static final int DEFAULT_BATCH_SIZE = 10000;

    public static final String TIMESTAMP_KEY_NAME = "timeStamp";

    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";

    public static final String TIMESTAMP_INDEX_ROW="timeStampIndex";
    /**
     * Meta Column families.
     * <p/>
     * 3. CFCursors - Stores last fetched row for each get operation in each sequence. This is used
     * for batched access to column families.
     * Structure :
     * ---------------------  --------------------------------------------------------------------------------
     * |  Row Key : CF Name  |  [Key : Sequence + Index of Get in Sequence] : [Value : Last fetched row's key] |
     * ---------------------- ---------------------------------------------------------------------------------
     */
    public static final String META_COLUMN_FAMILY_NAME = "CFInfo";
    public static final String INDEX_COLUMN_FAMILY_NAME = "CFIndexes";
    public static final String CURSORS_COLUMN_FAMILY_NAME = "CFCursors";

    public static final String CORRELATION_TABLE = "CORRELATION";
    public static final String META_TABLE = "META";
    public static final String EVENT_TABLE = "EVENT";

    public static final String DEFAULT_INDEX_ROW_KEY = "allkeys";

}
