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
package org.wso2.carbon.si.management.icp.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.si.management.icp.utils.Constants;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class DataHolder {

    private static DataHolder instance = new DataHolder();
    private static final Logger logger = LoggerFactory.getLogger(DataHolder.class);
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

    public void loadHttpsListenerConfig(ConfigProvider configProvider) {
        try {
            TransportsConfiguration transportsConfiguration =
                    configProvider.getConfigurationObject(Constants.WSO2_TRANSPORT_HTTP, TransportsConfiguration.class);
            Set<ListenerConfiguration> listenerConfigurations = transportsConfiguration.getListenerConfigurations();
            for (ListenerConfiguration config : listenerConfigurations) {
                if (config.getId().equals(Constants.MSF4J_HTTPS)) {
                    URI uri = new URI(Constants.HTTPS, null, config.getHost(), config.getPort(), Constants.SLASH, null,
                            null);
                    siddhiHost = uri.toURL().toString();
                }
            }
        } catch (ConfigurationException | URISyntaxException | MalformedURLException e) {
            logger.warn(
                    "Error loading MSF4J HTTPS Configuration. Starting ICP Reporter Service with default parameters.",
                    e);
        }
    }

    public String getSiddhiHost() {
        return siddhiHost;
    }
}
