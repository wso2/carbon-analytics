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
package org.wso2.carbon.si.metrics.prometheus.reporter.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.dropwizard.samplebuilder.CustomMappingSampleBuilder;
import io.prometheus.client.dropwizard.samplebuilder.MapperConfig;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.wso2.carbon.metrics.core.reporter.impl.AbstractReporter;
import org.wso2.carbon.si.metrics.prometheus.reporter.config.CustomMapperConfig;
import org.wso2.carbon.si.metrics.prometheus.reporter.config.CustomMappingBuilder;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * A reporter which outputs measurements to prometheus.
 */
public class PrometheusReporter extends AbstractReporter {

    private static final Logger log = Logger.getLogger(PrometheusReporter.class);
    private static final String MAPPINGS_RESOURCE_FILE = "configuration.yaml";
    private final MetricRegistry metricRegistry;
    private final MetricFilter metricFilter;
    private PrometheusReporter prometheusReporter;
    private CollectorRegistry collectorRegistry;
    private HTTPServer server;
    private String reporterName;
    private String serverURL;

    private PrometheusReporter(String reporterName, MetricRegistry metricRegistry,
                               MetricFilter metricFilter, String serverURL) {
        super(reporterName);
        this.reporterName = reporterName;
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.serverURL = serverURL;
    }

    @Override
    public void startReporter() {

        prometheusReporter = PrometheusReporter.forRegistry(metricRegistry, serverURL)
                .filter(metricFilter)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        collectorRegistry = new CollectorRegistry();

        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(MAPPINGS_RESOURCE_FILE)) {

            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(PrometheusMetricsLabelsMapper.class,
                    PrometheusMetricsLabelsMapper.class.getClassLoader()));
            yaml.setBeanAccess(BeanAccess.FIELD);
            PrometheusMetricsLabelsMapper metricsLabelsMapping = yaml.loadAs(inputStream,
                                                                                PrometheusMetricsLabelsMapper.class);
            List<CustomMapperConfig> metricsMappings = new ArrayList<>(
                                                                metricsLabelsMapping.getMetricsLabelMapping().values());
            SampleBuilder sampleBuilder = new CustomMappingBuilder(metricsMappings);
            Collector collector = new DropwizardExports(metricRegistry, sampleBuilder);
            collectorRegistry.register(collector);
        } catch (IOException e) {
            log.error("Unable to read the metrics labels mappings for 'Prometheus Reporter'. " +
                    "Starting reporter without mappings.");
        }

        try {

            URL target = new URL(serverURL);
            InetSocketAddress address = new InetSocketAddress(target.getHost(), target.getPort());
            server = new HTTPServer(address, collectorRegistry);
            log.info("Prometheus Server has successfully connected at " + serverURL);
        } catch (MalformedURLException e) {
            log.error("Invalid server url '" + serverURL + "' configured for '" + reporterName + "'.", e);
        } catch (IOException e) {
            log.error("Failed to start Prometheus reporter '" + reporterName + "' at '" + serverURL + "'.", e);
        }
    }

    @Override
    public void stopReporter() {
        if (prometheusReporter != null) {
            disconnect();
            destroy();
            prometheusReporter.stop();
            prometheusReporter = null;
        }
    }

    public static PrometheusReporter.Builder forRegistry(MetricRegistry registry, String serverURL) {
        return new PrometheusReporter.Builder(registry, serverURL);
    }

    private void disconnect() {
        if (server != null) {
            server.stop();
            log.info("Prometheus Server successfully stopped at " + serverURL);
        }
    }

    private void destroy() {
        if (collectorRegistry != null) {
            collectorRegistry.clear();
        }
    }

    /**
     * Builds a {@link PrometheusReporter} with the given properties.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private MetricFilter filter;
        private String serverURL;

        private Builder(MetricRegistry registry, String serverURL) {
            this.registry = registry;
            this.serverURL = serverURL;
            this.filter = MetricFilter.ALL;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            return this;
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            return this;
        }

        public PrometheusReporter build() {
            return new PrometheusReporter("prometheus", registry, filter, serverURL);
        }

    }

}
