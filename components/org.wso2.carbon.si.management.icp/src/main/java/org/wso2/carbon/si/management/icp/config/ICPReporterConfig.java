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
package org.wso2.carbon.si.management.icp.config;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.config.model.ReporterConfig;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.si.management.icp.impl.ICPReporter;

import java.util.Optional;

/**
 * Configuration for ICP Reporter. Implements {@link ReporterBuilder} to construct a {@link ICPReporter}.
 */
public class ICPReporterConfig extends ReporterConfig implements ReporterBuilder<ICPReporter> {

    private static final Logger logger = LoggerFactory.getLogger(ICPReporterConfig.class);
    private int port = 9070;
    private String dashboardURL = "https://localhost:9743/dashboard/api";
    private int heartbeatInterval = 5;
    private String groupID = "si_dev";
    private String nodeID = "node_1";

    public ICPReporterConfig() {
        super("icp");
    }

    public int getPort() {
        return port;
    }

    @Override
    public Optional<ICPReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        logger.info("Creating ICP Reporter '{}' for Metrics at '{}'", getName(), port);
        return Optional.of(
                ICPReporter.forRegistry(metricRegistry, port, dashboardURL, heartbeatInterval, groupID, nodeID)
                        .build());
    }

}
