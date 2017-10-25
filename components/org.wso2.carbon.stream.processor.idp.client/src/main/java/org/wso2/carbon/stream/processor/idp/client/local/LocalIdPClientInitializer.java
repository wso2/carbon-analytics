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
package org.wso2.carbon.stream.processor.idp.client.local;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.idp.client.core.api.IdPClient;
import org.wso2.carbon.stream.processor.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.stream.processor.idp.client.core.spi.IdPClientInitializer;

import java.util.Map;

@Component(
        name = "org.wso2.carbon.stream.processor.idp.client.local.LocalUSIdPClientInitializer",
        immediate = true
)
public class LocalIdPClientInitializer implements IdPClientInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(LocalIdPClientInitializer.class);

    @Activate
    protected void activate(BundleContext bundleContext) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Local User Store Initializer registered...");
        }
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
    }

    @Override
    public String getType() {
        return LocalIdPClientConstants.LOCAL_US_IDP_CLIENT_TYPE;
    }

    @Override
    public IdPClient initializeIdPClient(Map<String, String> properties) throws IdPClientException {
        return new LocalIdPClient();
    }

}

