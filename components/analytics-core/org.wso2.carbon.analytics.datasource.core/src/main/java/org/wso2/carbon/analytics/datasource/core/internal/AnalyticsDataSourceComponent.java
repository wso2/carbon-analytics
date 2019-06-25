/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Service component implementation for analytics data sources.
 */
@Component(
         name = "analytics.ds.component", 
         immediate = true)
public class AnalyticsDataSourceComponent {

    private static final Log log = LogFactory.getLog(AnalyticsDataSourceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Starting AnalyticsDataSourceComponent#activate");
        }
        BundleContext bundleContext = ctx.getBundleContext();
        bundleContext.registerService(AnalyticsDataSourceService.class, new AnalyticsDataSourceService() {
        }, null);
        if (log.isDebugEnabled()) {
            log.debug("Finished AnalyticsDataSourceComponent#activate");
        }
    }

    @Reference(
             name = "datasource.service", 
             service = org.wso2.carbon.ndatasource.core.DataSourceService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService service) {
        ServiceHolder.setDataSourceService(service);
    }

    protected void unsetDataSourceService(DataSourceService service) {
        ServiceHolder.setDataSourceService(null);
    }
}

