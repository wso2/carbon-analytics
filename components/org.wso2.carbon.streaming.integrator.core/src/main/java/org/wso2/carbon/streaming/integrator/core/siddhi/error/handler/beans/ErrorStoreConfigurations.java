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

package org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.beans;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.DBErrorStore;

@Configuration(namespace = "error.store", description = "Query configurations for storing erroneous events")
public class ErrorStoreConfigurations {
    private boolean enabled = false;
    private int bufferSize = 1024;
    private boolean dropWhenBufferFull = true;
    private String errorStore = DBErrorStore.class.getName();
    private ErrorStoreConfigs config;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isDropWhenBufferFull() {
        return dropWhenBufferFull;
    }

    public void setDropWhenBufferFull(boolean dropWhenBufferFull) {
        this.dropWhenBufferFull = dropWhenBufferFull;
    }

    public String getErrorStore() {
        return errorStore;
    }

    public void setErrorStore(String errorStore) {
        this.errorStore = errorStore;
    }

    public ErrorStoreConfigs getConfig() {
        return config;
    }

    public void setConfig(ErrorStoreConfigs config) {
        this.config = config;
    }
}
