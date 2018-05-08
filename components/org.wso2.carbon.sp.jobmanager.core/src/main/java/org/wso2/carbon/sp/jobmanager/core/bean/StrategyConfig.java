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
 * This class represents the cluster Coordination Strategy configuration.
 */
@Configuration(description = "Cluster Coordination Strategy Configuration")
public class StrategyConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String datasource;
    private int heartbeatInterval;
    private int heartbeatMaxRetry;
    private int eventPollingInterval;

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getHeartbeatMaxRetry() {
        return heartbeatMaxRetry;
    }

    public void setHeartbeatMaxRetry(int heartbeatMaxRetry) {
        this.heartbeatMaxRetry = heartbeatMaxRetry;
    }

    public int getEventPollingInterval() {
        return eventPollingInterval;
    }

    public void setEventPollingInterval(int eventPollingInterval) {
        this.eventPollingInterval = eventPollingInterval;
    }
}
