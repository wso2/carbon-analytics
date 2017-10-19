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

package org.wso2.carbon.stream.processor.core.coordination.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

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
    public static String sendRequest(URI uri) {

        HttpResponse response;
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
        get.addHeader("Accept", "application/json");
        if (log.isDebugEnabled()) {
            log.debug("Passive Node: Sending GET request to Active Node to URI " + uri);
        }

        try {
            response = client.execute(get);
            if (response.getStatusLine().getStatusCode() != 200) {
                log.error("Failed in connection with active node using live state sync. HTTP error : "
                        + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                return "";
            }
        } catch (IOException e) {
            log.error("Passive Node: Error occurred while connecting to Active Node using live state sync."
                    , e);
            return "";
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())))) {
            String output;
            String content = null;
            while ((output = br.readLine()) != null) {
                content = output;
            }
            return content;

        } catch (IOException e) {
            log.error("Passive Node: Error occurred while reading response from Active Node using live" +
                    " state sync.", e);
        }
        return "";
    }

}
