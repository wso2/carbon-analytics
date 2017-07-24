/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.dataservice.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This class provides utils methods that need to validate user authorization
 */
public class AuthorizationUtils {

    private static final Log logger = LogFactory.getLog(AuthorizationUtils.class);

    public static boolean isUserAuthorized(int tenantId, String username, String permission) throws AnalyticsException {

        if (logger.isDebugEnabled()) {
            logger.debug("User[" + username + "] calling method (" + Thread.currentThread().getStackTrace()[2]
                    .getMethodName() + ") with permission[" + permission + "]");
        }

        try {
            UserRealm userRealm = AnalyticsServiceHolder.getRealmService().getTenantUserRealm(tenantId);
            return userRealm.getAuthorizationManager().isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username), permission,
                                                                        CarbonConstants.UI_PERMISSION_ACTION);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get user permission information for user[" + username + "] due to " +
                                         e.getMessage(), e);
        }
    }
}
