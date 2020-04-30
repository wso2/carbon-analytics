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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * POJO containing info on how to map a graphite metric to a prometheus one.
 */
public class CustomMapperConfig {

    // Labels validation.
    private static final String LABEL_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]+$";
    private static final Pattern LABEL_PATTERN = Pattern.compile(LABEL_REGEX);

    /**
     * Regex used to match incoming metric name.
     * Uses a simplified glob syntax where only '*' and (.*) are allowed.
     * E.g:
     * org.company.controller.*.status.*
     * Will be used to match
     * org.company.controller.controller1.status.200
     * and
     * org.company.controller.controller2.status.400
     *
     * org.company.controller.(.*).status.*
     * Will be used to match
     * org.company.controller.controller1.status.200
     * and
     * org.company.controller.main.controller1.status.200
     */
    private String match;
    private String name;
    private Map<String, String> labels = new HashMap<String, String>();

    public CustomMapperConfig() {
        // empty constructor
    }

    // for tests
    public CustomMapperConfig(final String match) {
        this.match = match;
    }

    public CustomMapperConfig(final String match, final String name, final Map<String, String> labels) {
        this.name = name;
        this.match = match;
        validateLabels(labels);
        this.labels = labels;
    }

    @Override
    public String toString() {
        return String.format("MapperConfig{match=%s, name=%s, labels=%s}", match, name, labels);
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(final String match) {
        this.match = match;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;

    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(final Map<String, String> labels) {
        validateLabels(labels);
        this.labels = labels;
    }

    private void validateLabels(final Map<String, String> labels)
    {
        if (labels != null) {
            for (final String key : labels.keySet()) {
                if (!LABEL_PATTERN.matcher(key).matches()) {
                    throw new IllegalArgumentException(String.format("Label [%s] does not match required pattern %s", match, LABEL_PATTERN));
                }
            }

        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CustomMapperConfig that = (CustomMapperConfig) o;

        if (!Objects.equals(match, that.match)) return false;
        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(labels, that.labels);
    }

    @Override
    public int hashCode() {
        int result = match != null ? match.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        return result;
    }
}
