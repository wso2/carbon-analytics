/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.status.dashboard.core.services;

import com.zaxxer.hikari.HikariDataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;

/**
 * This is OSGi-components to register datasource provider class.
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.core.services.DatasourceServiceComponent",
        service = DatasourceServiceComponent.class,
        immediate = true
)
public class DatasourceServiceComponent {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceServiceComponent.class);
    private static final String DASHBOARD_DATASOURCE_DEFAULT = "WSO2_STATUS_DASHBOARD_DB";
    private static final String METRICS_DATASOURCE_DEFAULT = "WSO2_METRICS_DB";

    @Activate
    protected void start(BundleContext bundleContext) {
        logger.info("Status dashboard datasource service component is activated.");
    }

    @Deactivate
    protected void stop() throws Exception {
        logger.info("Status dashboard datasource service component is deactivated.");
    }


    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void onDataSourceServiceReady(DataSourceService service) throws DataSourceException {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) DataSourceService");
        }
        String dashboardDatasourceName = DashboardDataHolder.getInstance().getStatusDashboardDeploymentConfigs().getDashboardDatasourceName();
        dashboardDatasourceName = dashboardDatasourceName != null ? dashboardDatasourceName : DASHBOARD_DATASOURCE_DEFAULT;
        String metricsDatasourceName = DashboardDataHolder.getInstance().getStatusDashboardDeploymentConfigs().getMetricsDatasourceName();
        metricsDatasourceName = metricsDatasourceName != null ? metricsDatasourceName : METRICS_DATASOURCE_DEFAULT;
        DashboardDataHolder.getInstance().setDashboardDataSource((HikariDataSource) service.getDataSource
                (dashboardDatasourceName));
        DashboardDataHolder.getInstance().setMetricsDataSource((HikariDataSource) service.getDataSource
                (metricsDatasourceName));
    }

    protected void unregisterDataSourceService(DataSourceService service) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) DataSourceService");
        }
        DashboardDataHolder.getInstance().setDashboardDataSource(null);
        DashboardDataHolder.getInstance().setMetricsDataSource(null);
    }

    @Reference(
            name = "org.wso2.carbon.status.dashboard.core.services.ConfigServiceComponent",
            service = ConfigServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigSourceService"
    )
    protected void registerConfigSourceService(ConfigServiceComponent configServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) ConfigServiceComponent");
        }
    }

    protected void unregisterConfigSourceService(ConfigServiceComponent configServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) ConfigServiceComponent");
        }
    }


}
