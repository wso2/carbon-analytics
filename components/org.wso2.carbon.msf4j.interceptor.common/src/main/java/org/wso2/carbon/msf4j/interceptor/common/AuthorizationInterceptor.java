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
package org.wso2.carbon.msf4j.interceptor.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.security.caas.api.ProxyCallbackHandler;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.RequestInterceptor;

import java.util.Base64;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.ws.rs.core.MediaType;

/**
 * Interceptor to check basic authorization.
 */
public class AuthorizationInterceptor implements RequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationInterceptor.class);
    private static final String HEADER_AUTHORIZATION = "Authorization";

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authorizationHeader != null) {
            byte[] decodedAuthHeader = Base64.getDecoder().decode(authorizationHeader.split(" ")[1]);
            String authHeader = new String(decodedAuthHeader);
            String userName = authHeader.split(":")[0];
            String password = authHeader.split(":")[1];

            CarbonMessage carbonMessage = new DefaultCarbonMessage();
            carbonMessage.setHeader(HEADER_AUTHORIZATION, "Basic " + Base64.getEncoder()
                    .encodeToString((userName + ":" + password).getBytes())
            );
            ProxyCallbackHandler callbackHandler = new ProxyCallbackHandler(carbonMessage);
            LoginContext loginContext;
            loginContext = new LoginContext("CarbonSecurityConfig", callbackHandler);
            loginContext.login();
            return true;
        }
        LOG.error("Authorization header not found for request : '" + request.getUri() + "'");
        response.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode())
                .setEntity("Authorization is required to access uri '" + request.getUri() + "'. Please set " +
                        "the authentication header and try again.")
                .setMediaType(MediaType.TEXT_PLAIN);
        return false;
    }

    @Override
    public boolean onRequestInterceptionError(Request request, Response response, Exception e) {

        if (e instanceof LoginException) {
            LOG.error("Authorization invalid for request : '" + request.getUri() + "'", e);
            response.setEntity("Login credential is not valid in accessing the uri '" + request.getUri() + "'. " +
                    "Please check the credentials and try again.")
                    .setMediaType(MediaType.TEXT_PLAIN)
                    .setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
            return false;
        }

        String message = "Exception while executing request interceptor '" + this.getClass() + "' for uri : '" +
                request.getUri() + "'";
        LOG.error(message, e);
        response.setEntity(message)
                .setMediaType(MediaType.TEXT_PLAIN)
                .setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        return false;
    }
}

