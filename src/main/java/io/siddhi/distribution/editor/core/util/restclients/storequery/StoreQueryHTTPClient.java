/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.siddhi.distribution.editor.core.util.restclients.storequery;

import feign.Response;

/**
 * HTTP client to access the Siddhi store query REST API.
 */
public class StoreQueryHTTPClient {
    private static final String PROTOCOL = "http://";

    /**
     * Avoids Instantiation of the default constructor.
     */
    private StoreQueryHTTPClient() {
    }

    /**
     * Executes an Store query.
     *
     * @param host    Host and the port of the worker node in {host}:{port} format
     * @param payload Payload
     */
    public static Response executeStoreQuery(String host, String payload) {
        return SiddhiStoreQueryClientFactory
                .getStoreQueryHTTPClient(PROTOCOL + host)
                .executeStoreQuery(payload);
    }
}
