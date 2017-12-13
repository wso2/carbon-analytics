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

package org.wso2.carbon.sp.jobmanager.core.util;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utility class to handle HTTP requests.
 */
public class HTTPClientUtil {
    /**
     * Media type to send with the requests.
     */
    private static final MediaType MEDIA_TYPE_PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    /**
     * Send a GET request.
     *
     * @param url URL of the endpoint.
     * @return {@link Response} for the request.
     * @throws IOException when failed to connect.
     */
    public static Response doGetRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return getAuthenticatedClient(DEFAULT_USERNAME, DEFAULT_PASSWORD).newCall(request).execute();
    }

    /**
     * Send a POST request.
     *
     * @param url     URL of the endpoint.
     * @param payload payload string.
     * @return {@link Response} for the request.
     * @throws IOException when failed to connect.
     */
    public static Response doPostRequest(String url, String payload) throws IOException {
        RequestBody body = RequestBody.create(MEDIA_TYPE_PLAINTEXT, payload);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return getAuthenticatedClient(DEFAULT_USERNAME, DEFAULT_PASSWORD).newCall(request).execute();
    }

    /**
     * Send a PUT request.
     *
     * @param url     URL of the endpoint.
     * @param payload payload string.
     * @return {@link Response} for the request.
     * @throws IOException when failed to connect.
     */
    public static Response doPutRequest(String url, String payload) throws IOException {
        RequestBody body = RequestBody.create(MEDIA_TYPE_PLAINTEXT, payload);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        return getAuthenticatedClient(DEFAULT_USERNAME, DEFAULT_PASSWORD).newCall(request).execute();
    }

    /**
     * Send a DELETE request.
     *
     * @param url URL of the endpoint.
     * @return {@link Response} for the request.
     * @throws IOException when failed to connect.
     */
    public static Response doDeleteRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        return getAuthenticatedClient(DEFAULT_USERNAME, DEFAULT_PASSWORD).newCall(request).execute();
    }

    /**
     * Generate a authenticated {@link OkHttpClient} client.
     *
     * @param username username
     * @param password password
     * @return authenticated client
     */
    private static OkHttpClient getAuthenticatedClient(final String username, final String password) {
        return new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(username, password))
                .build();
    }
}
