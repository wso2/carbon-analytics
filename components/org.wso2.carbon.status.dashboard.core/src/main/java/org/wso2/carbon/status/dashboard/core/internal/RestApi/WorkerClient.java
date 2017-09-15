package org.wso2.carbon.status.dashboard.core.internal.RestApi;


import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;


public interface WorkerClient {

    @RequestLine("GET /statistics")
    Response getAllWorkers();

    @Headers("Content-Type: application/json")
    @RequestLine("PUT /statistics")
    Response enableWorkerStatistics(String statEnable);

    @RequestLine("GET /system-details")
    Response getSystemDetails();

    @RequestLine("GET /siddhi-apps/statistics")
    Response getAllAppDetails();

    @RequestLine("GET /siddhi-apps/{appName}/statistics")
    Response getAppDetails(@Param("appName") String appName);

    @Headers("Content-Type: application/json")
    @RequestLine("PUT /siddhi-apps/{appName}/statistics")
    Response enableAppStatistics(@Param("appName") String appName, String statEnable);

    @Headers("Content-Type: application/json")
    @RequestLine("POST /login")
    Response testConnection();
}
