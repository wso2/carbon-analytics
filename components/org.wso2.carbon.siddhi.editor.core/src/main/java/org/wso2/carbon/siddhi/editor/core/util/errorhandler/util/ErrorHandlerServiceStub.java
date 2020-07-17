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
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.wso2.carbon.siddhi.editor.core.exception.ErrorHandlerServiceStubException;

/**
 * Feign client for sending requests.
 */
public interface ErrorHandlerServiceStub {

    // TODO remove these references
//    @RequestLine("POST /siddhi-apps")
//    @Headers("Content-Type: text/plain; charset=utf-8")
//    Response doPostRequest(String payload) throws SiddhiAppDeployerServiceStubException;
//
//    @RequestLine("PUT /siddhi-apps")
//    @Headers("Content-Type: text/plain; charset=utf-8")
//    Response doPutRequest(String payload) throws SiddhiAppDeployerServiceStubException;
//
//    @RequestLine("GET /siddhi-apps/{appName}")
//    @Headers("Content-Type: application/json; charset=utf-8")
//    Response doGetSiddhiApp(@Param("appName") String appName) throws SiddhiAppDeployerServiceStubException;
//
//    @RequestLine("DELETE /siddhi-apps/{appName}")
//    @Headers("Content-Type: text/plain; charset=utf-8")
//    Response doDeleteRequest(@Param("appName") String appName) throws SiddhiAppDeployerServiceStubException;
//
//    @RequestLine("GET /siddhi-apps/{appName}/status")
//    @Headers("Content-Type: text/plain; charset=utf-8")
//    Response doGetRequest(@Param("appName") String appName) throws SiddhiAppDeployerServiceStubException;
//
//    @RequestLine("GET /siddhi-apps")
//    @Headers("Content-Type: application/json; charset=utf-8")
//    Response doGetSiddhiAppList() throws SiddhiAppDeployerServiceStubException;
//
//    @RequestLine("GET /siddhi-apps/count")
//    @Headers("Content-Type: application/json; charset=utf-8")
//    Response doGetSiddhiAppCount() throws SiddhiAppDeployerServiceStubException;
//
//    @RequestLine("GET /siddhi-apps/{appName}/isExists")
//    @Headers("Content-Type: application/json; charset=utf-8")
//    Response doGetSiddhiAppAvailability(@Param("appName") String appName) throws SiddhiAppDeployerServiceStubException;

    @RequestLine("GET /siddhi-apps")
    @Headers("Content-Type: application/json; charset=utf-8")
    Response doGetSiddhiAppList() throws ErrorHandlerServiceStubException;

//    @RequestLine("GET /error-handler/status") // TODO remove this
//    @Headers("Content-Type: application/json; charset=utf-8")
//    Response doGetStatus() throws ErrorHandlerServiceStubException;

    @RequestLine("GET /error-handler/error-entries/count")
    @Headers("Content-Type: application/json; charset=utf-8")
    Response doGetTotalErrorEntriesCount() throws ErrorHandlerServiceStubException;

    @RequestLine("GET /error-handler/error-entries/count?siddhiApp={siddhiAppName}")
    @Headers("Content-Type: application/json; charset=utf-8")
    Response doGetErrorEntriesCount(@Param("siddhiAppName") String siddhiAppName)
        throws ErrorHandlerServiceStubException;

    @RequestLine("GET /error-handler/error-entries?siddhiApp={siddhiAppName}&limit={limit}&offset={offset}")
    @Headers("Content-Type: application/json; charset=utf-8")
    Response doGetMinimalErrorEntries(@Param("siddhiAppName") String siddhiAppName, @Param("limit") String limit,
                                      @Param("offset") String offset) throws ErrorHandlerServiceStubException;

    @RequestLine("GET /error-handler/error-entries/{id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    Response doGetDescriptiveErrorEntry(@Param("id") String id) throws ErrorHandlerServiceStubException;

    @RequestLine("POST /error-handler")
    @Headers("Content-Type: application/json; charset=utf-8")
    Response doReplay(String payload) throws ErrorHandlerServiceStubException;

    @RequestLine("DELETE /error-handler/error-entries/{id}")
    @Headers("Content-Type: text/plain; charset=utf-8")
    Response doDiscardErrorEntry(@Param("id") String errorEntryId) throws ErrorHandlerServiceStubException;

    @RequestLine("DELETE /error-handler/error-entries?siddhiApp={siddhiAppName}")
    @Headers("Content-Type: text/plain; charset=utf-8")
    Response doDiscardErrorEntries(@Param("siddhiAppName") String siddhiAppName)
        throws ErrorHandlerServiceStubException;

    @RequestLine("DELETE /error-handler/error-entries?retentionDays={retentionDays}")
    @Headers("Content-Type: text/plain; charset=utf-8")
    Response doPurge(@Param("retentionDays") int retentionDays) throws ErrorHandlerServiceStubException;
}
