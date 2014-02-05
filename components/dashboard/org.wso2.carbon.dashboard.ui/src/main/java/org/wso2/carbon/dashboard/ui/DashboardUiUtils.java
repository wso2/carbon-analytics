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
package org.wso2.carbon.dashboard.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.dashboard.stub.types.bean.Gadget;
import org.wso2.carbon.dashboard.stub.types.bean.Tab;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DashboardUiUtils {
    private static Log log = LogFactory.getLog(DashboardUiUtils.class);
    public static final String EMPTY_STRING = "";
    public static StringBuffer currentContext = new StringBuffer();

    public static String getHttpServerRoot(String backendUrl, String backendHttpPort) {
        String response = "";
        try {
            String hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
            backendHttpPort = getBackendPort("http");
            // Removing the carbon part
            response = backendUrl.split("/carbon/")[0];

            URL newUrl = new URL(response);

            if ("".equals(newUrl.getPath())) {
                response = "http://" + newUrl.getHost() + ":" + backendHttpPort;
            } else {
                response = "http://" + newUrl.getHost() + ":" + backendHttpPort + newUrl.getPath();
            }

            if (hostName != null && !hostName.equals("")) {
                response = "http://" + hostName + ":" + backendHttpPort;
            } else if ((hostName != null && !hostName.equals("")) && !"".equals(newUrl.getPath())) {
                response = "http://" + hostName + ":" + backendHttpPort + newUrl.getPath();
            }

        } catch (Exception e) {
            log.error(e);
        }
        return response;
    }

    public static String[] sanitizeUrls(String[] gadgetUrls, String backendUrl,
                                        String backendHttpPort) {
        String[] response = new String[0];

        if (gadgetUrls != null) {
            ArrayList tempUrlHolder = new ArrayList();

            for (int x = 0; x < gadgetUrls.length; x++) {
                String currentUrl = gadgetUrls[x];

                // Check whether this is a relative URL. If so, attach the local server http root to it
                if ("/".equals(String.valueOf(currentUrl.charAt(0)))) {
                    currentUrl = DashboardUiUtils.getHttpServerRoot(backendUrl, backendHttpPort) +
                            currentUrl;
                }

                tempUrlHolder.add(currentUrl);
            }

            response = new String[tempUrlHolder.size()];
            tempUrlHolder.toArray(response);
        }

        return response;
    }

    public static String getHttpsServerRoot(String backendUrl) {
        String response = "";
        // Removing the services part
        response = backendUrl.split("/services/")[0];
        return response;

    }

    public static String getAdminConsoleURL(HttpServletRequest request) {
        /* /carbon in normal product mode; //carbon on multitenant mode */
        if (request.getContextPath().equals("/carbon") || request.getContextPath().equals("//carbon")) {
            return CarbonUIUtil.getAdminConsoleURL("/");
        } else {
            return CarbonUIUtil.getAdminConsoleURL(request);
        }
    }

    public static String getAdminConsoleURL(String context, String tdomain) {
        /* /carbon in normal product mode; //carbon on multitenant mode */
        if (context.equals("/")) {
            context = "";
        }
        return CarbonUIUtil.getAdminConsoleURL("/t/" + tdomain + context);

    }

    public static String getContextWithTenantDomain(String context, String tdomain) {
        /* /carbon in normal product mode; //carbon on multitenant mode */
        if (context.equals("/")) {
            context = "/carbon";
        }
        return "/t/" + tdomain + context;

    }

    public static String getHttpsPort(String backendUrl) {
        try {
            URL newUrl = new URL(backendUrl);
            return Integer.toString(newUrl.getPort());
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static String getPortalCss(HttpServletRequest request, ServletConfig config, DashboardServiceClient dashboardServiceClient) {
        String loggeduser = (String) request.getSession().getAttribute("logged-user");
        String tenantDomain = "";
        if (request.getSession().getAttribute(RegistryConstants.TENANT_DOMAIN) != null) {
            tenantDomain = (String) request.getSession().getAttribute(RegistryConstants.TENANT_DOMAIN);
        }

        String portalCSSStrippedPath = null;
        String portalCSS = "../dashboard/localstyles/gadget-server.css";
        if (loggeduser != null) {
            portalCSSStrippedPath = dashboardServiceClient.getPortalStylesUrl(loggeduser);
        }

        if (portalCSSStrippedPath != null) {

            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), request.getSession());
            if (currentContext.toString().contains("localhost")) {
                serverURL = "https://localhost:8443/services/";
            }

            if (serverURL.split(":") == currentContext.toString().split(":")) {

            }
            String serverRoot = serverURL.substring(0, serverURL.length()
                    - "services/".length());
            String themeRoot;

            if (tenantDomain != null && !"".equals(tenantDomain) && (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain))) {

                themeRoot = serverRoot + "t/" + tenantDomain
                        + "/gs/resource"
                        + RegistryConstants.CONFIG_REGISTRY_BASE_PATH;
                portalCSS = themeRoot + portalCSSStrippedPath;
            } else {
                themeRoot = serverRoot + "/gs/resource"
                        + RegistryConstants.CONFIG_REGISTRY_BASE_PATH;

                portalCSS = themeRoot + portalCSSStrippedPath;
            }
        }
        return portalCSS;
    }

    public static String getLoginUrl(String toPath, String httpsPort, HttpServletRequest request) {
        String currentUrl = request.getRequestURI();
        if (currentUrl.indexOf("//carbon") > -1) {
            currentUrl = currentUrl.replace("//carbon", "/carbon");
        }
        String pathName = currentUrl + "/" + "../" + toPath;
        String tenantdomainFromReq = (String) request.getAttribute(CarbonConstants.TENANT_DOMAIN);
        if (tenantdomainFromReq != null) {
            pathName = "/t/" + tenantdomainFromReq + pathName;
        }

        String retUrl = "https://" + request.getServerName() + ':' + httpsPort + pathName;
        return retUrl;
    }

    public static String[] getGadgetUrlsToLayout(String currentActiveTab, Tab[] tabs) {
        ArrayList<String> urls = new ArrayList<String>();
        if (tabs != null) {
            for (Tab tab : tabs) {
                if (tab.getTabId().equals(currentActiveTab)) {
                    Gadget[] gadgets = tab.getGadgets();
                    if (gadgets != null) {
                        for (Gadget gadget : gadgets) {
                            urls.add(gadget.getGadgetUrl());
                        }
                        String[] gadgetUrls = new String[urls.size()];
                        return urls.toArray(gadgetUrls);
                    }
                }
            }
        }
        return new String[0];
    }

    public static String getGadgetLayout(String currentActiveTab, Tab[] tabs) {
        if (tabs != null) {
            for (Tab tab : tabs) {
                if (tab.getTabId().equals(currentActiveTab)) {
                    return tab.getGadgetLayout();
                }
            }
        }
        return EMPTY_STRING;
    }

    public static String getTabTitle(String tabId, Tab[] tabs) {
        if (tabs != null) {
            for (Tab tab : tabs) {
                if (tab.getTabId().equals(tabId)) {
                    return tab.getTabName();
                }
            }
        }
        return EMPTY_STRING;
    }

    public static String getTabIdsWithTitles(Tab[] tabs) {

        StringBuilder result = new StringBuilder();
        if (tabs != null) {
            for (Tab tab : tabs) {
                result.append(tab.getTabId() + "-" + tab.getTabName().replaceAll("\n", ""));
                result.append(",");
            }
            if (result.length() != 0) {
                return result.substring(0, result.length() - 1);
            }
        }
        return EMPTY_STRING;
    }

    public static String getGadgetIdsWithPrefs(String currentActiveTab, Tab[] tabs) {

        StringBuilder result = new StringBuilder();
        if (tabs != null) {
            for (Tab tab : tabs) {
                if (tab.getTabId().equals(currentActiveTab)) {
                    Gadget[] gadgets = tab.getGadgets();
                    if (gadgets != null) {
                        for (Gadget gadget : gadgets) {
                            result.append(gadget.getGadgetId() + "$" + gadget.getGadgetPrefs());
                            result.append("#");
                        }
                    }
                }
            }
            if (result.length() != 0) {
                return result.substring(0, result.length() - 1);
            }
        }
        return EMPTY_STRING;
    }

    public static boolean isGadgetServer() {
        return CarbonUtils.getServerConfiguration().getFirstProperty("Name").toLowerCase().replaceAll(" ", "").equalsIgnoreCase("wso2gadgetserver") ||
                CarbonUtils.getServerConfiguration().getFirstProperty("Name").toLowerCase().replaceAll(" ", "").equalsIgnoreCase("wso2stratosgadgetserver") ||
                CarbonUtils.getServerConfiguration().getFirstProperty("Name").toLowerCase().replaceAll(" ", "").equalsIgnoreCase("wso2businessactivitymonitor") ||
                CarbonUtils.getServerConfiguration().getFirstProperty("Name").toLowerCase().replaceAll(" ", "").equalsIgnoreCase("wso2stratosbusinessactivitymonitor")||
                CarbonUtils.getServerConfiguration().getFirstProperty("Name").toLowerCase().replaceAll(" ", "").equalsIgnoreCase("wso2stratosmanager");
    }

    /**
     * Get the running transport port
     *
     * @param transport [http/https]
     * @return port
     */
    public static String getBackendPort(String transport) {
        int port;
        String backendPort;
        try {
            port = CarbonUtils.getTransportProxyPort(DashboardUiContext.getConfigContext(), transport);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(DashboardUiContext.getConfigContext(), transport);
            }
            backendPort = Integer.toString(port);
            return backendPort;
        } catch (DashboardUiException e) {
            log.error(e.getMessage());
            return null;
        }

    }

}
