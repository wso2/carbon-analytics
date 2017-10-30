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

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.wso2.carbon.business.rules.core.deployer.api.SiddhiAppApiHelperService;
import org.wso2.carbon.business.rules.core.deployer.util.HTTPClientUtil;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import okhttp3.Response;

/**
 * Consists of methods for additional features for the exposed Siddhi App Api
 */
public class SiddhiAppApiHelper implements SiddhiAppApiHelperService {
    private static final String SERVICE_ENDPOINT = "http://%s/siddhi-apps/%s";
    @Override
    public boolean deploySiddhiApp(String nodeUrl, String siddhiApp) throws SiddhiAppsApiHelperException {
        Response response;
        String url;
        try {
            url = String.format(SERVICE_ENDPOINT, nodeUrl, "");
            response = HTTPClientUtil.doPostRequest(url, siddhiApp);
            int status = response.code();
            switch (status) {
                case 201:
                    return true;
                case 400:
                    throw new SiddhiAppsApiHelperException("A validation error occurred during " +
                            "saving the siddhi app '" + siddhiApp +
                            "' on the node '" + nodeUrl + "'");
                case 409:
                    throw new SiddhiAppsApiHelperException("A Siddhi Application with " +
                            "the given name already exists  " +
                            "in the node '" + nodeUrl + "'");
                case 500:
                    throw new SiddhiAppsApiHelperException("Unexpected error occurred during " +
                            "saving the siddhi app '" + siddhiApp + "' " +
                            "on the node '" + nodeUrl + "'");
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "trying to deploy the siddhi app '" + siddhiApp + "' on node '" + nodeUrl + "'");
            }
        } catch (IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to deploy siddhi app '" + siddhiApp + "' on the node '" +
                    nodeUrl + "'. ", e);
        }
    }

    @Override
    public String getStatus(String nodeUrl, String siddhiAppName) throws SiddhiAppsApiHelperException {
        String url;
        Response response;
        try {
            url = String.format(SERVICE_ENDPOINT, nodeUrl, SiddhiAppApiConstants.STATUS);
            response = HTTPClientUtil.doGetRequest(url);
            int status = response.code();
            BufferedReader rd;
            StringBuffer result;
            String line;
            JSONObject statusMessage;
            switch (status) {
                case 200:
                    rd = new BufferedReader(response.body().charStream());
                    result = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    statusMessage = new JSONObject(result.toString());
                    rd.close();
                    return statusMessage.getString(SiddhiAppApiConstants.STATUS);
                case 404:
                    throw new SiddhiAppsApiHelperException("Specified siddhi app '" + siddhiAppName + "' " +
                            "is not found on the node '" + nodeUrl + "'");
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "requesting the status of siddhi app '" + siddhiAppName + "' from the node '" + nodeUrl +
                            "'");
            }
        } catch (IOException e) {
            throw new SiddhiAppsApiHelperException("URI generated for node url '" + nodeUrl + "' is invalid.", e);
        }
    }

    @Override
    public boolean delete(String nodeUrl, String siddhiAppName) throws SiddhiAppsApiHelperException {
        String url;
        Response response;
        try {
            url = String.format(SERVICE_ENDPOINT, nodeUrl, siddhiAppName);
            response = HTTPClientUtil.doGetRequest(url);
            int status = response.code();
            switch (status) {
                case 200:
                    return true;
                case 404:
                    throw new SiddhiAppsApiHelperException("Specified siddhi app '" + siddhiAppName +
                            "' is not found on the node '" + nodeUrl + "'");
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "trying to delete the siddhi app '" + siddhiAppName + "' from the node '" + nodeUrl + "'");
            }
        } catch (IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to delete siddhi app '" + siddhiAppName +
                    "' from the node '" + nodeUrl + "'. ", e);
        }
    }

    @Override
    public void update(String nodeUrl, String siddhiApp) throws SiddhiAppsApiHelperException {
        String url;
        Response response;
        try {
            url = String.format(SERVICE_ENDPOINT, nodeUrl, "");
            response = HTTPClientUtil.doPutRequest(url, siddhiApp);

            int status = response.code();
            switch (status) {
                case 400:
                    throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp +
                            "' on node '" + nodeUrl + "' due to a validation error occurred " +
                            "when updating the siddhi app");
                case 500:
                    throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp +
                            "' on node '" + nodeUrl + "' due to an unexpected error occurred during updating the " +
                            "siddhi app");
            }
        } catch (IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp + "' on node '"
                    + nodeUrl + "'. ", e);
        }
    }
}
