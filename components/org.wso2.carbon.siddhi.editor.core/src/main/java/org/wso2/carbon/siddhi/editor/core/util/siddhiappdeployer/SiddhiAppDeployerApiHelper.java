/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer;

import feign.Response;
import feign.RetryableException;
import org.wso2.carbon.siddhi.editor.core.exception.SiddhiAppDeployerServiceStubException;
import org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.api.SiddhiAppApiHelperService;
import org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.util.HTTPSClientUtil;

/**
 * Consists of a method for the deployment feature for the exposed Siddhi App Api.
 */
public class SiddhiAppDeployerApiHelper implements SiddhiAppApiHelperService {

    @Override
    public boolean deploySiddhiApp(String hostAndPort, String username, String password, String siddhiApp,
                                   String fileName) throws SiddhiAppDeployerServiceStubException {

        Response response = null;
        try {
            response = HTTPSClientUtil.doPutRequest(hostAndPort, username, password, siddhiApp);
            int status = response.status();
            switch (status) {
                case 200:
                    return true;
                case 201:
                    return true;
                case 400:
                    throw new SiddhiAppDeployerServiceStubException("Status code " + status +
                            " received. A validation error occurred during " +
                            "saving the siddhi app '" + fileName + "' on the node '" + hostAndPort + "'");
                case 401:
                    throw new SiddhiAppDeployerServiceStubException("Status code " + status +
                            " received. Invalid user name or password on the node '" +
                            hostAndPort + "' for the user " + username);
                case 500:
                    throw new SiddhiAppDeployerServiceStubException("Status code " + status +
                            " received. Unexpected error occurred during " +
                            "saving the siddhi app '" + fileName + "' on the node '" + hostAndPort + "'");
                default:
                    throw new SiddhiAppDeployerServiceStubException("Unexpected status code '" + status +
                            "' received when trying to deploy the siddhi app '" + fileName + "' on node '" +
                            hostAndPort + "'");
            }
        } catch (RetryableException e) {
            throw new SiddhiAppDeployerServiceStubException("Cannot connect to the worker node (" + hostAndPort + ") " +
                    "for retrieving status of the siddhi app " + fileName + ".", e);
        } finally {
            closeResponse(response);
        }
    }

    private void closeResponse(Response response) {

        if (response != null) {
            response.close();
        }
    }
}
