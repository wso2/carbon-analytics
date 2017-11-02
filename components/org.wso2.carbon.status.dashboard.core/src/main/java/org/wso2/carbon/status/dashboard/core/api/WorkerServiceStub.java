/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.status.dashboard.core.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;

/**
 * feign client for sending the request.
 */
// TODO: 11/2/17  Test for withot using basc auth header here
public interface WorkerServiceStub {

    @RequestLine("GET /statistics")
    @Headers("Content-Type: application/json")
    Response getWorker();

    @Headers("Content-Type: application/json")
    @RequestLine("PUT /statistics")
    Response enableWorkerStatistics(String statEnable);

    @RequestLine("GET /system-details")
    Response getSystemDetails();

    @Headers("Content-Type: application/json,Authorization: Basic YWRtaW46YWRtaW4=")
    @RequestLine("GET /siddhi-apps/statistics")
    Response getAllAppDetails();

    @Headers("Content-Type: application/json,Authorization: Basic YWRtaW46YWRtaW4=")
    @RequestLine("GET /siddhi-apps?isActive={isActive}")
    Response getSiddhiApps(@Param("isActive") boolean isActive);

    @Headers("Content-Type: application/json,Authorization: Basic YWRtaW46YWRtaW4=")
    @RequestLine("GET /siddhi-apps/{appName}")
    Response getSiddhiApp(@Param("appName") String appName);

    @RequestLine("GET /siddhi-apps/{appName}/statistics")
    Response getAppDetails(@Param("appName") String appName);

    @Headers("Content-Type: application/json,Authorization: Basic YWRtaW46YWRtaW4=")
    @RequestLine("PUT /siddhi-apps/{appName}/statistics")
    Response enableAppStatistics(@Param("appName") String appName, @Param("statsEnable") boolean statsEnable);

    @Headers("Content-Type: application/json")
    @RequestLine("POST /login")
    Response testConnection();
}
