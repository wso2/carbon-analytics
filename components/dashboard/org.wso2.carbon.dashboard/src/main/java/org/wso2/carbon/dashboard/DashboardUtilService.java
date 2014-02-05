/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.dashboard;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.dashboard.bean.DashboardUtilBean;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Dashboard Utility Service, which is being used at non-signed in modes
 */
public class DashboardUtilService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(DashboardUtilService.class);

    private Boolean isAnonModeActive(String tDomain) {
        Boolean response = false;

        try {
            Registry registry = DashboardContext.getRegistry(MultitenantUtils.getTenantId(DashboardContext.getConfigContext()));

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

    /**
     * Checking for session expiration
     *
     * @return true | false
     */
    private Boolean isSessionValid() {
        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession(false);
        return (!httpSession.isNew());
    }

    /**
     * A custom query to retrieve gadget data for unsigned users
     *
     * @return array of strings containing the gadget name and URLs
     */
    private String[] getGadgetUrlSetForUnSignedUser() {
        Registry registry;
        try {
            registry = getConfigSystemRegistry();
            Resource comQuery = registry.newResource();

            String sql = "SELECT R.REG_NAME, R.REG_PATH_ID FROM REG_RESOURCE R, REG_PROPERTY P, REG_RESOURCE_PROPERTY RP, REG_PATH PA WHERE "
                    + "R.REG_VERSION=RP.REG_VERSION AND "
                    + "P.REG_NAME='"
                    + DashboardConstants.UNSIGNED_USER_GADGET
                    + "' AND "
                    + "P.REG_VALUE='true' AND "
                    + "P.REG_ID=RP.REG_PROPERTY_ID AND "
                    + "PA.REG_PATH_ID=R.REG_PATH_ID";

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("query", sql);
            Collection qResults = registry.executeQuery(null, map);

            String[] qPaths = (String[]) qResults.getContent();
            ArrayList gadgetUrlsList = new ArrayList();

            for (String qPath : qPaths) {
                if (registry.resourceExists(qPath)) {
                    Resource tempRes = registry.get(qPath);
                    String gadgetNameTmp = tempRes.getProperty(DashboardConstants.GADGET_NAME);
                    String gadgetUrlTmp = tempRes.getProperty(DashboardConstants.GADGET_URL);
                    if (isGadgetAutharized(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, gadgetUrlTmp)) {
                        gadgetUrlsList.add(gadgetNameTmp + "," + gadgetUrlTmp);
                    }
                }
            }

            String[] nameUrlPair = new String[gadgetUrlsList.size()];
            gadgetUrlsList.toArray(nameUrlPair);

            return nameUrlPair;

        } catch (Exception e) {
            log.error("Backend server error - could not get the unsigned_user gadget url set", e);
            return null;
        }

    }

    private boolean isGadgetAutharized(String user, String gadgetUrl) throws UserStoreException {
        if (gadgetUrl.startsWith("/registry")) {
            gadgetUrl = gadgetUrl.split("resource")[1];
        } else {
            //GS is not hosting this gadget
            return true;
        }
        UserRegistry registry = (UserRegistry) getConfigUserRegistry();
        if (registry == null) {
            registry = (UserRegistry) getConfigSystemRegistry();
        }
        return registry.getUserRealm().getAuthorizationManager().isUserAuthorized(user, gadgetUrl, ActionConstants.GET);
    }

    private String getBackendHttpPort() {
        String httpPort = null;
        try {
//            httpPort = ServerConfiguration.getInstance().getFirstProperty("RegistryHttpPort");
/*            String apacheHttpPort = ServerConfiguration.getInstance().getFirstProperty(
                    "ApacheHttpPort");
            if (apacheHttpPort != null && !"".equals(apacheHttpPort)) {
                httpPort = apacheHttpPort;
            }*/
            int port = CarbonUtils.getTransportProxyPort(getConfigContext(), "http");
            if (port == -1) {
                port = CarbonUtils.getTransportPort(getConfigContext(), "http");
            }
            httpPort = Integer.toString(port);

/*            if (httpPort == null) {
                httpPort = (String) DashboardContext.getConfigContext()
                        .getAxisConfiguration().getTransportIn("http")
                        .getParameter("port").getValue();
            }*/

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return httpPort;
    }

    /**
     * The mothod returns all the Util properties belongs to the dashboard
     * @param tDomain
     * @return
     * @throws DashboardException
     */
    public DashboardUtilBean getDashboardUtils(String tDomain) throws DashboardException {
        DashboardUtilBean dashboardUtilBean = new DashboardUtilBean();
        dashboardUtilBean.setAnonModeActive(isAnonModeActive(tDomain));
        dashboardUtilBean.setSessionValid(isSessionValid());
        dashboardUtilBean.setGadgetUrlSetForUnSignedUser(getGadgetUrlSetForUnSignedUser());
        dashboardUtilBean.setBackendHttpPort(getBackendHttpPort());

        return dashboardUtilBean;
    }
}
