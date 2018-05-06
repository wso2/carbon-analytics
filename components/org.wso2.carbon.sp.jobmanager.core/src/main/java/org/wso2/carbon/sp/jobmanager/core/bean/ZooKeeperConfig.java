/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.config.annotation.Element;

import java.io.Serializable;

/**
 * This class represents the zooKeeper configurations.
 */
@Configuration(description = "ZooKeeper Configurations")
public class ZooKeeperConfig implements Serializable {

    @Element(description = "ZooKeeper urls of Kafka cluster", required = true)
    private String zooKeeperURLs;
    private int connectionTimeout = 10000;
    private int sessionTimeout = 10000;

    public String getZooKeeperURLs() {
        return zooKeeperURLs;
    }

    public void setZooKeeperURLs(String zooKeeperURLs) {
        this.zooKeeperURLs = zooKeeperURLs;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

}
