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

import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;

import java.util.List;

/**
 * Interface of AnalyticsProcessorService which exposes the
 * functionality of the execution engine, and scripts processing operations.
 * This is exposed as OSGI service for other carbon components to consume and
 * invoke the operations necessarily.
 */

public interface AnalyticsProcessorService {

    /**
     * Save the script information and the queries in the analytics persistence store.
     * @param tenantId       Id of the tenant for which this operation belongs to.
     * @param scriptName     Name of the script which needs to be saved.
     * @param scriptContent  queries content of the script.
     * @param cronExpression The cron expression to specify the
     *                       scheduling time interval of the script.
     * @throws AnalyticsPersistenceException
     */
    void saveScript(int tenantId, String scriptName, String scriptContent, String cronExpression)
            throws AnalyticsPersistenceException;

    /**
     * Delete the script with provided name, from the analytics store
     * from the provided tenant space.
     *
     * @param tenantId   Id of the tenant for which this operation belongs to.
     * @param scriptName Name of the script which needs to be deleted.
     * @throws AnalyticsPersistenceException
     */
    void deleteScript(int tenantId, String scriptName) throws AnalyticsPersistenceException;

    /**
     * Update the script information with given details for the specified tenant.
     *
     * @param tenantId       Id of the tenant for which this operation belongs to.
     * @param scriptName     Name of the script for which the information needs to be updated.
     * @param scriptContent  New queries content of the script.
     * @param cronExpression New cron expression of the script.
     * @throws AnalyticsPersistenceException
     */
    void updateScript(int tenantId, String scriptName, String scriptContent, String cronExpression)
            throws AnalyticsPersistenceException;

    /**
     * Get all the scripts in the provided tenant space and returns back.
     *
     * @param tenantId Id of the tenant for which this operation belongs to.
     * @return List of Analytics Scripts which will have associated name, queries, and cron expression.
     * @throws AnalyticsPersistenceException
     */
    List<AnalyticsScript> getAllScripts(int tenantId) throws AnalyticsPersistenceException;

    /**
     * Get a specific analytics scripts information with given script name in the provided tenant space.
     *
     * @param tenantId Id of the tenant for which this operation belongs to.
     * @param name     Name of the script.
     * @return Complete information of the script.
     * @throws AnalyticsPersistenceException
     */
    AnalyticsScript getScript(int tenantId, String name) throws AnalyticsPersistenceException;

    /**
     * Execute the script with provided name, in the provided tenant space,
     * and return the results of the execution. This is a asynchronous call,
     * and the thread won't be returned until all it's queries has been executed.
     *
     * @param tenantId   Id of the tenant for which this operation belongs to.
     * @param scriptName Name of the script which needs to be executed.
     * @return The Array of result of each queries of the script.
     * @throws AnalyticsExecutionException
     * @throws AnalyticsPersistenceException
     */
    AnalyticsQueryResult[] executeScript(int tenantId, String scriptName) throws AnalyticsExecutionException,
            AnalyticsPersistenceException;

    /**
     * Get each queries in the provided scripts content.
     *
     * @param scriptContent Content of the script.
     * @return Each queries existed in the script content.
     */
    String[] getQueries(String scriptContent);

    /**
     * Execute the provided single query in the provided tenant space, and return the results back.
     *
     * @param tenantId Id of the tenant for which this operation belongs to.
     * @param query    An query which was asked to execute.
     * @return The result returned from the execution of the query.
     * @throws AnalyticsExecutionException
     */
    AnalyticsQueryResult executeQuery(int tenantId, String query) throws AnalyticsExecutionException;

    /**
     * Checks whether the analytics execution is enabled for this node.
     *
     * @return
     */
    boolean isAnalyticsExecutionEnabled();


    /**
     * Checks whether the analytics scheduled task for analytics script is running in background.
     *
     * @return
     */
    boolean isAnalyticsTaskExecuting(String scriptName) throws AnalyticsExecutionException;
}
