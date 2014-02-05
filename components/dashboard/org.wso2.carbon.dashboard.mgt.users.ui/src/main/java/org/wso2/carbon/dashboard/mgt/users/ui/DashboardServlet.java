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

package org.wso2.carbon.dashboard.mgt.users.ui;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.CarbonConstants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DashboardServlet extends HttpServlet {

    public void init(ServletConfig servletConfig) throws ServletException {

    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws ServletException, IOException {

        String servletContext = servletRequest.getContextPath();

        // Storing the server url in servlet context
        String serverURL = CarbonUIUtil
                .getServerURL(GadgetServerUserManagementUiServiceContext.getServerConfiguration());
        if ("/".equals(servletContext)) {
            servletContext = "";
        }
        serverURL = serverURL.replace("${carbon.context}", servletContext);
        servletRequest.getSession().setAttribute(CarbonConstants.SERVER_URL, serverURL);

        String tenantDomain = "";
        if (servletRequest.getAttribute(RegistryConstants.TENANT_DOMAIN) != null) {
            tenantDomain = (String) servletRequest.getAttribute(RegistryConstants.TENANT_DOMAIN);
        }
//        System.out.println("t domain in servlet : " + tenantDomain);
        if (tenantDomain != null && !"".equals(tenantDomain)) {
//            System.out.println("Response redirect : " + servletRequest.getContextPath() + "t/" + tenantDomain + "/carbon/dashboard/index.jsp");
            servletResponse.sendRedirect(servletRequest.getContextPath() + "t/" + tenantDomain + "/carbon/dashboard/index.jsp");
        } else {
            // Redirect to Dashboard Component UI
            servletResponse
                    .sendRedirect(servletRequest.getContextPath() + "/carbon/dashboard/index.jsp");
        }
    }

    public String getServletInfo() {
        return null;
    }

    public void destroy() {

    }
}
