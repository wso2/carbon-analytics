/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.errorhandler.util;

import org.wso2.carbon.siddhi.editor.core.internal.EditorDataHolder;

/**
 * Factory that is used to produce a HTTPS client for calling a SI Server.
 */
public class ErrorHandlerFactory {

    private static final int CLIENT_CONNECTION_TIMEOUT = 5000;
    private static final int CLIENT_READ_TIMEOUT = 5000;

    private ErrorHandlerFactory(){}

    /**
     * Returns an HTTPS client for communicating with the SI server.
     *
     * @param httpsUrl HTTPS URL of the SI server.
     * @param username Username.
     * @param password Password.
     * @return ErrorHandlerServiceStub instance which functions as the HTTPS client.
     */
    public static ErrorHandlerServiceStub getErrorHandlerHttpsClient(String httpsUrl, String username,
                                                                     String password) {
        return EditorDataHolder.getInstance().getClientBuilderService().build(username, password,
            CLIENT_CONNECTION_TIMEOUT, CLIENT_READ_TIMEOUT, ErrorHandlerServiceStub.class, httpsUrl);
    }
}
