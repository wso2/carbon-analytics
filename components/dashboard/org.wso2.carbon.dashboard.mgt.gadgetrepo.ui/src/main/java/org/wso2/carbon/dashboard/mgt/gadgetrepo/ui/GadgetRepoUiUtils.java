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

package org.wso2.carbon.dashboard.mgt.gadgetrepo.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.ui.DashboardUiUtils;
import org.wso2.carbon.dashboard.ui.DashboardServiceClient;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

public class GadgetRepoUiUtils {

    private static Log log = LogFactory.getLog(GadgetRepoUiUtils.class);

    private static DashboardServiceClient dashboardServiceClient;

    public static String getHttpServerRoot(String backendUrl,
                                           String backendHttpPort) {
        String response = "";
        try {

            // Removing the services part
            response = backendUrl.split("/services/")[0];

            URL newUrl = new URL(response);

            if ("".equals(newUrl.getPath())) {
                response = "http://" + newUrl.getHost() + ":" + backendHttpPort;
            } else {
                response = "http://" + newUrl.getHost() + ":" + backendHttpPort
                        + "/" + newUrl.getPath();
            }

        } catch (MalformedURLException e) {
            log.error(e);
        }
        return response;
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

    public static String getPortalCss(String cookie, String backEndServerUrl, ConfigurationContext ctx, Locale locale, HttpServletRequest request, ServletConfig config) throws AxisFault {
        dashboardServiceClient = new DashboardServiceClient(cookie, backEndServerUrl, ctx, locale);
        return DashboardUiUtils.getPortalCss(request, config, dashboardServiceClient);
    }

}
