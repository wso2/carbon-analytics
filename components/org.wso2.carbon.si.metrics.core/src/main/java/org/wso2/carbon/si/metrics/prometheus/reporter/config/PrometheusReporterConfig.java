/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.si.metrics.prometheus.reporter.config;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.config.model.ReporterConfig;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.si.metrics.prometheus.reporter.impl.PrometheusReporter;

/**
 * Configuration for Prometheus Reporter. Implements {@link ReporterBuilder} to construct a {@link PrometheusReporter}.
 */
public class PrometheusReporterConfig extends ReporterConfig implements ReporterBuilder<PrometheusReporter> {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusReporterConfig.class);
    private String serverURL = "http://0.0.0.0:9080";

    public PrometheusReporterConfig() {
        super("prometheus");
    }

    public String getServerURL() {
        return serverURL;
    }

    @Override
    public Optional<PrometheusReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        logger.info("Creating Prometheus Reporter '{}' for Metrics at '{}'", getName(), serverURL);
        return Optional.of(PrometheusReporter.forRegistry(metricRegistry, serverURL).build());
    }

}
