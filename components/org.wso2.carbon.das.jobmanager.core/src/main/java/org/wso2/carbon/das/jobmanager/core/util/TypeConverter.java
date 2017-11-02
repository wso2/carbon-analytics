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

package org.wso2.carbon.das.jobmanager.core.util;

import org.wso2.carbon.das.jobmanager.core.bean.InterfaceConfig;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNodeConfig;

public class TypeConverter {

    public static InterfaceConfig convert(org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig config) {
        InterfaceConfig iConfig = new InterfaceConfig();
        iConfig.setHost(config.getHost());
        iConfig.setPort(config.getPort());
        return iConfig;
    }

    public static org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig convert(InterfaceConfig config) {
        org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig iConfig =
                new org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig();
        iConfig.setHost(config.getHost());
        iConfig.setPort(config.getPort());
        return iConfig;
    }

    public static ManagerNodeConfig convert(ManagerNode node) {
        ManagerNodeConfig config = new ManagerNodeConfig();
        config.setId(node.getId());
        config.setHttpInterface(convert(node.getHttpInterface()));
        config.setHeartbeatInterval(node.getHeartbeatInterval());
        config.setHeartbeatMaxRetry(node.getHeartbeatMaxRetry());
        return config;
    }

    public static ManagerNode convert(ManagerNodeConfig config) {
        ManagerNode node = new ManagerNode();
        node.setId(config.getId());
        node.setHttpInterface(convert(config.getHttpInterface()));
        node.setHeartbeatInterval(config.getHeartbeatInterval());
        node.setHeartbeatMaxRetry(config.getHeartbeatMaxRetry());
        return node;
    }

}
