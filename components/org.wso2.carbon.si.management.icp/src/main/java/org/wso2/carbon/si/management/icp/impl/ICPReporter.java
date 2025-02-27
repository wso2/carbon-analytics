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
package org.wso2.carbon.si.management.icp.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.reporter.impl.AbstractReporter;

/**
 * A reporter which outputs measurements to ICP.
 */
public class ICPReporter extends AbstractReporter {

    private static final Logger log = LoggerFactory.getLogger(ICPReporter.class);
    private MetricRegistry metricRegistry;
    private MetricFilter metricFilter;
    private ICPReporter icpReporter;
    private final String reporterName;
    private int port;
    private String dashboardURL;
    private int heartbeatInterval;
    private String groupId;
    private String nodeId;

    private ICPReporter(String reporterName, MetricRegistry metricRegistry, MetricFilter metricFilter, int port,
                        String dashboardURL, int heartbeatInterval, String groupId, String nodeId) {
        super(reporterName);
        this.reporterName = reporterName;
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.port = port;
        this.dashboardURL = dashboardURL;
        this.heartbeatInterval = heartbeatInterval;
        this.groupId = groupId;
        this.nodeId = nodeId;
    }

    @Override
    public void startReporter() {
        icpReporter = forRegistry(metricRegistry, port, dashboardURL, heartbeatInterval, groupId, nodeId)
                .filter(metricFilter)
                .build();

        ICPHeartbeatComponent
                icpHeartbeatComponent = new ICPHeartbeatComponent(dashboardURL, heartbeatInterval, groupId, nodeId);
        icpHeartbeatComponent.invokeHeartbeatExecutorService();
        log.info("ICP Reported Server has successfully connected at " + DataHolder.getInstance().getSiddhiHost());
    }

    @Override
    public void stopReporter() {
        if (icpReporter != null) {
            disconnect();
            destroy();
            icpReporter.stop();
            icpReporter = null;
        }
    }

    public static Builder forRegistry(MetricRegistry registry, int port, String dashboardURL, int heartbeatInterval,
                                      String groupId, String nodeId) {
        return new Builder(registry, port, dashboardURL, heartbeatInterval, groupId, nodeId);
    }

    private void disconnect() {
        log.info("ICP Server successfully stopped at " + port);
    }

    private void destroy() {
    }

    /**
     * Builds a {@link ICPReporter} with the given properties.
     */
    public static class Builder {

        private final MetricRegistry registry;
        private MetricFilter filter;
        private final int port;
        private String dashboardURL;
        private int heartbeatInterval;
        private String groupId;
        private String nodeId;

        private Builder(MetricRegistry registry, int port, String dashboardURL, int heartbeatInterval, String groupId,
                        String nodeId) {
            this.registry = registry;
            this.port = port;
            this.dashboardURL = dashboardURL;
            this.heartbeatInterval = heartbeatInterval;
            this.groupId = groupId;
            this.nodeId = nodeId;
            this.filter = MetricFilter.ALL;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public ICPReporter build() {
            return new ICPReporter("ICP", registry, filter, port, dashboardURL, heartbeatInterval, groupId, nodeId);
        }

    }

}
