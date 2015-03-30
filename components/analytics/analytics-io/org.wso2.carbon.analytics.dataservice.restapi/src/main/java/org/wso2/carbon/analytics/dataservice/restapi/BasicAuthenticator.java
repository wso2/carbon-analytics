/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.dataservice.restapi;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.Charset;

/**
 * This class authenticate the request coming to the analytics webapp
 */
public class BasicAuthenticator {

    private Log log = LogFactory.getLog(BasicAuthenticator.class);

    public static String authenticate(String authHeader) throws AnalyticsException {

        if (authHeader != null && authHeader.startsWith(Constants.BASIC_AUTH_HEADER)) {
            // Authorization: Basic base64credentials
            String base64Credentials = authHeader.substring(Constants.BASIC_AUTH_HEADER.length()).trim();
            String credentials = new String(Base64.decode(base64Credentials),
                                            Charset.forName("UTF-8"));
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            String username = values[0];
            String password = values[1];
            if ("".equals(username) || username == null || "".equals(password) || password == null) {
                throw new AnalyticsException("Username and password cannot be empty");
            }
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            String tenantLessUserName = MultitenantUtils.getTenantAwareUsername(username);
            try {
                // get super tenant context and get realm service which is an osgi service
                RealmService realmService = (RealmService) PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
                if (realmService != null) {
                    int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                        throw new AnalyticsException("Authentication failed - Invalid domain");

                    }
                    // get tenant's user realm
                    UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                    boolean isAuthenticated = userRealm.getUserStoreManager()
                            .authenticate(tenantLessUserName, password);
                    if (!isAuthenticated) {
                        throw  new AnalyticsException("User is not authenticated!");
                    } else {
                        return username;
                    }
                }
            } catch (UserStoreException e) {
                throw new AnalyticsException("Error while accessing the user realm of user :" + username, e);
            }
        }
        return null;
    }
}
