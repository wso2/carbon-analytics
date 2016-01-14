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
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.databridge.core.DataBridgeServiceValueHolder;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * CarbonAuthenticationHandler implementation that authenticate Agents
 * via Carbon AuthenticationService
 */
public class CarbonAuthenticationHandler implements AuthenticationHandler {
    private AuthenticationService authenticationService;
    private static final Log log = LogFactory.getLog(CarbonAuthenticationHandler.class);


    public CarbonAuthenticationHandler(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public boolean authenticate(String userName, String password) {
        PrivilegedCarbonContext.destroyCurrentContext();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        if (privilegedCarbonContext.getTenantDomain() == null) {
            privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        }
        boolean authenticated = authenticationService.authenticate(userName, password);
        if (authenticated) {

            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            String tenantLessUserName = MultitenantUtils.getTenantAwareUsername(userName);
            try {
                int tenantId = DataBridgeServiceValueHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
                authenticated = DataBridgeServiceValueHolder.getRealmService().
                        getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(tenantLessUserName, "/permission/admin/publish/wso2event", "ui.execute");
            } catch (UserStoreException e) {
                log.error("Error while checking the authorization of the user"+userName+ ". "+e.getMessage(), e);
            }
            // Load tenant : This is needed because we have removed ActivationHandler,
            // which did the tenant loading part earlier with login. So we load tenant after successful login
            try {
                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    TenantAxisUtils.getTenantConfigurationContext(tenantDomain,
                            DataBridgeServiceValueHolder.
                                    getConfigurationContextService().
                                    getServerConfigContext()
                    );
                }
            } catch (Exception e) {
                log.error("Error trying load tenant after successful login", e);
            }
        }
        return authenticated;
    }

    @Override
    public String getTenantDomain(String userName) {
        return MultitenantUtils.getTenantDomain(userName);
    }

    @Override
    public int getTenantId(String tenantDomain) throws UserStoreException {
        return DataBridgeServiceValueHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
    }

    @Override
    public void initContext(AgentSession agentSession) {
        int tenantId = agentSession.getCredentials().getTenantId();
        PrivilegedCarbonContext currentContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        if (currentContext.getTenantId(true) != tenantId) {
            PrivilegedCarbonContext.destroyCurrentContext();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(tenantId);
            privilegedCarbonContext.setTenantDomain(agentSession.getDomainName());
        }
    }

    @Override
    public void destroyContext(AgentSession agentSession) {
        PrivilegedCarbonContext.endTenantFlow();
    }

}
