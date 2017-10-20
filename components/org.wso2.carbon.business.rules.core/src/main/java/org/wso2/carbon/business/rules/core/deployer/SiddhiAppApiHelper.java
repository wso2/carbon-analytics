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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.deployer.api.SiddhiAppApiHelperService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Consists of methods for additional features for the exposed Siddhi App Api
 */

public class SiddhiAppApiHelper implements SiddhiAppApiHelperService {
    private Logger log = LoggerFactory.getLogger(SiddhiAppApiHelper.class);
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
    public boolean deploySiddhiApp(String nodeUrl, String siddhiApp) {
        URI uri = null;
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
                    log.error("A validation error occurred during saving the siddhi app '" + siddhiApp + "' in the " +
                            "node '" + nodeUrl + "'.");
                    return false;
                case 409:
                    log.error("A Siddhi Application with the given name already exists '" + siddhiApp + "' in the " +
                            "node '" + nodeUrl + "'.");
                    return false;
                case 500:
                    log.error("Unexpected error occurred during saving the siddhi app '" + siddhiApp + "' in the " +
                            "node '" + nodeUrl + "'.");
                    return false;
                default:
                    log.error("Unexpected status code '" + status + "' received.");
                    return false;
            }

        } catch (URISyntaxException e) {
            log.error("URI generated for node url '" + nodeUrl + "' is invalid.", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Provided parameter " + siddhiApp + "cannot be encoded due to " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Http post request to '" + uri + "' for saving the siddhi-app '" + siddhiApp + "' " +
                    "was failed due to " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public String getStatus(String nodeUrl, String siddhiAppName) {
        URI uri = null;
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
                    log.error("Specified siddhi app '" + siddhiAppName + "' is not found.");
                    return null;
                default:
                    log.error("Unexpected status code '" + status + "' received.");
                    return null;
            }

        } catch (URISyntaxException e) {
            log.error("URI generated for node url '" + nodeUrl + "' is invalid.", e);
        } catch (ClientProtocolException e) {
            log.error("Invalid request to " + uri);
        } catch (IOException e) {
            log.error("Http GET request to '" + uri + "' for getting status of the siddhi-app '" +
                    siddhiAppName + "' " +
                    "was failed due to " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean delete(String nodeUrl, String siddhiAppName) {
        URI uri = null;
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
                    log.error("Specified siddhi app '" + siddhiAppName + "' is not found.");
                    return false;
                default:
                    log.error("Unexpected status code '" + status + "' received.");
                    return false;
            }
        } catch (URISyntaxException e) {
            log.error("URI generated for node url '" + nodeUrl + "' is invalid.", e);
        } catch (ClientProtocolException e) {
            log.error("Invalid request to " + uri);
        } catch (IOException e) {
            log.error("Http DELETE request to '" + uri + "' for deleting the siddhi-app '" +
                    siddhiAppName + "' " +
                    "was failed due to " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean update(String nodeUrl, String siddhiApp) {
        URI uri = null;
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
                case 200:
                    return true;
                case 201:
                    return true;
                case 400:
                    log.error("Validation error occurred when updating the siddhi-app '" + siddhiApp + "'.");
                    return false;
                case 500:
                    log.error("An unexpected error occurred during updating the siddhi-app '" + siddhiApp + "'.");
                    return false;
            }
        } catch (URISyntaxException e) {
            log.error("URI generated for node url '" + nodeUrl + "' is invalid.", e);
        } catch (ClientProtocolException e) {
            log.error("Invalid request to " + uri);
        } catch (IOException e) {
            log.error("Http PUT request to '" + uri + "' for updating the siddhi-app '" +
                    siddhiApp + "' was failed due to " + e.getMessage(), e);
        }
        return false;
    }
}
