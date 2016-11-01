/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package org.wso2.carbon.analytics.api.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.SecureAnalyticsDataService;

/**
 * This class represents declarative services registration for analytics data service.
 * There could be cases the OSGI service analytics data service is not available
 * in the JVM, and in that cases this service component will not be activated.
 *
 * @scr.component name="analytics.api.dataservice.component" immediate="true"
 * @scr.reference name="analytics.component" interface="AnalyticsDataService"
 * cardinality="0..1" policy="dynamic"  bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 * @scr.reference name="analytics.secure.component" interface="SecureAnalyticsDataService"
 * cardinality="0..1" policy="dynamic"  bind="setSecureAnalyticsDataService" unbind="unsetSecureAnalyticsDataService"
 */

public class AnalyticsDataserviceDSComponent {
    private static final Log log = LogFactory.getLog(AnalyticsDataserviceDSComponent.class);

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Starting Analytics API - Data service component");
        }
    }

    protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(analyticsDataService);
    }

    protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(null);
    }

    protected void setSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.setSecureAnalyticsDataService(secureAnalyticsDataService);
    }

    protected void unsetSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.setSecureAnalyticsDataService(null);
    }
}
