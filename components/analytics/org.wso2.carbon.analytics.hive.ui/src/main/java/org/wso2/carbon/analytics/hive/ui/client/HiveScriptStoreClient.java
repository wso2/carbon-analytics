/**
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

package org.wso2.carbon.analytics.hive.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.stub.HiveScriptStoreServiceHiveScriptStoreException;
import org.wso2.carbon.analytics.hive.stub.HiveScriptStoreServiceStub;

import java.rmi.RemoteException;

public class HiveScriptStoreClient {
    private static Log log = LogFactory.getLog(HiveExecutionClient.class);

    private HiveScriptStoreServiceStub stub;

    public HiveScriptStoreClient(String cookie,
                                 String backEndServerURL,
                                 ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backEndServerURL + "HiveScriptStoreService";
        stub = new HiveScriptStoreServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void saveScript(String scriptName, String script, String cron) throws HiveScriptStoreServiceHiveScriptStoreException, RemoteException {
       stub.saveHiveScript(scriptName, script, cron);
    }

    public String[] getAllScriptNames() throws HiveScriptStoreServiceHiveScriptStoreException, RemoteException {
        return stub.getAllScriptNames();
    }

    public String getScript(String scriptName) throws HiveScriptStoreServiceHiveScriptStoreException, RemoteException {
          return stub.retrieveHiveScript(scriptName);
    }

    public void deleteScript(String scriptName) throws HiveScriptStoreServiceHiveScriptStoreException, RemoteException {
        stub.deleteScript(scriptName);
    }

    public String getCronExpression(String scriptName) throws HiveScriptStoreServiceHiveScriptStoreException, RemoteException {
       return stub.getCronExpression(scriptName);
    }

    public boolean isTaskRunning(String scriptName) throws HiveScriptStoreServiceHiveScriptStoreException, RemoteException {
      return stub.isTaskExecuting(scriptName);
    }
}
