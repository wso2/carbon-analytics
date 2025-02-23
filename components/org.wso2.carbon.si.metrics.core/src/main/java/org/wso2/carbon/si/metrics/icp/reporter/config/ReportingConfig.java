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
package org.wso2.carbon.si.metrics.icp.reporter.config;

import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for all reporters in Metrics Core.
 */

public class ReportingConfig {

    private Set<ICPReporterConfig> icp;

    public ReportingConfig() {
        this.icp = new HashSet<>();
        this.icp.add(new ICPReporterConfig());
    }

    public Set<ICPReporterConfig> getICP() {
        return icp;
    }

    public void setIcp(Set<ICPReporterConfig> icp) {
        this.icp = icp;
    }

    public Set<? extends ReporterBuilder> getReporterBuilders() {
        Set<ReporterBuilder> reporterBuilders = new HashSet<>();

        if (icp != null) {
            reporterBuilders.addAll(icp);
        }
        return reporterBuilders;
    }
}
