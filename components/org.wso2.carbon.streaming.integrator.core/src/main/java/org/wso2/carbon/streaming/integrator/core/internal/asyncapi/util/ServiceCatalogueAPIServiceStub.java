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

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.wso2.carbon.streaming.integrator.core.internal.exception.ServiceCatalogueAPIServiceStubException;

import java.io.File;

/**
 * Feign client for sending requests of the Error Handler.
 */
public interface ServiceCatalogueAPIServiceStub {

    @RequestLine("GET /api/am/service-catalog/v1/services?key={key}&shrink=true")
    @Headers("Content-Type: application/json; charset=utf-8")
    Response doGetKeyMd5s(@Param("key") String key) throws ServiceCatalogueAPIServiceStubException;

    @RequestLine("POST api/am/service-catalog/v1/services/import?overwrite=true")
    @Headers("Content-Type: multipart/form-data; charset=utf-8")
    Response uploadZip(@Param("file") File file, @Param("verifier") String verifier) throws ServiceCatalogueAPIServiceStubException;

    @RequestLine("DELETE /api/am/service-catalog/v1/services/{serviceUUID}")
    @Headers("Content-Type: text/plain; charset=utf-8")
    Response deleteAsyncAPI(@Param("serviceUUID") String serviceUUID) throws ServiceCatalogueAPIServiceStubException;
}
