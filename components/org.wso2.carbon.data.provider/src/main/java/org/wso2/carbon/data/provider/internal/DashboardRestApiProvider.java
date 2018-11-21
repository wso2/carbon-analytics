/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.data.provider.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.uiserver.api.App;
import org.wso2.carbon.uiserver.spi.RestApiProvider;
import org.wso2.msf4j.Microservice;

import java.util.Collections;
import java.util.Map;

import static org.wso2.carbon.data.provider.utils.DataProviderValueHolder.getDataProviderHelper;

/**
 * Provider that supplies Microservices for the {@link #DASHBOARD_PORTAL_APP_NAME} web app.
 */
@Component(service = RestApiProvider.class, immediate = true)
public class DashboardRestApiProvider implements RestApiProvider {

    public static final String DASHBOARD_PORTAL_APP_NAME = "portal";
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardRestApiProvider.class);

    @Activate
    protected void activate(BundleContext bundleContext) {
        LOGGER.debug("{} activated.", this.getClass().getName());
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        LOGGER.debug("{} deactivated.", this.getClass().getName());
    }

    @Reference(
            service = DataProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataProvider"
    )
    protected void setDataProvider(DataProvider dataProvider) {
        getDataProviderHelper().setDataProvider(dataProvider.providerName(), dataProvider);
    }

    protected void unsetDataProvider(DataProvider dataProvider) {
        getDataProviderHelper().removeDataProviderClass(dataProvider.providerName());
    }

    @Override
    public String getAppName() {
        return DASHBOARD_PORTAL_APP_NAME;
    }

    @Override
    public Map<String, Microservice> getMicroservices(App app) {
        return Collections.singletonMap(DataProviderAPI.API_CONTEXT_PATH, new DataProviderAPI());
    }
}
