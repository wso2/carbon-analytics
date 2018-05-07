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
import org.wso2.carbon.status.dashboard.core.model.StatsEnable;

/**
 * feign client for sending the request.
 */

public interface WorkerServiceStub {
    
    @RequestLine("GET /statistics")
    @Headers("Content-Type: application/json")
    Response getWorker();
    
    @Headers("Content-Type: application/json")
    @RequestLine("PUT /statistics")
    Response enableWorkerStatistics(String statEnable);
    
    @RequestLine("GET /system-details")
    Response getSystemDetails();
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /siddhi-apps/statistics")
    Response getAllAppDetails();
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /siddhi-apps?isActive={isActive}")
    Response getSiddhiApps(@Param("isActive") boolean isActive);
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /siddhi-apps/{appName}")
    Response getSiddhiApp(@Param("appName") String appName);
    
    @RequestLine("GET /siddhi-apps/{appName}/statistics")
    Response getAppDetails(@Param("appName") String appName);
    
    @Headers("Content-Type: application/json")
    @RequestLine("PUT /siddhi-apps/{appName}/statistics")
    Response enableAppStatistics(@Param("appName") String appName, StatsEnable statsEnable);
    
    @Headers("Content-Type: application/json")
    @RequestLine("POST /login")
    Response testConnection();
    
    //APIS related to distributed view
    @Headers("Content-Type: application/json")
    @RequestLine("GET /runTime")
    Response getRunTime();
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /managers")
    Response getManagerDetails();
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /managers/siddhi-apps")
    Response getSiddhiApps();
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /resourceClusterWorkers")
    Response getResourceClusterWorkers();
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /managers/siddhi-apps-execution/{appName}")
    Response getManagerSiddhiAppTextView(@Param("appName") String appName);
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /managers/siddhi-apps/{appName}")
    Response getChildAppDetails(@Param("appName") String appName);
    
    @Headers("Content-Type: application/json")
    @RequestLine("GET /managers/kafkaDetails/{appName}")
    Response getKafkaDetails(@Param("appName") String appName);
    
}
