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

package org.wso2.carbon.stream.processor.core.ha.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Util class used to handle http requests
 */
public class RequestUtil {

    private static final Logger log = Logger.getLogger(RequestUtil.class);

    /**
     * Send http GET requests to specified uri
     *
     * @param uri the desired destination to which the http request should be sent
     * @return String containing the http response
     */
    public static String requestAndGetResponseMessage(URI uri, String username, String password) {

        String content = "";
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
        get.addHeader("Accept", "application/json");
        get.addHeader("Authorization", "Basic " + java.util.Base64.getEncoder().
                encodeToString((username + ":" + password).getBytes(Charset.defaultCharset())));
        if (log.isDebugEnabled()) {
            log.debug("Passive Node: Sending GET request to Active Node to URI " + uri);
        }
        BufferedReader br = null;
        InputStreamReader inputStreamReader = null;
        try {
            String output;
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                inputStreamReader = new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset());
                br = new BufferedReader(inputStreamReader);
                while ((output = br.readLine()) != null) {
                    content = output;
                }
            }
        } catch (IOException e) {
            log.error("Passive Node: Error occurred while connecting to Active Node using live state sync.", e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                ((CloseableHttpClient) client).close();
            } catch (IOException e) {
                log.error("Passive Node: Error closing http client after sending request to Active Node to URI " + uri);
            }
        }
        return content;
    }

    public static int requestAndGetStatusCode(URI uri, String username, String password) {

        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
        get.addHeader("Accept", "application/json");
        get.addHeader("Authorization", "Basic " + java.util.Base64.getEncoder().
                encodeToString((username + ":" + password).getBytes(Charset.defaultCharset())));
        if (log.isDebugEnabled()) {
            log.debug("Passive Node: Sending GET request to Active Node to URI " + uri);
        }

        try {
            HttpResponse response = client.execute(get);
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            log.error("Passive Node: Error occurred while connecting to Active Node using live state sync.", e);
            return 500;
        } finally {
            try {
                ((CloseableHttpClient) client).close();
            } catch (IOException e) {
                log.error("Passive Node: Error closing http client after sending request to Active Node to URI " + uri);
            }
        }
    }
}
