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

import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SampleBuilder implementation to allow Dropwizard metrics to be translated to Prometheus metrics including
 * custom labels and names.
 */
public class CustomMappingBuilder implements SampleBuilder {
    private final List<CustomMappingBuilder.CompiledMapperConfig> compiledMapperConfigs;
    private final DefaultSampleBuilder defaultMetricSampleBuilder = new DefaultSampleBuilder();

    public CustomMappingBuilder(final List<CustomMapperConfig> mapperConfigs) {
        if (mapperConfigs == null || mapperConfigs.isEmpty()) {
            throw new IllegalArgumentException("CustomMappingSampleBuilder needs some mapper configs!");
        }

        this.compiledMapperConfigs = new ArrayList<>(mapperConfigs.size());
        for (CustomMapperConfig config : mapperConfigs) {
            this.compiledMapperConfigs.add(new CustomMappingBuilder.CompiledMapperConfig(config));
        }
    }

    @Override
    public Collector.MetricFamilySamples.Sample createSample(final String dropwizardName, final String nameSuffix, final List<String> additionalLabelNames, final List<String> additionalLabelValues, final double value) {
        if (dropwizardName == null) {
            throw new IllegalArgumentException("Dropwizard metric name cannot be null");
        }

        CustomMappingBuilder.CompiledMapperConfig matchingConfig = null;
        for (CustomMappingBuilder.CompiledMapperConfig config : this.compiledMapperConfigs) {
            if (config.pattern.matches(dropwizardName)) {
                matchingConfig = config;
                break;
            }
        }

        if (matchingConfig != null) {
            final Map<String, String> params = matchingConfig.pattern.extractParameters(dropwizardName);
            final CustomMappingBuilder.NameAndLabels nameAndLabels = getNameAndLabels(matchingConfig.mapperConfig, params);
            nameAndLabels.labelNames.addAll(additionalLabelNames);
            nameAndLabels.labelValues.addAll(additionalLabelValues);
            return defaultMetricSampleBuilder.createSample(
                    nameAndLabels.name, nameSuffix,
                    nameAndLabels.labelNames,
                    nameAndLabels.labelValues,
                    value
            );
        }


        return defaultMetricSampleBuilder.createSample(
                dropwizardName, nameSuffix,
                additionalLabelNames,
                additionalLabelValues,
                value
        );
    }

    protected CustomMappingBuilder.NameAndLabels getNameAndLabels(final CustomMapperConfig config, final Map<String, String> parameters) {
        final String metricName = formatTemplate(config.getName(), parameters);
        final List<String> labels = new ArrayList<>(config.getLabels().size());
        final List<String> labelValues = new ArrayList<>(config.getLabels().size());
        for (Map.Entry<String, String> entry : config.getLabels().entrySet()) {
            labels.add(entry.getKey());
            labelValues.add(formatTemplate(entry.getValue(), parameters));
        }

        return new CustomMappingBuilder.NameAndLabels(metricName, labels, labelValues);
    }

    private String formatTemplate(final String template, final Map<String, String> params) {
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    static class CompiledMapperConfig {
        final CustomMapperConfig mapperConfig;
        final CustomGraphiteNamePattern pattern;

        CompiledMapperConfig(final CustomMapperConfig mapperConfig) {
            this.mapperConfig = mapperConfig;
            this.pattern = new CustomGraphiteNamePattern(mapperConfig.getMatch());
        }
    }

    static class NameAndLabels {
        final String name;
        final List<String> labelNames;
        final List<String> labelValues;

        NameAndLabels(final String name, final List<String> labelNames, final List<String> labelValues) {
            this.name = name;
            this.labelNames = labelNames;
            this.labelValues = labelValues;
        }
    }
}