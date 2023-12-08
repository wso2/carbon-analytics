/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.siddhi.store.api.rest;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AnalyticsResponseInterceptor;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.siddhi.store.api.rest.factories.StoresApiServiceFactory;
import org.wso2.carbon.siddhi.store.api.rest.model.ModelApiResponse;
import org.wso2.carbon.siddhi.store.api.rest.model.Query;
import org.wso2.carbon.siddhi.store.api.rest.model.InitAggregationDTO;
import org.wso2.carbon.streaming.integrator.common.HAStateChangeListener;
import org.wso2.carbon.streaming.integrator.common.SiddhiAppRuntimeService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.util.Set;

@Component(
        name = "siddhi-store-query-service",
        service = HAStateChangeListener.class,
        immediate = true
)
@Path("/stores")
@io.swagger.annotations.Api(description = "The stores API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-11-01T11:26:25.925Z")
public class StoresApi implements HAStateChangeListener {
    private Logger log = LoggerFactory.getLogger(StoresApi.class);
    private final StoresApiService delegate = StoresApiServiceFactory.getStoresApi();
    private static TransportsConfiguration transportsConfiguration;
    private static MicroservicesRunner microservicesRunner;
    private static volatile boolean microserviceActive;
    private static final String ROOT_CONFIG_ELEMENT = "siddhi.stores.query.api";

    @POST
    @Path("/initAgg")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Submit Siddhi app & aggregation name to initizalize it externally",
         notes = "", response = ModelApiResponse.class, tags = {"store",})
    @io.swagger.annotations.ApiResponses(value = {
         @io.swagger.annotations.ApiResponse(code = 200, message = "OK, Request was successfully submitted",
                 response = ModelApiResponse.class),
            @io.swagger.annotations.ApiResponse(code = 405, message = "Invalid input",
                 response = ModelApiResponse
                         .class)})
     public Response initAgg(@ApiParam(value = "Object which contains app and the aggregation to be initialized",
            required = true) InitAggregationDTO body)
            throws NotFoundException {
     return delegate.initAgg(body);
      }

    @POST
    @Path("/query")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Submit a Siddhi query and get the result records from a store",
            notes = "", response = ModelApiResponse.class, tags = {"store",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK, query was successfully submitted",
                    response = ModelApiResponse.class),

            @io.swagger.annotations.ApiResponse(code = 405, message = "Invalid input",
                    response = ModelApiResponse
                            .class)})
    public Response query(@ApiParam(value = "Query object which contains the query which returns the store records",
            required = true) Query body)
            throws NotFoundException {
        return delegate.query(body);
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
        microservicesRunner = new MicroservicesRunner(transportsConfiguration);
        if (SiddhiStoreDataHolder.getInstance().getAuthenticationInterceptor() != null) {
            microservicesRunner.addGlobalRequestInterceptor(SiddhiStoreDataHolder.getInstance().
                    getAuthenticationInterceptor());
        }
        if (SiddhiStoreDataHolder.getInstance().getAnalyticsResponseInterceptor() != null) {
            microservicesRunner.addGlobalResponseInterceptor(SiddhiStoreDataHolder.getInstance().
                    getAnalyticsResponseInterceptor());
        }
        microservicesRunner.deploy(new StoresApi());
        startStoresApiMicroservice();
        log.debug("Siddhi Store REST API activated.");
    }

    /**
     * This is the activation method of Stores Api Microservice.
     */
    public static void startStoresApiMicroservice() {
        if (microservicesRunner != null && !microserviceActive) {
            microservicesRunner.start();
            microserviceActive = true;
        }
    }

    /**
     * This is the deactivate method of Stores Api Microservice.
     */
    public static void stopStoresApiMicroservice() {
        if (microservicesRunner != null && microserviceActive) {
            microservicesRunner.stop();
            microserviceActive = false;
        }
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.debug("Siddhi Store REST API deactivated.");
        stopStoresApiMicroservice();
    }

    @Reference(
            name = "siddhi.app.runtime.service.reference",
            service = SiddhiAppRuntimeService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiAppRuntimeService"
    )
    protected void setSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
        SiddhiStoreDataHolder.getInstance().setSiddhiAppRuntimeService(siddhiAppRuntimeService);
    }

    protected void unsetSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
        SiddhiStoreDataHolder.getInstance().setSiddhiAppRuntimeService(null);
    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        SiddhiStoreDataHolder.getInstance().setConfigProvider(configProvider);
        try {
            transportsConfiguration = configProvider.getConfigurationObject(ROOT_CONFIG_ELEMENT,
                    TransportsConfiguration.class);
        } catch (ConfigurationException e) {
            log.error("Error while loading TransportsConfiguration for " + ROOT_CONFIG_ELEMENT, e);
        }
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        SiddhiStoreDataHolder.getInstance().setConfigProvider(null);
    }

    @Reference(
            name = "org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor",
            service = AuthenticationInterceptor.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAuthenticationInterceptor"
    )
    protected void registerAuthenticationInterceptor(AuthenticationInterceptor authenticationInterceptor) {
        SiddhiStoreDataHolder.getInstance().setAuthenticationInterceptor(authenticationInterceptor);
    }

    protected void unregisterAuthenticationInterceptor(AuthenticationInterceptor authenticationInterceptor) {
        SiddhiStoreDataHolder.getInstance().setAuthenticationInterceptor(null);
    }

    @Reference(
            name = "org.wso2.carbon.analytics.msf4j.interceptor.common.AnalyticsResponseInterceptor",
            service = AnalyticsResponseInterceptor.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAnalyticsResponseInterceptor"
    )
    protected void registerAnalyticsResponseInterceptor(AnalyticsResponseInterceptor analyticsResponseInterceptor) {
        SiddhiStoreDataHolder.getInstance().setAnalyticsResponseInterceptor(analyticsResponseInterceptor);
    }

    protected void unregisterAnalyticsResponseInterceptor(AnalyticsResponseInterceptor analyticsResponseInterceptor) {
        SiddhiStoreDataHolder.getInstance().setAnalyticsResponseInterceptor(null);
    }

    @Override
    public void becameActive() {
        startStoresApiMicroservice();
    }

    @Override
    public void becamePassive() {
        stopStoresApiMicroservice();
    }
}
