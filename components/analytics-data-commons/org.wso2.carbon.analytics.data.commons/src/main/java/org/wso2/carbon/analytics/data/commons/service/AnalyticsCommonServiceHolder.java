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
package org.wso2.carbon.analytics.data.commons.service;

import org.wso2.carbon.analytics.data.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.data.commons.utils.AnalyticsCommonUtils;
import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;

public class AnalyticsCommonServiceHolder {

    private static DataSourceService dataSourceService;

    public static synchronized DataSourceService getDataSourceService() throws DataSourceException, AnalyticsException {
        if (dataSourceService == null) {
            DataSourceManager dataSourceManager = DataSourceManager.getInstance();
            dataSourceManager.initDataSources(AnalyticsCommonUtils.getAbsoluteDataSourceConfigDir());
            dataSourceService = new DataSourceServiceImpl();
        }
        return dataSourceService;
    }

}
