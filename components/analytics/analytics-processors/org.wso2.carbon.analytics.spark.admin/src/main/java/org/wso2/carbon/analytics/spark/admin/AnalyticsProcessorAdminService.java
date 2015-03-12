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
package org.wso2.carbon.analytics.spark.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.admin.dto.AnalyticsScriptDto;
import org.wso2.carbon.analytics.spark.admin.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.admin.dto.AnalyticsQueryResultDto;
import org.wso2.carbon.analytics.spark.admin.internal.AnalyticsResultConverter;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsProcessorAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(AnalyticsProcessorAdminService.class);

    public void saveScript(String scriptName, String scriptContent, String cronExpression)
            throws AnalyticsProcessorAdminException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ServiceHolder.getAnalyticsProcessorService().saveScript(tenantId, scriptName, scriptContent, cronExpression);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error occurred when persisting the script. " + e.getMessage(), e);
            throw new AnalyticsProcessorAdminException("Error occurred when persisting the script. "
                    + e.getMessage(), e);
        }
    }

    public void saveScriptContent(String scriptName, String scriptContent) throws AnalyticsProcessorAdminException {
        this.saveScript(scriptName, scriptContent, null);
    }

    public void deleteScript(String scriptName) throws AnalyticsProcessorAdminException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ServiceHolder.getAnalyticsProcessorService().deleteScript(tenantId, scriptName);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error while deleting the script : " + scriptName, e);
            throw new AnalyticsProcessorAdminException("Error while deleting the script : " + scriptName, e);
        }
    }

    public void updateScriptContent(String scriptName, String scriptContent) throws AnalyticsProcessorAdminException {
        this.updateScript(scriptName, scriptContent, AnalyticsConstants.DEFAULT_CRON);
    }

    public void updateScriptTask(String scriptName, String cronExpression) throws AnalyticsProcessorAdminException {
        this.updateScript(scriptName, null, cronExpression);
    }

    public void updateScript(String scriptName, String scriptContent, String cronExpression)
            throws AnalyticsProcessorAdminException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ServiceHolder.getAnalyticsProcessorService().updateScript(tenantId, scriptName, scriptContent, cronExpression);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error while updating the script : " + scriptName, e);
            throw new AnalyticsProcessorAdminException("Error while updating the script : " + scriptName, e);
        }
    }

    public String[] getAllScriptNames() throws AnalyticsProcessorAdminException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            List<AnalyticsScript> analyticsScripts = ServiceHolder.getAnalyticsProcessorService().getAllScripts(tenantId);
            List<String> analyticsNames = new ArrayList<>();
            for (AnalyticsScript analyticsScript : analyticsScripts) {
                analyticsNames.add(analyticsScript.getName());
            }
            return analyticsNames.toArray(new String[analyticsNames.size()]);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error while retrieving all scripts for tenant Id : "+ tenantId, e);
            throw new AnalyticsProcessorAdminException("Error while retrieving all scripts for tenant Id : "+ tenantId);
        }
    }

    public AnalyticsScriptDto getScript(String name) throws AnalyticsProcessorAdminException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            AnalyticsScript script = ServiceHolder.getAnalyticsProcessorService().getScript(tenantId, name);
            AnalyticsScriptDto scriptDto = new AnalyticsScriptDto(script.getName());
            scriptDto.setScriptContent(script.getScriptContent());
            scriptDto.setCronExpression(script.getCronExpression());
            return scriptDto;
        } catch (AnalyticsPersistenceException ex) {
            log.error("Error while retrieving the script : " + name, ex);
            throw new AnalyticsProcessorAdminException("Error while retrieving the script : " + name, ex);
        }
    }

    public AnalyticsQueryResultDto[] executeScript(String scriptName) throws AnalyticsProcessorAdminException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            AnalyticsScript script = ServiceHolder.getAnalyticsProcessorService().getScript(tenantId, scriptName);
            return execute(script.getScriptContent());
        } catch (AnalyticsPersistenceException e) {
            log.error("Error while running the script : " + scriptName, e);
            throw new AnalyticsProcessorAdminException("Error while running the script : " + scriptName, e);
        }
    }

    public AnalyticsQueryResultDto[] execute(String scriptContent) throws AnalyticsProcessorAdminException {
        if (scriptContent != null && !scriptContent.trim().isEmpty()) {
            String[] queries = ServiceHolder.getAnalyticsProcessorService().getQueries(scriptContent);
            AnalyticsQueryResultDto[] results = new AnalyticsQueryResultDto[queries.length];
            int index = 0;
            for (String query : queries) {
                AnalyticsQueryResultDto queryResult = executeQuery(query);
                if (queryResult == null) queryResult = new AnalyticsQueryResultDto(query);
                results[index] = queryResult;
                index++;
            }
            return results;
        } else {
            log.error("No queries provided to execute at tenant id :" + PrivilegedCarbonContext.
                    getThreadLocalCarbonContext().getTenantId());
            throw new AnalyticsProcessorAdminException("No queries provided to execute.");
        }
    }

    public AnalyticsQueryResultDto executeQuery(String query) throws AnalyticsProcessorAdminException {
        if (query != null && !query.trim().isEmpty()) {
            try {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                AnalyticsQueryResultDto queryResult = AnalyticsResultConverter.
                        convertResults(ServiceHolder.getAnalyticsProcessorService().executeQuery(tenantId, query));
                if (queryResult != null) queryResult.setQuery(query);
                return queryResult;
            } catch (AnalyticsExecutionException e) {
                log.error("Error while executing query : " + query, e);
                throw new AnalyticsProcessorAdminException("Error while executing query : " + query, e);
            }
        } else {
            log.error("No queries provided to execute at tenant id :" + PrivilegedCarbonContext.
                    getThreadLocalCarbonContext().getTenantId());
            throw new AnalyticsProcessorAdminException("No queries provided to execute.");
        }
    }
}
