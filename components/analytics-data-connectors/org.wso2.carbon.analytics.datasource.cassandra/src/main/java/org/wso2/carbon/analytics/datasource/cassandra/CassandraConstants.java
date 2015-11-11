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
package org.wso2.carbon.analytics.datasource.cassandra;

/**
 * Constants related to Cassandra DAS data connector.
 */
public class CassandraConstants {
    
    public static final String DATASOURCE_NAME = "datasource";
    
    public static final String KEYSPACE = "keyspace";
    
    public static final String CLASS = "class";
    
    public static final String REPLICATION_FACTOR = "replication_factor";

    public static final int STREAMING_BATCH_SIZE = 1000;
    
    public static final int RECORD_INSERT_STATEMENTS_CACHE_SIZE = 5000;

    public static final int TS_MULTIPLIER = (int) Math.pow(2, 30);
    
    public static final String DEFAULT_ARS_KS_NAME = "ARS";
    
    public static final String DEFAULT_AFS_KS_NAME = "AFS";
    
    public static final String DEFAULT_CLASS = "SimpleStrategy";
    
    public static final int DEFAULT_REPLICATION_FACTOR = 3;
    
}
