/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.stream.processor.idp.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.stream.processor.idp.client.core.api.IdPClient;
import org.wso2.carbon.stream.processor.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.stream.processor.idp.client.core.spi.IdPClientInitializer;
import org.wso2.carbon.stream.processor.idp.client.core.utils.config.IdPClientConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class IdPClientManager {
    private static final Logger LOG = LoggerFactory.getLogger(IdPClientManager.class);
    private static final String SP_AUTH_NAMESPACE = "auth.configs";

    private static IdPClient idPClient;
    private static boolean initialized = false;
    private Map<String, IdPClientInitializer> initializers;

    public IdPClientManager() {
        initializers = new HashMap<>();
    }

    public void initIdPClient(ConfigProvider configProvider, Map<String, IdPClientInitializer> initializers)
            throws IdPClientException {
        this.initializers = initializers;
        IdPClientInitializer initializer;
        if (initialized) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("IDPClient Manager already initialised.");
            }
        } else {
            try {
                IdPClientConfiguration idPClientConfiguration;
                if (configProvider.getConfigurationObject(SP_AUTH_NAMESPACE) == null) {
                    idPClientConfiguration = new IdPClientConfiguration();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Enabling default IdPClient Local User Store.");
                    }
                } else {
                    idPClientConfiguration = configProvider.
                            getConfigurationObject(IdPClientConfiguration.class);
                }
                initializer = getIdPClientInitializer(idPClientConfiguration.getType());
                idPClient = initializer.initializeIdPClient(idPClientConfiguration.getProperties());
                initialized = true;
            } catch (ConfigurationException e) {
                throw new IdPClientException("Error in reading '" + SP_AUTH_NAMESPACE + "' from file..", e);
            }

        }
    }

    public IdPClient getIdPClient() throws IdPClientException {
        if (initialized) {
            return idPClient;
        } else {
            throw new IdPClientException("The IdP client is not initalized.");
        }
    }

    private IdPClientInitializer getIdPClientInitializer(String initializerType) throws IdPClientException {
        IdPClientInitializer initializer = initializers.get(initializerType);
        if (initializer == null) {
            throw new IdPClientException("No initializer found for type: " + initializerType);
        }
        return initializer;
    }
}
