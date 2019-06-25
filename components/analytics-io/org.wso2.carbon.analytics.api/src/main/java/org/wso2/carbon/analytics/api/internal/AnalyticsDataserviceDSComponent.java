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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * This class represents declarative services registration for analytics data service.
 * There could be cases the OSGI service analytics data service is not available
 * in the JVM, and in that cases this service component will not be activated.
 */
@Component(
         name = "analytics.api.dataservice.component", 
         immediate = true)
public class AnalyticsDataserviceDSComponent {

    private static final Log log = LogFactory.getLog(AnalyticsDataserviceDSComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Starting Analytics API - Data service component");
        }
    }

    @Reference(
             name = "analytics.component", 
             service = AnalyticsDataService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAnalyticsDataService")
    protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(analyticsDataService);
    }

    protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(null);
    }

    @Reference(
             name = "analytics.secure.component", 
             service = SecureAnalyticsDataService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetSecureAnalyticsDataService")
    protected void setSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.setSecureAnalyticsDataService(secureAnalyticsDataService);
    }

    protected void unsetSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.setSecureAnalyticsDataService(null);
    }
}

