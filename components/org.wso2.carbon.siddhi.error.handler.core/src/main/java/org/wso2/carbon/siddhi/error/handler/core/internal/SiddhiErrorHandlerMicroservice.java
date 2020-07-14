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

package org.wso2.carbon.siddhi.error.handler.core.internal;

import com.google.gson.JsonArray;
import io.siddhi.core.util.error.handler.store.ErrorStore;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.error.handler.core.exception.SiddhiErrorHandlerException;
import org.wso2.carbon.siddhi.error.handler.core.execution.ErrorStoreAccessor;
import org.wso2.carbon.siddhi.error.handler.core.execution.RePlayer;
import org.wso2.carbon.siddhi.error.handler.core.util.SiddhiErrorHandlerUtils;
import org.wso2.carbon.streaming.integrator.common.ErrorStoreListener;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Exposes Siddhi Error Handler as a micro-service.
 */
@Component(
    name = "org.wso2.carbon.siddhi.error.handler.core.internal.SiddhiErrorHandlerMicroservice",
    service = {ErrorStoreListener.class, Microservice.class},
    immediate = true
)
@Path("/error-handler")
public class SiddhiErrorHandlerMicroservice implements ErrorStoreListener, Microservice {

    private static final Logger logger = LoggerFactory.getLogger(SiddhiErrorHandlerMicroservice.class);

    private static final String ENTRIES_COUNT_KEY = "entriesCount";

    @GET
    @Path("/error-entries/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErrorEntriesCount(@QueryParam("siddhiApp") String siddhiAppName) {
        try {
            Map<String, Integer> response = new HashMap<>();
            response.put(ENTRIES_COUNT_KEY, (siddhiAppName != null) ?
                ErrorStoreAccessor.getErrorEntriesCount(siddhiAppName) :
                ErrorStoreAccessor.getTotalErrorEntriesCount());
            return Response.ok().entity(response).type(MediaType.APPLICATION_JSON).build();
        } catch (SiddhiErrorHandlerException e) {
            String message = "Failed to get error entries count.";
            logger.error(message, e);
            return Response.serverError().entity(message).build();
        }
    }

    @GET
    @Path("/error-entries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErrorEntries(@QueryParam("siddhiApp") String siddhiAppName,
                                    @QueryParam("descriptive") String isDescriptive,
                                    @QueryParam("limit") String limit, @QueryParam("offset") String offset) {
        if (siddhiAppName != null) {
            try {
                return Response.ok().
                    entity(ErrorStoreAccessor.getErrorEntries(siddhiAppName, isDescriptive, limit, offset))
                    .type(MediaType.APPLICATION_JSON).build();
            } catch (SiddhiErrorHandlerException e) {
                logger.error("Failed to get erroneous events.", e);
                return Response.serverError().entity("Failed to get erroneous events.").build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Mandatory query parameter 'siddhiApp' is not found.").build();
        }
    }

    @GET
    @Path("/error-entries/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErrorEntry(@PathParam("id") int id) {
        try {
            return Response.ok().entity(ErrorStoreAccessor.getErrorEntry(id)).type(MediaType.APPLICATION_JSON).build();
        } catch (SiddhiErrorHandlerException e) {
            logger.error("Failed to get erroneous event.", e);
            return Response.serverError().entity("Failed to get erroneous event.").build();
        }
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response rePlayErrorEntries(JsonArray errorEntriesBody) {
        try {
            RePlayer.rePlay(SiddhiErrorHandlerUtils.convertToList(errorEntriesBody));
            return Response.ok().build();
        } catch (SiddhiErrorHandlerException e) {
            logger.error("Failed to re-stream errors.", e);
            return Response.serverError().entity("Failed to re-stream errors.").build();
        }
    }

    @DELETE
    @Path("/error-entries/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response discardErrorEntry(@PathParam("id") int id) {
        try {
            ErrorStoreAccessor.discardErrorEntry(id);
            return Response.ok().build();
        } catch (SiddhiErrorHandlerException e) {
            String message = "Failed to discard error entry with id: " + id + " .";
            logger.error(message);
            return Response.serverError().entity(message).build();
        }
    }

    @DELETE
    @Path("/error-entries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response discardErrorEntries(@QueryParam("siddhiApp") String siddhiAppName) {
        try {
            if (siddhiAppName != null) {
                ErrorStoreAccessor.discardErrorEntries(siddhiAppName);
            } else {
                ErrorStoreAccessor.purgeErrorStore(Collections.emptyMap()); // TODO retention policy
            }
            ErrorStoreAccessor.discardErrorEntries(siddhiAppName);
            return Response.ok().build();
        } catch (SiddhiErrorHandlerException e) {
            String message = "Failed to discard error entries.";
            logger.error(message);
            return Response.serverError().entity(message).build();
        }
    }

    /**
     * This is the activation method of SiddhiErrorHandlerMicroservice.
     * This will be called when its references are satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception Error occurred while executing the activate method.
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

    }

    /**
     * This is the deactivation method of SiddhiErrorHandlerMicroservice.
     * This will be called when this component is being stopped or references are satisfied during runtime.
     *
     * @throws Exception Error occurred while executing the de-activate method.
     */
    @Deactivate
    protected void stop() throws Exception {

    }

    @Override
    public void onInitialize(ErrorStore errorStore) {
        SiddhiErrorHandlerDataHolder.getInstance().setErrorStore(errorStore);
    }
}
