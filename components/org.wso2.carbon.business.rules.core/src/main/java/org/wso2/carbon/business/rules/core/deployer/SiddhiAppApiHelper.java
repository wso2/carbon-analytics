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

import org.json.JSONObject;
import org.wso2.carbon.business.rules.core.datasource.configreader.ConfigReader;
import org.wso2.carbon.business.rules.core.deployer.api.SiddhiAppApiHelperService;
import org.wso2.carbon.business.rules.core.deployer.util.HTTPClientUtil;
import org.wso2.carbon.business.rules.core.deployer.util.HTTPSClientUtil;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;

import java.io.BufferedReader;
import java.io.IOException;

import feign.Response;

/**
 * Consists of methods for additional features for the exposed Siddhi App Api
 */
public class SiddhiAppApiHelper implements SiddhiAppApiHelperService {
    private static final String SERVICE_ENDPOINT = "https://%s/siddhi-apps/%s/%s"; // TODO remove
    public static final String PROTOCOL = "https://";
    private ConfigReader configReader;
    private String username;
    private String password;
    public SiddhiAppApiHelper() {
        configReader = new ConfigReader();
        username = configReader.getUserName();
        password = configReader.getPassword();
    }

    private static String generateURL(String hostAndPort) {
        return PROTOCOL + hostAndPort;
    }

    @Override
    public boolean deploySiddhiApp(String hostAndPort, String siddhiApp) throws SiddhiAppsApiHelperException {
        Response response = null;
        try {
            response = HTTPSClientUtil.doPostRequest(generateURL(hostAndPort), username, password, siddhiApp);
            int status = response.status();
            switch (status) {
                case 201:
                    return true;
                case 400:
                    throw new SiddhiAppsApiHelperException("A validation error occurred during " +
                            "saving the siddhi app '" + siddhiApp +
                            "' on the node '" + hostAndPort + "'");
                case 409:
                    throw new SiddhiAppsApiHelperException("A Siddhi Application with " +
                            "the given name already exists  " +
                            "in the node '" + hostAndPort + "'");
                case 500:
                    throw new SiddhiAppsApiHelperException("Unexpected error occurred during " +
                            "saving the siddhi app '" + siddhiApp + "' " +
                            "on the node '" + hostAndPort + "'");
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "trying to deploy the siddhi app '" + siddhiApp + "' on node '" + hostAndPort + "'");
            }
        } catch (IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to deploy siddhi app '" + siddhiApp + "' on the node '" +
                    hostAndPort + "'. ", e);
        } finally {
            closeResponse(response);
        }
    }

    @Override
    public String getStatus(String hostAndPort, String siddhiAppName) throws SiddhiAppsApiHelperException {
        String url;
        Response response = null;
        try {
            response = HTTPSClientUtil.doGetRequest(generateURL(hostAndPort), username, password);
            int status = response.status();
            BufferedReader rd;
            StringBuffer result;
            String line;
            JSONObject statusMessage;
            switch (status) {
                case 200:
//                    rd = new BufferedReader(response.body().charStream());
                    rd = new BufferedReader(response.body().asReader());
                    result = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    statusMessage = new JSONObject(result.toString());
                    rd.close();
                    return statusMessage.getString(SiddhiAppApiConstants.STATUS);
                case 404:
                    throw new SiddhiAppsApiHelperException("Specified siddhi app '" + siddhiAppName + "' " +
                            "is not found on the node '" + hostAndPort + "'");
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "requesting the status of siddhi app '" + siddhiAppName + "' from the node '" + hostAndPort +
                            "'");
            }
        } catch (IOException e) {
            throw new SiddhiAppsApiHelperException("URI generated for node url '" + hostAndPort + "' is invalid.", e);
        } finally {
            closeResponse(response);
        }
    }

    @Override
    public boolean delete(String hostAndPort, String siddhiAppName) throws SiddhiAppsApiHelperException {
        Response response = null;
        try {
            response = HTTPSClientUtil.doDeleteRequest(generateURL(hostAndPort), username, password, siddhiAppName);
            int status = response.status();
            switch (status) {
                case 200:
                    return true;
                case 404:
                    throw new SiddhiAppsApiHelperException("Specified siddhi app '" + siddhiAppName +
                            "' is not found on the node '" + hostAndPort + "'");
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "trying to delete the siddhi app '" + siddhiAppName + "' from the node '" + hostAndPort + "'");
            }
        } catch (IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to delete siddhi app '" + siddhiAppName +
                    "' from the node '" + hostAndPort + "'. ", e);
        } finally {
            closeResponse(response);
        }
    }

    @Override
    public void update(String hostAndPort, String siddhiApp) throws SiddhiAppsApiHelperException {
        String url;
        Response response = null;
        try {
            response = HTTPSClientUtil.doPutRequest(generateURL(hostAndPort), username, password, siddhiApp);
            int status = response.status();
            switch (status) {
                case 400:
                    throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp +
                            "' on node '" + hostAndPort + "' due to a validation error occurred " +
                            "when updating the siddhi app");
                case 500:
                    throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp +
                            "' on node '" + hostAndPort + "' due to an unexpected error occurred during updating the " +
                            "siddhi app");
            }
        } catch (IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp + "' on node '"
                    + hostAndPort + "'. ", e);
        } finally {
            closeResponse(response);
        }
    }

    private void closeResponse(feign.Response response) {
        if (response != null) {
            response.close();
        }
    }
}
