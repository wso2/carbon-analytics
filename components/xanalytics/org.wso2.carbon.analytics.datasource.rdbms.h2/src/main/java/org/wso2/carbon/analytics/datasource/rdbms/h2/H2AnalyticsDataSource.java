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
package org.wso2.carbon.analytics.datasource.rdbms.h2;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.DataType;
import org.wso2.carbon.analytics.datasource.core.FileSystem;
import org.wso2.carbon.analytics.datasource.rdbms.common.RDBMSAnalyticsDataSource;

/**
 * MySQL {@link AnalyticsDataSource} implementation.
 */
public class H2AnalyticsDataSource extends RDBMSAnalyticsDataSource {

    @Override
    public void init() {
    }
    
    @Override
    public String getSQLType(DataType dataType) {
        switch (dataType.type) {
        case STRING:
            if (dataType.size == -1) {
                return H2AnalyticsDSConstants.DATA_TYPE_LONG_TEXT;
            } else {
                return H2AnalyticsDSConstants.DATA_TYPE_VARCHAR + " (" + dataType.size + ")";
            }
        case BOOLEAN:
            return H2AnalyticsDSConstants.DATA_TYPE_BOOLEAN;
        case DOUBLE:
            return H2AnalyticsDSConstants.DATA_TYPE_DOUBLE;
        case INTEGER:
            return H2AnalyticsDSConstants.DATA_TYPE_INTEGER;
        case LONG:
            return H2AnalyticsDSConstants.DATA_TYPE_LONG;
        default:
            throw new RuntimeException("Unknown data type: " + dataType);
        }
    }

    @Override
    public FileSystem getFileSystem() throws AnalyticsDataSourceException {
        return new H2FileSystem(this.getDataSource());
    }

}
