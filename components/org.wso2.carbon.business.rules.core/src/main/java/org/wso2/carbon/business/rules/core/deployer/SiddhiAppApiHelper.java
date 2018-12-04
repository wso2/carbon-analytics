/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.business.rules.core.deployer;

import feign.Response;
import feign.RetryableException;
import org.json.JSONObject;
import org.wso2.carbon.business.rules.core.datasource.configreader.ConfigReader;
import org.wso2.carbon.business.rules.core.deployer.api.SiddhiAppApiHelperService;
import org.wso2.carbon.business.rules.core.deployer.util.HTTPSClientUtil;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppDeployerServiceStubException;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;

/**
 * Consists of methods for additional features for the exposed Siddhi App Api
 */
public class SiddhiAppApiHelper implements SiddhiAppApiHelperService {
    private ConfigReader configReader;
    private String username;
    private String password;

    public SiddhiAppApiHelper() {
        configReader = new ConfigReader();
        username = configReader.getUserName();
        password = configReader.getPassword();
    }

    @Override
    public boolean deploySiddhiApp(String hostAndPort, String siddhiApp) throws SiddhiAppsApiHelperException {
        Response response = null;
        try {
            response = HTTPSClientUtil.doPostRequest(hostAndPort, username, password, siddhiApp);
            int status = response.status();
            switch (status) {
                case 201:
                    return true;
                case 400:
                    throw new SiddhiAppsApiHelperException("A validation error occurred during " +
                            "saving the siddhi app '" + siddhiApp + "' on the node '" + hostAndPort + "'", status);
                case 409:
                    throw new SiddhiAppsApiHelperException("A Siddhi Application with " +
                            "the given name already exists in the node '" + hostAndPort + "'", status);
                case 500:
                    throw new SiddhiAppsApiHelperException("Unexpected error occurred during " +
                            "saving the siddhi app '" + siddhiApp + "' on the node '" + hostAndPort + "'", status);
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "trying to deploy the siddhi app '" + siddhiApp + "' on node '" + hostAndPort + "'", status);
            }
        } catch (SiddhiAppDeployerServiceStubException e) {
            throw new SiddhiAppsApiHelperException("Failed to deploy siddhi app '" + siddhiApp + "' on the node '" +
                    hostAndPort + "'. ", e);
        } catch (RetryableException e) {
            throw new SiddhiAppsApiHelperException("Cannot connect to the worker node (" + hostAndPort + ") for " +
                    "retrieving status of the siddhi app " + siddhiApp + ".", e);
        } finally {
            closeResponse(response);
        }
    }

    @Override
    public String getStatus(String hostAndPort, String siddhiAppName) throws SiddhiAppsApiHelperException {
        Response response = null;
        try {
            response = HTTPSClientUtil.doGetRequest(hostAndPort, username, password, siddhiAppName);
            int status = response.status();
            JSONObject statusMessage;
            switch (status) {
                case 200:
                    statusMessage = new JSONObject(response.body().toString());
                    return statusMessage.getString(SiddhiAppApiConstants.STATUS);
                case 404:
                    throw new SiddhiAppsApiHelperException("Specified siddhi app '" + siddhiAppName + "' " +
                            "is not found on the node '" + hostAndPort + "'", status);
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "requesting the status of siddhi app '" + siddhiAppName + "' from the node '" +
                            hostAndPort + "'", status);
            }
        } catch (SiddhiAppDeployerServiceStubException e) {
            throw new SiddhiAppsApiHelperException("URI generated for node url '" + hostAndPort + "' is invalid.", e);
        } catch (RetryableException e) {
            throw new SiddhiAppsApiHelperException("Cannot connect to the worker node (" + hostAndPort + ") for " +
                    "retrieving status of the siddhi app " + siddhiAppName + ".", e);
        } finally {
            closeResponse(response);
        }
    }

    @Override
    public boolean delete(String hostAndPort, String siddhiAppName) throws SiddhiAppsApiHelperException {
        Response response = null;
        try {
            response = HTTPSClientUtil.doDeleteRequest(hostAndPort, username, password, siddhiAppName);
            int status = response.status();
            switch (status) {
                case 200:
                    return true;
                case 404:
                    throw new SiddhiAppsApiHelperException("Specified siddhi app '" + siddhiAppName +
                            "' is not found on the node '" + hostAndPort + "'", status);
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "trying to delete the siddhi app '" + siddhiAppName + "' from the node '" +
                            hostAndPort + "'", status);
            }
        } catch (SiddhiAppDeployerServiceStubException e) {
            throw new SiddhiAppsApiHelperException("Failed to delete siddhi app '" + siddhiAppName +
                    "' from the node '" + hostAndPort + "'. ", e);
        } catch (RetryableException e) {
            throw new SiddhiAppsApiHelperException("Cannot connect to the worker node (" + hostAndPort + ") for " +
                    "retrieving status of the siddhi app " + siddhiAppName + ".", e);
        } finally {
            closeResponse(response);
        }
    }

    @Override
    public void update(String hostAndPort, String siddhiApp) throws SiddhiAppsApiHelperException {
        Response response = null;
        try {
            response = HTTPSClientUtil.doPutRequest(hostAndPort, username, password, siddhiApp);
            int status = response.status();
            switch (status) {
                case 400:
                    throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp +
                            "' on node '" + hostAndPort + "' due to a validation error occurred " +
                            "when updating the siddhi app", status);
                case 500:
                    throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp +
                            "' on node '" + hostAndPort + "' due to an unexpected error occurred during updating the " +
                            "siddhi app", status);
            }
        } catch (SiddhiAppDeployerServiceStubException e) {
            throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp + "' on node '"
                    + hostAndPort + "'. ", e);
        } catch (RetryableException e) {
            throw new SiddhiAppsApiHelperException("Cannot connect to the worker node (" + hostAndPort + ") for " +
                    "retrieving status of the siddhi app " + siddhiApp + ".", e);
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
