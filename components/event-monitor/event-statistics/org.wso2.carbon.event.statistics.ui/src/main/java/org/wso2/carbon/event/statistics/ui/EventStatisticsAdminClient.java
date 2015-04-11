/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.statistics.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.statistics.stub.client.EventStatisticsAdminServiceStub;
import org.wso2.carbon.event.statistics.stub.client.EventStatisticsAdminServiceStub.StatsDTO;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class EventStatisticsAdminClient {

    private static final Log log = LogFactory.getLog(EventStatisticsAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.event.statistics.ui.i18n.Resources";
    private EventStatisticsAdminServiceStub stub;
    private ResourceBundle bundle;

    public EventStatisticsAdminClient(String cookie,
                                      String backendServerURL,
                                      ConfigurationContext configCtx,
                                      Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "EventStatisticsAdminService";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new EventStatisticsAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public StatsDTO getGlobalCount() throws RemoteException {
        try {
            return stub.getGlobalCount();
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.event.stats"), e);
        }
        return null;
    }

    public StatsDTO getCategoryCount(String categoryName) throws RemoteException {
        try {
            return stub.getCategoryCount(categoryName);
        } catch (RemoteException e) {
            handleException(MessageFormat.format(bundle.getString("cannot.get.event.category.stats"),
                                                 categoryName), e);
        }
        return null;
    }

    public StatsDTO getDeploymentCount(String categoryName, String deploymentName) throws RemoteException {
        try {
            return stub.getDeploymentCount(categoryName, deploymentName);
        } catch (RemoteException e) {
            handleException(MessageFormat.format(bundle.getString("cannot.get.event.deployment.stats"),
                                                 categoryName, deploymentName), e);
        }
        return null;
    }

    public StatsDTO getElementCount(String categoryName, String deploymentName, String elementName) throws RemoteException {
        try {
            return stub.getElementCount(categoryName, deploymentName, elementName);
        } catch (RemoteException e) {
            handleException(MessageFormat.format(bundle.getString("cannot.get.event.element.stats"),
                                                 categoryName, deploymentName, elementName), e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }
}
