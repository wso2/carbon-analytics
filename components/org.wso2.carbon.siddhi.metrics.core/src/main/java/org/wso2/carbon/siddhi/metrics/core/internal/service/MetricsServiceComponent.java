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
package org.wso2.carbon.siddhi.metrics.core.internal.service;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiMetricsDataHolder;

/**
 * Service component for getting the wso2 carbon metrics service.
 */
@Component(
        name = "org.wso2.carbon.siddhi.metrics.core.internal.service.MetricsServiceComponent",
        service = MetricsServiceComponent.class,
        immediate = true
)
public class MetricsServiceComponent {
    private static final Logger log = LoggerFactory.getLogger(MetricsServiceComponent.class);
    
    public MetricsServiceComponent() {
    }
    
    @Activate
    protected void start(BundleContext bundleContext) {
        log.debug("MetricsServiceComponent has been activated.");
    }
    
    
    @Deactivate
    protected void stop() throws Exception {
        log.debug("MetricsServiceComponent has been stop.");
    }
    
    
    /**
     * This is the bind method which gets called at the registration of {@link MetricService}.
     *
     * @param metricService The {@link MetricService} instance registered as an OSGi service
     */
    @Reference(
            name = "carbon.metrics.service",
            service = MetricService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricService"
    )
    protected void setMetricService(MetricService metricService) {
        SiddhiMetricsDataHolder.getInstance().setMetricService(metricService);
        if (log.isDebugEnabled()) {
            log.debug("@Reference(bind) CarbonMetricsService");
        }
    }
    
    /**
     * This is the unbind method which gets called at the un-registration of {@link MetricService}.
     *
     * @param metricService The {@link MetricService} instance registered as an OSGi service
     */
    protected void unsetMetricService(MetricService metricService) {
        SiddhiMetricsDataHolder.getInstance().setMetricService(null);
        if (log.isDebugEnabled()) {
            log.debug("@Reference(unbind) EventStreamService");
        }
    }
    
    @Reference(
            name = "carbon.metrics.management.service",
            service = MetricManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricManagementService"
    )
    protected void setMetricManagementService(MetricManagementService metricManagementService) {
        SiddhiMetricsDataHolder.getInstance().setMetricManagementService(metricManagementService);
    }
    
    /**
     * This is the unbind method for unbound carbon metrics MetricManagementService.
     *
     * @param metricManagementService the carbon metrics MetricManagementService service that get unregistered.
     */
    protected void unsetMetricManagementService(MetricManagementService metricManagementService) {
        SiddhiMetricsDataHolder.getInstance().setMetricManagementService(null);
    }
}
