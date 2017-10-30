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
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.wso2.carbon.business.rules.core.deployer.api.SiddhiAppApiHelperService;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Consists of methods for additional features for the exposed Siddhi App Api
 */
public class SiddhiAppApiHelper implements SiddhiAppApiHelperService {
    private CloseableHttpClient httpClient = null;
    private String auth;
    private byte[] encodedAuth;
    private String authHeader;

    public SiddhiAppApiHelper() {
        httpClient = HttpClients.createDefault();
        auth = SiddhiAppApiConstants.DEFAULT_USER + ":" + SiddhiAppApiConstants.DEFAULT_PASSWORD;
        encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));
        authHeader = "Basic " + new String(encodedAuth, Charset.forName("UTF-8"));
    }

    @Override
    public boolean deploySiddhiApp(String nodeUrl, String siddhiApp) throws SiddhiAppsApiHelperException {
        URI uri;
        HttpResponse response;
        try {
            uri = new URIBuilder()
                    .setScheme(SiddhiAppApiConstants.HTTP)
                    .setHost(nodeUrl + "/")
                    .setPath(SiddhiAppApiConstants.PATH_SIDDHI_APPS)
                    .build();
            StringEntity stringEntity = new StringEntity(siddhiApp);
            HttpPost post = new HttpPost(uri);
            post.addHeader(SiddhiAppApiConstants.CONTENT_TYPE, SiddhiAppApiConstants.TEXT_PLAIN);
            post.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            post.setEntity(stringEntity);
            response = httpClient.execute(post);

            int status = response.getStatusLine().getStatusCode();
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

        } catch (URISyntaxException | IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to deploy siddhi app '" + siddhiApp + "' on the node '" +
                    nodeUrl + "' due to " + e.getMessage(), e);
        }
    }

    @Override
    public String getStatus(String nodeUrl, String siddhiAppName) throws SiddhiAppsApiHelperException {
        URI uri;
        HttpResponse response;
        try {
            uri = new URIBuilder()
                    .setScheme(SiddhiAppApiConstants.HTTP)
                    .setHost(nodeUrl + "/")
                    .setPath(SiddhiAppApiConstants.PATH_SIDDHI_APPS + siddhiAppName + SiddhiAppApiConstants.PATH_STATUS)
                    .build();
            HttpGet get = new HttpGet(uri);
            get.addHeader(SiddhiAppApiConstants.CONTENT_TYPE, SiddhiAppApiConstants.APPLICATION_JSON);
            get.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            response = httpClient.execute(get);

            int status = response.getStatusLine().getStatusCode();
            BufferedReader rd;
            StringBuffer result;
            String line;
            JSONObject statusMessage;
            switch (status) {
                case 200:
                    rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                            Charset.forName("UTF-8")));
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

        } catch (URISyntaxException | IOException e) {
            throw new SiddhiAppsApiHelperException("URI generated for node url '" + nodeUrl + "' is invalid.", e);
        }
    }

    @Override
    public boolean delete(String nodeUrl, String siddhiAppName) throws SiddhiAppsApiHelperException {
        URI uri;
        HttpResponse response;
        try {
            uri = new URIBuilder()
                    .setScheme(SiddhiAppApiConstants.HTTP)
                    .setHost(nodeUrl + "/")
                    .setPath(SiddhiAppApiConstants.PATH_SIDDHI_APPS + siddhiAppName)
                    .build();

            HttpDelete delete = new HttpDelete(uri);
            delete.addHeader(SiddhiAppApiConstants.CONTENT_TYPE, SiddhiAppApiConstants.APPLICATION_JSON);
            delete.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            response = httpClient.execute(delete);

            int status = response.getStatusLine().getStatusCode();
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
        } catch (URISyntaxException | IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to delete siddhi app '" + siddhiAppName +
                    "' from the node '" + nodeUrl + "' due to " + e.getMessage(), e);
        }
    }

    @Override
    public void update(String nodeUrl, String siddhiApp) throws SiddhiAppsApiHelperException {
        URI uri;
        HttpResponse response;
        try {
            uri = new URIBuilder()
                    .setScheme(SiddhiAppApiConstants.HTTP)
                    .setHost(nodeUrl + "/")
                    .setPath(SiddhiAppApiConstants.PATH_SIDDHI_APPS)
                    .build();
            HttpPut put = new HttpPut(uri);
            StringEntity stringEntity = new StringEntity(siddhiApp);
            put.addHeader(SiddhiAppApiConstants.CONTENT_TYPE, SiddhiAppApiConstants.TEXT_PLAIN);
            put.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            put.setEntity(stringEntity);
            response = httpClient.execute(put);

            int status = response.getStatusLine().getStatusCode();
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
        } catch (URISyntaxException | IOException e) {
            throw new SiddhiAppsApiHelperException("Failed to update the siddhi app '" + siddhiApp + "' on node '"
                    + nodeUrl + "' due to " + e.getMessage(), e);
        }
    }
}
