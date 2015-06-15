/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.dashboard.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.DashboardConstants;
import org.wso2.carbon.analytics.dashboard.DashboardException;
import org.wso2.carbon.analytics.dashboard.internal.ServiceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin service for Analytics Dashboard admin service
 */
public class DashboardAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(DashboardAdminService.class);

    public String[] getDashboardsList() throws DashboardException {
        List<String> dashboards = new ArrayList<String>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Registry registry = null;
        try {
            registry = ServiceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
            Collection dashboardsCollection = (Collection) registry.get(DashboardConstants.DASHBOARDS_RESOURCE_PATH);
            String[] resourceNames = dashboardsCollection.getChildren();
            for (String resourceName : resourceNames) {
                String dashboard = resourceName.substring(resourceName.lastIndexOf("/") + 1,
                        resourceName.length());
                dashboards.add(dashboard);
            }
        } catch (RegistryException e) {
            String errorMsg = "Error occurred while reading dashboards list.";
            log.error(errorMsg,e);
            throw new DashboardException(errorMsg,e);
        }
        return dashboards.toArray(new String[dashboards.size()]);
    }

}
