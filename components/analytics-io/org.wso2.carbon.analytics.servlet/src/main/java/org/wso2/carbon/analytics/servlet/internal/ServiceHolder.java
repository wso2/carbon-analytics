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
package org.wso2.carbon.analytics.servlet.internal;

import com.hazelcast.core.HazelcastInstance;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.SecureAnalyticsDataService;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Class holder to register the osgi services registered within the component.
 *
 */
public class ServiceHolder {
    private static AuthenticationService authenticationService;
    private static AnalyticsDataService analyticsDataService;
    private static SecureAnalyticsDataService secureAnalyticsDataService;
    private static AnalyticsAPIAuthenticator authenticator;
    private static HttpService httpService;
    private static HazelcastInstance hazelcastInstance;
    private static RealmService realmService;

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

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        ServiceHolder.realmService = realmService;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public static void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        ServiceHolder.hazelcastInstance = hazelcastInstance;
    }

    public static void setHttpService(HttpService httpService) {
        ServiceHolder.httpService = httpService;
    }

    public static SecureAnalyticsDataService getSecureAnalyticsDataService() {
        return secureAnalyticsDataService;
    }

    public static void setSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.secureAnalyticsDataService = secureAnalyticsDataService;
    }
}