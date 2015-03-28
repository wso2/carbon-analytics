/*
 * *
 *  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.wso2.carbon.analytics.dashboard.batch.admin.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.dashboard.batch.admin.BatchAnalyticsDashboardAdminService;

/**
 * Declarative service for the Batch Analytics Dashboard Admin service component
 *
 * @scr.component name="dashboard.component" immediate="true"
 * @scr.reference name="analytics.component" interface="org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 */
public class BatchAnalyticsDashboardAdminServiceDS {

    private static final Log logger = LogFactory.getLog(BatchAnalyticsDashboardAdminServiceDS.class);

    /**
     * initialize the ui adapter service here service here.
     *
     * @param ctx
     */
    protected void activate(ComponentContext ctx) {

        if (logger.isDebugEnabled()) {
            logger.debug("Activating Analytics DashboardAdminComponent module.");
        }
        BundleContext bundleContext = ctx.getBundleContext();
        bundleContext.registerService(BatchAnalyticsDashboardAdminService.class,
                new BatchAnalyticsDashboardAdminService(), null);
    }


    protected void setAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        BatchAnalyticsDashboardAdminValueHolder.setAnalyticsDataService(secureAnalyticsDataService);
    }

    protected void unsetAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        BatchAnalyticsDashboardAdminValueHolder.setAnalyticsDataService(null);
    }
}
