/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util;

import feign.Response;
import org.wso2.carbon.streaming.integrator.core.internal.exception.ServiceCatalogueAPIServiceStubException;

import java.io.File;

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


    public static Response doGetKeyMd5s(String hostAndPort, String username, String password, String key)
        throws ServiceCatalogueAPIServiceStubException {
        return ServiceCatalogueHandlerFactory.getServiceCatalogueHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .doGetKeyMd5s(key);
    }

    public static Response uploadZip(File file, String verifier, String hostAndPort, String username, String password)
        throws ServiceCatalogueAPIServiceStubException {
        return ServiceCatalogueHandlerFactory.getServiceCatalogueHandlerHttpsClient2(generateURL(hostAndPort), username, password)
            .uploadZip(file, verifier);
    }

    public static Response deleteAsyncAPI(String serviceUUID, String hostAndPort, String username, String password)
        throws ServiceCatalogueAPIServiceStubException {
        return ServiceCatalogueHandlerFactory.getServiceCatalogueHandlerHttpsClient(generateURL(hostAndPort), username, password)
            .deleteAsyncAPI(serviceUUID);
    }
}
