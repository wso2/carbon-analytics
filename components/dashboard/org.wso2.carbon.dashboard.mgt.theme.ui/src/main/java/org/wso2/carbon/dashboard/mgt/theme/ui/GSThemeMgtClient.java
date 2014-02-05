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

package org.wso2.carbon.dashboard.mgt.theme.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.mgt.theme.stub.GSThemeMgtServiceException;
import org.wso2.carbon.dashboard.mgt.theme.stub.types.carbon.Theme;
import org.wso2.carbon.dashboard.mgt.theme.stub.GSThemeMgtServiceStub;

import javax.activation.DataHandler;
import java.lang.*;
import java.lang.Exception;
import java.rmi.RemoteException;
import java.util.Locale;


public class GSThemeMgtClient {
    private static final Log log = LogFactory.getLog(GSThemeMgtClient.class);
    public GSThemeMgtServiceStub stub;

    public GSThemeMgtClient(String cookie,
                            String backendServerURL,
                            ConfigurationContext configCtx,
                            Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "GSThemeMgtService";
        stub = new GSThemeMgtServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);
    }

    public Theme[] getThemes(String user) throws java.lang.Exception {
        return stub.getThemes(user);
    }

    public boolean setThemeForUser(String themePath, String user) throws Exception {
        return stub.setThemeForUser(themePath, user);
    }

    public String getDefaultThemeForUser(String user) throws Exception {
        return stub.getDefaultThemeForUser(user);
    }

    public void addResource(String path, String mediaType, String description, DataHandler content,
                            String symlinkLocation)
            throws Exception {

        try {
            Options options = stub._getServiceClient().getOptions();
            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            options.setTimeOutInMilliSeconds(300000);
            stub.addResource(path, mediaType, description, content, symlinkLocation);

        } catch (Exception e) {

            String msg = "Failed to add resource " + path + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void getRollbackToDefTheme(String user) {
        try {
            stub.rollbackToDefaultGSTheme(user);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        } catch (GSThemeMgtServiceException e) {
            log.error(e.getMessage(), e);
        }
    }
}
