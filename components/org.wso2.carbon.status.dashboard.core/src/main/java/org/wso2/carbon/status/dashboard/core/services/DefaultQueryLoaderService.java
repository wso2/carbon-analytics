/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.status.dashboard.core.services;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.status.dashboard.core.bean.SpDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.configuration.ConfigurationBuilder;

/**
 * Default query loader from the deployment yaml.
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.core.internal.config.loaderServiceComponent",
        service = DefaultQueryLoaderService.class,
        immediate = true
)
public class DefaultQueryLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultQueryLoaderService.class);
    private  SpDashboardConfiguration dashboardDefaultConfiguration;

    public DefaultQueryLoaderService() {
    }

    private static DefaultQueryLoaderService instance = new DefaultQueryLoaderService();

    public static DefaultQueryLoaderService getInstance() {
        return instance;
    }

    @Activate
    protected void start(BundleContext bundleContext) {
        logger.info("Default query loader component is up.");
        dashboardDefaultConfiguration = ConfigurationBuilder.getInstance().getConfiguration();
    }

    public SpDashboardConfiguration getDashboardDefaultConfigurations() {
        return dashboardDefaultConfiguration;
    }
}
