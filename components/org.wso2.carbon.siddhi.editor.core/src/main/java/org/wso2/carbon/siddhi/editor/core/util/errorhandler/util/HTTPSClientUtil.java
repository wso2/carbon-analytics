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

import com.google.gson.JsonArray;
import feign.Response;
import org.wso2.carbon.siddhi.editor.core.exception.ErrorHandlerServiceStubException;

/**
 * Contains HTTPS client related methods.
 */
public class HTTPSClientUtil {

    private static final String PROTOCOL = "https";

    /**
     * Avoids Instantiation.
     */
    private HTTPSClientUtil() {

    }

    /**
     * Generates an HTTPS URL with the given hostAndPort.
     *
     * @param hostAndPort Host and Port of the Worker node in {Host}:{Port} format
     * @return HTTPS URL
     */
    private static String generateURL(String hostAndPort) {

        return PROTOCOL + "://" + hostAndPort;
    }


    public static Response doGetSiddhiAppList(String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetSiddhiAppList();
    }

    public static Response doGetStatus(String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetStatus();
    }

    public static Response doGetErrorEntries(String hostAndPort, String username, String password,
                                             String siddhiAppName) throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetErrorEntries(siddhiAppName);
    }

    public static Response doReplay(String hostAndPort, String username, String password, String payload)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doReplay(payload);
    }

    public static Response doDeleteErroneousEvent(String hostAndPort, String username, String password,
                                                  String erroneousEventId) throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doDeleteErroneousEvent(erroneousEventId);
    }
}
