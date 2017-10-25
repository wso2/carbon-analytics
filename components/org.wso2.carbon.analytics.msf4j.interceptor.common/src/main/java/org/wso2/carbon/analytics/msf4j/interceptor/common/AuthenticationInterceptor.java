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
package org.wso2.carbon.analytics.msf4j.interceptor.common;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.msf4j.interceptor.common.internal.DataHolder;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.stream.processor.idp.client.core.api.IdPClient;
import org.wso2.carbon.stream.processor.idp.client.core.exception.AuthenticationException;
import org.wso2.carbon.stream.processor.idp.client.core.utils.IdPClientConstants;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.RequestInterceptor;

import java.util.Arrays;
import javax.ws.rs.core.MediaType;

import static org.wso2.carbon.stream.processor.idp.client.core.utils.IdPClientConstants.HEADER_AUTHORIZATION;

/**
 * Interceptor to check authentication into sp
 */
@Component(
        name = "Authentication-Interceptor",
        service = RequestInterceptor.class
)
public class AuthenticationInterceptor implements RequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {

        IdPClient idPClient = DataHolder.getInstance().getIdPClient();

        Headers headers = request.getHeaders();
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authorizationHeader != null) {
            String partialTokenFromHeader = authorizationHeader.split(" ")[1];
            if (partialTokenFromHeader != null) {
                String cookieHeader = headers.get(IdPClientConstants.HEADER_COOKIE);
                String partialTokenFromCookie = null;
                if (cookieHeader != null) {
                    cookieHeader = cookieHeader.trim();
                    String[] cookies = cookieHeader.split(";");
                    String token2 = Arrays.stream(cookies)
                            .filter(name -> name.contains(IdPClientConstants.WSO2_SP_TOKEN_2))
                            .findFirst().orElse("");
                    String tokensArr[] = token2.split("=");
                    if (tokensArr.length == 2) {
                        partialTokenFromCookie = tokensArr[1];
                    }
                }
                String accessToken = (partialTokenFromCookie != null) ?
                                    authorizationHeader + partialTokenFromCookie :
                                    authorizationHeader;
                return idPClient.authenticate(accessToken);
            } else {
                LOG.error("Malformed Authorization header found for request : '" + request.getUri() + "'.");
                response.setEntity("Malformed authorization header when accessing uri '" +
                        request.getUri() + "'. Please reset the authentication header and try again.")
                        .setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
                return false;
            }
        } else {
            LOG.error("Authorization header not found for request : '" + request.getUri() + "'");
            response.setEntity("Authorization is required to access uri '" + request.getUri() + "'. " +
                    "Please set the authentication header and try again.")
                    .setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
            return false;
        }
    }

    @Override
    public boolean onRequestInterceptionError(Request request, Response response, Exception e) {
        if (e instanceof AuthenticationException) {
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

