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
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
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
@Component(
        name = "data-provider-api",
        service = Microservice.class,
        immediate = true
)
@Path("/portal/apis/data-provider")
@RequestInterceptor(AuthenticationInterceptor.class)
public class DataProviderAPI implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(DataProviderAPI.class);

    public DataProviderAPI() {
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.info("Data Provider Service Component is activated");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Data Provider Service Component is deactivated");
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    public Response dataProviderList() {
        try {
            return Response.status(Response.Status.OK)
                    .entity(getDataProviderHelper().getDataProviderNameSet())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Throwable throwable) {
            log.error("Server error occurred when listing data providers.", throwable);
            return Response.serverError().entity("Server error occurred when listing data providers: " +
                    throwable.getMessage()).build();
        }

    }

    @GET
    @Path("/{providerName}/config")
    @Produces("application/json")
    public Response dataProviderConfig(@PathParam("providerName") String providerName) {
        try {
            return Response.status(Response.Status.OK)
                    .entity(getDataProviderHelper().getDataProvider(providerName).providerConfig())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Throwable throwable) {
            log.error("Server error occurred when retrieving configuration of data provider: '{}'.",
                    replaceCRLFCharacters(providerName), throwable);
            return Response.serverError().entity("Server error occurred when when retrieving configuration " +
                    "of data provider: '" + providerName + "'. " + throwable.getMessage()).build();
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
            return Response.ok(dataProvider.dataSetMetadata(), MediaType.APPLICATION_JSON)
                    .build();
        } catch (DataProviderException | IllegalAccessException | InstantiationException e) {
            log.error("Server error occurred when validating the provider name: '{}'.",
                    replaceCRLFCharacters(providerName), e);
            return Response.serverError().entity("Server error occurred when validating the provider name: '" +
                    providerName + "'. " + e.getMessage()).build();
        }
    }

    @Reference(
            name = "data-providers-service",
            service = DataProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonTransport"
    )
    protected void setCarbonTransport(DataProvider dataProvider) {
        getDataProviderHelper().setDataProvider(dataProvider.providerName(), dataProvider);
    }

    protected void unsetCarbonTransport(DataProvider dataProvider) {
        getDataProviderHelper().removeDataProviderClass(dataProvider.providerName());
    }

    private String replaceCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }
}
