/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.io.mgwfile;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.extension.siddhi.io.mgwfile.task.MGWFileCleanUpTask;
import org.wso2.extension.siddhi.io.mgwfile.util.MGWFileSourceDBUtil;

import java.util.Timer;
import java.util.TimerTask;


/**
 * This is the activation and deactivation class for MGWFile osgi component
 */
@Component(
        name = "org.wso2.analytics.apim.MGWFileSourceDS",
        immediate = true
)
public class MGWFileSourceDS {

    private static final Log log = LogFactory.getLog(MGWFileSourceDS.class);
    private static String fileReaderFrequency;
    private static String fileRetentionDays;
    private static String fileCleanupFrequency;
    private static String workerThreadCount;

    /**
     * This is the activation method of MGWFileSource service. This will be called when its references are
     * satisfied. Agent server is initialized here
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("MGWFileSource Component is started");
        }
        initializeSystemProperties();
        TimerTask fileCleanupTask = new MGWFileCleanUpTask();
        Timer cleanupTimer = new Timer();
        cleanupTimer.schedule(fileCleanupTask, 1000, Long.parseLong(fileCleanupFrequency));
    }

    /**
     * This is the deactivation method of WSO2EventSource service. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("MGWFileSource Component is stopped");
        }
    }

    private void initializeSystemProperties() {
        //initilize fileReaderFrequency
        fileReaderFrequency = System
                .getProperty(MGWFileSourceConstants.UPLOADED_USAGE_PUBLISH_FREQUENCY_PROPERTY);
        if (StringUtils.isEmpty(fileReaderFrequency)) {
            log.debug("Default usage publishing frequency will be used");
            fileReaderFrequency = MGWFileSourceConstants.DEFAULT_UPLOADED_USAGE_PUBLISH_FREQUENCY;
        }
        //initilize fileCleanupFrequency
        fileCleanupFrequency = System
                .getProperty(MGWFileSourceConstants.UPLOADED_USAGE_CLEANUP_FREQUENCY_PROPERTY);
        if (StringUtils.isEmpty(fileCleanupFrequency)) {
            log.debug("Default cleanup frequency will be used");
            fileCleanupFrequency = MGWFileSourceConstants.DEFAULT_UPLOADED_USAGE_CLEANUP_FREQUENCY;
        }
        //initialize fileRetentionDays
        fileRetentionDays = System.getProperty(MGWFileSourceConstants.FILE_RETENTION_DAYS_PROPERTY);
        if (StringUtils.isEmpty(fileRetentionDays)) {
            log.debug("Default file retention days will be used");
            fileRetentionDays = MGWFileSourceConstants.DEFAULT_FILE_RETENTION_DAYS;
        }

        //initialize workerThreadCount
        workerThreadCount = System
                .getProperty(MGWFileSourceConstants.WORKER_THREAD_COUNT_PROPERTY);
        if (StringUtils.isEmpty(workerThreadCount)) {
            log.debug("Default worker thread count will be used");
            workerThreadCount = MGWFileSourceConstants.DEFAULT_WORKER_THREAD_COUNT;
        }
    }

    public static String getFileReaderFrequency() {
        return fileReaderFrequency;
    }

    public static String getFileRetentionDays() {
        return fileRetentionDays;
    }

    public static String getWorkerThreadCount() {
        return workerThreadCount;
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void onDataSourceServiceReady(DataSourceService service) {
        try {
            HikariDataSource dsObject = (HikariDataSource) service.getDataSource("WSO2AM_MGW_ANALYTICS_DB");
            MGWFileSourceDBUtil.setDataSource(dsObject);
        } catch (DataSourceException e) {
            log.error("error occurred while fetching the data source.", e);
        }
    }

    protected void unregisterDataSourceService(DataSourceService dataSourceService) {
        log.info("Unregistering data sources sample");
    }

}
