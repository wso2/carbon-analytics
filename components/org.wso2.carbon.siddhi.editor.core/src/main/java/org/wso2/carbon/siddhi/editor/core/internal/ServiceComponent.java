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
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.siddhi.editor.core.Workspace;
import org.wso2.carbon.siddhi.editor.core.commons.metadata.MetaData;
import org.wso2.carbon.siddhi.editor.core.commons.request.ValidationRequest;
import org.wso2.carbon.siddhi.editor.core.commons.response.DebugRuntimeResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.GeneralResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.MetaDataResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.Status;
import org.wso2.carbon.siddhi.editor.core.commons.response.ValidationSuccessResponse;
import org.wso2.carbon.siddhi.editor.core.internal.local.LocalFSWorkspace;
import org.wso2.carbon.siddhi.editor.core.util.Constants;
import org.wso2.carbon.siddhi.editor.core.util.DebugCallbackEvent;
import org.wso2.carbon.siddhi.editor.core.util.DebugStateHolder;
import org.wso2.carbon.siddhi.editor.core.util.MimeMapper;
import org.wso2.carbon.siddhi.editor.core.util.SourceEditorUtils;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.common.utils.config.FileConfigManager;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.debugger.SiddhiDebugger;
import org.wso2.siddhi.core.util.SiddhiComponentActivator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.google.gson.JsonObject;


