package org.wso2.carbon.event.simulator.core.api;

import org.wso2.status.dashboard.api.annotations.ApiParam;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.factories.FilesApiServiceFactory;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;


import org.wso2.carbon.event.simulator.core.model.InlineResponse2001;

import java.nio.file.Paths;

import java.io.InputStream;

import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorMap;
import org.wso2.carbon.utils.Utils;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;


@Component(
        name = "simulator-core-file-services",
        service = Microservice.class,
        immediate = true
)
@Path("/simulation/files")
@org.wso2.status.dashboard.api.annotations.Api(description = "the files API")
@javax.annotation.Generated(value = "org.wso2.status.dashboard.core.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public class FilesApi implements Microservice {
    private final FilesApiService delegate = FilesApiServiceFactory.getFilesApi();
    private static final Logger log = LoggerFactory.getLogger(FilesApi.class);
    @DELETE
    @Path("/{fileName}")
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "Update CSV file to simulate event flow", notes = "",
                                         response = void.class, tags = {"simulator",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 200, message = "Successfully deleted the csv file",
                                                response = void.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 404,
                                                message = "No event simulation configuration available under "
                                                        + "simulation name",
                                                response = void.class)})
    public Response deleteFile( @ApiParam(value = "CSV File for name to delete", required = true)
                                @PathParam ("fileName") String fileName) throws NotFoundException {
        return delegate.deleteFile(fileName);
    }

    @GET
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "Get CSV file names", notes = "", response = InlineResponse2001.class,
                                         tags = {"simulator",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 200, message = "Successfully retrieved file names",
                                                response = InlineResponse2001.class),
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 404,
                                                message = "No event simulation configuration available under "
                                                        + "simulation name",
                                                response = InlineResponse2001.class)})
    public Response getFileNames() throws NotFoundException {
        return delegate.getFileNames();
    }

    @PUT
    @Path("/{fileName}")
    @Consumes({"multipart/form-data"})
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "Update CSV file to simulate event flow", notes = "",
                                         response = void.class, tags = {"simulator",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 200, message = "Successfully updated the csv file",
                                                response = void.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 404,
                                                message = "No event simulation configuration available under "
                                                        + "simulation name",
                                                response = void.class)})
    public Response updateFile(@ApiParam(value = "CSV File for name to update", required = true)
                                   @PathParam("fileName") String fileName,
                               @FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FileInfo fileDetail)
            throws NotFoundException {
        return delegate.updateFile(fileName, fileInputStream, fileDetail);
    }

    @POST

    @Consumes({"multipart/form-data"})
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "Upload CSV file to simulate event flow", notes = "",
                                         response = void.class, tags = {"simulator",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 200, message = "Successfully uploaded file",
                                                response = void.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 404,
                                                message = "No event simulation configuration available under simulation name",
                                                response = void.class)})
    public Response uploadFile(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FileInfo fileDetail) throws NotFoundException {
        return delegate.uploadFile(fileInputStream, fileDetail);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        //set maximum csv file size to 8MB
        EventSimulatorDataHolder.getInstance().setMaximumFileSize(8388608);
        EventSimulatorDataHolder.getInstance().setCsvFileDirectory(Paths.get(Utils.getCarbonHome().toString(),
                                                                             EventSimulatorConstants.DIRECTORY_DEPLOYMENT, EventSimulatorConstants.DIRECTORY_CSV_FILES).toString());
        log.info("Event Simulator file service component is activated");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        EventSimulatorMap.getInstance().stopAllActiveSimulations();
        log.info("Simulator service file component is deactivated");
    }
}
