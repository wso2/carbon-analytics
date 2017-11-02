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
package org.wso2.carbon.stream.processor.statistics.service;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.stream.processor.statistics.internal.StreamProcessorStatisticDataHolder;

/**
 * This is OSGi-components to register config provider class.
 */
@Component(
        name = "org.wso2.carbon.stream.processor.statistics.service.ConfigServiceComponent",
        service = ConfigServiceComponent.class,
        immediate = true
)
public class ConfigServiceComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceComponent.class);

    public ConfigServiceComponent() {
    }

    @Activate
    protected void start(BundleContext bundleContext) {
        bundleContext.registerService(ConfigServiceComponent.class.getName(),
                new ConfigServiceComponent(), null);
    }
    /**
     * Get the ConfigProvider service.
     * This is the bind method that gets called for ConfigProvider service registration that satisfy the policy.
     *
     * @param configProvider the ConfigProvider service that is registered as a service.
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) throws ConfigurationException {
        StreamProcessorStatisticDataHolder.getInstance().setConfigProvider(configProvider);
    }

    /**
     * This is the unbind method for the above reference that gets called for ConfigProvider instance un-registrations.
     *
     * @param configProvider the ConfigProvider service that get unregistered.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        StreamProcessorStatisticDataHolder.getInstance().setConfigProvider(null);
    }

    /**
     * Get the carbon metrics MetricManagementService service.
     * This is the bind method that gets called for ConfigProvider service registration that satisfy the policy.
     *
     * @param metricManagementService the carbon metrics MetricManagementService service that is registered as a service.
     */
    @Reference(
            name = "carbon.metrics.management.service",
            service = MetricManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricManagementService"
    )
    protected void setMetricManagementService(MetricManagementService metricManagementService) {
        StreamProcessorStatisticDataHolder.getInstance().setMetricsManagementService(metricManagementService);
    }

    /**
     * This is the unbind method for unbound carbon metrics MetricManagementService.
     *
     * @param metricManagementService the carbon metrics MetricManagementService service that get unregistered.
     */
    protected void unsetMetricManagementService(MetricManagementService metricManagementService) {
        StreamProcessorStatisticDataHolder.getInstance().setMetricsManagementService(null);
    }
}
