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
package org.wso2.carbon.analytics.api.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;

/**
 * This class represents the analytics api declarative services component.
 *
 * @scr.component name="analytics.api.component" immediate="true"
 * @scr.reference name="analytics.component" interface="org.wso2.carbon.analytics.dataservice.AnalyticsDataService"
 * cardinality="0..1" policy="dynamic"  bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 */
public class AnalyticsApiDSComponent {
    private static final Log log = LogFactory.getLog(AnalyticsApiDSComponent.class);

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) log.debug("Starting Analytics API component");
        BundleContext bundleContext = ctx.getBundleContext();
        try {
            bundleContext.registerService(AnalyticsDataAPI.class, new CarbonAnalyticsAPI(), null);
        } catch (Throwable e) {
            log.error("Error while starting the Analytics API component. " + e.getMessage(), e);
        }
    }

    protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(analyticsDataService);
    }

    protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(null);
    }
}
