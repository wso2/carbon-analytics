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

/**
 * Auth rest API constants.
 */
public class AuthRESTAPIConstants {

    public static final String WSO2_SP_TOKEN = "WSO2_SP_TOKEN";
    public static final String WSO2_SP_REFRESH_TOKEN = "WSO2_SP_REFRESH_TOKEN";

    public static final String HTTP_ONLY_COOKIE = "HttpOnly";
    public static final String SECURE_COOKIE = "Secure";
    public static final String EXPIRES_COOKIE = "Expires=";

    public static final String LOGIN_CONTEXT = "/login";
    public static final String LOGOUT_CONTEXT = "/logout";

    private AuthRESTAPIConstants() {
    }
}
