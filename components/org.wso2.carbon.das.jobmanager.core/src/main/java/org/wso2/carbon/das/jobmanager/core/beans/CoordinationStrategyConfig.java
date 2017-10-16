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

/**
 * This class represents the cluster Coordination Strategy configuration.
 */
@Configuration(description = "Cluster Coordination Strategy Configuration")
public class CoordinationStrategyConfig {
    private String datasource;
    private int heartbeatInterval;
    private int heartbeatMaxAge;
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

    public int getHeartbeatMaxAge() {
        return heartbeatMaxAge;
    }

    public void setHeartbeatMaxAge(int heartbeatMaxAge) {
        this.heartbeatMaxAge = heartbeatMaxAge;
    }

    public int getEventPollingInterval() {
        return eventPollingInterval;
    }

    public void setEventPollingInterval(int eventPollingInterval) {
        this.eventPollingInterval = eventPollingInterval;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode() not implemented";
        return -1;
    }
}
