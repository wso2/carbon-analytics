package org.wso2.carbon.bam.gadgetgenwizard.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.json.JSONObject;
import org.wso2.carbon.bam.gadgetgenwizard.stub.GadgetGenAdminServiceGadgetGenException;
import org.wso2.carbon.bam.gadgetgenwizard.stub.GadgetGenAdminServiceStub;
import org.wso2.carbon.bam.gadgetgenwizard.stub.beans.DBConnInfo;
import org.wso2.carbon.bam.gadgetgenwizard.stub.beans.WSMap;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GadgetGenAdminClient {

    private GadgetGenAdminServiceStub stub;

    public GadgetGenAdminClient(String cookie, String backendServerURL,
                                ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "GadgetGenAdminService";
        stub = new GadgetGenAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }


    public String generateGraph(WSMap map) throws Exception {
        try {
            return stub.createGadget(map);
        } catch (GadgetGenAdminServiceGadgetGenException e) {
            throw new Exception(e.getFaultMessage().getGadgetGenException().getMessage(), e);
        }
    }

    public boolean validateDBConn(DBConnInfo dbConnInfo) throws Exception {
        try {
            return stub.validateDBConnection(dbConnInfo);
        } catch (GadgetGenAdminServiceGadgetGenException e) {
            throw new Exception( e.getFaultMessage().getGadgetGenException().getMessage(), e);
        }
    }

    public JSONObject executeQuery(DBConnInfo dbConnInfo, String sql) throws Exception {
        try {
            return GGWUIUtils.convertToJSONObj(stub.executeQuery(dbConnInfo, sql));
        } catch (GadgetGenAdminServiceGadgetGenException e) {
            throw new Exception(e.getFaultMessage().getGadgetGenException().getMessage(), e);
        }
    }


}
