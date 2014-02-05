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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.mgt.users.stub.GadgetServerUserManagementServiceStub;

import java.util.Locale;
import java.rmi.RemoteException;

public class GadgetServerUserManagementServiceClient {

    private static final Log log = LogFactory
            .getLog(GadgetServerUserManagementServiceClient.class);

    GadgetServerUserManagementServiceStub stub;

    public GadgetServerUserManagementServiceClient(String cookie, String backendServerURL,
                                                   ConfigurationContext configCtx, Locale locale)
            throws AxisFault {

        String serviceURL = backendServerURL + "GadgetServerUserManagementService";
        stub = new GadgetServerUserManagementServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);

    }

    public Boolean setUserSelfRegistration(boolean flag) {
        try {
            return stub.setUserSelfRegistration(flag);
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }

    public Boolean setUserExternalGadgetAddition(boolean flag) {
        try {
            return stub.setUserExternalGadgetAddition(flag);
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }
     public Boolean isSessionValid() {
        try {
            return stub.isSessionValid();
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }
    
    public Boolean isSelfRegistration(String tDomain) {
      
        try {

            return stub.isSelfRegistration(tDomain);
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }

    public Boolean isExternalGadgetAddition() {
        try {
            return stub.isExternalGadgetAddition();
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }

    public Boolean isAnonModeActive(String tDomain) {
        try {
            return stub.isAnonModeActive(tDomain);
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }

    public Boolean setAnonModeState(boolean flag) {
        try {
            return stub.setAnonModeState(flag);
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }
}
