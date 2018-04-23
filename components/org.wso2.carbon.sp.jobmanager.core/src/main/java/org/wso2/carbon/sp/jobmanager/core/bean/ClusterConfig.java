/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core.bean;

import org.wso2.carbon.config.annotation.Configuration;

import java.io.Serializable;

/**
 * This class represents the cluster configuration.
 */
@Configuration(namespace = "cluster.config", description = "Cluster Configuration")
public class ClusterConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean enabled = false;
    private String groupId = "sp-distributed";
    private String coordinationStrategyClass;
    private StrategyConfig strategyConfig;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCoordinationStrategyClass() {
        return coordinationStrategyClass;
    }

    public void setCoordinationStrategyClass(String coordinationStrategyClass) {
        this.coordinationStrategyClass = coordinationStrategyClass;
    }

    public StrategyConfig getStrategyConfig() {
        return strategyConfig;
    }

    public void setStrategyConfig(StrategyConfig strategyConfig) {
        this.strategyConfig = strategyConfig;
    }
}
