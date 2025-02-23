/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.si.metrics.icp.reporter.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.wso2.msf4j.Request;

import java.io.IOException;

/**
 * Utilities to execute http requests.
 */
public class HttpUtils {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private HttpUtils() {
    }

    public static String extractAuthToken(Request request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null) {
            String[] parts = authorizationHeader.split(" ", 2);
            if (parts.length == 2) {
                return parts[1];
            }
        }
        return "";
    }

    public static JsonArray getArtifactList(Request request, String basePath, String resource) {
        String accessToken = extractAuthToken(request);
        CloseableHttpResponse artifactDetails = HttpUtils.doGet(accessToken, basePath + resource);
        return getJsonArray(artifactDetails);
    }

    public static JsonObject getArtifactObject(Request request, String basePath, String resource) {
        String accessToken = extractAuthToken(request);
        CloseableHttpResponse artifactDetails = HttpUtils.doGet(accessToken, basePath + resource);
        return getJsonResponse(artifactDetails);
    }

    private static CloseableHttpResponse doGet(String accessToken, String url) {
        try {
            final HttpGet httpGet = new HttpGet(url);

            String authHeader = BEARER + accessToken;
            httpGet.setHeader(Constants.ACCEPT, Constants.HEADER_VALUE_APPLICATION_JSON);
            httpGet.setHeader(AUTHORIZATION, authHeader);

            CloseableHttpClient httpClient = getHttpClient();
            return httpClient.execute(httpGet);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while sending get http request.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while creating http get request." + 400 + e.getCause());
        }
    }

    public static CloseableHttpResponse doPut(String accessToken, String url) {
        final HttpPut httpPut = new HttpPut(url);

        String authHeader = BEARER + accessToken;
        httpPut.setHeader(Constants.ACCEPT, Constants.HEADER_VALUE_APPLICATION_JSON);
        httpPut.setHeader(Constants.ACCEPT, Constants.HEADER_VALUE_APPLICATION_JSON);
        httpPut.setHeader(AUTHORIZATION, authHeader);

        try {
            CloseableHttpClient httpClient = getHttpClient();
            return httpClient.execute(httpPut);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while sending http put request.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while creating http put request." + 400 + e.getCause());
        }
    }

    public static JsonObject getJsonResponse(CloseableHttpResponse response) {
        String stringResponse = getStringResponse(response);
        return JsonParser.parseString(stringResponse).getAsJsonObject();
    }

    private static JsonArray getJsonArray(CloseableHttpResponse response) {
        String stringResponse = getStringResponse(response);
        return JsonParser.parseString(stringResponse).getAsJsonArray();
    }

    private static String getStringResponse(CloseableHttpResponse response) {
        HttpEntity entity = response.getEntity();
        try {
            return EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while converting Http response to string", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while closing Http response", e);
            }
        }
    }

    private static CloseableHttpClient getHttpClient() throws Exception {
        return HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE)).build();
    }
}
