/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.webservice.internal;

import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * This class represents the analytics data service web service declarative services component.
 */
@Component(
         name = "analytics.webservice.component", 
         immediate = true)
public class AnalyticsWebServiceComponent {

    @Reference(
             name = "analytics.api.component", 
             service = org.wso2.carbon.analytics.api.AnalyticsDataAPI.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAnalyticsDataAPIService")
    protected void setAnalyticsDataAPIService(AnalyticsDataAPI analyticsDataAPI) {
        ServiceHolder.setAnalyticsDataAPIService(analyticsDataAPI);
    }

    protected void unsetAnalyticsDataAPIService(AnalyticsDataAPI analyticsDataAPI) {
    // Nothing to do in unset
    }

    @Reference(
             name = "org.wso2.carbon.event.stream.core.EventStreamService", 
             service = org.wso2.carbon.event.stream.core.EventStreamService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
    // Nothing to do in unset
    }
}

