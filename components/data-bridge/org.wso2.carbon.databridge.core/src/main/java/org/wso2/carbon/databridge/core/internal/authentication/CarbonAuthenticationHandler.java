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
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.security.caas.api.ProxyCallbackHandler;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Base64;


/**
 * CarbonAuthenticationHandler implementation that authenticate Agents
 * via Carbon AuthenticationService
 */

public class CarbonAuthenticationHandler implements AuthenticationHandler {

    private static final Log log = LogFactory.getLog(CarbonAuthenticationHandler.class);

    public boolean authenticate(String userName, String password) {
        boolean isAuthenticated = false;
        try {
            PrivilegedCarbonContext.destroyCurrentContext();

            CarbonMessage carbonMessage = new DefaultCarbonMessage();
            carbonMessage.setHeader("Authorization", "Basic " + Base64.getEncoder()
                    .encodeToString((userName + ":" + password).getBytes())
            );

            ProxyCallbackHandler callbackHandler = new ProxyCallbackHandler(carbonMessage);
            LoginContext loginContext = new LoginContext("CarbonSecurityConfig", callbackHandler);
            loginContext.login();
            isAuthenticated = true;
        } catch (LoginException e) {
            log.error("Error trying load tenant after successful login", e);
        }

        return isAuthenticated;
    }

    @Override
    public void initContext(AgentSession agentSession) {

        //TODO not required
//        int tenantId = agentSession.getCredentials().getTenantId();
//        PrivilegedCarbonContext currentContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//        if (currentContext.getTenantId(true) != tenantId) {
//            PrivilegedCarbonContext.destroyCurrentContext();
//            PrivilegedCarbonContext.startTenantFlow();
//            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//            privilegedCarbonContext.setTenantId(tenantId);
//            privilegedCarbonContext.setTenantDomain(agentSession.getDomainName());
//        }
    }

    @Override
    public void destroyContext(AgentSession agentSession) {

        //TODO not required
        PrivilegedCarbonContext.destroyCurrentContext();
    }

}

