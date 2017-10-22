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

package org.wso2.carbon.sp.distributed.resource.core.util;

import com.google.gson.Gson;

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
     * Instance of {@link Gson} to un/marshall request/response.
     */
    public static final Gson GSON = new Gson();
    /**
     * Media type to send with the requests.
     */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    /**
     * Client to handle HTTP requests.
     */
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    /**
     * Send a POST request.
     *
     * @param url     URL of the endpoint.
     * @param payload payload object.
     * @return {@link Response} for the request.
     * @throws IOException when failed to connect.
     */
    public static Response doPostRequest(String url, Object payload) throws IOException {
        RequestBody body = RequestBody.create(JSON, GSON.toJson(payload));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return HTTP_CLIENT.newCall(request).execute();
    }

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
        return HTTP_CLIENT.newCall(request).execute();
    }
}
