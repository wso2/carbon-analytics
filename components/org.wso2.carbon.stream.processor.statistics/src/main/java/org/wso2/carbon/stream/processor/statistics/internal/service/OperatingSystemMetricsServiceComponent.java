/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.stream.processor.statistics.internal.service;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.statistics.internal.OperatingSystemMetricSet;

/**
 * This is OSGi-components to register OperatingSystemMetricSet class.
 */
@Component(
        name = "OperatingSystemMetricsServiceComponent",
        immediate = true
)
public class OperatingSystemMetricsServiceComponent {
    private static final Logger logger = LoggerFactory.getLogger(OperatingSystemMetricsServiceComponent.class);

    @Activate
    protected void activate(BundleContext bundleContext) {
        try {
            OperatingSystemMetricSet operatingSystemMetricSet = new OperatingSystemMetricSet();
            operatingSystemMetricSet.initConnection();
            bundleContext.registerService(OperatingSystemMetricSet.class, operatingSystemMetricSet, null);
            logger.info("OperatingSystemMetricsService Component activated");
        } catch (Exception e) {
            logger.info("OperatingSystemMetricsService Component activation failed");
        }

    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        logger.debug("OperatingSystemMetricsService Component");
    }

    @Reference(
            name = "org.wso2.carbon.stream.processor.statistics.internal.service.ConfigServiceComponent",
            service = ConfigServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigServiceComponent"
    )
    protected void registerConfigServiceComponent(ConfigServiceComponent configServiceComponent) {
        //to make to read the metrics MBean name
    }

    protected void unregisterConfigServiceComponent(ConfigServiceComponent configServiceComponent) {

    }

    @Reference(
            name = "org.wso2.carbon.stream.processor.statistics.internal.service.NodeConfigServiceComponent",
            service = NodeConfigServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterNodeConfigServiceComponent"
    )
    protected void registerNodeConfigServiceComponent(NodeConfigServiceComponent nodeConfigServiceComponent) {
        //to make to read the metrics MBean name
    }

    protected void unregisterNodeConfigServiceComponent(NodeConfigServiceComponent nodeConfigServiceComponent) {
    }

    @Reference(
            name = "org.wso2.carbon.stream.processor.statistics.internal.service.SiddhiAppRuntimeServiceComponent",
            service = SiddhiAppRuntimeServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterSiddhiAppRuntimeServiceComponent"
    )
    protected void registerSiddhiAppRuntimeServiceComponent(SiddhiAppRuntimeServiceComponent serviceComponent) {
        //to make to read the metrics MBean name
    }

    protected void unregisterSiddhiAppRuntimeServiceComponent(SiddhiAppRuntimeServiceComponent serviceComponent) {
    }
}


