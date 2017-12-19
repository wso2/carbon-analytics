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
package org.wso2.carbon.business.rules.core.api;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uiserver.api.App;
import org.wso2.carbon.uiserver.spi.RestApiProvider;
import org.wso2.msf4j.Microservice;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider that supplies Microservices for the {@link #BUSINESS_RULES_APP_NAME} web app.
 *
 * @since 4.0.0
 */
@Component(service = RestApiProvider.class,
        immediate = true)
public class BusinessRulesApiProvider implements RestApiProvider {
    public static final String BUSINESS_RULES_APP_NAME = "business-rules";
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessRulesApiProvider.class);

    private BusinessRulesApiService businessRulesProvider;

    @Activate
    protected void activate(BundleContext bundleContext) {
        LOGGER.debug("{} activated.", this.getClass().getName());
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        LOGGER.debug("{} deactivated.", this.getClass().getName());
    }

    protected void unsetBusinessRulesProvider(BusinessRulesApiService businessRulesMetadataProvider) {
        this.businessRulesProvider = null;
        LOGGER.debug("BusinessRulesProvider '{}' unregistered.", businessRulesMetadataProvider.getClass().getName());
    }

    @Override
    public String getAppName() {
        return BUSINESS_RULES_APP_NAME;
    }

    @Override
    public Map<String, Microservice> getMicroservices(App app) {
        Map<String, Microservice> microservices = new HashMap<>(1);
        microservices.put(BusinessRulesApi.API_CONTEXT_PATH, new BusinessRulesApi());
        return microservices;
    }
}
