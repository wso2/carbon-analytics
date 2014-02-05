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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;

public class GadgetServerUserManagementContext {
    private static Log log = LogFactory.getLog(GadgetServerUserManagementContext.class);

    private static RegistryService registryService = null;

    private static UserRealm userRealm = null;
    private static ConfigurationContextService configCtx;

    public static UserRealm getUserRealm() throws GadgetServerUserManagementException {
        if (userRealm == null) {
            throw new GadgetServerUserManagementException("UserRealm is null");
        } else {
            return userRealm;
        }
    }

    public static void setUserRealm(UserRealm userRealm) {
        GadgetServerUserManagementContext.userRealm = userRealm;
    }

    public static void setRegistryService(RegistryService registryService) {
        GadgetServerUserManagementContext.registryService = registryService;
    }

    public static Registry getRegistry(int tenantId) throws GadgetServerUserManagementException {
        if (registryService == null) {
            throw new GadgetServerUserManagementException("Registry is null");
        }
        try {
            return registryService.getConfigSystemRegistry(tenantId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GadgetServerUserManagementException(e);
        }
    }

    public static boolean setPortalAdminData(int tenantId) {
        try {
            if (!GadgetServerUserManagementContext.isPortalPermissionsSet(tenantId)) {
                // Setting self registration on by default
                GadgetServerUserManagementContext.setSelfRegistration(false, tenantId);

                // Seting external Gadget addition to true by default
                GadgetServerUserManagementContext.setExternalGadgetAddition(true, tenantId);

                GadgetServerUserManagementContext.setAnonModeState(false, tenantId);
            }
            return true;
        } catch (Exception e) {
            log.error(e);
            return false;
        }
    }

    /**
     * Duplicating the admin data populator service to fie on tenant login
     */
    public static Boolean isPortalPermissionsSet(int tenantId) {
        try {
            Registry registry = GadgetServerUserManagementContext.getRegistry(tenantId);
            return registry.resourceExists(
                    DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public static Boolean setSelfRegistration(boolean flag, int tenantId) {
        Boolean response = false;

        try {
            Registry registry = GadgetServerUserManagementContext.getRegistry(tenantId);

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

    public static Boolean setExternalGadgetAddition(boolean flag, int tenantId) {
        Boolean response = false;

        try {
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

    public static Boolean setAnonModeState(boolean flag, int tenantId) {
        Boolean response = false;

        try {
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

    public static Boolean isAnonModeActive(int tenantId) {
        Boolean response = false;

        try {
            Registry registry = GadgetServerUserManagementContext.getRegistry(tenantId);

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

    public static void setConfigContextService(ConfigurationContextService configCtx) {
        GadgetServerUserManagementContext.configCtx = configCtx;
    }

    public static ConfigurationContextService getConfigCtx() {
        return configCtx;
    }

    public static ConfigurationContext getConfigContext() throws GadgetServerUserManagementException {
        if (configCtx == null) {
            throw new GadgetServerUserManagementException("ConfigurationContextService is null");
        }
        try {
            return configCtx.getServerConfigContext();
        } catch (Exception e) {
            log.error(e);
            throw new GadgetServerUserManagementException(e);
        }
    }
}