@Component(
        name = "editor-core-services",
        service = Microservice.class,
        immediate = true
)
@Path("/editor")
public class ServiceComponent implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String STATUS = "status";
    private static final String SUCCESS = "success";
    private ServiceRegistration serviceRegistration;
    private Workspace workspace;
    private ExecutorService executorService = Executors
            .newScheduledThreadPool(
                    5, new ThreadFactoryBuilder()
                            .setNameFormat("Debugger-scheduler-thread-%d")
                            .build()
            );
    private ConfigProvider configProvider;

    public ServiceComponent() {
        workspace = new LocalFSWorkspace();
    }

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
        } catch (Exception e) {
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
                .build();
    }

    @GET
    @Path("/workspace/root")
    @Produces("application/json")
    public Response root() {
        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.listRoots())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }

    }

    @GET
    @Path("/workspace/listFilesInPath")
    @Produces("application/json")
    public Response listFilesInPath(@QueryParam("path") String path) {
        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.listDirectoryFiles(new String(Base64.getDecoder().decode(path))))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }

    }

    @GET
    @Path("/workspace/list")
    @Produces("application/json")
    public Response directoriesInPath(@QueryParam("path") String path) {
        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.listDirectoriesInPath(new String(Base64.getDecoder().decode(path))))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @GET
    @Path("/workspace/exists")
    @Produces("application/json")
    public Response fileExists(@QueryParam("path") String path) {
        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.exists(new String(Base64.getDecoder().decode(path))))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @POST
    @Path("/workspace/exists/workspace")
    @Produces("application/json")
    public Response fileExistsAtWorkspace(String payload) {
        try {
            String configName = "";
            String[] splitConfigContent = payload.split("configName=");
            if (splitConfigContent.length > 1) {
                configName = splitConfigContent[1];
            }
            byte[] base64ConfigName = Base64.getDecoder().decode(configName);
            String location = (Paths.get(Constants.CARBON_HOME,
                    Constants.DIRECTORY_DEPLOYMENT,
                    Constants.DIRECTORY_WORKSPACE)).toString();
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(location).append(System.getProperty(FILE_SEPARATOR)).append(new String(base64ConfigName));
            return Response.status(Response.Status.OK)
                    .entity(workspace.exists(pathBuilder.toString()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @GET
    @Path("/workspace/listFiles")
    @Produces("application/json")
    public Response filesInPath(@QueryParam("path") String path) {
        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.listFilesInPath(new String(Base64.getDecoder().decode(path))))
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @POST
    @Path("/workspace/write")
    @Produces("application/json")
    public Response write(String payload) {
        try {
            String location = (Paths.get(Constants.CARBON_HOME,
                    Constants.DIRECTORY_DEPLOYMENT,
                    Constants.DIRECTORY_WORKSPACE)).toString();
            String configName = "";
            String config = "";
            Matcher configNameMatcher = Pattern.compile("configName=(.*?)&").matcher(payload);
            while (configNameMatcher.find()) {
                configName = configNameMatcher.group(1);
            }
            String[] splitConfigContent = payload.split("config=");
            if (splitConfigContent.length > 1) {
                config = splitConfigContent[1];
            }
            byte[] base64Config = Base64.getDecoder().decode(config);
            byte[] base64ConfigName = Base64.getDecoder().decode(configName);
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(location).append(System.getProperty(FILE_SEPARATOR)).append(new String(base64ConfigName));
            Files.write(Paths.get(pathBuilder.toString()), base64Config);
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            entity.addProperty("path", location);
            return Response.status(Response.Status.OK).entity(entity)
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @POST
    @Path("/workspace/export")
    @Produces("application/json")
    public Response export(String payload) {
        try {
            String location = "";
            String configName = "";
            String config = "";
            Matcher locationMatcher = Pattern.compile("location=(.*?)&configName").matcher(payload);
            while (locationMatcher.find()) {
                location = locationMatcher.group(1);
            }
            Matcher configNameMatcher = Pattern.compile("configName=(.*?)&").matcher(payload);
            while (configNameMatcher.find()) {
                configName = configNameMatcher.group(1);
            }
            String[] splitConfigContent = payload.split("config=");
            if (splitConfigContent.length > 1) {
                config = splitConfigContent[1];
            }
            byte[] base64Config = Base64.getDecoder().decode(config);
            byte[] base64ConfigName = Base64.getDecoder().decode(configName);
            byte[] base64Location = Base64.getDecoder().decode(location);
            Files.write(Paths.get(new String(base64Location) + System.getProperty(FILE_SEPARATOR)
                    + new String(base64ConfigName)), base64Config);
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return Response.status(Response.Status.OK).entity(entity)
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @POST
    @Path("/workspace/read")
    @Produces("application/json")
    public Response read(String path) {
        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.read(path))
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @POST
    @Path("/workspace/read/sample")
    @Produces("application/json")
    public Response readSample(String path) {
        try {
            String sampleAbsoluteLocation = (Paths.get(Constants.CARBON_HOME,path)).toString();
            return Response.status(Response.Status.OK)
                    .entity(workspace.read(sampleAbsoluteLocation))
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @POST
    @Path("/workspace/import")
    @Produces("application/json")
    public Response importFile(String path) {
        try {
            JsonObject content = workspace.read(path);
            String location = (Paths.get(Constants.CARBON_HOME,
                    Constants.DIRECTORY_DEPLOYMENT,
                    Constants.DIRECTORY_WORKSPACE)).toString();
            String configName = path.substring(path.lastIndexOf(System.getProperty(FILE_SEPARATOR)) + 1);
            String config = content.get("content").getAsString();
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(location).append(System.getProperty(FILE_SEPARATOR)).append(configName);
            Files.write(Paths.get(pathBuilder.toString()), config.getBytes());
            return Response.status(Response.Status.OK)
                    .entity(content)
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @GET
    @Path("/metadata")
    public Response getMetaData() {
        MetaDataResponse response = new MetaDataResponse(Status.SUCCESS);
        Map<String, MetaData> extensions = SourceEditorUtils.getExtensionProcessorMetaData();
        response.setInBuilt(extensions.remove(""));
        response.setExtensions(extensions);
        String jsonString = new Gson().toJson(response);
        return Response.ok(jsonString, MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{executionPlanName}/start")
    public Response start(@PathParam("executionPlanName") String executionPlanName) {
        List<String> streams = EditorDataHolder
                .getDebugProcessorService()
                .getExecutionPlanRuntimeHolder(executionPlanName)
                .getStreams();
        List<String> queries = EditorDataHolder
                .getDebugProcessorService()
                .getExecutionPlanRuntimeHolder(executionPlanName)
                .getQueries();
        EditorDataHolder
                .getDebugProcessorService()
                .start(executionPlanName);
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new DebugRuntimeResponse(Status.SUCCESS, null, executionPlanName, streams, queries)).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{executionPlanName}/debug")
    public Response debug(@PathParam("executionPlanName") String executionPlanName) {
        List<String> streams = EditorDataHolder
                .getDebugProcessorService()
                .getExecutionPlanRuntimeHolder(executionPlanName)
                .getStreams();
        List<String> queries = EditorDataHolder
                .getDebugProcessorService()
                .getExecutionPlanRuntimeHolder(executionPlanName)
                .getQueries();
        EditorDataHolder
                .getDebugProcessorService()
                .debug(executionPlanName);
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new DebugRuntimeResponse(Status.SUCCESS, null, executionPlanName, streams, queries)).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{executionPlanName}/stop")
    public Response stopDebug(@PathParam("executionPlanName") String executionPlanName) {
        EditorDataHolder
                .getDebugProcessorService()
                .stop(executionPlanName);
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new GeneralResponse(Status.SUCCESS, "Execution Plan " + executionPlanName +
                        " stopped successfully."))
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{executionPlanName}/acquire")
    public Response acquireBreakPoint(@PathParam("executionPlanName") String executionPlanName,
                                      @QueryParam("queryIndex") Integer queryIndex,
                                      @QueryParam("queryTerminal") String queryTerminal) {
        if (queryIndex != null && queryTerminal != null && !queryTerminal.isEmpty()) {
            // acquire only specified break point
            SiddhiDebugger.QueryTerminal terminal = ("in".equalsIgnoreCase(queryTerminal)) ?
                    SiddhiDebugger.QueryTerminal.IN : SiddhiDebugger.QueryTerminal.OUT;
            String queryName = (String) EditorDataHolder
                    .getDebugProcessorService()
                    .getExecutionPlanRuntimeHolder(executionPlanName)
                    .getQueries()
                    .toArray()[queryIndex];
            EditorDataHolder
                    .getDebugProcessorService()
                    .getExecutionPlanRuntimeHolder(executionPlanName)
                    .getDebugger()
                    .acquireBreakPoint(queryName, terminal);
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "Terminal " + queryTerminal +
                            " breakpoint acquired for query " + executionPlanName + ":" + queryName))
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
    @Path("/{executionPlanName}/release")
    public Response releaseBreakPoint(@PathParam("executionPlanName") String executionPlanName,
                                      @QueryParam("queryIndex") Integer queryIndex,
                                      @QueryParam("queryTerminal") String queryTerminal) {
        if (queryIndex == null || queryTerminal == null || queryTerminal.isEmpty()) {
            // release all break points
            EditorDataHolder
                    .getDebugProcessorService()
                    .getExecutionPlanRuntimeHolder(executionPlanName)
                    .getDebugger()
                    .releaseAllBreakPoints();
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "All breakpoints released for executionPlanName " + executionPlanName))
                    .build();
        } else {
            // release only specified break point
            SiddhiDebugger.QueryTerminal terminal = ("in".equalsIgnoreCase(queryTerminal)) ?
                    SiddhiDebugger.QueryTerminal.IN : SiddhiDebugger.QueryTerminal.OUT;
            String queryName = (String) EditorDataHolder
                    .getDebugProcessorService()
                    .getExecutionPlanRuntimeHolder(executionPlanName)
                    .getQueries()
                    .toArray()[queryIndex];
            EditorDataHolder
                    .getDebugProcessorService()
                    .getExecutionPlanRuntimeHolder(executionPlanName)
                    .getDebugger()
                    .releaseBreakPoint(queryName, terminal);
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "Terminal " + queryTerminal +
                            " breakpoint released for query " + executionPlanName + ":" + queryIndex))
                    .build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/{executionPlanName}/next")
    public Response next(@PathParam("executionPlanName") String executionPlanName) {
        EditorDataHolder
                .getDebugProcessorService()
                .getExecutionPlanRuntimeHolder(executionPlanName)
                .getDebugger()
                .next();
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse(Status.SUCCESS, "Debug action :next executed on " + executionPlanName))
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{executionPlanName}/play")
    public Response play(@PathParam("executionPlanName") String executionPlanName) {
        EditorDataHolder
                .getDebugProcessorService()
                .getExecutionPlanRuntimeHolder(executionPlanName)
                .getDebugger()
                .play();
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse(Status.SUCCESS, "Debug action :play executed on " + executionPlanName))
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{executionPlanName}/state")
    public Response getQueryState(@PathParam("executionPlanName") String executionPlanName) throws InterruptedException {
        DebugCallbackEvent event = EditorDataHolder
                .getDebugProcessorService()
                .getExecutionPlanRuntimeHolder(executionPlanName)
                .getCallbackEventsQueue()
                .poll(5, TimeUnit.SECONDS);

        Map<String, Map<String, Object>> queryState = new HashMap<>();
        List<String> queries = EditorDataHolder
                .getDebugProcessorService()
                .getExecutionPlanRuntimeHolder(executionPlanName)
                .getQueries();

        for (String query : queries) {
            queryState.put(query, EditorDataHolder
                    .getDebugProcessorService()
                    .getExecutionPlanRuntimeHolder(executionPlanName)
                    .getDebugger()
                    .getQueryState(query)
            );
        }
        return Response
                .status(Response.Status.OK)
                .entity(new DebugStateHolder(event, queryState)).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{executionPlanName}/{queryName}/state")
    public Response getQueryState(@PathParam("executionPlanName") String executionPlanName,
                                  @PathParam("queryName") String queryName) {
        return Response
                .status(Response.Status.OK)
                .entity(
                        EditorDataHolder
                                .getDebugProcessorService()
                                .getExecutionPlanRuntimeHolder(executionPlanName)
                                .getDebugger()
                                .getQueryState(queryName)
                ).build();
    }

    @POST
    @Produces("application/json")
    @Path("/{executionPlanName}/{streamId}/send")
    public Response mock(String data,
                         @PathParam("executionPlanName") String executionPlanName,
                         @PathParam("streamId") String streamId) {
        Gson gson = new Gson();
        Object[] event = gson.fromJson(data, Object[].class);
        executorService.execute(() -> {
            try {
                EditorDataHolder
                        .getDebugProcessorService()
                        .getExecutionPlanRuntimeHolder(executionPlanName)
                        .getInputHandler(streamId)
                        .send(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse(Status.SUCCESS, "Event " + Arrays.deepToString(event) +
                        " sent to stream " + streamId + " of runtime " + executionPlanName))
                .build();
    }


    @GET
    @Produces("application/json")
    @Path("/artifact/listExecutionPlans")
    public Response getExecutionPlans() {
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(
                        new ArrayList<>(EditorDataHolder.getExecutionPlanMap().values())
                ).build();
    }

    @GET
    @Produces("application/json")
    @Path("/artifact/listStreams/{executionPlanName}")
    public Response getStreams(@PathParam("executionPlanName") String executionPlanName) {
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(
                        EditorDataHolder
                                .getDebugProcessorService()
                                .getExecutionPlanRuntimeHolder(executionPlanName)
                                .getStreams()
                ).build();
    }

    @GET
    @Produces("application/json")
    @Path("/artifact/listAttributes/{executionPlanName}/{streamName}")
    public Response getAttributes(@PathParam("executionPlanName") String executionPlanName,
                                  @PathParam("streamName") String streamName) {
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(
                        EditorDataHolder
                                .getDebugProcessorService()
                                .getExecutionPlanRuntimeHolder(executionPlanName)
                                .getStreamAttributes(streamName)
                ).build();
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
        log.info("Editor Started on : http://localhost:9090/editor");
        // Create Stream Processor Service
        EditorDataHolder.setDebugProcessorService(new DebugProcessorService());
        SiddhiManager siddhiManager = new SiddhiManager();
        FileConfigManager fileConfigManager = new FileConfigManager(configProvider);
        siddhiManager.setConfigManager(fileConfigManager);
        EditorDataHolder.setSiddhiManager(siddhiManager);
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
        EditorDataHolder.getExecutionPlanMap().values().forEach(DebugRuntime::stop);
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
        // Nothing to do
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    protected void unsetSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {
        // Nothing to do
    }
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        this.configProvider = null;
    }
}
