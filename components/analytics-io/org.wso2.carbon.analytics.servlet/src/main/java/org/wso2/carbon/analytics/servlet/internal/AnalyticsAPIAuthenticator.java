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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

/**
 * Authenticator class which is used for authenticating analytics API and generates
 * relevant session Id for the request which is stored in a distributed hazlecast map.
 */

public class AnalyticsAPIAuthenticator {
    private static Log log = LogFactory.getLog(AnalyticsAPIAuthenticator.class);
    private static final String SESSION_CACHE_NAME = "ANALYTICS_API_SERVICE_SESSION_CACHE";
    private Map<String, Boolean> sessionIds;

    public AnalyticsAPIAuthenticator() {
        if (ServiceHolder.getHazelcastInstance() != null) {
            sessionIds = ServiceHolder.getHazelcastInstance().getMap(SESSION_CACHE_NAME);
        } else {
            sessionIds = new HashMap<>();
        }
    }

    public String authenticate(String username, String password) throws AnalyticsAPIAuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            logAndThrowAuthException("Username is not provided!");
        }
        if (password == null || password.trim().isEmpty()) {
            logAndThrowAuthException("Password is not provided!");
        }
        String userName = MultitenantUtils.getTenantAwareUsername(username);
        if (MultitenantUtils.getTenantDomain(username).equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            boolean authenticated = ServiceHolder.getAuthenticationService().authenticate(userName, password);
            if (authenticated) {
                try {
                    boolean authorized = ServiceHolder.getRealmService().
                            getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).
                            getAuthorizationManager().isUserAuthorized(username,
                            AnalyticsAPIConstants.ANALYTICS_REMOTE_API_INVOCATION_PERMISSION,
                            CarbonConstants.UI_PERMISSION_ACTION);
                    if (authorized) {
                        String sessionId = UUID.randomUUID().toString();
                        sessionIds.put(sessionId, Boolean.TRUE);
                        return sessionId;
                    } else {
                        logAndThrowAuthException("User :" + userName + " don't have necessary permissions " +
                                "to connect to remote analytics API.");
                    }
                } catch (UserStoreException e) {
                    logAndThrowAuthException("User :" + userName + " don't have necessary permissions " +
                            "to connect to remote analytics API.");
                }
            } else {
                logAndThrowAuthException("Login failed for user :" + userName);
            }
        } else {
            logAndThrowAuthException("Only super tenant users is authenticated to use the service!");
        }
        return null;
    }

    private void logAndThrowAuthException(String message) throws AnalyticsAPIAuthenticationException {
        log.error(message);
        throw new AnalyticsAPIAuthenticationException(message);
    }

    public void validateSessionId(String sessionId) throws AnalyticsAPIAuthenticationException {
        if (sessionIds.get(sessionId) == null || !sessionIds.get(sessionId)) {
            logAndThrowAuthException("Unauthenticated session Id : " + sessionId);
        }
    }
}
