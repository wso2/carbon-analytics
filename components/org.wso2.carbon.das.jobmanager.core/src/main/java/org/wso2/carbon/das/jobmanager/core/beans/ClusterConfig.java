/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.das.jobmanager.core.beans;


import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.das.jobmanager.core.util.Utils;

import java.util.List;

/**
 * This class represents the cluster configuration.
 */
@Configuration(namespace = "cluster.config", description = "Cluster Configuration")
public class ClusterConfig {
    private Boolean enabled;
    private String groupId;
    private String coordinationStrategyClass;
    private CoordinationModeConfig modeConfig;
    private CoordinationStrategyConfig strategyConfig;
    private List<ResourceManagerConfig> resourceManagers;

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
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

    public void setCoordinationStrategyClass(String coordinationStrategy) {
        this.coordinationStrategyClass = coordinationStrategy;
    }

    public CoordinationModeConfig getModeConfig() {
        return modeConfig;
    }

    public void setModeConfig(CoordinationModeConfig modeConfig) {
        this.modeConfig = modeConfig;
    }

    public CoordinationStrategyConfig getStrategyConfig() {
        return strategyConfig;
    }

    public void setStrategyConfig(CoordinationStrategyConfig strategyConfig) {
        this.strategyConfig = strategyConfig;
    }

    public List<ResourceManagerConfig> getResourceManagers() {
        return resourceManagers;
    }

    public void setResourceManagers(List<ResourceManagerConfig> resourceManagers) {
        this.resourceManagers = resourceManagers;
    }

    @Override
    public boolean equals(Object rhs) {
        if (!(rhs instanceof ClusterConfig)) {
            return false;
        }
        ClusterConfig dsmInfo = (ClusterConfig) rhs;
        if (!Utils.nullAllowEquals(dsmInfo.isEnabled(), this.isEnabled())) {
            return false;
        }
        if (!Utils.nullAllowEquals(dsmInfo.getGroupId(), this.getGroupId())) {
            return false;
        }
        if (!Utils.nullAllowEquals(dsmInfo.getCoordinationStrategyClass(), this.getCoordinationStrategyClass())) {
            return false;
        }
        if (!Utils.nullAllowEquals(dsmInfo.getModeConfig(), this.getModeConfig())) {
            return false;
        }
        if (!Utils.nullAllowEquals(dsmInfo.getStrategyConfig(), this.getStrategyConfig())) {
            return false;
        }
        return Utils.nullAllowEquals(dsmInfo.getResourceManagers(), this.getResourceManagers());
    }

    @Override
    public int hashCode() {
        assert false : "hashCode() not implemented";
        return -1;
    }
}
