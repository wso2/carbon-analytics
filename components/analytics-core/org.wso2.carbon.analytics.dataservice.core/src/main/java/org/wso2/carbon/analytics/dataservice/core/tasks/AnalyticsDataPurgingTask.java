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
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.ntask.core.AbstractTask;

import java.util.Calendar;
import java.util.Map;

/**
 * This class is responsible to execute purging task operation
 */
public class AnalyticsDataPurgingTask extends AbstractTask {
    private static final Log log = LogFactory.getLog(AnalyticsDataPurgingTask.class);

    @Override
    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("Starting execution of analytics data purging task");
        }
        Map<String, String> taskProperties = this.getProperties();
        String retention = taskProperties.get(Constants.RETENTION_PERIOD);
        if (retention != null && !retention.isEmpty()) {
            String table = taskProperties.get(Constants.TABLE);
            String tenantId = taskProperties.get(Constants.TENANT_ID);
            int retentionPeriod = Integer.parseInt(retention);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.add(Calendar.DATE, -retentionPeriod);
            try {
                log.info("All data records before " + calendar.getTime() + "[" + calendar.getTimeInMillis() +
                            "] will be purged from " + table);
                AnalyticsServiceHolder.getAnalyticsDataService().delete(Integer.parseInt(tenantId), table, Long.MIN_VALUE,
                                                               calendar.getTimeInMillis());
            } catch (Exception e) {
                log.error("Unable to perform data purging task due to " + e.getMessage(), e);
            }
        } else {
            log.error("Retention period either empty or null.");
        }
    }
}
