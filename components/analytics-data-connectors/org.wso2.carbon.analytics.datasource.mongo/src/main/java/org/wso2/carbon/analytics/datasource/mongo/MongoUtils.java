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
package org.wso2.carbon.analytics.datasource.mongo;

import java.util.Map;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * Extractor of properties from analytics configuration.
 *
 */
public class MongoUtils {

    public static String extractDataSourceName(Map<String, String> properties) throws AnalyticsException {
        String dsName = properties.get(MongoConstants.DATASOURCE_NAME);
        if (dsName == null) {
            throw new AnalyticsException("The Mongo connector property '" + MongoConstants.DATASOURCE_NAME + "' is mandatory");
        }
        return dsName;
    }

    public static String extractARSDatabaseName(Map<String, String> properties) {
        String ks = properties.get(MongoConstants.DATABASE);
        if (ks == null) {
            ks = MongoConstants.DEFAULT_ARS_DB_NAME;
        }
        return ks;
    }

    public static Integer extractWriteConcernTimeout(Map<String, String> properties) {
        String ks = properties.get(MongoConstants.TIMEOUT_WRITE_CONCERN);
        if (ks == null) {
            ks = MongoConstants.DEFAULT_TIMEOUT_WRITE_CONCERN;
        }
        return Integer.parseInt(ks);
    }

}
