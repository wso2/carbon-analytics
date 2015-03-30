/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.dataservice.servlet.internal;

import org.osgi.service.http.HttpService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.identity.authentication.AuthenticationService;

public class ServiceHolder {
    private static AuthenticationService authenticationService;
    private static AnalyticsDataService analyticsDataService;
    private static AnalyticsAPIAuthenticator authenticator;
    private static HttpService httpService;

    public static AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public static void setAuthenticationService(AuthenticationService authenticationService) {
        ServiceHolder.authenticationService = authenticationService;
    }

    public static AnalyticsDataService getAnalyticsDataService() {
        return analyticsDataService;
    }

    public static void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.analyticsDataService = analyticsDataService;
    }

    public static AnalyticsAPIAuthenticator getAuthenticator() {
        return authenticator;
    }

    public static void setAuthenticator(AnalyticsAPIAuthenticator authenticator) {
        ServiceHolder.authenticator = authenticator;
    }

    public static HttpService getHttpService() {
        return httpService;
    }

    public static void setHttpService(HttpService httpService) {
        ServiceHolder.httpService = httpService;
    }
}