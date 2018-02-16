/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.sp.jobmanager.core.bean;

import org.wso2.carbon.sp.jobmanager.core.impl.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * ManagerConfigurationDetails class contains the configuration details of the manager
 */

public class ManagerConfigurationDetails {
    private String managerId;
    private String host;
    private int port;

    public ManagerConfigurationDetails(String managerId, String host, int port) {
        this.managerId = managerId;
        this.host = host;
        this.port = port;
    }

    public ManagerConfigurationDetails() {
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, Object> managerConfigurationDetails() {
        Map<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(Constants.MANAGERID, managerId);
        objectObjectHashMap.put(Constants.HOST, host);
        objectObjectHashMap.put(Constants.PORT, port);
        return objectObjectHashMap;
    }
}
