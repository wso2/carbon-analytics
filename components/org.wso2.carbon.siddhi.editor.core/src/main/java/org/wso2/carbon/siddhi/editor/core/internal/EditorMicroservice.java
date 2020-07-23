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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.debugger.SiddhiDebugger;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.util.SiddhiComponentActivator;
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.exception.SiddhiAppContextException;
import net.minidev.json.JSONArray;
import org.apache.axiom.om.DeferredParsingException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.jaxen.JaxenException;
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
import org.wso2.carbon.siddhi.editor.core.EditorSiddhiAppRuntimeService;
import org.wso2.carbon.siddhi.editor.core.Workspace;
import org.wso2.carbon.siddhi.editor.core.commons.configs.DockerBuildConfig;
import org.wso2.carbon.siddhi.editor.core.commons.metadata.MetaData;
import org.wso2.carbon.siddhi.editor.core.commons.request.AppStartRequest;
import org.wso2.carbon.siddhi.editor.core.commons.request.ExportAppsRequest;
import org.wso2.carbon.siddhi.editor.core.commons.request.ValidationRequest;
import org.wso2.carbon.siddhi.editor.core.commons.response.DebugRuntimeResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.GeneralResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.MetaDataResponse;
import org.wso2.carbon.siddhi.editor.core.commons.response.Status;
import org.wso2.carbon.siddhi.editor.core.commons.response.ValidationSuccessResponse;
import org.wso2.carbon.siddhi.editor.core.exception.DockerGenerationException;
import org.wso2.carbon.siddhi.editor.core.exception.ErrorHandlerServiceStubException;
import org.wso2.carbon.siddhi.editor.core.exception.InvalidExecutionStateException;
import org.wso2.carbon.siddhi.editor.core.exception.KubernetesGenerationException;
import org.wso2.carbon.siddhi.editor.core.exception.SiddhiAppDeployerServiceStubException;
import org.wso2.carbon.siddhi.editor.core.exception.SiddhiStoreQueryHelperException;
import org.wso2.carbon.siddhi.editor.core.internal.local.LocalFSWorkspace;
import org.wso2.carbon.siddhi.editor.core.model.SiddhiAppStatus;
import org.wso2.carbon.siddhi.editor.core.util.Constants;
import org.wso2.carbon.siddhi.editor.core.util.DebugCallbackEvent;
import org.wso2.carbon.siddhi.editor.core.util.DebugStateHolder;
import org.wso2.carbon.siddhi.editor.core.util.FileJsonObjectReaderUtil;
import org.wso2.carbon.siddhi.editor.core.util.LogEncoder;
import org.wso2.carbon.siddhi.editor.core.util.MetaInfoRetrieverUtils;
import org.wso2.carbon.siddhi.editor.core.util.MimeMapper;
import org.wso2.carbon.siddhi.editor.core.util.SampleEventGenerator;
import org.wso2.carbon.siddhi.editor.core.util.SecurityUtil;
import org.wso2.carbon.siddhi.editor.core.util.SourceEditorUtils;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.ToolTip;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.SiddhiAppConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.CodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.deserializers.DeserializersRegisterer;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.DesignGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.errorhandler.ErrorHandlerApiHelper;
import org.wso2.carbon.siddhi.editor.core.util.metainforetriever.beans.CSVConfig;
import org.wso2.carbon.siddhi.editor.core.util.metainforetriever.beans.JSONConfig;
import org.wso2.carbon.siddhi.editor.core.util.metainforetriever.beans.XMLConfig;
import org.wso2.carbon.siddhi.editor.core.util.restclients.storequery.StoreQueryAPIHelper;
import org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.SiddhiAppDeployerApiHelper;
import org.wso2.carbon.streaming.integrator.common.EventStreamService;
import org.wso2.carbon.streaming.integrator.common.SiddhiAppRuntimeService;
import org.wso2.carbon.streaming.integrator.common.utils.config.FileConfigManager;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.siddhi.editor.core.util.MetaInfoRetrieverUtils.getDataSourceConfiguration;
import static org.wso2.carbon.siddhi.editor.core.util.MetaInfoRetrieverUtils.getDbConnection;

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
    private Map<String, String> dataStoreMap = new HashMap<>();

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
    @Path("/etl-wizard/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response saveSiddhiApp(
            @FormDataParam("configName") String configName,
            @FormDataParam("config") String encodedEventFlowJson,
            @FormDataParam("overwrite") boolean overwrite) {
        String fileName = new String(Base64.getDecoder().decode(configName), Charset.defaultCharset());
        java.nio.file.Path filePath = Paths.get(Constants.RUNTIME_PATH, Constants.DIRECTORY_DEPLOYMENT,
                Constants.DIRECTORY_WORKSPACE, fileName).toAbsolutePath();

        try {
            if (!overwrite) {
                // Check if the file already exist. If so return 409 CONFLICT
                if (workspace.exists(filePath).get("exists").getAsBoolean()) {
                    return Response.status(Response.Status.CONFLICT).build();
                }
            }

            // Get the app source from the encodedEventFlowJson.
            String eventFlowJson = new String(Base64.getDecoder().decode(encodedEventFlowJson), StandardCharsets.UTF_8);
            Gson gson = DeserializersRegisterer.getGsonBuilder().disableHtmlEscaping().create();
            EventFlow eventFlow = gson.fromJson(eventFlowJson, EventFlow.class);
            String siddhiAppCode = new CodeGenerator().generateSiddhiAppCode(eventFlow);

            // Save the Siddhi app in the file system
            Files.write(filePath, siddhiAppCode.getBytes(Charset.defaultCharset()));

            // Deploy Siddhi app
            WorkspaceDeployer.deployConfigFile(fileName, siddhiAppCode);

            JsonObject responseEntity = new JsonObject();
            responseEntity.addProperty(STATUS, SUCCESS);
            responseEntity.addProperty("path", filePath.toString());

            // Return status
            return Response.ok().entity(responseEntity).type(MediaType.APPLICATION_JSON).build();
        } catch (CodeGenerationException e) {
            log.error("Unable to generate code view.", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error(String.format("Error occurred when trying to read/write the %s app.", fileName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error(String.format("Error occurred while saving the %s app.", fileName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
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
            if (EXPORT_TYPE_KUBERNETES.equals(exportType)) {
                kubernetesEnabled = true;
            }


            if (EXPORT_REQUEST_TYPE_DOWNLOAD_ONLY.equals(requestType)) {
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

            if (EXPORT_REQUEST_TYPE_BUILD_ONLY.equals(requestType) && !kubernetesEnabled) {
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
                if (eventType.equals(Constants.TYPE_XML)) {
                    return Response.ok().entity(SampleEventGenerator.generateXMLEvent(streamDefinition)).build();
                } else if (eventType.equals(Constants.TYPE_JSON)) {
                    return Response.ok().entity(SampleEventGenerator.generateJSONEvent(streamDefinition)).build();
                } else if (eventType.equals(Constants.TYPE_TEXT)) {
                    return Response.ok().entity(SampleEventGenerator.generateTextEvent(streamDefinition)).build();
                } else {
                    errorResponse.put("error", "Invalid type: " + eventType + " given to retrieve the sample event.");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse.toString()).
                            build();
                }
            }
        }
    }

    @POST
    @Path("/retrieveFileDataAttributes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveFileReadAttributes(@Context Request request,
                                               @FormDataParam("config") String fileConfig,
                                               @FormDataParam("file") InputStream fileInputStream) {
        JsonObject errorResponse = new JsonObject();
        JsonParser parser = new JsonParser();
        JsonObject reqObj = (JsonObject) parser.parse(fileConfig);
        String type = reqObj.get("type").toString().replaceAll("\"", "");
        String config = reqObj.get("config").toString();
        BufferedReader bufferedReader = null;
        String[] attributeNameList = null;
        String[] attributeValueList;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
            if (type.equalsIgnoreCase(Constants.TYPE_CSV)) {
                CSVConfig csvConfig = new Gson().fromJson(config, CSVConfig.class);
                String firstLine;
                if (csvConfig.isHeaderExist()) {
                    if (null != (firstLine = bufferedReader.readLine())) {
                        attributeNameList = firstLine.split(csvConfig.getDelimiter());
                    } else {
                        String message = "Error while reading the csv file, CSV file is empty ";
                        log.error(message);
                        errorResponse.addProperty(Constants.ERROR, message);
                        return Response
                                .serverError()
                                .entity(errorResponse)
                                .type(MediaType.APPLICATION_JSON)
                                .build();
                    }
                }
                if (null != (firstLine = bufferedReader.readLine())) {
                    String splitRegex = csvConfig.getDelimiter() + Constants.REGEX_TO_KEEP_QUOTES;
                    attributeValueList = firstLine.split(splitRegex, -1);
                    if (csvConfig.isHeaderExist() && attributeNameList.length != attributeValueList.length) {
                        String message = "Header attribute length : " + attributeNameList.length +
                                " and corresponding value length : " + attributeValueList.length + " does not match ";
                        log.error(message);
                        errorResponse.addProperty(Constants.ERROR, message);
                        return Response
                                .serverError()
                                .entity(errorResponse)
                                .type(MediaType.APPLICATION_JSON)
                                .build();
                    }
                    return Response.ok().entity(MetaInfoRetrieverUtils.createResponseForCSV(attributeNameList,
                            attributeValueList)).build();
                } else {
                    String message = "Error while reading the csv file, CSV file is empty ";
                    log.error(message);
                    errorResponse.addProperty(Constants.ERROR, message);
                    return Response
                            .serverError()
                            .entity(errorResponse)
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                }
            } else if (type.equalsIgnoreCase(Constants.TYPE_JSON)) {
                JSONConfig jsonConfig = new Gson().fromJson(config, JSONConfig.class);
                JsonParser jsonParser = new JsonParser();
                Object fileContent = jsonParser.parse(bufferedReader);
                if (!MetaInfoRetrieverUtils.isJsonValid(fileContent.toString())) {
                    String message = "Invalid Json String :" + fileContent.toString();
                    log.error(message);
                    errorResponse.addProperty(Constants.ERROR, message);
                    return Response
                            .serverError()
                            .entity(errorResponse)
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                }
                Object jsonObj = null;
                ReadContext readContext = JsonPath.parse(fileContent.toString());
                try {
                    jsonObj = readContext.read(jsonConfig.getEnclosingElement());
                } catch (PathNotFoundException e) {
                    String message = "Could not find an element using enclosing element " +
                            jsonConfig.getEnclosingElement() + " .Make sure enclosing element provided correctly." +
                            e.getMessage() + ".";
                    log.error(message);
                    errorResponse.addProperty(Constants.ERROR,
                            message);
                    return Response
                            .serverError()
                            .entity(errorResponse)
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                }

                if (jsonObj == null) {
                    String message = "Enclosing element " + jsonConfig.getEnclosingElement() + " cannot be found in " +
                            "the json string " + fileContent.toString() + ".";
                    log.error(message);
                    errorResponse.addProperty(Constants.ERROR,
                            message);
                    return Response
                            .serverError()
                            .entity(errorResponse)
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                }
                if (jsonObj instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) jsonObj;
                    return Response.ok().entity(MetaInfoRetrieverUtils.createResponseForJSON(jsonArray.get(0))).build();
                } else {
                    return Response.ok().entity(MetaInfoRetrieverUtils.createResponseForJSON(jsonObj)).build();
                }
            } else if (type.equalsIgnoreCase(Constants.TYPE_XML)) {
                XMLConfig xmlConfig = new Gson().fromJson(config, XMLConfig.class);
                Map<String, String> namespaceMap = null;
                AXIOMXPath enclosingElementSelectorPath = null;
                if (xmlConfig.getNamespaces() != null) {
                    namespaceMap = new HashMap<>();
                    MetaInfoRetrieverUtils.buildNamespaceMap(namespaceMap, xmlConfig.getNamespaces());
                }
                if (xmlConfig.getEnclosingElement() != null) {
                    try {
                        enclosingElementSelectorPath = new AXIOMXPath(xmlConfig.getEnclosingElement());
                        if (namespaceMap != null) {
                            for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
                                try {
                                    enclosingElementSelectorPath.addNamespace(entry.getKey(), entry.getValue());
                                } catch (JaxenException e) {
                                    String message = "Error occurred when adding namespace: " + entry.getKey() + ":" +
                                            entry.getValue() + " to XPath element: " + xmlConfig.getEnclosingElement();
                                    log.error(message, e);
                                    errorResponse.addProperty(Constants.ERROR, message);
                                    return Response
                                            .serverError()
                                            .entity(errorResponse)
                                            .type(MediaType.APPLICATION_JSON)
                                            .build();
                                }
                            }
                        }
                    } catch (JaxenException e) {
                        String message = "Could not get XPath from expression: " + xmlConfig.getEnclosingElement();
                        log.error(message, e);
                        errorResponse.addProperty(Constants.ERROR,
                                message);
                        return Response
                                .serverError()
                                .entity(errorResponse)
                                .type(MediaType.APPLICATION_JSON)
                                .build();
                    }

                }
                String line = bufferedReader.readLine();
                StringBuilder xmlFile = new StringBuilder();
                while (line != null) {
                    xmlFile.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
                OMElement rootOMElement;
                try {
                    rootOMElement = AXIOMUtil.stringToOM(xmlFile.toString());
                } catch (XMLStreamException | DeferredParsingException e) {
                    String message = "Error parsing read XML content : " + xmlFile + ". Reason: " + e.getMessage();
                    log.error(message, e);
                    errorResponse.addProperty(Constants.ERROR,
                            message);
                    return Response
                            .serverError()
                            .entity(errorResponse)
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                }
                if (enclosingElementSelectorPath != null) {
                    List enclosingNodeList;
                    try {
                        enclosingNodeList = enclosingElementSelectorPath.selectNodes(rootOMElement);
                        if (enclosingNodeList.size() == 0) {
                            String message = "Provided enclosing element " + xmlConfig.getEnclosingElement() +
                                    " did not match any xml node.";
                            log.error(message);
                            errorResponse.addProperty(Constants.ERROR,
                                    message);
                            return Response
                                    .serverError()
                                    .entity(errorResponse)
                                    .type(MediaType.APPLICATION_JSON)
                                    .build();
                        }
                    } catch (JaxenException e) {
                        String message = "Error occurred when selecting nodes from XPath: " +
                                xmlConfig.getEnclosingElement();
                        log.error(message, e);
                        errorResponse.addProperty(Constants.ERROR,
                                message);
                        return Response
                                .serverError()
                                .entity(errorResponse)
                                .type(MediaType.APPLICATION_JSON)
                                .build();
                    }
                    return Response.ok().entity(
                            MetaInfoRetrieverUtils.createResponseForXML((OMElement) enclosingNodeList.get(0))).build();

                } else {
                    return Response.ok().entity(
                            MetaInfoRetrieverUtils.createResponseForXML(rootOMElement)).build();
                }
            } else {
                String message = "File type provided : " + type + " is not supported.";
                log.error(message);
                errorResponse.addProperty(Constants.ERROR,
                        message);
                return Response
                        .serverError()
                        .entity(errorResponse)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        } catch (IOException e) {
            String message = "Cannot retrieve file attributes." + e.getMessage();
            errorResponse.addProperty(Constants.ERROR,
                    message);
            log.error(message, e);
            return Response
                    .serverError()
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            String message = "Error occurred while processing the file " + e.getMessage();
            errorResponse.addProperty(Constants.ERROR,
                    message);
            log.error(message, e);
            return Response
                    .serverError()
                    .entity(errorResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error("Cannot close the buffer reader. " + e.getMessage(), e);
                }
            }
        }
    }

    @POST
    @Path("/connectToDatabase")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseConnection(JsonElement element) {
        JsonObject jsonResponse = new JsonObject();
        JsonObject jsonInput = element.getAsJsonObject();
        Set<String> keys = jsonInput.keySet();
        dataStoreMap.clear();
        for (String key : keys) {
            dataStoreMap.put(key, jsonInput.get(key).toString().replaceAll("\"", ""));
        }
        if (dataStoreMap.containsKey(Constants.DATASOURCE_NAME)
                && (dataStoreMap.containsKey(Constants.DB_URL)
                || dataStoreMap.containsKey(Constants.DB_USERNAME)
                || dataStoreMap.containsKey(Constants.DB_PASSWORD))) {
            jsonResponse.addProperty(Constants.CONNECTION, Constants.FALSE);
            jsonResponse.addProperty(Constants.ERROR, "Provide valid data source configuration or " +
                    "jdbcURL, username and password only.");
            return Response
                    .serverError()
                    .entity(jsonResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } else if (dataStoreMap.containsKey(Constants.DATASOURCE_NAME)) {
            String[] dataSourceConfiguration =
                    getDataSourceConfiguration(dataStoreMap.get(Constants.DATASOURCE_NAME));
            if (dataSourceConfiguration == null) {
                jsonResponse.addProperty(Constants.CONNECTION, Constants.FALSE);
                jsonResponse.addProperty(Constants.ERROR, "Provide valid data source configuration or " +
                        "jdbcURL, username and password only.");
                return Response
                        .serverError()
                        .entity(jsonResponse)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            } else {
                dataStoreMap.put(Constants.DB_URL, dataSourceConfiguration[0]);
                dataStoreMap.put(Constants.DB_USERNAME, dataSourceConfiguration[1]);
                dataStoreMap.put(Constants.DB_PASSWORD, dataSourceConfiguration[2]);
            }
        }
        try {
            String[] splittedURL = dataStoreMap.get(Constants.DB_URL).split(Constants.COLLON);
            if (splittedURL[0].equalsIgnoreCase(Constants.JDBC) &&
                    (splittedURL[1].equalsIgnoreCase(Constants.MYSQL_DATABASE)
                            || splittedURL[1].equalsIgnoreCase(Constants.MSSQL_DATABASE)
                            || splittedURL[1].equalsIgnoreCase(Constants.ORACLE_DATABASE)
                            || splittedURL[1].equalsIgnoreCase(Constants.POSTGRESQL))) {
                if (splittedURL[1].equalsIgnoreCase(Constants.MYSQL_DATABASE)) {
                    Class.forName(Constants.MYSQL_DRIVER_CLASS_NAME);
                } else if (splittedURL[1].equalsIgnoreCase(Constants.MSSQL_DATABASE)) {
                    Class.forName(Constants.MSSQL_DRIVER_CLASS_NAME);
                } else if (splittedURL[1].equalsIgnoreCase(Constants.ORACLE_DATABASE)) {
                    Class.forName(Constants.ORACLE_DRIVER_CLASS_NAME);
                } else if (splittedURL[1].equalsIgnoreCase(Constants.POSTGRESQL)) {
                    Class.forName(Constants.POSTGRESQL_DRIVER_CLASS_NAME);
                }
                Connection conn = DriverManager.getConnection(dataStoreMap.get(Constants.DB_URL),
                        dataStoreMap.get(Constants.DB_USERNAME),
                        dataStoreMap.get(Constants.DB_PASSWORD));
                conn.close();
            } else {
                jsonResponse.addProperty(Constants.CONNECTION, Constants.FALSE);
                jsonResponse.addProperty(Constants.ERROR, "Unsupported schema. Expected schema: " +
                        "mysql, sqlserver, oracle and postgresql. But found: "
                        + splittedURL[0] + ":" + splittedURL[1]);
                return Response
                        .serverError()
                        .entity(jsonResponse)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            jsonResponse.addProperty(Constants.CONNECTION, Constants.TRUE);
            return Response
                    .status(Response.Status.OK)
                    .entity(jsonResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (SQLException | ClassNotFoundException e) {
            jsonResponse.addProperty(Constants.CONNECTION, Constants.FALSE);
            jsonResponse.addProperty(Constants.ERROR, e.getMessage());
            return Response
                    .serverError()
                    .entity(jsonResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @POST
    @Path("/retrieveTableNames")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseTables(JsonElement element) {
        Response response = getDatabaseConnection(element);
        if (response.getStatus() == 200) {
            JsonObject jsonResponse = new JsonObject();
            try {
                Connection conn = getDbConnection(dataStoreMap.get(Constants.DB_URL),
                        dataStoreMap.get(Constants.DB_USERNAME), dataStoreMap.get(Constants.DB_PASSWORD));
                DatabaseMetaData dbMetadata = conn.getMetaData();
                ResultSet rs = dbMetadata.getTables(conn.getCatalog(), null, "%", null);
                JsonArray tableNames = new JsonArray();
                while (rs.next()) {
                    tableNames.add(new JsonPrimitive(rs.getString(3)));
                }
                jsonResponse.add(Constants.TABLES, tableNames);
                return Response
                        .status(Response.Status.OK)
                        .entity(jsonResponse)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            } catch (SQLException e) {
                jsonResponse.addProperty(Constants.ERROR, e.getMessage());
                return Response
                        .serverError()
                        .entity(jsonResponse)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        } else {
            return response;
        }
    }

    @POST
    @Path("/retrieveTableColumnNames")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseDetails(JsonElement element) {
        Response response = getDatabaseConnection(element);
        if (response.getStatus() == 200) {
            JsonObject jsonResponse = new JsonObject();
            if (!dataStoreMap.containsKey(Constants.TABLE_NAME)) {
                jsonResponse.addProperty(Constants.CONNECTION, Constants.FALSE);
                jsonResponse.addProperty(Constants.ERROR, "Provide jdbcURL, username, password and " +
                        "table name.");
                return Response
                        .serverError()
                        .entity(jsonResponse)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            try {
                Connection conn = getDbConnection(dataStoreMap.get(Constants.DB_URL),
                        dataStoreMap.get(Constants.DB_USERNAME), dataStoreMap.get(Constants.DB_PASSWORD));
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rsColumns = meta.getColumns(conn.getCatalog(), null, dataStoreMap.get(Constants.TABLE_NAME),
                        null);
                JsonArray tableNamesArray = new JsonArray();
                while (rsColumns.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty(Constants.NAME, rsColumns.getString(Constants.COLUMN_NAME));
                    object.addProperty(Constants.DATA_TYPE, rsColumns.getString(Constants.TYPE_NAME));
                    tableNamesArray.add(object);
                }
                jsonResponse.add(Constants.ATTRIBUTES, tableNamesArray);
                return Response
                        .status(Response.Status.OK)
                        .entity(jsonResponse)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            } catch (SQLException e) {
                jsonResponse.addProperty(Constants.ERROR, e.getMessage());
                return Response
                        .serverError()
                        .entity(jsonResponse)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        } else {
            return response;
        }
    }

    @GET
    @Path("/error-handler/server/siddhi-apps")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSiddhiAppList(@HeaderParam("serverHost") String host, @HeaderParam("serverPort") String port,
                                     @HeaderParam("username") String username,
                                     @HeaderParam("password") String password) {
        ErrorHandlerApiHelper errorHandlerApiHelper = new ErrorHandlerApiHelper();
        String hostAndPort = host + ":" + port;
        try {
            return Response.ok()
                .entity(errorHandlerApiHelper.getSiddhiAppList(hostAndPort, username, password)).build();
        } catch (ErrorHandlerServiceStubException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/error-handler/error-entries/count")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTotalErrorEntriesCount(@HeaderParam("serverHost") String host,
                                              @HeaderParam("serverPort") String port,
                                              @HeaderParam("username") String username,
                                              @HeaderParam("password") String password) {
        ErrorHandlerApiHelper errorHandlerApiHelper = new ErrorHandlerApiHelper();
        String hostAndPort = host + ":" + port;
        try {
            return Response.ok()
                .entity(errorHandlerApiHelper.getTotalErrorEntriesCount(hostAndPort, username, password)).build();
        } catch (ErrorHandlerServiceStubException e) {
            return Response.serverError().entity(e).build();
        }
    }

    @GET
    @Path("/error-handler/error-entries/count")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErrorEntriesCount(@QueryParam("siddhiApp") String siddhiAppName,
                                         @HeaderParam("serverHost") String host, @HeaderParam("serverPort") String port,
                                         @HeaderParam("username") String username,
                                         @HeaderParam("password") String password) {
        ErrorHandlerApiHelper errorHandlerApiHelper = new ErrorHandlerApiHelper();
        String hostAndPort = host + ":" + port;
        try {
            return Response.ok().entity(
                errorHandlerApiHelper.getErrorEntriesCount(siddhiAppName, hostAndPort, username, password)).build();
        } catch (ErrorHandlerServiceStubException e) {
            return Response.serverError().entity(e).build();
        }
    }

    @GET
    @Path("/error-handler/error-entries")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMinimalErrorEntries(@QueryParam("siddhiApp") String siddhiAppName,
                                           @QueryParam("limit") String limit, @QueryParam("offset") String offset,
                                           @HeaderParam("serverHost") String host,
                                           @HeaderParam("serverPort") String port,
                                           @HeaderParam("username") String username,
                                           @HeaderParam("password") String password) {
        ErrorHandlerApiHelper errorHandlerApiHelper = new ErrorHandlerApiHelper();
        String hostAndPort = host + ":" + port;
        try {
            JsonArray jsonArray = errorHandlerApiHelper.getMinimalErrorEntries(siddhiAppName, limit, offset,
                hostAndPort, username, password);
            return Response.ok().entity(jsonArray).build();
        } catch (ErrorHandlerServiceStubException e) {
            return Response.serverError().entity(e).build();
        }
    }

    @GET
    @Path("/error-handler/error-entries/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDescriptiveErrorEntry(@PathParam("id") String id, @HeaderParam("serverHost") String host,
                                             @HeaderParam("serverPort") String port,
                                             @HeaderParam("username") String username,
                                             @HeaderParam("password") String password) {
        ErrorHandlerApiHelper errorHandlerApiHelper = new ErrorHandlerApiHelper();
        String hostAndPort = host + ":" + port;
        try {
            JsonObject jsonObject = errorHandlerApiHelper.getDescriptiveErrorEntry(id, hostAndPort, username, password);
            return Response.ok().entity(jsonObject).build();
        } catch (ErrorHandlerServiceStubException e) {
            return Response.serverError().entity(e).build();
        }
    }

    @POST
    @Path("/error-handler")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response replayErrorEntry(JsonArray payload, @HeaderParam("serverHost") String host,
                                     @HeaderParam("serverPort") String port, @HeaderParam("username") String username,
                                     @HeaderParam("password") String password) {
        ErrorHandlerApiHelper errorHandlerApiHelper = new ErrorHandlerApiHelper();
        String hostAndPort = host + ":" + port;
        try {
            boolean isSuccess = errorHandlerApiHelper.replay(payload, hostAndPort, username, password);
            if (isSuccess) {
                return Response.ok().entity(new Gson().toJson("{}")).build();
            }
            return Response.serverError().entity("There were failures during the replay.").build();
        } catch (ErrorHandlerServiceStubException e) {
            return Response.serverError().entity(e).build();
        }
    }

    @DELETE
    @Path("/error-handler/error-entries/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response discardErrorEntry(@PathParam("id") String id, @HeaderParam("serverHost") String host,
                                      @HeaderParam("serverPort") String port, @HeaderParam("username") String username,
                                      @HeaderParam("password") String password) {
        ErrorHandlerApiHelper errorHandlerApiHelper = new ErrorHandlerApiHelper();
        String hostAndPort = host + ":" + port;
        try {
            boolean isSuccess = errorHandlerApiHelper.discardErrorEntry(id, hostAndPort, username, password);
            if (isSuccess) {
                return Response.ok().entity(new Gson().toJson("{}")).build();
            }
            return Response.serverError().entity(String.format("Failed to discard error entry: %s.", id)).build();
        } catch (ErrorHandlerServiceStubException e) {
            return Response.serverError().entity(e).build();
        }
    }

    @DELETE
    @Path("/error-handler/error-entries")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response discardErrorEntries(@QueryParam("siddhiApp") String siddhiAppName,
                                        @QueryParam("retentionDays") String retentionDays,
                                        @HeaderParam("serverHost") String host, @HeaderParam("serverPort") String port,
                                        @HeaderParam("username") String username,
                                        @HeaderParam("password") String password) {
        ErrorHandlerApiHelper errorHandlerApiHelper = new ErrorHandlerApiHelper();
        String hostAndPort = host + ":" + port;
        if (siddhiAppName != null && retentionDays == null) {
            try {
                boolean isSuccess =
                    errorHandlerApiHelper.discardErrorEntries(siddhiAppName, hostAndPort, username, password);
                if (isSuccess) {
                    return Response.ok().entity(new Gson().toJson("{}")).build();
                }
                return Response.serverError().entity(
                    String.format("Failed to discard error entries for Siddhi app: %s.", siddhiAppName)).build();
            } catch (ErrorHandlerServiceStubException e) {
                return Response.serverError().entity(e).build();
            }
        } else if (retentionDays != null && siddhiAppName == null) {
            try {
                boolean isSuccess =
                    errorHandlerApiHelper.doPurge(Integer.parseInt(retentionDays), hostAndPort, username, password);
                if (isSuccess) {
                    return Response.ok().entity(new Gson().toJson("{}")).build();
                }
                return Response.serverError().entity("Failed to purge the error store.").build();
            } catch (ErrorHandlerServiceStubException e) {
                return Response.serverError().entity(e).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("Exactly one of the following query parameters is required: 'siddhiApp', 'retentionDays'.").build();
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

//    @Reference(
//            name = "siddhi-manager-service",
//            service = SiddhiManager.class,
//            cardinality = ReferenceCardinality.MANDATORY,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unsetSiddhiManager"
//    )
//    protected void setSiddhiManager(SiddhiManager siddhiManager) {
//        EditorDataHolder.setSiddhiManager(siddhiManager);
//    }
//
//    protected void unsetSiddhiManager(SiddhiManager siddhiManager) {
//        EditorDataHolder.setSiddhiManager(null);
//    }
}
