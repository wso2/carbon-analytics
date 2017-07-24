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

package org.wso2.carbon.analytics.dataservice.core.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.ntask.core.AbstractTask;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class is responsible to perform global purging operation
 */
public class AnalyticsGlobalDataPurgingTask extends AbstractTask {
    private static final Log logger = LogFactory.getLog(AnalyticsGlobalDataPurgingTask.class);

    @Override
    public void execute() {
        String includeTableNames = this.getProperties().get(Constants.INCLUDE_TABLES);
        String[] includeTables = includeTableNames.split(Constants.INCLUDE_CLASS_SPLITTER);
        purge(includeTables, MultitenantConstants.SUPER_TENANT_ID);
        TenantManager tenantManager = AnalyticsServiceHolder.getRealmService().getTenantManager();
        if (tenantManager != null) {
            try {
                Tenant[] allTenants = tenantManager.getAllTenants();
                if (allTenants != null) {
                    for (Tenant tenant : allTenants) {
                        purge(includeTables, tenant.getId());
                    }
                }
            } catch (UserStoreException e) {
                logger.error("Unable to get tenant related information: " + e.getMessage(), e);
            }
        }
    }

    private void purge(String[] includeTables, int tenantId) {
        try {
            String retention = this.getProperties().get(Constants.RETENTION_PERIOD);
            AnalyticsDataService analyticsDataService = AnalyticsServiceHolder.getAnalyticsDataService();
            List<String> allTables = analyticsDataService.listTables(tenantId);
            if (allTables != null && !allTables.isEmpty()) {
                Set<String> purgeEligibleTables = new HashSet<>();
                for (String includeTable : includeTables) {
                    Pattern pattern = Pattern.compile(includeTable, Pattern.CASE_INSENSITIVE);
                    for (String tableName : allTables) {
                        if (pattern.matcher(tableName).matches()) {
                            purgeEligibleTables.add(tableName);
                        }
                    }
                }
                int retentionPeriod = Integer.parseInt(retention);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                calendar.add(Calendar.DATE, -retentionPeriod);
                for (String purgeEligibleTable : purgeEligibleTables) {
                    logger.info("Records persist before " + calendar.getTime() + "[" + calendar.getTimeInMillis() +
                                "] going to purge from " + purgeEligibleTable + " in tenant[" + tenantId + "]");
                    AnalyticsServiceHolder.getAnalyticsDataService().delete(tenantId, purgeEligibleTable, Long.MIN_VALUE,
                                                                            calendar.getTimeInMillis());
                }
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to perform data purging task for tenant[" + tenantId + "]: " + e.getMessage(), e);
        }
    }
}
