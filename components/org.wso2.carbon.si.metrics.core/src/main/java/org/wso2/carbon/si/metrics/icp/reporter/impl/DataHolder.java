/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.si.metrics.icp.reporter.impl;

import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.si.metrics.icp.reporter.utils.Constants;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.util.Set;

public class DataHolder {

    private static DataHolder instance = new DataHolder();
    private String siddhiHost = "https://localhost:9443/";

    private DataHolder() {
    }

    /**
     * Provide instance of DataHolder class.
     *
     * @return Instance of DataHolder
     */
    public static DataHolder getInstance() {
        return instance;
    }

    public void loadHttpsListenerConfig(ConfigProvider configProvider) throws ConfigurationException {
        TransportsConfiguration transportsConfiguration =
                configProvider.getConfigurationObject(Constants.WSO_2_TRANSPORT_HTTP, TransportsConfiguration.class);
        Set<ListenerConfiguration> listenerConfigurations = transportsConfiguration.getListenerConfigurations();
        for (ListenerConfiguration config : listenerConfigurations) {
            if (config.getId().equals(Constants.MSF4J_HTTPS)) {
                siddhiHost = Constants.HTTPS + config.getHost() + Constants.COLON + config.getPort() + Constants.SLASH;
            }
        }
    }

    public String getSiddhiHost() {
        return siddhiHost;
    }
}
