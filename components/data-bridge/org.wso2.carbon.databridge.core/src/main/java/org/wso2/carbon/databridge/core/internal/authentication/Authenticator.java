/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.databridge.core.internal.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.internal.authentication.session.SessionBean;
import org.wso2.carbon.databridge.core.internal.authentication.session.SessionCache;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.UUID;

/**
 * Authenticates all the incoming connections and manage sessions
 */
public final class Authenticator {

    private static final Log log = LogFactory.getLog(Authenticator.class);

    private SessionCache sessionCache;
    private AuthenticationHandler authenticationHandler;

    public Authenticator(AuthenticationHandler authenticationHandler,
                         DataBridgeConfiguration dataBridgeConfiguration) {
        this.authenticationHandler = authenticationHandler;
        sessionCache = new SessionCache(dataBridgeConfiguration.getClientTimeOut() / 1000);
    }

    public String authenticate(String userName, String password) throws AuthenticationException {

        if (userName == null) {
            logAndAuthenticationException("Authentication request was missing the user name ");
        }
//
//        if (userName.indexOf("@") > 0) {
//            String domainName = userName.substring(userName.indexOf("@") + 1);
//            if (domainName == null || domainName.trim().equals("")) {
//                logAndAuthenticationException("Authentication request was missing the domain name of" +
//                                              " the user");
//            }
//        }
//
        if (password == null) {
            logAndAuthenticationException("Authentication request was missing the required password");
        }

        boolean isSuccessful = authenticationHandler.authenticate(userName, password);

        if (isSuccessful) {
            String sessionId = UUID.randomUUID().toString();
            int tenantId = 0;
            try {
                String tenantDomain = authenticationHandler.getTenantDomain(userName);
                tenantId = authenticationHandler.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                logAndAuthenticationException("Could not resolve the user to a valid tenant.");
            }
            Credentials credentials = new Credentials(userName, password, authenticationHandler.getTenantDomain(userName), tenantId);
            sessionCache.getSession(new SessionBean(sessionId, credentials));
            return sessionId;
        }
        logAndAuthenticationException("wrong userName or password");

        return null;

    }

    private void logAndAuthenticationException(String msg) throws AuthenticationException {
        log.error(msg);
        throw new AuthenticationException(msg);
    }

    public void logout(String sessionId) {
        sessionCache.removeSession(sessionId);
    }

    public AgentSession getSession(String sessionId) {
        return sessionCache.getSession(new SessionBean(sessionId));
    }
}
