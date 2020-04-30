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

import io.prometheus.client.dropwizard.samplebuilder.MapperConfig;
import org.wso2.carbon.si.metrics.prometheus.reporter.config.CustomMapperConfig;

import java.util.Map;

/**
 * class which hold metric yaml configuration.
 */
public class PrometheusMetricsLabelsMapper {

    private Map<String, CustomMapperConfig> metricsLabelMapping;

    public Map<String, CustomMapperConfig> getMetricsLabelMapping() {
        return metricsLabelMapping;
    }

    public void setMetricsLabelMapping(Map<String, CustomMapperConfig> metricsLabelMapping) {
        this.metricsLabelMapping = metricsLabelMapping;
    }

    @Override
    public String toString() {
        return "YamlConfig{" + "metrics=" + metricsLabelMapping + '}';
    }
}
