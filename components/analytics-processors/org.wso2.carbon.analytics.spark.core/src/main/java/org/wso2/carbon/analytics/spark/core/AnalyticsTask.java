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
package org.wso2.carbon.analytics.spark.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.internal.jmx.AnalyticsScriptLastExecutionStartTimeHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

/**
 * This is the task implementation to execute the tasks registered
 * to trigger the spark script execution.
 */
public class AnalyticsTask implements Task {

    private static final Log log = LogFactory.getLog(AnalyticsTask.class);

    private int tenantId;
    private String scriptName;

    @Override
    public void setProperties(Map<String, String> properties) {
        this.tenantId = Integer.parseInt(properties.get(AnalyticsConstants.TASK_TENANT_ID_PROPERTY));
        this.scriptName = properties.get(AnalyticsConstants.TASK_SCRIPT_NAME_PROPERTY);
    }

    @Override
    public void init() {
    }

    @Override
    public void execute() {
        if (ServiceHolder.isAnalyticsExecutionEnabled()) {
            try {
                if (log.isDebugEnabled()){
                    log.debug("Executing the schedule task for: " + scriptName + " for tenant id: " + tenantId);
                }
                if (ServiceHolder.getAnalyticsProcessorService() != null) {
                    ServiceHolder.getAnalyticsProcessorService().executeScript(tenantId, this.scriptName);
                    String id = AnalyticsScriptLastExecutionStartTimeHolder.generateId(tenantId, scriptName);
                    AnalyticsScriptLastExecutionStartTimeHolder.add(id, System.currentTimeMillis());
                } else {
                    log.warn("Analytics Processor inactive now, and hence ignoring the triggered execution");
                }
            } catch (AnalyticsExecutionException | AnalyticsPersistenceException e) {
                log.error("Error while executing the scheduled task for the script: " + scriptName, e);
            }
        } else {
            log.warn("Analytics is disabled in this node, therefore ignoring the triggered execution.");
        }
    }

}
