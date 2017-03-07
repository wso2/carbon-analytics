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
package org.wso2.carbon.event.tracer.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.tracer.stub.client.EventTracerAdminServiceEventTracerAdminServiceExceptionException;
import org.wso2.carbon.event.tracer.stub.client.EventTracerAdminServiceStub;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

public class EventTracerAdminServiceClient {

    private static final Log log = LogFactory.getLog(EventTracerAdminServiceClient.class);
    private static final String BUNDLE = "org.wso2.carbon.event.tracer.ui.i18n.Resources";
    private ResourceBundle bundle;
    public EventTracerAdminServiceStub stub;

    public EventTracerAdminServiceClient(String cookie,
                                         String backendServerURL,
                                         ConfigurationContext configCtx,
                                         Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "EventTracerAdminService";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new EventTracerAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] getTraceLogs() throws AxisFault {
        try {
            return stub.getTraceLogs();
        } catch (RemoteException e) {
            String msg = bundle.getString("cannot.get.service.binding");
            handleException(msg, e);
        }
        return null;
    }

    public void clearLogs() throws AxisFault {
        try {
            stub.clearTraceLogs();
        } catch (RemoteException e) {
            String msg = bundle.getString("Cannot clear the logs");
            handleException(msg, e);
        }
    }

    public String[] searchLogs(String key, boolean ignoreCase) throws AxisFault {
        try {
            return stub.searchTraceLog(key, ignoreCase);
        } catch (RemoteException e) {
            String msg = bundle.getString("Cannot search the logs");
            handleException(msg, e);
        } catch (EventTracerAdminServiceEventTracerAdminServiceExceptionException e) {
            String msg = bundle.getString("Cannot search the logs");
            handleException(msg, e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
