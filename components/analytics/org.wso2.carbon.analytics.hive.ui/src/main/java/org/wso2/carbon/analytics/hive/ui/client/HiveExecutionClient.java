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
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceHiveExecutionException;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub.QueryResult;

import java.rmi.RemoteException;

public class HiveExecutionClient {

    private static Log log = LogFactory.getLog(HiveExecutionClient.class);

    private HiveExecutionServiceStub stub;

    public HiveExecutionClient(String cookie,
                               String backEndServerURL,
                               ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backEndServerURL + "HiveExecutionService";
        stub = new HiveExecutionServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();


        int timeout = 10 * 60 * 1000; // 10 minutes
        stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(timeout);

//or

        stub._getServiceClient().getOptions().setProperty(
                         HTTPConstants.SO_TIMEOUT, timeout);
        stub._getServiceClient().getOptions().setProperty(
                         HTTPConstants.CONNECTION_TIMEOUT, timeout);


        Options option = client.getOptions();
        option.setManageSession(true);
        option.setTimeOutInMilliSeconds(timeout);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public QueryResult[] executeScript(String name, String script) throws RemoteException, HiveExecutionServiceHiveExecutionException {
        try {
            QueryResult[] res = stub.executeHiveScript(name, script);
            return res;
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (HiveExecutionServiceHiveExecutionException e) {
            log.error(e.getFaultMessage().getHiveExecutionException().getExceptionMessage(), e);
            throw new HiveExecutionServiceHiveExecutionException(e.getFaultMessage().getHiveExecutionException().getExceptionMessage());
        }
    }



}
