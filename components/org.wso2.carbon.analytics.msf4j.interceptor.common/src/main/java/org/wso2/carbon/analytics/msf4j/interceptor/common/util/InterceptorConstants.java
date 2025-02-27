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
package org.wso2.carbon.analytics.msf4j.interceptor.common.util;

/**
 * Interceptor Constants.
 */
public class InterceptorConstants {
    public static final String PROPERTY_USERNAME = "username";
    public static final String ACCESS_TOKEN = "Access_Token";

    public static final String BEARER_PREFIX = "Bearer";
    public static final String BASIC_PREFIX = "Basic";

    public static final String HTTP_STRICT_TRANSPORT_SECURITY_HEADER = "Strict-Transport-Security";

    public static final String MANAGEMENT_LOGIN_URI = "/management/login";

    private InterceptorConstants() {

    }
}
