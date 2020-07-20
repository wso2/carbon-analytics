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

import feign.Response;
import org.wso2.carbon.siddhi.editor.core.exception.ErrorHandlerServiceStubException;

/**
 * Contains HTTPS client related methods for the Error Handler.
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
     * @param hostAndPort Host and Port of the Server node in {Host}:{Port} format.
     * @return HTTPS URL.
     */
    private static String generateURL(String hostAndPort) {
        return PROTOCOL + "://" + hostAndPort;
    }


    public static Response doGetSiddhiAppList(String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetSiddhiAppList();
    }

    public static Response doGetTotalErrorEntriesCount(String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetTotalErrorEntriesCount();
    }

    public static Response doGetErrorEntriesCount(String siddhiAppName, String hostAndPort, String username,
                                                  String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetErrorEntriesCount(siddhiAppName);
    }

    public static Response doGetMinimalErrorEntries(String siddhiAppName, String limit, String offset,
                                                    String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetMinimalErrorEntries(siddhiAppName, limit, offset);
    }

    public static Response doGetDescriptiveErrorEntry(String id, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetDescriptiveErrorEntry(id);
    }

    public static Response doReplay(String payload, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doReplay(payload);
    }

    public static Response doDiscardErrorEntry(String id, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doDiscardErrorEntry(id);
    }

    public static Response doDiscardErrorEntries(String siddhiAppName, String hostAndPort, String username,
                                                 String password) throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doDiscardErrorEntries(siddhiAppName);
    }

    public static Response doPurge(int retentionDays, String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        return ErrorHandlerFactory.getErrorHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doPurge(retentionDays);
    }
}
