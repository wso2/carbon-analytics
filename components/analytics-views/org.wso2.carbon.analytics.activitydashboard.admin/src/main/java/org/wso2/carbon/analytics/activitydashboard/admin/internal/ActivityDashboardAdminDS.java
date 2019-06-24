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
package org.wso2.carbon.analytics.activitydashboard.admin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * This class represents the activity dashboard service declarative services component.
 */
@Component(
         name = "acivitydashboard.component", 
         immediate = true)
public class ActivityDashboardAdminDS {

    private static final Log logger = LogFactory.getLog(ActivityDashboardAdminDS.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Activating Analytics Activity Dashboard module.");
        }
    }

    @Reference(
             name = "analytics.api.component", 
             service = org.wso2.carbon.analytics.api.AnalyticsDataAPI.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAnalyticsDataAPI")
    protected void setAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        if (logger.isDebugEnabled()) {
            logger.info("Setting the Analytics Data Service");
        }
        ServiceHolder.setAnalyticsDataAPI(analyticsDataAPI);
    }

    protected void unsetAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        if (logger.isDebugEnabled()) {
            logger.info("Unsetting the Analytics Data Service");
        }
        ServiceHolder.setAnalyticsDataAPI(null);
    }
}

