/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.spark.ServiceHolder;
import org.wso2.carbon.analytics.spark.impl.SparkExecutorServiceImpl;
import org.wso2.carbon.analytics.spark.service.SparkExecutorService;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Created by niranda on 1/20/15.
 */
public class SparkServiceComponent {
    private static final Log log = LogFactory.getLog(SparkServiceComponent.class);

    private static final String CARBON_HOME_ENV = "CARBON_HOME";

    private static final String LOG4J_LOCATION = "repository" + File.separator + "conf" +
                                                 File.separator + "log4j.properties";

    private static final String LOG4J_PROPERTY = "log4j.properties";

    private ServiceRegistration<SparkExecutorService> sparkExecutorServiceRegistration;

    protected void activate(ComponentContext ctx) {
        try {

            if (log.isDebugEnabled()) {
                log.debug("Starting 'SparkServiceComponent'");
            }

//            CassandraStreamDefnConfigReader.readConfigFile();

            // Set CARBON_HOME if not already set for the use of Hive in order
            // to load hive configurations.
            String carbonHome = System.getProperty(CARBON_HOME_ENV);
            if (carbonHome == null) {
                carbonHome = CarbonUtils.getCarbonHome();
                System.setProperty(CARBON_HOME_ENV, carbonHome);
            }

            // Setting up log4j system property so that forked VM during local
            // mode execution can obtain
            // carbon log4j configurations

            String log4jFile = carbonHome + File.separator + LOG4J_LOCATION;
            System.setProperty(LOG4J_PROPERTY, log4jFile);

//            String carbonConf = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
//            try {
//                String path = carbonConf + File.separator + HiveConstants.ANALYZER_CONFIG_XML;
//                AnalyzerFactory.loadAnalyzers(path);
//            } catch (AnalyzerConfigException e) {
//                String errorMessage = "Error loading analyzer-config.xml";
//                log.error(errorMessage, e);
//            }

            // Set and register HiveExecutorService
            ServiceHolder.setSparkExecutorService(new SparkExecutorServiceImpl());

            sparkExecutorServiceRegistration = (ServiceRegistration<SparkExecutorService>) ctx.getBundleContext().
                    registerService(SparkExecutorService.class.getName(),
                                    ServiceHolder.getSparkExecutorService(), null);

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
}
