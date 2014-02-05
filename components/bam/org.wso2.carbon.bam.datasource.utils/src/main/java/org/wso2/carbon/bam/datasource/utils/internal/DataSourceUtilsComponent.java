/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.datasource.utils.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.ndatasource.core.DataSourceService;

/**
 * @scr.component name="bam.data.source.utils.component" immediate="true"
 * @scr.reference name="datasources.service" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 * @scr.reference name="dataaccess.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 */
public class DataSourceUtilsComponent {

    private static DataSourceService carbonDataSourceService;
    
    private static DataAccessService dataAccessService;
    
    private static Log log = LogFactory.getLog(DataSourceUtilsComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            log.debug("BAM Data Source Utils bundle is activated ");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("BAM Data Source Utils bundle is deactivated ");
    }

    public static DataSourceService getCarbonDataSourceService() {
        return carbonDataSourceService;
    }

    public static void setDataSourceService(DataSourceService carbonDataSourceService) {
        DataSourceUtilsComponent.carbonDataSourceService = carbonDataSourceService;
    }
    
    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        DataSourceUtilsComponent.setDataSourceService(null);
    }
    
    protected void setDataAccessService(DataAccessService dataAccessService) {
        DataSourceUtilsComponent.dataAccessService = dataAccessService;
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        DataSourceUtilsComponent.setDataSourceService(null);
    }
    
    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }
    
}
