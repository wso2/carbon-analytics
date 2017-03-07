/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.statistics;


import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.statistics.internal.data.StatsDTO;
import org.wso2.carbon.event.statistics.internal.ds.EventStatisticsServiceHolder;

public class EventStatisticsAdminService {


    public EventStatisticsAdminService() {
    }

    public StatsDTO getGlobalCount() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return EventStatisticsServiceHolder.getInstance().getEventStatisticsService().getGlobalCount(tenantId);
    }

    public StatsDTO getCategoryCount(String categoryName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return EventStatisticsServiceHolder.getInstance().getEventStatisticsService().getCategoryCount(tenantId, categoryName);
    }

    public StatsDTO getDeploymentCount(String categoryName, String deploymentName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return EventStatisticsServiceHolder.getInstance().getEventStatisticsService().getDeploymentCount(tenantId, categoryName, deploymentName);
    }

    public StatsDTO getElementCount(String categoryName, String deploymentName, String elementName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return EventStatisticsServiceHolder.getInstance().getEventStatisticsService().getElementCount(tenantId, categoryName, deploymentName, elementName);
    }

}