/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cluster.coordinator.commons.configs;

import java.util.Map;

/**
 * The class which extracts configuration property names from the yaml.
 */
public class CoordinationStrategyConfigUtil {

    private Map<String, Integer> rdbmsConfig;
    private Map<String, String> zookeeperConfig;
    private String strategy;

    public Map<String, Integer> getRdbmsConfig() {
        return this.rdbmsConfig;
    }

    public void setRdbmsConfig(Map<String, Integer> rdbmsConfig) {
        this.rdbmsConfig = rdbmsConfig;
    }

    public Map<String, String> getZookeeperConfig() {
        return this.zookeeperConfig;
    }

    public void setZookeeperConfig(Map<String, String> zookeeperConfig) {
        this.zookeeperConfig = zookeeperConfig;
    }

    public String getStrategy() {
        return this.strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
