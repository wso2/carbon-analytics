/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.execution.manager.ui;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

/**
 * Consist of the required UI utility methods
 */
public class ExecutionManagerUIUtils {

    /**
     * To avoid instantiating
     */
    private ExecutionManagerUIUtils() {
    }

    /**
     * Provides created  ExecutionManagerAdminServiceStub object
     *
     * @param config  Servlet Configuration
     * @param session Http Session
     * @return Created ExecutionManagerAdminServiceStub object
     * @throws AxisFault
     */
    public static ExecutionManagerAdminServiceStub getExecutionManagerAdminService(
            ServletConfig config, HttpSession session)
            throws AxisFault {
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "ExecutionManagerAdminService.ExecutionManagerAdminServiceHttpsSoap12Endpoint";
        ExecutionManagerAdminServiceStub stub = new ExecutionManagerAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }


    /**
     * Provides created  AuthenticationAdminStub object
     *
     * @param config  Servlet Configuration
     * @param session Http Session
     * @return Created ExecutionManagerAdminServiceStub object
     * @throws AxisFault
     */
    public static AuthenticationAdminStub getAuthenticationAdminService(
            ServletConfig config, HttpSession session)
            throws AxisFault {
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "AuthenticationAdmin.AuthenticationAdminHttpsSoap12Endpoint";
        AuthenticationAdminStub stub = new AuthenticationAdminStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }
}
