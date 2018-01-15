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

package org.wso2.carbon.stream.processor.core.persistence.beans;

import org.wso2.carbon.config.annotation.Configuration;

@Configuration(namespace = "state.persistence", description = "Query configurations for state persistence")
public class PersistenceConfigurations {
    private boolean enabled = false;
    private int intervalInMin = 1;
    private int revisionsToKeep = 3;
    private String persistenceStore = "org.wso2.carbon.stream.processor.core.persistence.FileSystemPersistenceStore";
    private PersistenceStoreConfigs config;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getIntervalInMin() {
        return intervalInMin;
    }

    public void setIntervalInMin(int intervalInMin) {
        this.intervalInMin = intervalInMin;
    }

    public int getRevisionsToKeep() {
        return revisionsToKeep;
    }

    public void setRevisionsToKeep(int revisionsToKeep) {
        this.revisionsToKeep = revisionsToKeep;
    }

    public String getPersistenceStore() {
        return persistenceStore;
    }

    public void setPersistenceStore(String persistenceStore) {
        this.persistenceStore = persistenceStore;
    }

    public PersistenceStoreConfigs getConfig() {
        return config;
    }

    public void setConfig(PersistenceStoreConfigs config) {
        this.config = config;
    }
}
