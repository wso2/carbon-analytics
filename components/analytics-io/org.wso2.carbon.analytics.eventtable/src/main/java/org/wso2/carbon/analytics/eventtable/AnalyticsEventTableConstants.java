/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.eventtable;

/**
 * Constants related to analytics event table.
 */
public class AnalyticsEventTableConstants {
    
    public static final String ANNOTATION_TABLE_NAME = "table.name";
    
    public static final String ANNOTATION_SCHEMA = "schema";
    
    public static final String ANNOTATION_PRIMARY_KEYS = "primary.keys";
    
    public static final String ANNOTATION_INDICES = "indices";
    
    public static final String ANNOTATION_MERGE_SCHEMA = "merge.schema";
    
    public static final String ANNOTATION_WAIT_FOR_INDEXING = "wait.for.indexing";
    
    public static final String ANNOTATION_CACHING = "caching";
    
    public static final String ANNOTATION_CACHE_TIMEOUT_SECONDS = "cache.timeout.seconds";
    
    public static final String ANNOTATION_CACHE_SIZE_BYTES = "cache.size.bytes";
    
    public static final String ANNOTATION_MAX_SEARCH_RESULT_COUNT = "max.search.result.count";

    public static final String ANNOTATION_RECORD_StORE = "record.store";
    
    public static final int DEFAULT_CACHE_TIMEOUT = 10; // 10 seconds
    
    public static final int DEFAULT_CACHE_SIZE = 1024 * 1024 * 10; // 10 MB
    
    public static final String CACHE_KEY_PREFIX_LUCENE = "@LQ:";
    
    public static final String CACHE_KEY_PREFIX_ALL_RECORDS = "@AL:";
    
    public static final String CACHE_KEY_PREFIX_PK = "@PK:";

    public static final String INTERNAL_TIMESTAMP_ATTRIBUTE = "_timestamp";
    
}
