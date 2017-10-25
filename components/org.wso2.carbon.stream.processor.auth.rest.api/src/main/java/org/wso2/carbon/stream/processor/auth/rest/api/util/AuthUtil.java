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
package org.wso2.carbon.stream.processor.auth.rest.api.util;

import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.stream.processor.idp.client.core.utils.IdPClientConstants;

import java.util.Arrays;
import javax.ws.rs.core.NewCookie;

public class AuthUtil {

    private static final String COOKIE_PATH_SEPERATOR = "; path=";
    private static final String COOKIE_VALUE_SEPERATOR = "; ";

    public static String extractTokenFromHeaders(Headers headers, String cookieHeader) {
        String authHeader = headers.get(IdPClientConstants.AUTHORIZATION_HEADER);
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
        String cookie = headers.get(IdPClientConstants.COOKIE_HEADER);
        if (cookie != null) {
            cookie = cookie.trim();
            String[] cookies = cookie.split(";");
            String tokenFromCookie = Arrays.stream(cookies).filter(name -> name.contains(cookieHeader)).findFirst()
                    .orElse("");
            String[] tokenParts = tokenFromCookie.split("=");
            if (tokenParts.length == 2) {
                token += tokenParts[1];
            }
        }
        return token;
    }

    public static NewCookie cookieBuilder(String name, String value, String path, boolean isSecure,
                                          boolean isHttpOnly, String expiresIn) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(value).append(COOKIE_PATH_SEPERATOR).append(path).append(COOKIE_VALUE_SEPERATOR);
        if (isHttpOnly) {
            stringBuilder.append(IdPClientConstants.HTTP_ONLY_COOKIE).append(COOKIE_VALUE_SEPERATOR);
        }
        if (isSecure) {
            stringBuilder.append(IdPClientConstants.SECURE_COOKIE);
        }
        if (expiresIn != null && !expiresIn.isEmpty()) {
            stringBuilder.append(COOKIE_VALUE_SEPERATOR).append(expiresIn);
        }
        return new NewCookie(name, stringBuilder.toString());
    }
}
