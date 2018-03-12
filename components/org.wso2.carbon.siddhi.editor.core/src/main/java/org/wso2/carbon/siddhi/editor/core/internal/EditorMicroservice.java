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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import org.wso2.carbon.config.provider.ConfigProvider;
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
import org.wso2.carbon.siddhi.editor.core.util.LogEncoder;
import org.wso2.carbon.siddhi.editor.core.util.MimeMapper;
import org.wso2.carbon.siddhi.editor.core.util.SecurityUtil;
import org.wso2.carbon.siddhi.editor.core.util.SourceEditorUtils;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.SiddhiAppMap;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.common.utils.config.FileConfigManager;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.debugger.SiddhiDebugger;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.util.SiddhiComponentActivator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component(
        service = Microservice.class,
        immediate = true
)
@Path("/editor")
public class EditorMicroservice implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(EditorMicroservice.class);
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String STATUS = "status";
    private static final String SUCCESS = "success";
    private ServiceRegistration serviceRegistration;
    private Workspace workspace;
    private ExecutorService executorService = Executors
            .newScheduledThreadPool(5, new ThreadFactoryBuilder()
                    .setNameFormat("Debugger-scheduler-thread-%d")
                    .build()
            );
    private ConfigProvider configProvider;

    public EditorMicroservice() {
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
        } catch (Throwable e) {
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
        File file = getResourceAsFile("/web" + rawUriPath);
        if (file != null) {
            return Response.ok(new FileInputStream(file)).type(mimeType).build();
        }
        log.error(" File not found [" + rawUriPath + "], Requesting path [" + rawUriPath + "] ");
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/validator")
    public Response validateSiddhiApp(String validationRequestString) {
        ValidationRequest validationRequest = new Gson().fromJson(validationRequestString, ValidationRequest.class);
        String jsonString;
        try {
            SiddhiAppRuntime siddhiAppRuntime =
                    EditorDataHolder.getSiddhiManager().createSiddhiAppRuntime(validationRequest.getSiddhiApp());

            // Status SUCCESS to indicate that the siddhi app is valid
            ValidationSuccessResponse response = new ValidationSuccessResponse(Status.SUCCESS);

            // Getting requested stream definitions
            if (validationRequest.getMissingStreams() != null) {
                response.setStreams(SourceEditorUtils.getStreamDefinitions(
                        siddhiAppRuntime, validationRequest.getMissingStreams()
                ));
            }

            // Getting requested aggregation definitions
            if (validationRequest.getMissingAggregationDefinitions() != null) {
                response.setAggregationDefinitions(SourceEditorUtils.getAggregationDefinitions(
                        siddhiAppRuntime, validationRequest.getMissingAggregationDefinitions()
                ));
            }
            jsonString = new Gson().toJson(response);
        } catch (Throwable t) {
            jsonString = new Gson().toJson(t);
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
                    .entity(workspace.listDirectoryFiles(
                            new String(Base64.getDecoder().decode(path), Charset.defaultCharset())))
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
                    .entity(workspace.listDirectoriesInPath(
                            new String(Base64.getDecoder().decode(path), Charset.defaultCharset())))
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
                    .entity(workspace.exists(
                            Paths.get(new String(Base64.getDecoder().decode(path), Charset.defaultCharset()))))
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
            String location = (Paths.get(Constants.RUNTIME_PATH,
                    Constants.DIRECTORY_DEPLOYMENT)).toString();

            return Response.status(Response.Status.OK)
                    .entity(workspace.exists(SecurityUtil.resolvePath(Paths.get(location).toAbsolutePath(),
                            Paths.get(new String(base64ConfigName,
                                    Charset.defaultCharset())))))
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
    @Path("/workspace/listFiles/workspace")
    @Produces("application/json")
    public Response filesInWorkspacePath(@QueryParam("path") String relativePath) {
        try {
            String location = (Paths.get(Constants.RUNTIME_PATH,
                    Constants.DIRECTORY_DEPLOYMENT)).toString();
            java.nio.file.Path pathLocation = SecurityUtil.resolvePath(Paths.get(location).toAbsolutePath(),
                    Paths.get(new String(Base64.getDecoder().
                            decode(relativePath), Charset.defaultCharset())));
            return Response.status(Response.Status.OK)
                    .entity(workspace.listFilesInPath(pathLocation))
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
    @Path("/workspace/listFiles/samples")
    @Produces("application/json")
    public Response filesInSamplePath(@QueryParam("path") String relativePath) {
        try {
            String location = (Paths.get(Constants.CARBON_HOME, Constants.DIRECTORY_SAMPLE,
                    Constants.DIRECTORY_ARTIFACTS)).toString();
            java.nio.file.Path pathLocation = SecurityUtil.resolvePath(Paths.get(location).toAbsolutePath(),
                    Paths.get(new String(Base64.getDecoder().
                            decode(relativePath), Charset.defaultCharset())));
            return Response.status(Response.Status.OK)
                    .entity(workspace.listFilesInPath(pathLocation))
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
    @Path("/workspace/listFiles")
    @Produces("application/json")
    public Response filesInPath(@QueryParam("path") String path) {
        try {
            java.nio.file.Path pathLocation = Paths.get(new String(Base64.getDecoder().decode(path), Charset
                    .defaultCharset()));
            return Response.status(Response.Status.OK)
                    .entity(workspace.listFilesInPath(pathLocation))
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
            String location = (Paths.get(Constants.RUNTIME_PATH,
                    Constants.DIRECTORY_DEPLOYMENT)).toString();
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
            java.nio.file.Path filePath = SecurityUtil.resolvePath(
                    Paths.get(location).toAbsolutePath(),
                    Paths.get(new String(base64ConfigName, Charset.defaultCharset())));
            Files.write(filePath, base64Config);
            java.nio.file.Path fileNamePath = filePath.getFileName();
            if (null != fileNamePath) {
                String siddhiAppName = fileNamePath.toString().replace(Constants.SIDDHI_APP_FILE_EXTENSION, "");
                if (null != EditorDataHolder.getDebugProcessorService().getSiddhiAppRuntimeHolder(siddhiAppName)) {
                    //making the app faulty until the file gets deployed again for editor usage purposes
                    EditorDataHolder.getDebugProcessorService().getSiddhiAppRuntimeHolder(siddhiAppName).setMode(
                            DebugRuntime.Mode.FAULTY);
                }
            }
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            entity.addProperty("path", Constants.DIRECTORY_WORKSPACE);
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
            Files.write(Paths.get(new String(base64Location, Charset.defaultCharset())
                    + System.getProperty(FILE_SEPARATOR)
                    + new String(base64ConfigName, Charset.defaultCharset())), base64Config);
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return Response.status(Response.Status.OK).entity(entity)
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (AccessDeniedException e) {
            Map<String, String> errorMap = new HashMap<>(1);
            errorMap.put("Error", "File access denied. You don't have enough permission to access");
            return Response.serverError().entity(errorMap)
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
    @Path("/workspace/read")
    @Produces("application/json")
    public Response read(String relativePath) {
        try {
            String location = (Paths.get(Constants.RUNTIME_PATH,
                    Constants.DIRECTORY_DEPLOYMENT)).toString();
            return Response.status(Response.Status.OK)
                    .entity(workspace.read(SecurityUtil.resolvePath(Paths.get(location).toAbsolutePath(),
                            Paths.get(relativePath))))
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
    public Response readSample(String relativePath) {
        try {
            String location = (Paths.get(Constants.CARBON_HOME, Constants.DIRECTORY_SAMPLE)).toString();
            return Response.status(Response.Status.OK)
                    .entity(workspace.read(SecurityUtil.resolvePath(Paths.get(location).toAbsolutePath(),
                            Paths.get(relativePath))))
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
            JsonObject content = workspace.read(Paths.get(path));
            String location = (Paths.get(Constants.RUNTIME_PATH,
                    Constants.DIRECTORY_DEPLOYMENT,
                    Constants.DIRECTORY_WORKSPACE)).toString();
            String configName = path.substring(path.lastIndexOf(System.getProperty(FILE_SEPARATOR)) + 1);
            String config = content.get("content").getAsString();
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(location).append(System.getProperty(FILE_SEPARATOR)).append(configName);
            Files.write(Paths.get(pathBuilder.toString()), config.getBytes(Charset.defaultCharset()));
            return Response.status(Response.Status.OK)
                    .entity(content)
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (AccessDeniedException e) {
            Map<String, String> errorMap = new HashMap<>(1);
            errorMap.put("Error", "File access denied. You don't have enough permission to access");
            return Response.serverError().entity(errorMap)
                    .build();
        } catch (IOException e) {
            return Response.serverError().entity("failed." + e.getMessage())
                    .build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @DELETE
    @Path("/workspace/delete")
    @Produces("application/json")
    public Response deleteFile(@QueryParam("siddhiAppName") String siddhiAppName, @QueryParam("relativePath") String
            relativePath) {
        try {
            java.nio.file.Path location = SecurityUtil.
                    resolvePath(Paths.get(Constants.RUNTIME_PATH,
                            Constants.DIRECTORY_DEPLOYMENT).toAbsolutePath(),
                            Paths.get(relativePath));
            File file = new File(location.toString());
            if (file.delete()) {
                log.info("Siddi App: " + LogEncoder.removeCRLFCharacters(siddhiAppName) + " is deleted");
                JsonObject entity = new JsonObject();
                entity.addProperty(STATUS, SUCCESS);
                entity.addProperty("path", Constants.DIRECTORY_WORKSPACE);
                entity.addProperty("message", "Siddi App: " + siddhiAppName + " is deleted");
                return Response.status(Response.Status.OK).entity(entity)
                        .type(MediaType.APPLICATION_JSON).build();
            } else {
                log.error("Siddi App: " + LogEncoder.removeCRLFCharacters(siddhiAppName) + " could not deleted");
                return Response.serverError().entity("Siddi App: " + siddhiAppName + " could not deleted")
                        .build();
            }
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
    @Path("/{siddhiAppName}/start")
    public Response start(@PathParam("siddhiAppName") String siddhiAppName) {
        List<String> streams = EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiAppRuntimeHolder(siddhiAppName)
                .getStreams();
        List<String> queries = EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiAppRuntimeHolder(siddhiAppName)
                .getQueries();
        EditorDataHolder
                .getDebugProcessorService()
                .start(siddhiAppName);
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new DebugRuntimeResponse(Status.SUCCESS, null, siddhiAppName, streams, queries)).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{siddhiAppName}/debug")
    public Response debug(@PathParam("siddhiAppName") String siddhiAppName) {
        List<String> streams = EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiAppRuntimeHolder(siddhiAppName)
                .getStreams();
        List<String> queries = EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiAppRuntimeHolder(siddhiAppName)
                .getQueries();
        EditorDataHolder
                .getDebugProcessorService()
                .debug(siddhiAppName);
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new DebugRuntimeResponse(Status.SUCCESS, null, siddhiAppName, streams, queries)).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{siddhiAppName}/stop")
    public Response stopDebug(@PathParam("siddhiAppName") String siddhiAppName) {
        EditorDataHolder
                .getDebugProcessorService()
                .stop(siddhiAppName);
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new GeneralResponse(Status.SUCCESS, "Siddhi App " + siddhiAppName +
                        " stopped successfully."))
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{siddhiAppName}/acquire")
    public Response acquireBreakPoint(@PathParam("siddhiAppName") String siddhiAppName,
                                      @QueryParam("queryIndex") Integer queryIndex,
                                      @QueryParam("queryTerminal") String queryTerminal) {
        if (queryIndex != null && queryTerminal != null && !queryTerminal.isEmpty()) {
            // acquire only specified break point
            SiddhiDebugger.QueryTerminal terminal = ("in".equalsIgnoreCase(queryTerminal)) ?
                    SiddhiDebugger.QueryTerminal.IN : SiddhiDebugger.QueryTerminal.OUT;
            String queryName = (String) EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiAppRuntimeHolder(siddhiAppName)
                    .getQueries()
                    .toArray()[queryIndex];
            EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiAppRuntimeHolder(siddhiAppName)
                    .getDebugger()
                    .acquireBreakPoint(queryName, terminal);
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "Terminal " + queryTerminal +
                            " breakpoint acquired for query " + siddhiAppName + ":" + queryName))
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
    @Path("/{siddhiAppName}/release")
    public Response releaseBreakPoint(@PathParam("siddhiAppName") String siddhiAppName,
                                      @QueryParam("queryIndex") Integer queryIndex,
                                      @QueryParam("queryTerminal") String queryTerminal) {
        if (queryIndex == null || queryTerminal == null || queryTerminal.isEmpty()) {
            // release all break points
            EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiAppRuntimeHolder(siddhiAppName)
                    .getDebugger()
                    .releaseAllBreakPoints();
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "All breakpoints released for siddhiAppName " + siddhiAppName))
                    .build();
        } else {
            // release only specified break point
            SiddhiDebugger.QueryTerminal terminal = ("in".equalsIgnoreCase(queryTerminal)) ?
                    SiddhiDebugger.QueryTerminal.IN : SiddhiDebugger.QueryTerminal.OUT;
            String queryName = (String) EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiAppRuntimeHolder(siddhiAppName)
                    .getQueries()
                    .toArray()[queryIndex];
            EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiAppRuntimeHolder(siddhiAppName)
                    .getDebugger()
                    .releaseBreakPoint(queryName, terminal);
            return Response
                    .status(Response.Status.OK)
                    .entity(new GeneralResponse(Status.SUCCESS, "Terminal " + queryTerminal +
                            " breakpoint released for query " + siddhiAppName + ":" + queryIndex))
                    .build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/{siddhiAppName}/next")
    public Response next(@PathParam("siddhiAppName") String siddhiAppName) {
        EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiAppRuntimeHolder(siddhiAppName)
                .getDebugger()
                .next();
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse(Status.SUCCESS, "Debug action :next executed on " + siddhiAppName))
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{siddhiAppName}/play")
    public Response play(@PathParam("siddhiAppName") String siddhiAppName) {
        EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiAppRuntimeHolder(siddhiAppName)
                .getDebugger()
                .play();
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse(Status.SUCCESS, "Debug action :play executed on " + siddhiAppName))
                .build();
    }

    @GET
    @Produces("application/json")
    @Path("/{siddhiAppName}/state")
    public Response getQueryState(@PathParam("siddhiAppName") String siddhiAppName) throws InterruptedException {
        DebugCallbackEvent event = EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiAppRuntimeHolder(siddhiAppName)
                .getCallbackEventsQueue()
                .poll(5, TimeUnit.SECONDS);

        Map<String, Map<String, Object>> queryState = new HashMap<>();
        List<String> queries = EditorDataHolder
                .getDebugProcessorService()
                .getSiddhiAppRuntimeHolder(siddhiAppName)
                .getQueries();

        for (String query : queries) {
            queryState.put(query, EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiAppRuntimeHolder(siddhiAppName)
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
    @Path("/{siddhiAppName}/{queryName}/state")
    public Response getQueryState(@PathParam("siddhiAppName") String siddhiAppName,
                                  @PathParam("queryName") String queryName) {
        return Response
                .status(Response.Status.OK)
                .entity(
                        EditorDataHolder
                                .getDebugProcessorService()
                                .getSiddhiAppRuntimeHolder(siddhiAppName)
                                .getDebugger()
                                .getQueryState(queryName)
                ).build();
    }

    @POST
    @Produces("application/json")
    @Path("/{siddhiAppName}/{streamId}/send")
    public Response mock(String data,
                         @PathParam("siddhiAppName") String siddhiAppName,
                         @PathParam("streamId") String streamId) {
        Gson gson = new Gson();
        Object[] event = gson.fromJson(data, Object[].class);
        executorService.execute(() -> {
            try {
                EditorDataHolder
                        .getDebugProcessorService()
                        .getSiddhiAppRuntimeHolder(siddhiAppName)
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
                        " sent to stream " + streamId + " of runtime " + siddhiAppName))
                .build();
    }


    @GET
    @Produces("application/json")
    @Path("/artifact/listSiddhiApps")
    public Response getSiddhiApps() {
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(
                        new ArrayList<>(EditorDataHolder.getSiddhiAppMap().values())
                ).build();
    }

    @GET
    @Produces("application/json")
    @Path("/artifact/listStreams/{siddhiAppName}")
    public Response getStreams(@PathParam("siddhiAppName") String siddhiAppName) {
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(
                        EditorDataHolder
                                .getDebugProcessorService()
                                .getSiddhiAppRuntimeHolder(siddhiAppName)
                                .getStreams()
                ).build();
    }

    @GET
    @Produces("application/json")
    @Path("/artifact/listAttributes/{siddhiAppName}/{streamName}")
    public Response getAttributes(@PathParam("siddhiAppName") String siddhiAppName,
                                  @PathParam("streamName") String streamName) {
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(
                        EditorDataHolder
                                .getDebugProcessorService()
                                .getSiddhiAppRuntimeHolder(siddhiAppName)
                                .getStreamAttributes(streamName)
                ).build();
    }

    /**
     * Converts a given Siddhi App string to a specific JSON format for a graph that diagrammatically
     * display's the Siddhi App to be generated in the Editor design view.
     *
     * @param siddhiAppBase64 The Siddhi App (encoded to Base64) to be converted to JSON
     * @return The JSON result in a predefined format
     */
    @POST
    @Path("/event-flow")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response constructEventFlowJsonString(String siddhiAppBase64) {
        try {
            String siddhiAppString = new String(Base64.getDecoder().decode(siddhiAppBase64), StandardCharsets.UTF_8);

            SiddhiAppMap siddhiAppMap = new SiddhiAppMap(siddhiAppString);
            EventFlow eventFlow = new EventFlow(siddhiAppMap);

            // The 'Access-Control-Allow-Origin' header must be set to '*' as this might be accessed
            // by other domains in the future.
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(eventFlow.getEventFlowJSON().toString())
                    .build();
        } catch (SiddhiAppCreationException e) {
            log.error("Unable to generate graph view.", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Unable to construct event flow JSON string.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * This is the activation method of EditorMicroservice. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
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
     * This is the deactivation method of EditorMicroservice. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Service Component is deactivated");
        EditorDataHolder.getSiddhiAppMap().values().forEach(DebugRuntime::stop);
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
