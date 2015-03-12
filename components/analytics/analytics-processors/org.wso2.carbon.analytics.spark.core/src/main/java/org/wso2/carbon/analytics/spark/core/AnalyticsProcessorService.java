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
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsDeploymentException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsProcessorException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.internal.AnalyticsDeployerManager;
import org.wso2.carbon.analytics.spark.core.internal.AnalyticsPersistenceManager;
import org.wso2.carbon.analytics.spark.core.internal.SparkAnalyticsExecutor;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsProcessorService {
    private static final Log log = LogFactory.getLog(AnalyticsProcessorService.class);

    public void saveScript(int tenantId, String scriptName, String scriptContent, String cronExpression)
            throws AnalyticsPersistenceException {
        try {
            AnalyticsPersistenceManager.getInstance().saveScript(tenantId, scriptName, scriptContent, cronExpression);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error occurred when persisting the script. " + e.getErrorMessage(), e);
            throw e;
        }
    }

    public void deleteScript(int tenantId, String scriptName) throws AnalyticsPersistenceException {
        try {
            AnalyticsPersistenceManager.getInstance().deleteScript(tenantId, scriptName);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error while deleting the script : " + scriptName, e);
            throw e;
        }
    }

    public void updateScript(int tenantId, String scriptName, String scriptContent, String cronExpression)
            throws AnalyticsPersistenceException {
        try {
            AnalyticsPersistenceManager.getInstance().updateScript(tenantId, scriptName, scriptContent, cronExpression);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error while updating the script : " + scriptName, e);
            throw e;
        }
    }

    public List<AnalyticsScript> getAllScripts(int tenantId) {
        return AnalyticsDeployerManager.getInstance().getAnalyticScripts(tenantId);
    }

    public AnalyticsScript getScript(int tenantId, String name) throws AnalyticsProcessorException {
        try {
            return AnalyticsDeployerManager.getInstance().getAnalyticsScript(tenantId, name);
        } catch (AnalyticsDeploymentException ex) {
            log.error("Error while retrieving the script : " + name, ex);
            throw new AnalyticsProcessorException("Error while retrieving the script : " + name, ex);
        }
    }

    public AnalyticsQueryResult[] executeScript(int tenantId, String scriptName) throws AnalyticsProcessorException {
        try {
            AnalyticsScript script = AnalyticsDeployerManager.getInstance().getAnalyticsScript(tenantId, scriptName);
            String[] queries = getQueries(script.getScriptContent());
            if (queries == null) {
                throw new AnalyticsProcessorException("No complete queries provided in the script. " + script.getScriptContent());
            }
            AnalyticsQueryResult[] results = new AnalyticsQueryResult[queries.length];
            int queryIndex = 0;
            for (String query : queries) {
                results[queryIndex] = executeQuery(tenantId, query);
                queryIndex++;
            }
            return results;
        } catch (AnalyticsDeploymentException e) {
            log.error("Error while retrieving the script : " + scriptName, e);
            throw new AnalyticsProcessorException("Error while retrieving the script : " + scriptName, e);
        }
    }

    public String[] getQueries(String scriptContent) {
        if (scriptContent != null && !scriptContent.trim().isEmpty()) {
            String[] queries =  scriptContent.split(";\\r?\\n|;");
            List<String> processedQueries = new ArrayList<>();
            for (String query : queries){
                if (query != null  && !query.trim().isEmpty()){
                    processedQueries.add(query);
                }
            }
            return processedQueries.toArray(new String[processedQueries.size()]);
        }
        return null;
    }

    public AnalyticsQueryResult executeQuery(int tenantId, String query) throws AnalyticsProcessorException {
        if (query != null && !query.trim().isEmpty()) {
            try {
                return SparkAnalyticsExecutor.executeQuery(tenantId, query);
            } catch (AnalyticsExecutionException e) {
                log.error("Error while executing query : " + query, e);
                throw new AnalyticsProcessorException("Error while executing query : " + query, e);
            }
        } else {
            log.error("No queries provided to execute at tenant id :" + tenantId);
            throw new AnalyticsProcessorException("No queries provided to execute.");
        }
    }
}
