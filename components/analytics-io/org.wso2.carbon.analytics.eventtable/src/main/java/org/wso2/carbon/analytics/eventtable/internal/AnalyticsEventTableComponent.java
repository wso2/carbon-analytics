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
package org.wso2.carbon.analytics.eventtable.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;

/**
 * This class represents the analytics event table component class.
 * @scr.component name="analytics.eventstable.comp" immediate="true"
 * @scr.reference name="analytics.component" interface="AnalyticsDataService"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsDataservice" unbind="unsetAnalyticsDataservice"
 */
public class AnalyticsEventTableComponent {
    
    private static Log log = LogFactory.getLog(AnalyticsEventTableComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Started the Analytics Event Table component");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopped Analytics Event Table component");
        }
    }

    protected void setAnalyticsDataservice(AnalyticsDataService analyticsDataservice) {
        ServiceHolder.setAnalyticsDataService(analyticsDataservice);
    }

    protected void unsetAnalyticsDataservice(AnalyticsDataService analyticsDataservice) {
        ServiceHolder.setAnalyticsDataService(null);
    }
    
}
