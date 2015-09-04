/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.datasource.rdbms;

/**
 * Constants related to RDBMS based {@link AnalyticsDataSource}s.
 */
public class RDBMSAnalyticsDSConstants {

    public static final String DATASOURCE = "datasource";
    
    public static final String CATEGORY = "category";
    
    public static final String PARTITION_COUNT = "partitionCount";
    
    public static final String DEFAULT_CHARSET = "UTF8";
        
    public static final int RECORD_BATCH_SIZE = 1000;
    
    public static final int DEFAULT_PARTITION_COUNT = 100;

}
