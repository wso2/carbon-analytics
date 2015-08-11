/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.sources;

import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.ndatasource.common.DataSourceException;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by niranda on 8/10/15.
 */
public class AnalyticsDatasourceWrapper implements Serializable{

    private static final long serialVersionUID = 8861289441809536781L;

    private String dsName;

    public AnalyticsDatasourceWrapper(String dsName) {
        this.dsName = dsName;
    }

    public Connection getConnection() throws DataSourceException, SQLException {
        return ((DataSource)GenericUtils.loadGlobalDataSource(this.dsName)).getConnection();
    }
}
