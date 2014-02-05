/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.dashboard.mgt.users;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class GadgetServerUserManagementService extends AbstractAdmin {

    private static final Log log =
            LogFactory.getLog(GadgetServerUserManagementService.class);

    public Boolean setUserSelfRegistration(boolean flag) {
        return setSelfRegistration(flag);
    }

    public Boolean setUserExternalGadgetAddition(boolean flag) {
        return setExternalGadgetAddition(flag);
    }

    private int getTenantId() {
        return PrivilegedCarbonContext.getCurrentContext().getTenantId();
    }

    /**
     * Sets User Self Registration flag in registry. If false, users can not self register. An admin should add users manually.
     *
     * @param flag Indicates true or false
     * @return Flag indicating whether the operation was successful or not.
     */
    public Boolean setSelfRegistration(boolean flag) {
        Boolean response = false;

        try {
            Registry registry = GadgetServerUserManagementContext.getRegistry(getTenantId());

            Resource regAdminDataResource;
            if (registry.resourceExists(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH)) {
                regAdminDataResource = registry.get(DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);
            } else {
                regAdminDataResource = registry.newResource();
            }

            regAdminDataResource
                    .setProperty(DashboardConstants.USER_SELF_REG_PROPERTY_ID,
                            String.valueOf(flag));

            registry.put(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH,
                    regAdminDataResource);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    public Boolean isSelfRegistration(String tDomain) {
        Boolean response = false;

        try {
            Registry registry = GadgetServerUserManagementContext.getRegistry(getTenantId());

            Resource regAdminDataResource;
            if (registry.resourceExists(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH)) {
                regAdminDataResource = registry.get(
                        DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);

                String storedValue = regAdminDataResource
                        .getProperty(DashboardConstants.USER_SELF_REG_PROPERTY_ID);

                if ((storedValue != null) && ("true".equals(storedValue))) {
                    return true;
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }


    public Boolean isExternalGadgetAddition() {
        Boolean response = false;

        try {
            int tenantId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
            Registry registry = GadgetServerUserManagementContext.getRegistry(tenantId);

            Resource regAdminDataResource;
            if (registry.resourceExists(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH)) {
                regAdminDataResource = registry.get(
                        DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);

                String storedValue = regAdminDataResource
                        .getProperty(
                                DashboardConstants.USER_EXTERNAL_GADGET_ADD_PROPERTY_ID);

                if ((storedValue != null) && ("true".equals(storedValue))) {
                    return true;
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * Sets External Gadget Addition flag in Registry. If false, users cannot type
     *
     * @param flag Indicates true or false
     * @return Flag indicating whether the operation was successful or not.
     */
    public Boolean setExternalGadgetAddition(boolean flag) {
        Boolean response = false;

        try {
            int tenantId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
            Registry registry = GadgetServerUserManagementContext.getRegistry(tenantId);

            Resource regAdminDataResource;
            if (registry.resourceExists(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH)) {
                regAdminDataResource = registry.get(
                        DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);
            } else {
                regAdminDataResource = registry.newResource();
            }

            regAdminDataResource
                    .setProperty(
                            DashboardConstants.USER_EXTERNAL_GADGET_ADD_PROPERTY_ID,
                            String.valueOf(flag));

            registry.put(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH,
                    regAdminDataResource);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }


    public Boolean isPortalPermissionsSet() {
        try {
            Registry registry = GadgetServerUserManagementContext.getRegistry(getTenantId());
            return registry.resourceExists(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public Boolean isAnonModeActive(String tDomain) {
        Boolean response = false;

        try {
            Registry registry = GadgetServerUserManagementContext.getRegistry(getTenantId());

            Resource regAdminDataResource;
            if (registry.resourceExists(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH)) {
                regAdminDataResource = registry.get(
                        DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);

                String storedValue = regAdminDataResource
                        .getProperty(
                                DashboardConstants.ANON_MODE_ACT);

                if ((storedValue != null) && ("true".equals(storedValue))) {
                    return true;
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;

    }

    public Boolean setAnonModeState(boolean flag) {
        Boolean response = false;

        try {
            Registry registry = GadgetServerUserManagementContext.getRegistry(getTenantId());

            Resource regAdminDataResource;
            if (registry.resourceExists(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH)) {
                regAdminDataResource = registry.get(
                        DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);
            } else {
                regAdminDataResource = registry.newResource();
            }

            regAdminDataResource
                    .setProperty(
                            DashboardConstants.ANON_MODE_ACT,
                            String.valueOf(flag));

            registry.put(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH,
                    regAdminDataResource);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

     /**
     * Checking for session expiration
     *
     * @return true | false
     */
    public Boolean isSessionValid() {
        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession(false);
        return (!httpSession.isNew());
    }
}
