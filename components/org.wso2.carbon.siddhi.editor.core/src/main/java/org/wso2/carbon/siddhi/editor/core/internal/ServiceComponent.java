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

package org.wso2.carbon.siddhi.editor.core.internal;


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.editor.core.commons.metadata.DebugCallbackEvent;
import org.wso2.carbon.siddhi.editor.core.commons.request.ValidationRequest;
import org.wso2.carbon.siddhi.editor.core.commons.response.DebugRuntimeResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.GeneralResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.MetaDataResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.Status;
import org.wso2.carbon.siddhi.editor.core.commons.response.ValidationSuccessResponse;
import org.wso2.carbon.siddhi.editor.core.util.MetaDataHolder;
import org.wso2.carbon.siddhi.editor.core.util.MimeMapper;
import org.wso2.carbon.siddhi.editor.core.util.SourceEditorUtils;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.debugger.SiddhiDebugger;
import org.wso2.siddhi.core.util.SiddhiComponentActivator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;


@Component(
        name = "editor-core-services",
        service = Microservice.class,
        immediate = true
)
@Path("/editor")
public class ServiceComponent implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private ServiceRegistration serviceRegistration;
    private ExecutorService executorService = Executors
            .newScheduledThreadPool(
                    5, new ThreadFactoryBuilder()
                            .setNameFormat("Debugger-scheduler-thread-%d")
                            .build()
            );

    private File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = this.getClass().getResource(resourcePath).openStream();
            if (in == null) {
                return null;
            }
            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);
            return tempFile;
        } catch (IOException e) {
            log.warn("Couldn't load requested resource: " + resourcePath);
            return null;
        }
    }

    @GET
    public Response handleRoot(@Context Request request) throws FileNotFoundException {
        return handleGet(request);
    }

    @GET
    @Path("/**")
    public Response handleGet(@Context Request request) throws FileNotFoundException {
        String rawUri = request.getUri().replaceFirst("^/editor", "");
        String rawUriPath, mimeType;
        if (rawUri == null || rawUri.trim().length() == 0 || "/".equals(rawUri)) {
            rawUriPath = "/index.html";
        } else {
            int uriPathEndIndex = rawUri.indexOf('?');
            if (uriPathEndIndex != -1) {
                // handling query Params.
                rawUriPath = rawUri.substring(0, uriPathEndIndex);
            } else {
                rawUriPath = rawUri;
            }
        }
        try {
            mimeType = MimeMapper.getMimeType(FilenameUtils.getExtension(rawUriPath));
        } catch (Throwable ignored) {
            mimeType = "text/plain";
        }
        mimeType = (mimeType == null) ? "text/plain" : mimeType;
        File file = getResourceAsFile("/web" + rawUriPath);
        if (file != null) {
            return Response.ok(new FileInputStream(file)).type(mimeType).build();
        }
        log.error(" File not found [" + rawUriPath + "], Requesting path [" + rawUriPath + "] ");
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/validator")
    public Response validateExecutionPlan(String validationRequestString) {
        ValidationRequest validationRequest = new Gson().fromJson(validationRequestString, ValidationRequest.class);
        String jsonString;

        try {
            ExecutionPlanRuntime executionPlanRuntime =
                    SourceEditorUtils.validateExecutionPlan(validationRequest.getExecutionPlan());

            // Status SUCCESS to indicate that the execution plan is valid
            ValidationSuccessResponse response = new ValidationSuccessResponse(Status.SUCCESS);

            // Getting requested inner stream definitions
            if (validationRequest.getMissingInnerStreams() != null) {
                response.setInnerStreams(SourceEditorUtils.getInnerStreamDefinitions(
                        executionPlanRuntime, validationRequest.getMissingInnerStreams()
                ));
            }

            // Getting requested stream definitions
            if (validationRequest.getMissingStreams() != null) {
                response.setStreams(SourceEditorUtils.getStreamDefinitions(
                        executionPlanRuntime, validationRequest.getMissingStreams()
                ));
            }
            jsonString = new Gson().toJson(response);
        } catch (Throwable t) {
            jsonString = new Gson().toJson(new GeneralResponse(Status.ERROR, t.getMessage()));
        }
        return Response.ok(jsonString, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Path("/save")
    public Response saveExecutionPlan(String saveConfig) {
        JSONObject jsonObject = new JSONObject(saveConfig);
        String executionPlan = "", filePath = "";
        try {
            if (jsonObject.has("executionPlan") && !jsonObject.getString("executionPlan").isEmpty()) {
                executionPlan = jsonObject.getString("executionPlan");
            } else {
                log.error("Execution Plan cannot be null or an empty value");
            }
            if (jsonObject.has("filePath") && !jsonObject.getString("filePath").isEmpty()) {
                filePath = jsonObject.getString("filePath");
            } else {
                log.error("File Path cannot be null or an empty value");
            }
            if (!executionPlan.isEmpty() && !filePath.isEmpty()) {
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filePath), "utf-8"))) {
                    writer.write(executionPlan);
                }
                return Response.ok().entity("ok")
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            }
        } catch (Throwable ignored) {
        }
        return Response.serverError().entity("failed")
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @GET
    @Path("/metadata")
    public Response getMetaData() {
        MetaDataResponse response = new MetaDataResponse(Status.SUCCESS);
        response.setInBuilt(MetaDataHolder.getInBuiltProcessorMetaData());
        response.setExtensions(SourceEditorUtils.getExtensionProcessorMetaData());
        String jsonString = new Gson().toJson(response);
        return Response.ok(jsonString, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Produces("application/json")
    @Path("/debug")
    public Response startDebug(String executionPlan) {
        String runtimeId = EditorDataHolder
                .getDebugProcessorService()
                .deployAndDebug(executionPlan);
        Set<String> streams = EditorDataHolder
                .getDebugProcessorService()
                .getRuntimeSpecificStreamsMap()
                .get(runtimeId);
        return Response
                .status(Response.Status.OK)
                .entity(new DebugRuntimeResponse(Status.SUCCESS, null, runtimeId, streams)).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{runtimeId}/stop")
    public Response stopDebug(@PathParam("runtimeId") String runtimeId) {
        EditorDataHolder
                .getDebugProcessorService()
                .stopAndUndeploy(runtimeId);
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse(Status.SUCCESS, "Runtime " + runtimeId +
                        " stopped successfully."))
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{runtimeId}/acquire")
    public Response acquireBreakPoint(@PathParam("runtimeId") String runtimeId,
                                      @QueryParam("queryName") String queryName,
                                      @QueryParam("queryTerminal") String queryTerminal) {
        if (queryName != null && queryTerminal != null && !queryName.isEmpty() && !queryTerminal.isEmpty()) {
            // acquire only specified break point
            SiddhiDebugger.QueryTerminal terminal = ("in".equalsIgnoreCase(queryTerminal)) ?
                    SiddhiDebugger.QueryTerminal.IN : SiddhiDebugger.QueryTerminal.OUT;
            EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiDebuggerMap()
                    .get(runtimeId)
                    .acquireBreakPoint(queryName, terminal);
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "Terminal " + queryTerminal +
                            " breakpoint acquired for query " + runtimeId + ":" + queryName))
                    .build();
        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new GeneralResponse(Status.ERROR, "Missing Parameters"))
                    .build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/{runtimeId}/poll")
    public Response pollState(@PathParam("runtimeId") String runtimeId) throws InterruptedException {
        DebugCallbackEvent event = EditorDataHolder
                .getDebugProcessorService()
                .getRuntimeSpecificCallbackMap()
                .get(runtimeId)
                .poll(5, TimeUnit.SECONDS);
        return Response
                .status(Response.Status.OK)
                .entity(event)
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{runtimeId}/release")
    public Response releaseBreakPoint(@PathParam("runtimeId") String runtimeId,
                                      @QueryParam("queryName") String queryName,
                                      @QueryParam("queryTerminal") String queryTerminal) {
        if (queryName == null || queryTerminal == null || queryName.isEmpty() || queryTerminal.isEmpty()) {
            // release all break points
            EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiDebuggerMap()
                    .get(runtimeId)
                    .releaseAllBreakPoints();
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "All breakpoints released for runtimeId " + runtimeId))
                    .build();
        } else {
            // release only specified break point
            SiddhiDebugger.QueryTerminal terminal = ("in".equalsIgnoreCase(queryTerminal)) ?
                    SiddhiDebugger.QueryTerminal.IN : SiddhiDebugger.QueryTerminal.OUT;
            EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiDebuggerMap()
                    .get(runtimeId)
                    .releaseBreakPoint(queryName, terminal);
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "Terminal " + queryTerminal +
                            " breakpoint released for query " + runtimeId + ":" + queryName))
                    .build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/{runtimeId}/next")
    public Response next(@PathParam("runtimeId") String runtimeId) {
        EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiDebuggerMap()
                .get(runtimeId)
                .next();
        return Response
                .status(Response.Status.OK)
                .entity("{'status':'ok'}")
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{runtimeId}/play")
    public Response play(@PathParam("runtimeId") String runtimeId) {
        EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiDebuggerMap()
                .get(runtimeId)
                .play();
        return Response
                .status(Response.Status.OK)
                .entity("{'status':'ok'}")
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{runtimeId}/{queryName}/state")
    public Response getQueryState(@PathParam("runtimeId") String runtimeId,
                                  @PathParam("queryName") String queryName) {
        return Response
                .status(Response.Status.OK)
                .entity(
                        EditorDataHolder
                                .getDebugProcessorService()
                                .getSiddhiDebuggerMap()
                                .get(runtimeId)
                                .getQueryState(queryName)
                ).build();
    }

    // TODO: 4/16/17 Integrate simulator instead of this
    @POST
    @Produces("application/json")
    @Path("/{runtimeId}/{streamId}/send")
    public Response mock(String data,
                         @PathParam("runtimeId") String runtimeId,
                         @PathParam("streamId") String streamId) {
        Gson gson = new Gson();
        Object[] event = gson.fromJson(data, Object[].class);
        executorService.execute(() -> {
            try {
                EditorDataHolder
                        .getDebugProcessorService()
                        .getRuntimeSpecificInputHandlerMap()
                        .get(runtimeId)
                        .get(streamId)
                        .send(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse(Status.SUCCESS, "Event " + Arrays.deepToString(event) +
                        " sent to stream " + streamId + " of runtime " + runtimeId))
                .build();
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
        log.info("Service Component is activated");

        // Create Stream Processor Service
        EditorDataHolder.setDebugProcessorService(new DebugProcessorService());
        EditorDataHolder.setSiddhiManager(new SiddhiManager());
        EditorDataHolder.setBundleContext(bundleContext);

        serviceRegistration = bundleContext.registerService(EventStreamService.class.getName(),
                new DebuggerEventStreamService(), null);
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Service Component is deactivated");
        Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = EditorDataHolder.
                getDebugProcessorService().getExecutionPlanRunTimeMap();
        executionPlanRunTimeMap.values().forEach(ExecutionPlanRuntime::shutdown);
        EditorDataHolder.setBundleContext(null);
        serviceRegistration.unregister();
    }

    /**
     * This bind method will be called when Siddhi ComponentActivator OSGi service is registered.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    @Reference(
            name = "siddhi.component.activator.service",
            service = SiddhiComponentActivator.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiComponentActivator"
    )
    protected void setSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {

    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    protected void unsetSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {

    }
}