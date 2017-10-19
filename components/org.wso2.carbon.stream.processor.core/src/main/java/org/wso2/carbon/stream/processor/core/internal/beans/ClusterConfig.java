/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core.internal.beans;

import org.wso2.carbon.config.annotation.Configuration;

/**
 * This class represents the cluster configuration.
 */
@Configuration(namespace = "cluster.config", description = "Cluster Configuration")
public class ClusterConfig {
    private Boolean enabled;
    private String groupId;
    private String coordinationStrategyClass;

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

}
