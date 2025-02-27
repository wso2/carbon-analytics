/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.si.management.icp;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.spi.MetricsExtension;
import org.wso2.carbon.si.management.icp.config.ICPMetricsConfig;
import org.wso2.carbon.si.management.icp.config.ICPReporterConfig;
import org.wso2.carbon.si.management.icp.impl.DataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * * Metrics Extension to support ICP Reporter.
 */
@Component(
        name = "org.wso2.carbon.si.management.icp.ICPMetricsExtension",
        service = MetricsExtension.class
)
public class ICPMetricsExtension implements MetricsExtension {

    private static final Logger logger = LoggerFactory.getLogger(ICPMetricsExtension.class);
    List<String> reporterNames = new ArrayList<>();

    @Override
    public void activate(ConfigProvider configProvider, MetricService metricService,
                         MetricManagementService metricManagementService) {
        ICPMetricsConfig icpMetricsConfig;
        try {
            icpMetricsConfig
                    = configProvider.getConfigurationObject(ICPMetricsConfig.class);
            DataHolder.getInstance().loadHttpsListenerConfig(configProvider);
        } catch (ConfigurationException e) {
            logger.warn("Error loading Metrics Configuration. Starting ICP Reporter " +
                    "with default parameters.", e);
            icpMetricsConfig = new ICPMetricsConfig();
        }
        Set<ICPReporterConfig> icpReporterConfigs = icpMetricsConfig.getReporting().getICP();
        if (icpReporterConfigs != null) {
            icpReporterConfigs.forEach(reporterConfig -> {
                        try {
                            metricManagementService.addReporter(reporterConfig);
                            reporterNames.add(reporterConfig.getName());
                        } catch (ReporterBuildException e) {
                            logger.warn("Failed to start icp reporter '" + reporterConfig.getName() + "'.", e);
                        }
                    }
            );
        }
    }

    @Override
    public void deactivate(MetricService metricService, MetricManagementService metricManagementService) {
        if (reporterNames != null) {
            reporterNames.forEach(metricManagementService::removeReporter);
        }
    }

    @Reference(
            name = "carbon.metrics.service",
            service = MetricService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricService"
    )
    protected void setMetricService(MetricService metricService) {
        // This extension should be activated only after getting MetricService.
        // Metrics Component will activate this extension.
        logger.debug("Metric Service is available as an OSGi service.");

    }

    protected void unsetMetricService(MetricService metricService) {
        // Ignore
    }
}
