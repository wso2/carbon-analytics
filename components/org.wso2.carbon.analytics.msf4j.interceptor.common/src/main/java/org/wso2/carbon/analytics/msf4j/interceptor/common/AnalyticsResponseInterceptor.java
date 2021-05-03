/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.msf4j.interceptor.common;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wso2.carbon.analytics.msf4j.interceptor.common.internal.DataHolder;
import org.wso2.carbon.analytics.msf4j.interceptor.common.util.InterceptorConstants;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.ResponseInterceptor;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.ConfigurationBuilder;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor to check authentication into si.
 */
@Component(
        service = AnalyticsResponseInterceptor.class,
        immediate = true
)
public class AnalyticsResponseInterceptor implements ResponseInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsResponseInterceptor.class);
    private Map<String, String> headerMap = null;
    private static final String TRANSPORT_ROOT_CONFIG_ELEMENT = "wso2.transport.http";

    @Override
    public boolean interceptResponse(Request request, Response response) throws Exception {
        if (headerMap == null) {
            headerMap = new HashMap<>();
            TransportsConfiguration transportsConfiguration =
                    DataHolder.getInstance().getConfigProvider().getConfigurationObject(TRANSPORT_ROOT_CONFIG_ELEMENT,
                    TransportsConfiguration.class);
            transportsConfiguration.getListenerConfigurations().forEach(listenerConfiguration -> {
                if (listenerConfiguration.getResponseHeaderList() != null) {
                    String[] responseHeaders =  listenerConfiguration.getResponseHeaderList().split(",");
                    if (responseHeaders.length > 0) {
                        for (String header : responseHeaders) {
                            String[] headerKeyValue = header.split(":");
                            headerMap.put(headerKeyValue[0].trim(), headerKeyValue[1].trim());
                        }
                    }
                }
                if (listenerConfiguration.getStrictTransportSecurityHeader() != null) {
                    headerMap.put(InterceptorConstants.HTTP_STRICT_TRANSPORT_SECURITY_HEADER,
                            listenerConfiguration.getStrictTransportSecurityHeader().trim());
                    if (!Constants.HTTPS_SCHEME.equalsIgnoreCase(listenerConfiguration.getScheme())) {
                        LOGGER.warn("HTTP Strict Transport Security header must be used with https scheme");
                    }
                }
            });
        }
        if (headerMap != null) {
            response.setHeaders(headerMap);
        }
        return true;
    }
}

