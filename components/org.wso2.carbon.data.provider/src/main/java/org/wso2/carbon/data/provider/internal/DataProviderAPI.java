/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.data.provider.internal;

import com.google.gson.JsonElement;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.data.provider.exception.DataProviderException;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.data.provider.utils.DataProviderValueHolder.getDataProviderHelper;

/**
 * Data provider api service component.
 */
@RequestInterceptor(AuthenticationInterceptor.class)
public class DataProviderAPI implements Microservice {

    public static final String API_CONTEXT_PATH = "/apis/data-provider";
    private static final Logger log = LoggerFactory.getLogger(DataProviderAPI.class);

    @GET
    @Path("/list")
    @Produces("application/json")
    public Response dataProviderList() {
        try {
            return Response.ok()
                    .entity(getDataProviderHelper().getDataProviderNameSet())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            log.error("Cannot list data providers.", e);
            return Response.serverError()
                    .entity("Server error occurred when listing data providers.")
                    .build();
        }
    }

    @GET
    @Path("/{providerName}/config")
    @Produces("application/json")
    public Response dataProviderConfig(@PathParam("providerName") String providerName) {
        try {
            return Response.ok()
                    .entity(getDataProviderHelper().getDataProvider(providerName).providerConfig())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("Cannot retrieve configuration of data provider '{}'.", replaceCRLFCharacters(providerName), e);
            return Response.serverError()
                    .entity("Server error occurred when when retrieving configuration of data provider '" +
                            providerName + "'. ")
                    .build();
        }

    }

    @POST
    @Path("/{providerName}/validate")
    @Produces("application/json")
    @Consumes("application/json")
    public Response validateProviderConfig(JsonElement dataProviderConfig, @PathParam("providerName")
            String providerName) {
        try {
            DataProvider dataProvider = getDataProviderHelper().getDataProvider(providerName);
            //mock the topic and session as it only needs to validate the configuration.
            dataProvider.init("mock_topic", "mock_session", dataProviderConfig);
            return Response.ok()
                    .entity(dataProvider.dataSetMetadata())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (DataProviderException | IllegalAccessException | InstantiationException e) {
            log.error("Cannot validate configurations '{}' for data provider '{}'.", dataProviderConfig,
                      replaceCRLFCharacters(providerName), e);
            return Response.serverError()
                    .entity("Server error occurred when validating configuration for data provider '" + providerName +
                            "'.")
                    .build();
        }
    }

    private String replaceCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }
}
