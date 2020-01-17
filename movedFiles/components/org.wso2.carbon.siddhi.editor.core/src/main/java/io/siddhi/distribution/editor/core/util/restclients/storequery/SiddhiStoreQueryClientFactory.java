/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package io.siddhi.distribution.editor.core.util.restclients.storequery;

import io.siddhi.distribution.editor.core.internal.EditorDataHolder;

/**
 * Factory used to create a HTTP client for Siddhi Query APIs.
 */
public class SiddhiStoreQueryClientFactory {

    private static final int CLIENT_CONNECTION_TIMEOUT = 5000;
    private static final int CLIENT_READ_TIMEOUT = 5000;

    /**
     * Returns an HTTP client for executing Siddhi store queries.
     *
     * @param url      HTTP URL of the Store API
     * @param username Username
     * @param password Password
     * @return SiddhiStoreQueryServiceStub instance which acts as the HTTPS client
     */
    public static SiddhiStoreQueryServiceStub getStoreQueryHTTPClient(String url, String username, String password) {

        return EditorDataHolder
                .getInstance()
                .getClientBuilderService()
                .build(username, password, CLIENT_CONNECTION_TIMEOUT, CLIENT_READ_TIMEOUT,
                        SiddhiStoreQueryServiceStub.class, url);
    }

    public static SiddhiStoreQueryServiceStub getStoreQueryHTTPClient(String url) {

        return getStoreQueryHTTPClient(url, "", "");
    }
}
