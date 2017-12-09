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
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.AuthenticationException;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.msf4j.interceptor.common.internal.DataHolder;
import org.wso2.carbon.analytics.msf4j.interceptor.common.util.InterceptorConstants;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.RequestInterceptor;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Interceptor to check authentication into sp.
 */
@Component(
        service = RequestInterceptor.class,
        immediate = true
)
public class AuthenticationInterceptor implements RequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        if (!DataHolder.getInstance().isInterceptorEnabled()) {
            return true;
        } else {
            for (Pattern url : DataHolder.getInstance().getExcludeURLPatternList()) {
                if (url.matcher(request.getUri()).matches()) {
                    return true;
                }
            }
            IdPClient idPClient = DataHolder.getInstance().getIdPClient();
            HttpHeaders headers = request.getHeaders();
            String authorizationHeader = request.getHeader(IdPClientConstants.AUTHORIZATION_HEADER);
            if (authorizationHeader != null && authorizationHeader.contains(" ")) {
                String headerPrefix = authorizationHeader.split(" ")[0];
                String headerPostfix = authorizationHeader.split(" ")[1];
                if (headerPostfix != null) {
                    if (headerPrefix.equalsIgnoreCase(IdPClientConstants.BEARER_PREFIX)) {
                        String cookieHeader = headers.getHeaderString(IdPClientConstants.COOKIE_HEADER);
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
                                headerPostfix + partialTokenFromCookie :
                                headerPostfix;
                        String username = idPClient.authenticate(accessToken);
                        if (username != null) {
                            request.setProperty(InterceptorConstants.PROPERTY_USERNAME, username);
                            return true;
                        }
                        return false;
                    } else if (headerPrefix.equalsIgnoreCase(IdPClientConstants.BASIC_PREFIX)) {
                        byte[] decodedAuthHeader = Base64.getDecoder().decode(headerPostfix);
                        String authHeader = new String(decodedAuthHeader, Charset.forName("UTF-8"));
                        if (authHeader.contains(":")) {
                            String userName = authHeader.split(":")[0];
                            String password = authHeader.split(":")[1];
                            String appName = request.getUri().split("/\\|?")[1];

                            Map<String, String> loginProperties = new HashMap<>();
                            loginProperties.put(IdPClientConstants.APP_NAME, appName);
                            loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
                            loginProperties.put(IdPClientConstants.USERNAME, userName);
                            loginProperties.put(IdPClientConstants.PASSWORD, password);
                            Map<String, String> loginValues = idPClient.login(loginProperties);

                            if (Objects.equals(loginValues.get(IdPClientConstants.LOGIN_STATUS),
                                    IdPClientConstants.LoginStatus.LOGIN_FAILURE)) {
                                LOGGER.debug("Authentication failed for the request to '{}' due to Error: '{}', " +
                                                "Error Description: '{}'.", request.getUri(),
                                        loginValues.get(IdPClientConstants.ERROR),
                                        loginValues.get(IdPClientConstants.ERROR_DESCRIPTION));
                                response.setEntity("Authentication failed for the request to : '" + request.getUri() +
                                        "' due to Error :'" + loginValues.get(IdPClientConstants.ERROR) + "'," +
                                        " Error Description : '" +
                                        loginValues.get(IdPClientConstants.ERROR_DESCRIPTION))
                                        .setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
                                return false;
                            }
                            request.setProperty(InterceptorConstants.PROPERTY_USERNAME, userName);
                            return true;
                        }
                        LOGGER.debug("Malformed Authorization header found for request : '{}'.", request.getUri());
                        response.setEntity("Malformed authorization header when accessing uri '" +
                                request.getUri() + "'. Please reset the authentication header and try again.")
                                .setStatus(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode());
                        return false;
                    } else {
                        LOGGER.debug("Authorization method '{}' not supported for : '{}'.", headerPrefix,
                                request.getUri());
                        response.setEntity("Authorization method '" + headerPrefix + "' not supported for : '"
                                + request.getUri() + "'. Please reset the authentication header and try again.")
                                .setStatus(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode());
                        return false;
                    }
                } else {
                    LOGGER.debug("Malformed Authorization header found for request : '{}'.", request.getUri());
                    response.setEntity("Malformed authorization header when accessing uri '" +
                            request.getUri() + "'. Please reset the authentication header and try again.")
                            .setStatus(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode());
                    return false;
                }
            } else {
                LOGGER.debug("Authorization header not found for request '{}'", request.getUri());
                response.setEntity("Authorization is required to access uri '" + request.getUri() + "'. " +
                        "Please set the authentication header and try again.")
                        .setStatus(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode());
                return false;
            }
        }
    }

    @Override
    public boolean onRequestInterceptionError(Request request, Response response, Exception e) {
        if (e instanceof AuthenticationException) {
            LOGGER.debug("Authorization invalid for request '{}'.", request.getUri(), e);
            response.setEntity(e.getMessage())
                    .setMediaType(MediaType.TEXT_PLAIN)
                    .setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
            return false;
        }
        String message = "Exception while executing request interceptor '" + this.getClass() + "' for uri : '" +
                request.getUri() + "'. Error: '" + e.getMessage() + "'";
        LOGGER.debug(message, e);
        response.setEntity(message)
                .setMediaType(MediaType.TEXT_PLAIN)
                .setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        return false;
    }
}

