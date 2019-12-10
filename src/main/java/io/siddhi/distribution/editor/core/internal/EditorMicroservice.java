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

package io.siddhi.distribution.editor.core.internal;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.debugger.SiddhiDebugger;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.util.SiddhiComponentActivator;
import io.siddhi.distribution.common.common.EventStreamService;
import io.siddhi.distribution.common.common.SiddhiAppRuntimeService;
import io.siddhi.distribution.common.common.utils.config.FileConfigManager;
import io.siddhi.distribution.editor.core.EditorSiddhiAppRuntimeService;
import io.siddhi.distribution.editor.core.Workspace;
import io.siddhi.distribution.editor.core.commons.configs.DockerBuildConfig;
import io.siddhi.distribution.editor.core.commons.metadata.MetaData;
import io.siddhi.distribution.editor.core.commons.request.AppStartRequest;
import io.siddhi.distribution.editor.core.commons.request.ExportAppsRequest;
import io.siddhi.distribution.editor.core.commons.request.ValidationRequest;
import io.siddhi.distribution.editor.core.commons.response.DebugRuntimeResponse;
import io.siddhi.distribution.editor.core.commons.response.GeneralResponse;
import io.siddhi.distribution.editor.core.commons.response.MetaDataResponse;
import io.siddhi.distribution.editor.core.commons.response.Status;
import io.siddhi.distribution.editor.core.commons.response.ValidationSuccessResponse;
import io.siddhi.distribution.editor.core.exception.DockerGenerationException;
import io.siddhi.distribution.editor.core.exception.InvalidExecutionStateException;
import io.siddhi.distribution.editor.core.exception.KubernetesGenerationException;
import io.siddhi.distribution.editor.core.exception.SiddhiAppDeployerServiceStubException;
import io.siddhi.distribution.editor.core.exception.SiddhiStoreQueryHelperException;
import io.siddhi.distribution.editor.core.internal.local.LocalFSWorkspace;
import io.siddhi.distribution.editor.core.model.SiddhiAppStatus;
import io.siddhi.distribution.editor.core.util.Constants;
import io.siddhi.distribution.editor.core.util.DebugCallbackEvent;
import io.siddhi.distribution.editor.core.util.DebugStateHolder;
import io.siddhi.distribution.editor.core.util.FileJsonObjectReaderUtil;
import io.siddhi.distribution.editor.core.util.LogEncoder;
import io.siddhi.distribution.editor.core.util.MimeMapper;
import io.siddhi.distribution.editor.core.util.SampleEventGenerator;
import io.siddhi.distribution.editor.core.util.SecurityUtil;
import io.siddhi.distribution.editor.core.util.SourceEditorUtils;
import io.siddhi.distribution.editor.core.util.designview.beans.EventFlow;
import io.siddhi.distribution.editor.core.util.designview.beans.ToolTip;
import io.siddhi.distribution.editor.core.util.designview.beans.configs.SiddhiAppConfig;
import io.siddhi.distribution.editor.core.util.designview.codegenerator.CodeGenerator;
import io.siddhi.distribution.editor.core.util.designview.deserializers.DeserializersRegisterer;
import io.siddhi.distribution.editor.core.util.designview.designgenerator.DesignGenerator;
import io.siddhi.distribution.editor.core.util.designview.exceptions.CodeGenerationException;
import io.siddhi.distribution.editor.core.util.designview.exceptions.DesignGenerationException;
import io.siddhi.distribution.editor.core.util.restclients.storequery.StoreQueryAPIHelper;
import io.siddhi.distribution.editor.core.util.siddhiappdeployer.SiddhiAppDeployerApiHelper;
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.exception.SiddhiAppContextException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.wso2.carbon.analytics.idp.client.core.api.AnalyticsHttpClientBuilderService;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Editor micro service implementation class.
 */
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
    private static final String EXPORT_TYPE_KUBERNETES = "kubernetes";
    private static final String EXPORT_REQUEST_TYPE_DOWNLOAD_ONLY = "downloadOnly";
    private static final String EXPORT_REQUEST_TYPE_BUILD_ONLY = "buildOnly";
    private static final String EXPORT_REQUEST_GET_STATUS_HEADER = "Siddhi-Docker-Key";
    private ServiceRegistration serviceRegistration;
    private Workspace workspace;
    private ExecutorService executorService = Executors
            .newScheduledThreadPool(5, new ThreadFactoryBuilder()
                    .setNameFormat("Debugger-scheduler-thread-%d")
                    .build()
            );
    private ConfigProvider configProvider;
    private ServiceRegistration siddhiAppRuntimeServiceRegistration;
    private StoreQueryAPIHelper storeQueryAPIHelper;
    private Map<String, DockerBuilderStatus> dockerBuilderStatusMap = new HashMap<>();

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
        String jsonString = "";
        try {
            String siddhiApp = validationRequest.getSiddhiApp();
            if (validationRequest.getVariables().size() != 0) {
                siddhiApp = SourceEditorUtils.populateSiddhiAppWithVars(validationRequest.getVariables(), siddhiApp);
            }
            if (EditorDataHolder.getSiddhiManager() != null) {
                SiddhiAppRuntime siddhiAppRuntime =
                        EditorDataHolder.getSiddhiManager().createSiddhiAppRuntime(siddhiApp);
                String siddhiAppName = siddhiAppRuntime.getName();
                DebugRuntime debugRuntime = EditorDataHolder.getSiddhiAppMap().get(siddhiAppName);
                if (debugRuntime != null && debugRuntime.getMode() != DebugRuntime.Mode.RUN) {
                    debugRuntime.setSiddhiAppRuntime(siddhiAppRuntime);
                    debugRuntime.setMode(DebugRuntime.Mode.STOP);
                    EditorDataHolder.getSiddhiAppMap().put(siddhiAppName, debugRuntime);
                }

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
            }
        } catch (Throwable t) {
            // If the exception is a SiddhiAppCreationException and its message is null, append the stacktrace as the
            // message.
            if (t instanceof SiddhiAppContextException &&
                    ((SiddhiAppContextException) t).getMessageWithOutContext() == null) {
                SiddhiAppContextException e = (SiddhiAppContextException) t;
                SiddhiAppCreationException appCreationException = new SiddhiAppCreationException(
                        ExceptionUtils.getStackTrace(t), t, e.getQueryContextStartIndex(), e.getQueryContextEndIndex());
                jsonString = new Gson().toJson(appCreationException);
            } else {
                jsonString = new Gson().toJson(t);
            }
        }
        return Response.ok(jsonString, MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("/workspace/root")
    @Produces(MediaType.APPLICATION_JSON)
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

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeStoreQuery(JsonElement element, @Context Request request) {

        if (storeQueryAPIHelper == null) {
            storeQueryAPIHelper = new StoreQueryAPIHelper(configProvider);
        }

        try {
            feign.Response response = storeQueryAPIHelper.executeStoreQuery(element.toString());
            String payload = response.body().toString();
            // If the response HTTP status code is OK, return the response.
            if (response.status() == 200) {
                return Response.ok().entity(payload).build();
            }

            // If the error response code is sent, extract the original error message and response back.
            JsonObject errorObj = new JsonParser().parse(payload).getAsJsonObject();
            if (errorObj.get("message") != null) {
                return Response.serverError().entity(errorObj.get("message")).build();
            }
            return Response.serverError().entity(payload).build();
        } catch (SiddhiStoreQueryHelperException e) {
            log.error("Cannot execute the store query.", e);
            return Response.serverError().entity("Failed executing the Siddhi query.").build();
        }
    }

    @GET
    @Path("/listDirectoriesInPath")
    @Produces(MediaType.APPLICATION_JSON)
    public Response filterDirectories(@QueryParam("path") String path,
                                                            @QueryParam("directoryList") String directoryList) {
        try {
            List<String> directories = Arrays.stream(
                    new String(Base64.getDecoder().decode(directoryList), Charset.defaultCharset())
                            .split(","))
                    .filter(directory -> directory != null && !directory.trim().isEmpty())
                    .map(directory -> "\"" + directory + "\"")
                    .collect(Collectors.toList());

            String baseLocation = Paths.get(Constants.CARBON_HOME).toString();
            java.nio.file.Path pathLocation = SecurityUtil.resolvePath(
                    Paths.get(baseLocation).toAbsolutePath(),
                    Paths.get(new String(Base64.getDecoder().decode(path), Charset.defaultCharset())));

            JsonArray filteredDirectoryFiles = FileJsonObjectReaderUtil.listDirectoryInPath(
                                                                                pathLocation.toString(), directories);

            JsonObject rootElement = FileJsonObjectReaderUtil.getJsonRootObject(filteredDirectoryFiles);

            return Response.status(Response.Status.OK)
                    .entity(rootElement)
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
    @Path("/listFilesInPath")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFilesInRootPath(@QueryParam("path") String path) {
        try {
            String location = Paths.get(Constants.CARBON_HOME).toString();
            java.nio.file.Path pathLocation = SecurityUtil.resolvePath(
                    Paths.get(location).toAbsolutePath(),
                    Paths.get(new String(Base64.getDecoder().decode(path), Charset.defaultCharset())));

            return Response.status(Response.Status.OK)
                    .entity(FileJsonObjectReaderUtil.listFilesInPath(pathLocation, "jar"))
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Path("/workspace/exists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fileExists(@QueryParam("path") String path) {

        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.exists(
                            Paths.get(Constants.DIRECTORY_WORKSPACE + System.getProperty(FILE_SEPARATOR) +
                                    new String(Base64.getDecoder().decode(path), Charset.defaultCharset()))))
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
    @Produces(MediaType.APPLICATION_JSON)
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
                            Paths.get(Constants.DIRECTORY_WORKSPACE + System.getProperty(FILE_SEPARATOR) +
                                    new String(base64ConfigName, Charset.defaultCharset())))))
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
    @Path("/workspace/deploy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject deploy(JsonElement element) {

        JsonArray success = new JsonArray();
        JsonArray failure = new JsonArray();
        JsonObject deploymentStatus = new JsonObject();
        String fileName;
        String sidhiFile = null;
        Integer siddhiFilesCount = element.getAsJsonObject().getAsJsonArray(Constants.SIDDHI_FILE_LIST).size();
        Integer serverListCount = element.getAsJsonObject().getAsJsonArray("serverList").size();
        String location = (Paths.get(Constants.RUNTIME_PATH,
                Constants.DIRECTORY_DEPLOYMENT, Constants.DIRECTORY_WORKSPACE)).toString();
        SiddhiAppDeployerApiHelper siddhiAppDeployerApiHelper = new SiddhiAppDeployerApiHelper();
        for (int i = 0; i < siddhiFilesCount; i++) {
            fileName = element.getAsJsonObject().getAsJsonArray(Constants.SIDDHI_FILE_LIST).get(i).
                    getAsJsonObject().get(Constants.SIDDHI_APP_NAME).toString().replaceAll("\"", "");
            try {
                sidhiFile = new String((Files.readAllBytes(Paths.
                        get(location + "/" + fileName))), "UTF-8");
            } catch (IOException e) {
                JsonPrimitive status = new JsonPrimitive(String.valueOf(e));
                failure.add(status);
            }
            for (int x = 0; x < serverListCount; x++) {
                String host = element.getAsJsonObject().getAsJsonArray(Constants.SERVER_LIST).get(x).
                        getAsJsonObject().get(Constants.DEPLOYMENT_HOST).toString().replaceAll("\"",
                        "");
                String port = element.getAsJsonObject().getAsJsonArray(Constants.SERVER_LIST).get(x).
                        getAsJsonObject().get(Constants.DEPLOYMENT_PORT).toString().replaceAll("\"",
                        "");
                String hostAndPort = host + ":" + port;
                String username = element.getAsJsonObject().getAsJsonArray(Constants.SERVER_LIST).get(x).
                        getAsJsonObject().get(Constants.DEPLOYMENT_USERNAME).toString().replaceAll("\"",
                        "");
                String password = element.getAsJsonObject().getAsJsonArray(Constants.SERVER_LIST).get(x).
                        getAsJsonObject().get(Constants.DEPLOYMENT_PASSWORD).toString().replaceAll("\"",
                        "");
                try {
                    boolean response = siddhiAppDeployerApiHelper.
                            deploySiddhiApp(hostAndPort, username, password, sidhiFile, fileName);
                    if (response == true) {
                        JsonPrimitive status = new JsonPrimitive(fileName + " was successfully deployed to " +
                                hostAndPort);
                        success.add(status);
                    }
                } catch (SiddhiAppDeployerServiceStubException e) {
                    JsonPrimitive status = new JsonPrimitive(String.valueOf(e.getMessage()));
                    failure.add(status);
                }
            }
        }
        deploymentStatus.add("success", success);
        deploymentStatus.add("failure", failure);
        return deploymentStatus;
    }

    @GET
    @Path("/workspace/listFiles/workspace")
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Path("/workspace/listFiles/samples/descriptions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response filesInSamplePathWithDescription() {

        try {
            Map<String, String> siddhiSampleMap = EditorDataHolder.getSiddhiSampleMap();
            return Response.status(Response.Status.OK)
                    .entity(workspace.listSamplesInPath(siddhiSampleMap))
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (Throwable ignored) {
            return Response.serverError().entity("failed")
                    .build();
        }
    }

    @GET
    @Path("/workspace/config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigs() {

        JsonObject config = new JsonObject();
        config.addProperty("fileSeparator", System.getProperty(FILE_SEPARATOR));
        return Response.status(Response.Status.OK)
                .entity(config)
                .type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/workspace/listFiles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response filesInPath(@QueryParam("path") String path) {

        try {
            String location = (Paths.get(Constants.CARBON_HOME)).toString();
            java.nio.file.Path pathLocation = SecurityUtil.resolvePath(Paths.get(location).toAbsolutePath(),
                    Paths.get(new String(Base64.getDecoder().decode(path), Charset.defaultCharset())));
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
    @Produces(MediaType.APPLICATION_JSON)
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
                    Paths.get(Constants.DIRECTORY_WORKSPACE + System.getProperty(FILE_SEPARATOR) +
                            new String(base64ConfigName, Charset.defaultCharset())));
            Files.write(filePath, base64Config);
            WorkspaceDeployer.deployConfigFile(new String(base64ConfigName, Charset.defaultCharset()),
                    new String(base64Config, Charset.defaultCharset()));
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
    @Path("/workspace/read")
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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

    @DELETE
    @Path("/workspace/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFile(@QueryParam("siddhiAppName") String siddhiAppName) {

        try {
            java.nio.file.Path location = SecurityUtil.
                    resolvePath(Paths.get(Constants.RUNTIME_PATH,
                            Constants.DIRECTORY_DEPLOYMENT).toAbsolutePath(),
                            Paths.get(Constants.DIRECTORY_WORKSPACE + System.getProperty(FILE_SEPARATOR) +
                                    siddhiAppName));
            File file = new File(location.toString());
            if (file.delete()) {
                log.info("Siddi App: " + LogEncoder.removeCRLFCharacters(siddhiAppName) + " is deleted");
                JsonObject entity = new JsonObject();
                entity.addProperty(STATUS, SUCCESS);
                entity.addProperty("path", Constants.DIRECTORY_WORKSPACE);
                entity.addProperty("message", "Siddi App: " + siddhiAppName + " is deleted");
                WorkspaceDeployer.unDeployConfigFile(siddhiAppName);
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{siddhiAppName}/start")
    public Response start(@PathParam("siddhiAppName") String siddhiAppName) {
        List<String> streams = new ArrayList<>();
        List<String> queries = new ArrayList<>();
        try {
            streams = EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiAppRuntimeHolder(siddhiAppName)
                    .getStreams();
            queries = EditorDataHolder
                    .getDebugProcessorService()
                    .getSiddhiAppRuntimeHolder(siddhiAppName)
                    .getQueries();
        } catch (InvalidExecutionStateException ignored) {
        }
        EditorDataHolder
                .getDebugProcessorService()
                .start(siddhiAppName);
        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new DebugRuntimeResponse(Status.SUCCESS, null, siddhiAppName, streams, queries)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/start")
    public Response startWithVariables(String appStartRequestString) {
        AppStartRequest appStartRequest = new Gson().fromJson(appStartRequestString, AppStartRequest.class);
        String siddhiAppName = appStartRequest.getSiddhiAppName();
        if (appStartRequest.getVariables().size() > 0) {
            DebugRuntime existingRuntime = EditorDataHolder.getSiddhiAppMap().get(siddhiAppName);
            String siddhiApp = existingRuntime.getSiddhiApp();
            DebugRuntime runtimeHolder = new DebugRuntime(siddhiAppName, siddhiApp, appStartRequest.getVariables());
            EditorDataHolder.getSiddhiAppMap().put(siddhiAppName, runtimeHolder);

        }
        return start(siddhiAppName);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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
                    .entity(new GeneralResponse(Status.SUCCESS, "All breakpoints released for siddhiAppName " +
                            siddhiAppName))
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/artifact/listSiddhiApps")
    public Response getSiddhiApps(@QueryParam("envVar") String encodedEnvVar) {

        if (encodedEnvVar != null) {
            try {
                String envVarJson = new String(Base64.getDecoder().decode(encodedEnvVar), StandardCharsets.UTF_8);
                Gson gson = DeserializersRegisterer.getGsonBuilder().disableHtmlEscaping().create();
                HashMap<String, String> envVariables = gson.fromJson(envVarJson, HashMap.class);
                if (!envVariables.isEmpty()) {
                    EditorDataHolder.getSiddhiAppMap().forEach((siddhiAppName, debugRuntime) -> {
                        String populatedSiddhiApp = SourceEditorUtils.populateSiddhiAppWithVars(envVariables,
                                debugRuntime.getSiddhiApp());
                        SiddhiAppRuntime siddhiAppRuntime =
                                EditorDataHolder.getSiddhiManager().createSiddhiAppRuntime(populatedSiddhiApp);
                        if (debugRuntime.getMode() != DebugRuntime.Mode.RUN) {
                            debugRuntime.setSiddhiAppRuntime(siddhiAppRuntime);
                            debugRuntime.setMode(DebugRuntime.Mode.STOP);
                            EditorDataHolder.getSiddhiAppMap().put(siddhiAppName, debugRuntime);
                        }
                    });
                }
            } catch (Throwable ignored) {
                // If error in json syntax, return siddhi app list without populating
            }
        }

        List<SiddhiAppStatus> siddhiAppStatusList = EditorDataHolder.getSiddhiAppMap().values().stream()
                .map((runtime -> new SiddhiAppStatus(runtime.getSiddhiAppName(), runtime.getMode().toString())))
                .collect(Collectors.toList());

        return Response
                .status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(siddhiAppStatusList)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
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

    @POST
    @Path("/design-view")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getDesignView(String siddhiAppBase64) {

        try {
            FileConfigManager fileConfigManager = new FileConfigManager(configProvider);
            SiddhiManager siddhiManager = EditorDataHolder.getSiddhiManager();
            siddhiManager.setConfigManager(fileConfigManager);
            DesignGenerator designGenerator = new DesignGenerator();
            designGenerator.setSiddhiManager(siddhiManager);
            String siddhiAppString = new String(Base64.getDecoder().decode(siddhiAppBase64), StandardCharsets.UTF_8);
            EventFlow eventFlow = designGenerator.getEventFlow(siddhiAppString);
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            String eventFlowJson = gson.toJson(eventFlow);

            String encodedEventFlowJson =
                    new String(Base64.getEncoder().encode(eventFlowJson.getBytes(StandardCharsets.UTF_8)),
                            StandardCharsets.UTF_8);
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(encodedEventFlowJson)
                    .build();
        } catch (SiddhiAppCreationException e) {
            log.error("Unable to generate design view", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        } catch (DesignGenerationException e) {
            log.error("Failed to convert Siddhi app code to design view", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/code-view")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getCodeView(String encodedEventFlowJson) {

        try {
            String eventFlowJson =
                    new String(Base64.getDecoder().decode(encodedEventFlowJson), StandardCharsets.UTF_8);
            Gson gson = DeserializersRegisterer.getGsonBuilder().disableHtmlEscaping().create();
            EventFlow eventFlow = gson.fromJson(eventFlowJson, EventFlow.class);
            CodeGenerator codeGenerator = new CodeGenerator();
            String siddhiAppCode = codeGenerator.generateSiddhiAppCode(eventFlow);

            String encodedSiddhiAppString =
                    new String(Base64.getEncoder().encode(siddhiAppCode.getBytes(StandardCharsets.UTF_8)),
                            StandardCharsets.UTF_8);
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(encodedSiddhiAppString)
                    .build();
        } catch (CodeGenerationException e) {
            log.error("Unable to generate code view", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/tooltips")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToolTips(String encodedSiddhiAppConfigJson) {

        try {
            String siddhiAppConfigJson =
                    new String(Base64.getDecoder().decode(encodedSiddhiAppConfigJson), StandardCharsets.UTF_8);
            Gson gson = DeserializersRegisterer.getGsonBuilder().disableHtmlEscaping().create();
            SiddhiAppConfig siddhiAppConfig = gson.fromJson(siddhiAppConfigJson, SiddhiAppConfig.class);
            CodeGenerator codeGenerator = new CodeGenerator();
            List<ToolTip> toolTipList = codeGenerator.generateSiddhiAppToolTips(siddhiAppConfig);

            String jsonString = new Gson().toJson(toolTipList);
            return Response.status(Response.Status.OK)
                    .entity(jsonString)
                    .build();
        } catch (CodeGenerationException e) {
            log.error("Unable to generate tooltips", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Export given Siddhi apps and other configurations to docker or kubernetes artifacts.
     *
     * @param exportType Export type (docker or kubernetes
     * @return Docker or Kubernetes artifacts
     */
    @POST
    @Path("/export")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response exportApps(
            @QueryParam("exportType") String exportType,
            @QueryParam("requestType") String requestType,
            @FormParam("payload") String payload
    ) {
        String dockerBuilderStatusKey = "";
        String errorMessage = "";
        try {
            if (payload == null) {
                errorMessage = "Form parameter payload cannot be null while exporting docker/k8. " +
                        "Expected payload format: " +
                        "{'templatedSiddhiApps': ['<SIDDHI-APPS>'], " +
                        "'configuration': '<SIDDHI-CONFIG-OF-DEPLOYMENT.YAML>', " +
                        "'templatedVariables': ['<TEMPLATED-VERIABLES>'], " +
                        "'kubernetesConfiguration': '<K8S-CONFIG>', " +
                        "'dockerConfiguration': '<DOCKER-CONFIG>'}";
                log.error(errorMessage);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(errorMessage)
                        .build();
            }
            ExportAppsRequest exportAppsRequest = new Gson().fromJson(payload, ExportAppsRequest.class);
            ExportUtils exportUtils = new ExportUtils(configProvider, exportAppsRequest, exportType);
            File zipFile = exportUtils.createZipFile();
            String fileName = exportUtils.getZipFileName();
            boolean kubernetesEnabled = false;
            if (exportType != null) {
                if (exportType.equals(EXPORT_TYPE_KUBERNETES)) {
                    kubernetesEnabled = true;
                }
            }

            if (requestType != null && requestType.equals(EXPORT_REQUEST_TYPE_DOWNLOAD_ONLY)) {
                return Response
                        .status(Response.Status.OK)
                        .entity(zipFile)
                        .header("Content-Disposition", ("attachment; filename=" + fileName))
                        .build();
            }
            DockerBuilderStatus dockerBuilderStatus = new DockerBuilderStatus("", "");
            if (exportAppsRequest != null && exportAppsRequest.getDockerConfiguration() != null &&
                    exportAppsRequest.getDockerConfiguration().isPushDocker()) {

                DockerBuildConfig dockerBuildConfig = exportAppsRequest.getDockerConfiguration();
                if ((StringUtils.isEmpty(dockerBuildConfig.getImageName())) ||
                        (StringUtils.isEmpty(dockerBuildConfig.getUserName())) ||
                        (StringUtils.isEmpty(dockerBuildConfig.getEmail())) ||
                        (StringUtils.isEmpty(dockerBuildConfig.getPassword()))
                ) {
                    errorMessage = "Missing required Docker build configuration " +
                            "of (DockerImageName|UserName|Email|Password)";
                    log.error(errorMessage);
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(errorMessage)
                            .build();
                }
                if (!dockerBuildConfig.getImageName().equals(dockerBuildConfig.getImageName().toLowerCase())) {
                    errorMessage = "Invalid docker image name " +
                            dockerBuildConfig.getImageName() +
                            ". Docker image name must be in lowercase.";
                    log.error(errorMessage);
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(errorMessage)
                            .build();
                }
                DockerBuilder dockerBuilder = new DockerBuilder(
                        dockerBuildConfig.getImageName(),
                        dockerBuildConfig.getUserName(),
                        dockerBuildConfig.getEmail(),
                        dockerBuildConfig.getPassword(),
                        exportUtils.getTempDockerPath(),
                        dockerBuilderStatus
                );
                dockerBuilder.start();
                UUID uuid = UUID.randomUUID();
                dockerBuilderStatusKey = uuid.toString();
                dockerBuilderStatusMap.clear();
                dockerBuilderStatusMap.put(dockerBuilderStatusKey, dockerBuilderStatus);
            }

            if (requestType != null && requestType.equals(EXPORT_REQUEST_TYPE_BUILD_ONLY) && !kubernetesEnabled) {
                return Response
                        .status(Response.Status.OK)
                        .header(EXPORT_REQUEST_GET_STATUS_HEADER, dockerBuilderStatusKey)
                        .build();
            }
            return Response
                    .status(Response.Status.OK)
                    .entity(zipFile)
                    .header("Content-Disposition", ("attachment; filename=" + fileName))
                    .header("Siddhi-Docker-Key", dockerBuilderStatusKey)
                    .build();
        } catch (JsonSyntaxException e) {
            log.error("Incorrect JSON configuration format found while exporting Docker/K8s", e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Incorrect JSON configuration format. " + e.getMessage())
                    .build();
        } catch (DockerGenerationException e) {
            log.error("Exception caught while generating Docker export artifacts. ", e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Exception caught while generating Docker export artifacts. " + e.getMessage())
                    .build();
        } catch (KubernetesGenerationException e) {
            log.error("Exception caught while generating Kubernetes export artifacts. ", e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Exception caught while generating Kubernetes export artifacts. " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Cannot generate export-artifacts archive.", e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Cannot generate export-artifacts archive." + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/dockerBuildStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context Request request) {
        JsonObject dockerBuilderStatus = new JsonObject();
        if (request.getHeader(EXPORT_REQUEST_GET_STATUS_HEADER) != null) {
            if (dockerBuilderStatusMap.get(request.getHeader(EXPORT_REQUEST_GET_STATUS_HEADER)) != null) {
                DockerBuilderStatus currentStatus = dockerBuilderStatusMap.get(
                        request.getHeader(EXPORT_REQUEST_GET_STATUS_HEADER)
                );
                dockerBuilderStatus.addProperty(
                        "Step",
                        currentStatus.getStep()
                );
                dockerBuilderStatus.addProperty(
                        "Status",
                        currentStatus.getStatus()
                );
            } else {
                dockerBuilderStatus.addProperty(
                        "Step",
                        ""
                );
                dockerBuilderStatus.addProperty(
                        "Status",
                        ""
                );
            }
        }
        return Response
                .status(Response.Status.OK)
                .entity(dockerBuilderStatus)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Get sample event for a particular event stream.
     *
     * @param appName    Siddhi app name.
     * @param streamName Stream name.
     * @param eventType  The event type os the requested event (json, xml, text).
     * @return Sample event
     */
    @GET
    @Path("/siddhi-apps/{appName}/streams/{streamName}/event/{type}")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getDefaultSampleStreamEvent(@PathParam("appName") String appName,
                                                @PathParam("streamName") String streamName,
                                                @PathParam("type") String eventType)
            throws NotFoundException {

        SiddhiAppRuntime siddhiAppRuntime = EditorDataHolder.getSiddhiManager().getSiddhiAppRuntime(appName);
        JSONObject errorResponse = new JSONObject();
        if (siddhiAppRuntime == null) {
            errorResponse.put("error", "There is no Siddhi App exist with provided name : " + appName);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse.toString()).build();
        } else {
            StreamDefinition streamDefinition = siddhiAppRuntime.getStreamDefinitionMap().get(streamName);
            if (streamDefinition == null) {
                errorResponse.put("error", "There is no Stream called " + streamName + " in " +
                        appName + " Siddhi App.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse.toString()).build();
            } else {
                if (eventType.equals(Constants.XML_EVENT)) {
                    return Response.ok().entity(SampleEventGenerator.generateXMLEvent(streamDefinition)).build();
                } else if (eventType.equals(Constants.JSON_EVENT)) {
                    return Response.ok().entity(SampleEventGenerator.generateJSONEvent(streamDefinition)).build();
                } else if (eventType.equals(Constants.TEXT_EVENT)) {
                    return Response.ok().entity(SampleEventGenerator.generateTextEvent(streamDefinition)).build();
                } else {
                    errorResponse.put("error", "Invalid type: " + eventType + " given to retrieve the sample event.");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse.toString()).
                            build();
                }
            }
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
        SiddhiManager siddhiManager = EditorDataHolder.getSiddhiManager();
        FileConfigManager fileConfigManager = new FileConfigManager(configProvider);
        siddhiManager.setConfigManager(fileConfigManager);
        EditorDataHolder.setSiddhiManager(siddhiManager);
        EditorDataHolder.setBundleContext(bundleContext);
        siddhiAppRuntimeServiceRegistration = bundleContext.registerService(SiddhiAppRuntimeService.class.getName(),
                new EditorSiddhiAppRuntimeService(), null);
        serviceRegistration = bundleContext.registerService(EventStreamService.class.getName(),
                new DebuggerEventStreamService(), null);
        loadSampleFiles();
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

    protected void loadSampleFiles() {

        String location = (Paths.get(Constants.CARBON_HOME, Constants.DIRECTORY_SAMPLE,
                Constants.DIRECTORY_ARTIFACTS)).toString();
        String relativePath = "";
        java.nio.file.Path pathLocation = SecurityUtil.resolvePath(Paths.get(location).toAbsolutePath(),
                Paths.get(new String(Base64.getDecoder().
                        decode(relativePath), Charset.defaultCharset())));

        String regex = "@[Aa][Pp][Pp]:[Dd][Ee][Ss][Cc][Rr][Ii][Pp][Tt][Ii][Oo][Nn]\\(['|\"](.*?)['|\"]\\)";
        Pattern pattern = Pattern.compile(regex);
        try {
            Map<String, String> sampleMap = new HashMap<>();
            List<java.nio.file.Path> collect = Files.walk(pathLocation)
                    .filter(s -> s.toString().endsWith(".siddhi"))
                    .sorted()
                    .collect(Collectors.toList());
            for (java.nio.file.Path path : collect) {
                String fileContent = new String(Files.readAllBytes(path), Charset.defaultCharset());
                Matcher matcher = pattern.matcher(fileContent);
                String descriptionText = "";
                if (matcher.find()) {
                    String description = matcher.group();
                    descriptionText = description.substring(description.indexOf("(") + 1, description.lastIndexOf(")"));
                }
                java.nio.file.Path relativeSamplePath = pathLocation.relativize(path);
                sampleMap.put(relativeSamplePath.toString(), descriptionText);
            }
            EditorDataHolder.setSiddhiSampleMap(sampleMap);
        } catch (IOException e) {
            log.error("Error while reading the sample descriptions.", e);
        }
    }

    @Reference(
            name = "carbon.anaytics.common.clientservice",
            service = AnalyticsHttpClientBuilderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAnalyticsHttpClient"
    )
    protected void registerAnalyticsHttpClient(AnalyticsHttpClientBuilderService service) {

        EditorDataHolder.getInstance().setClientBuilderService(service);
    }

    protected void unregisterAnalyticsHttpClient(AnalyticsHttpClientBuilderService service) {

        EditorDataHolder.getInstance().setClientBuilderService(null);
    }

    @Reference(
            name = "siddhi-manager-service",
            service = SiddhiManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiManager"
    )
    protected void setSiddhiManager(SiddhiManager siddhiManager) {
        EditorDataHolder.setSiddhiManager(siddhiManager);
    }

    protected void unsetSiddhiManager(SiddhiManager siddhiManager) {
        EditorDataHolder.setSiddhiManager(null);
    }
}
