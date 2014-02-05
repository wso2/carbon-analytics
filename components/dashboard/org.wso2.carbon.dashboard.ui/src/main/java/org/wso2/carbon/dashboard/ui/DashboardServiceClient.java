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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.stub.DashboardServiceStub;
import org.wso2.carbon.dashboard.stub.types.bean.DashboardContentBean;
import org.wso2.carbon.ui.CarbonSSOSessionManager;

import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.Locale;

public class DashboardServiceClient {
    private static final Log log = LogFactory.getLog(DashboardServiceClient.class);

    DashboardServiceStub stub;

    public DashboardServiceClient(String cookie,
                                  String backendServerURL,
                                  ConfigurationContext configCtx,
                                  Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "DashboardService";


        stub = new DashboardServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String getTabLayout(String userId, String dashboardName) {
        try {
            return stub.getTabLayout(userId, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return null;
    }

    public String getGadgetLayout(String loggeduser, String currentActiveTab,
                                  String dashboardName) {
        try {
            return stub.getGadgetLayout(loggeduser, currentActiveTab, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return null;
    }

    public Boolean addGadgetToUser(String loggeduser, String currentActiveTab, String gadgetUrl,
                                   String dashboardName, String gadgetGroup) {
        try {
            return stub.addGadgetToUser(loggeduser, currentActiveTab, gadgetUrl, dashboardName, gadgetGroup);
        } catch (RemoteException e) {
            log.error(e);
        }

        return false;
    }

    public String[] getGadgetUrlsToLayout(String loggeduser, String currentActiveTab,
                                          String dashboardName, String backendURL) {
        try {
            return stub.getGadgetUrlsToLayout(loggeduser, currentActiveTab, dashboardName, backendURL);
        } catch (RemoteException e) {
            log.error(e);
        }

        return null;
    }

    public String getTabTitle(String loggeduser, String tabId, String dashboardName) {
        try {
            return stub.getTabTitle(loggeduser, tabId, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return null;
    }

    public Boolean isReadOnlyMode(String userId) {
        try {
            return stub.isReadOnlyMode(userId);
        } catch (RemoteException e) {
            log.error(e);
        }

        return true;
    }

    public Integer addNewTab(String userId, String tabTitle, String dashboardName) {
        try {
            return stub.addNewTab(userId, tabTitle, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return null;
    }

    public String getGadgetPrefs(String userId, String gadgetId, String prefId,
                                 String dashboardName) {
        try {
            return stub.getGadgetPrefs(userId, gadgetId, prefId, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return null;
    }

    public Boolean removeGadget(String userId, String tabId, String gadgetId,
                                String dashboardName) {
        try {
            return stub.removeGadget(userId, tabId, gadgetId, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return false;
    }

    public Boolean removeTab(String userId, String tabId, String dashboardName) {
        try {
            return stub.removeTab(userId, tabId, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return false;
    }

    public Boolean setGadgetLayout(String userId, String tabId, String newLayout,
                                   String dashboardName) {
        try {
            return stub.setGadgetLayout(userId, tabId, newLayout, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return false;
    }

    public Boolean setGadgetPrefs(String userId, String gadgetId, String prefId, String value,
                                  String dashboardName) {
        try {
            return stub.setGadgetPrefs(userId, gadgetId, prefId, value, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
        }

        return false;
    }

    public String getBackendHttpPort() {
        try {
            return stub.getBackendHttpPort();
        } catch (RemoteException e) {
            log.error(e);
        }

        return null;
    }

    public Boolean isSelfRegistrationEnabled() {
        try {
            return stub.isSelfRegistrationEnabled();
        } catch (RemoteException e) {
            log.error(e);
        }

        return false;
    }

    public Boolean isExternalGadgetAdditionEnabled() {
        try {
            return stub.isExternalGadgetAdditionEnabled();
        } catch (RemoteException e) {
            log.error(e);
        }

        return false;
    }

    public String[] getDefaultGadgetUrlSet(String userId) {
        try {
            return stub.getDefaultGadgetUrlSet(userId);
        } catch (RemoteException e) {
            log.error(e);
            return null;
        }
    }

/*    public String[] getGadgetUrlSetForUnSignedUser() {
        try {
            return stub.getGadgetUrlSetForUnSignedUser();
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }*/

    public Integer duplicateTab(String userId, String dashboardName, String sourceTabId, String newTabName) {
        try {
            return stub.duplicateTab(userId, dashboardName, sourceTabId, newTabName);
        } catch (RemoteException e) {
            log.error(e);
            return 0;
        }

    }


    public Boolean copyGadget(String userId, String tab, String dashboardName,
                              String sourceGadgetId, String grp) {
        try {
            return stub.copyGadget(userId, dashboardName, sourceGadgetId, tab, grp);
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }


    public Boolean moveGadgetToTab(String userId, String dashboardName,
                                   String sourceGadgetId, String tab) {
        try {
            return stub.moveGadgetToTab(userId, dashboardName, sourceGadgetId, tab);
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

    public Boolean isSessionValid(HttpSession httpSession) {
        try {
            return (stub.isSessionValid() && (!httpSession.isNew()) && (CarbonSSOSessionManager.getInstance().isSessionValid(httpSession.getId())));
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }

    public String getPortalStylesUrl(String user) {
        try {
            return stub.getPortalStylesUrl(user);
        } catch (RemoteException e) {
            log.error(e);
            return null;
        }
    }

    public String populateDefaultThreeColumnLayout(String userId, String tabId) {
        try {
            return stub.populateDefaultThreeColumnLayout(userId, tabId);
        } catch (RemoteException e) {
            log.error(e);
            return null;
        }
    }

/*    public Boolean isAnonModeActive(String tDomain) {
        try {
            return stub.isAnonModeActive(tDomain);
        } catch (Exception e) {
            log.error(e);
            return false;
        }
    }*/

    public boolean populateCustomLayouts(String userId, String tabId, String layout, String dashboardName) {
        try {
            return stub.populateCustomLayouts(userId, tabId, layout, dashboardName);
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }

    public DashboardContentBean getDashboardContentBean(String userId, String dashboardName, String tDomain, String backendServerURL) {
        try {
            return stub.getDashboardContent(userId, dashboardName, tDomain, backendServerURL);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public String getDashboardContentBeanAsJson(String userId, String dashboardName, String tDomain, String backendServerURL) {
        try {
            return stub.getDashboardContentAsJson(userId, dashboardName, tDomain, backendServerURL);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public String getTabContentBeanAsJson(String userId, String dashboardName, String tDomain, String backendServerURL, String currentTab) {
        try {
            return stub.getTabContentAsJson(userId, dashboardName, tDomain, backendServerURL, currentTab);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public String getTabLayoutWithNames(String userId, String dashboardName) {
        try {
            return stub.getTabLayoutWithNames(userId, dashboardName);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public boolean populateDashboardTab(String tabId) {
        try {
            return stub.populateDashboardTab(tabId);
        } catch (Exception e) {
            log.error(e);
        }
        return false;
    }
}
