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
package org.wso2.carbon.analytics.auth.rest.api.util;

import org.wso2.carbon.streaming.integrator.common.utils.SPConstants;
import org.wso2.msf4j.Request;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;

/**
 * AuthUtils.
 */
public class AuthUtil {

    private static final String COOKIE_PATH_SEPERATOR = "; path=";
    private static final String COOKIE_VALUE_SEPERATOR = "; ";

    public static String extractTokenFromHeaders(HttpHeaders headers, String cookieHeader) {
        String authHeader = headers.getHeaderString(SPConstants.AUTHORIZATION_HEADER);
        String token = "";
        if (authHeader != null) {
            authHeader = authHeader.trim();
            String[] authHeaderParts = authHeader.split(" ");
            if (authHeaderParts.length == 2) {
                token = authHeaderParts[1];
            } else if (authHeaderParts.length < 2) {
                return null;
            }
        } else {
            return null;
        }
        String httpONLYCookieValue = getHttpONLYCookieValue(headers, cookieHeader);
        if (httpONLYCookieValue == null) {
            return null;
        } else {
            token += httpONLYCookieValue;
        }
        return token;
    }


    public static String extractIdTokenFromHeaders(HttpHeaders headers, String cookieHeader) {
        String token;
        String authHeader = headers.getHeaderString(AuthRESTAPIConstants.ID_TOKEN_HEADER);
        if (authHeader == null) {
            return null;
        }
        String httpONLYCookieValue = getHttpONLYCookieValue(headers, cookieHeader);
        if (httpONLYCookieValue == null) {
            return null;
        } else {
            token = authHeader + httpONLYCookieValue;
        }
        return token;
    }

    private static String getHttpONLYCookieValue(HttpHeaders headers, String cookieHeader) {
        String cookie = headers.getHeaderString(SPConstants.COOKIE_HEADER);
        if (cookie != null) {
            cookie = cookie.trim();
            String[] cookies = cookie.split(";");
            String tokenFromCookie = Arrays.stream(cookies).filter(name -> name.contains(cookieHeader)).findFirst()
                    .orElse("");
            String[] tokenParts = tokenFromCookie.split("=");
            if (tokenParts.length == 2) {
                return tokenParts[1];
            }
        }
        return null;
    }

    public static NewCookie cookieBuilder(String name, String value, String path, boolean isSecure,
                                          boolean isHttpOnly, int expiresIn) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(value).append(COOKIE_PATH_SEPERATOR).append(path).append(COOKIE_VALUE_SEPERATOR);
        if (isHttpOnly) {
            stringBuilder.append(AuthRESTAPIConstants.HTTP_ONLY_COOKIE).append(COOKIE_VALUE_SEPERATOR);
        }
        if (isSecure) {
            stringBuilder.append(AuthRESTAPIConstants.SECURE_COOKIE);
        }
        if (expiresIn > 0) {
            stringBuilder.append(COOKIE_VALUE_SEPERATOR).append(AuthRESTAPIConstants.EXPIRES_COOKIE)
                    .append(ZonedDateTime.now().plusSeconds(expiresIn).format(DateTimeFormatter.RFC_1123_DATE_TIME))
                    .append(COOKIE_VALUE_SEPERATOR);
        } else if (expiresIn == 0) {
            stringBuilder.append(COOKIE_VALUE_SEPERATOR).append(AuthRESTAPIConstants.EXPIRES_COOKIE)
                    .append(AuthRESTAPIConstants.DEFAULT_EXPIRES_COOKIE).append(COOKIE_VALUE_SEPERATOR);
        }
        return new NewCookie(name, stringBuilder.toString());
    }

    /**
     * Set tenant domain via X-WSO2-Tenant header. This method is specifically implemented to
     * support custom URLs for dashboard portal in api-cloud.
     * @param request
     * @return value of X-WSO2-Tenant header.
     */
    public static String getDomainFromHeader(Request request) {

        String tenantDomain = request.getHeader(AuthRESTAPIConstants.DOMAIN_HEADER);
        if (tenantDomain == null) {
            tenantDomain = "carbon.super";
        }
        return tenantDomain;
    }
}
