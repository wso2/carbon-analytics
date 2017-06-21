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

package org.wso2.carbon.event.simulator.core.service;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.exception.InvalidFileException;
import org.wso2.carbon.event.simulator.core.internal.bean.DatabaseConnectionDetailsDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.SingleEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.util.FileUploader;
import org.wso2.carbon.event.simulator.core.internal.generator.database.util.DatabaseConnector;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.event.simulator.core.service.bean.ActiveSimulatorData;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;
import org.wso2.carbon.utils.Utils;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Service component implements Microservices and provides services used for event simulation
 */
@Component(
        name = "event-simulator-core-services",
        service = Microservice.class,
        immediate = true
)
@Path("/simulation")
public class ServiceComponent implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private static final ExecutorService executorServices = Executors.newFixedThreadPool(10);

    /**
     * Send single event for simulation
     *
     * @param singleEventConfiguration jsonString to be converted to SingleEventSimulationDTO object.
     * @return response
     * @throws InvalidConfigException          if the simulation configuration contains invalid data
     * @throws InsufficientAttributesException if the number of attributes specified for the event is not equal to
     *                                         the number of stream attributes
     * @throws ResourceNotFoundException       if a resource required for simulation is not found
     */
    @POST
    @Path("/single")
    @Produces("application/json")
    public Response singleEventSimulation(String singleEventConfiguration)
            throws InvalidConfigException, InsufficientAttributesException, ResourceNotFoundException {
        SingleEventGenerator.sendEvent(singleEventConfiguration);
        return Response.ok().header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.OK, "Single Event simulation started successfully"))
                .build();
    }

    /**
     * service used to provide the methods allowed for URL 'simulation/feed/{simulationName}'
     *
     * @return response ok with the methods allowed
     */
    @OPTIONS
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response allowedOptionsForSimulations() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "OPTIONS, POST, GET, PUT, DELETE")
                .build();
    }


    /**
     * service used to upload feed simulation configuration to the system
     *
     * @param simulationConfiguration jsonString to be converted to EventSimulationDto object
     * @return Response
     * @throws InvalidConfigException          if the simulation configuration does not contain a simulation name
     * @throws InsufficientAttributesException if the source configuration cannot generate values for all them stream
     *                                         attributes
     * @throws FileAlreadyExistsException      if a configuration already exists in the system under the given
     *                                         simulation name
     * @throws FileOperationsException         if an IOException occurs while uploading the simulation configuration
     */
    @POST
    @Path("/feed")
    @Produces("application/json")
    public Response uploadFeedSimulationConfig(String simulationConfiguration)
            throws InvalidConfigException, InsufficientAttributesException, FileOperationsException,
            FileAlreadyExistsException {
        SimulationConfigUploader simulationConfigUploader = SimulationConfigUploader.getConfigUploader();
        if (!EventSimulatorMap.getInstance().getActiveSimulatorMap().containsKey(
                simulationConfigUploader.getSimulationName(simulationConfiguration))) {
            try {
                EventSimulator.validateSimulationConfig(simulationConfiguration);
            } catch (ResourceNotFoundException e) {
//                do nothing
            }
            simulationConfigUploader.uploadSimulationConfig(simulationConfiguration,
                    (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                            EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
            return Response.status(Response.Status.CREATED)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.CREATED, "Successfully uploaded simulation " +
                            "configuration '" + simulationConfigUploader.getSimulationName(simulationConfiguration) +
                            "'"))
                    .build();

        } else {
            return Response.status(Response.Status.CONFLICT)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.CONFLICT, "A simulation already exists under " +
                            "the name '" + simulationConfigUploader.getSimulationName(simulationConfiguration) + "'"))
                    .build();
        }
    }

    /**
     * SimulationConfigUploader.getConfigUploader().uploadSimulationConfig(simulationConfiguration,
     * (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
     * EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
     * service used to update an uploaded simulation configuration
     *
     * @param simulationName          unique identifies of the simulation configuration
     * @param simulationConfigDetails new simulation configuration
     * @return response
     * @throws InvalidConfigException          if the simulation configuration does not contain a simulation name
     * @throws FileOperationsException         if an IOException occurs while uploading the simulation configuration
     * @throws InsufficientAttributesException if a configuration cannot generate values for all stream attributes
     */
    @PUT
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response updateFeedSimulationConfig(@PathParam("simulationName") String simulationName, String
            simulationConfigDetails) throws InvalidConfigException, InsufficientAttributesException,
            FileOperationsException, FileAlreadyExistsException {
        SimulationConfigUploader simulationConfigUploader = SimulationConfigUploader.getConfigUploader();
        if (simulationConfigUploader.checkSimulationExists(simulationName, Paths.get(Utils.getCarbonHome().toString(),
                EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS).toString())) {
            try {
                EventSimulator.validateSimulationConfig(simulationConfigDetails);
            } catch (ResourceNotFoundException e) {
//                do nothing
            }
            boolean deleted = simulationConfigUploader.deleteSimulationConfig(simulationName,
                    (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                            EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
            if (deleted) {
                simulationConfigUploader.uploadSimulationConfig(simulationConfigDetails,
                        (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
//                todo remove when in same port
                return Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.OK, "Successfully updated simulation " +
                                "configuration '" + simulationName + "'."))
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation" +
                                " configuration available under simulation name '" + simulationName + "'"))
                        .build();
            }

        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation" +
                            " configuration available under simulation name '" + simulationName + "'"))
                    .build();
        }
    }

    /**
     * service used to retrieve a simulation configuration
     *
     * @param simulationName name of simulation configuration being retrieved
     * @return response
     * @throws FileOperationsException if an IOException occurs when reading the configuration file
     */
    @GET
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response getFeedSimulationConfig(@PathParam("simulationName") String simulationName) throws
            FileOperationsException {
        if (EventSimulatorMap.getInstance().getActiveSimulatorMap().containsKey(simulationName)) {
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.OK, "Simulation configuration : " +
                            new JSONObject(SimulationConfigUploader.getConfigUploader()
                                    .getSimulationConfig(simulationName, (Paths.get(Utils.getCarbonHome().toString(),
                                            EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                            EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString()))))
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No simulation configuration" +
                            "available under simulation name '" + simulationName + "'"))
                    .build();
        }
    }

    /**
     * service used to delete a simulation configuration from the system
     *
     * @param simulationName name of simulation being deleted
     * @return response
     * @throws FileOperationsException if an IOException occurs while deleting simulation configuration file
     */
    @DELETE
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response deleteFeedSimulationConfig(@PathParam("simulationName") String simulationName) throws
            FileOperationsException {
        boolean deleted = SimulationConfigUploader.getConfigUploader().deleteSimulationConfig(simulationName,
                (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                        EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
        if (deleted) {
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
//                    .header("Access-Control-Allow-Headers", "")
                    .entity(new ResponseMapper(Response.Status.OK, "Successfully deleted simulation " +
                            "configuration '" + simulationName + "'"))
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation configuration" +
                            " available under simulation name '" + simulationName + "'"))
                    .build();
        }
    }

    /**
     * service used to change status of a simulation
     *
     * @param simulationName name of simulation whose status is being changed
     * @return response
     * @throws InvalidConfigException          if the simulation configuration contains invalid data
     * @throws InsufficientAttributesException if the simulation configuration cannot produce events with values for
     *                                         all stream attributes
     * @throws FileOperationsException         if an IOException occurs during file manipulations
     */
    @POST
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response changeSimulationStatus(@PathParam("simulationName") String simulationName, @QueryParam("action")
            String action) throws InvalidConfigException, InsufficientAttributesException, FileOperationsException {
        if (action != null && !action.isEmpty()) {
            try {
                switch (EventSimulator.Action.valueOf(action.toUpperCase(Locale.ENGLISH))) {
                    case RUN:
                        return run(simulationName);
                    case PAUSE:
                        return pause(simulationName);
                    case RESUME:
                        return resume(simulationName);
                    case STOP:
                        return stop(simulationName);
                    default:
                        /**
                         *  this statement is never reached since action is an enum. Nevertheless a response is added
                         *  since the method signature mandates it
                         */
                        return Response.status(Response.Status.BAD_REQUEST)
                                .header("Access-Control-Allow-Origin", "*")
                                .entity(new ResponseMapper(Response.Status.BAD_REQUEST, "Invalid action '" +
                                        action + "' specified for simulation '" + simulationName + "'. Actions " +
                                        "supported are " + EventSimulator.Action.RUN + ", " +
                                        EventSimulator.Action.PAUSE + ", " + EventSimulator.Action.RESUME + ", " +
                                        EventSimulator.Action.STOP + "."))
                                .build();
                }
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.BAD_REQUEST, "Invalid action '" + action +
                                "' specified for simulation '" + simulationName + "'. Actions supported are '" +
                                EventSimulator.Action.RUN + "', '" + EventSimulator.Action.PAUSE + "', '" +
                                EventSimulator.Action.RESUME + "', '" + EventSimulator.Action.STOP + "'."))
                        .build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.BAD_REQUEST, "Invalid action '" + action +
                            "' specified for simulation '" + simulationName + "'. Actions supported are '" +
                            EventSimulator.Action.RUN + "', '" + EventSimulator.Action.PAUSE + "', '" +
                            EventSimulator.Action.RESUME + "', '" + EventSimulator.Action.STOP + "'."))
                    .build();

        }
    }

    /**
     * run() is used to start a feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     * @throws InvalidConfigException          if the simulation configuration contains invalid data
     * @throws InsufficientAttributesException if the simulation configuration cannot produce events with values for
     *                                         all stream attributes
     * @throws FileOperationsException         if an IOException occurs during file manipulations
     */
    private Response run(String simulationName) throws FileOperationsException, InvalidConfigException,
            InsufficientAttributesException {
        ActiveSimulatorData activeSimulatorData = EventSimulatorMap.getInstance().getActiveSimulatorMap()
                .get(simulationName);
        if (activeSimulatorData != null) {
            EventSimulator eventSimulator = activeSimulatorData.getEventSimulator();
            switch (eventSimulator.getStatus()) {
                case STOP:
                    executorServices.execute(eventSimulator);
                    return Response.ok()
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.OK, "Successfully started simulation" +
                                    " '" + simulationName + "'."))
                            .build();
                case PAUSE:
                    return Response.status(Response.Status.FORBIDDEN)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.FORBIDDEN, "Simulation '" +
                                    simulationName + "' is currently paused and cannot be restarted."))
                            .build();
                case RUN:
                    return Response.status(Response.Status.CONFLICT)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.CONFLICT, "Simulation '" +
                                    simulationName + "' is currently in progress and cannot be restarted."))
                            .build();
                default:
                    /**
                     *  this statement is never reached since status is an enum. Nevertheless a response is added
                     *  since the method signature mandates it
                     */
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, "Invalid " +
                                    "status '" + eventSimulator.getStatus() + "' allocated for simulation '" +
                                    simulationName + "'. Valid statuses are '" + EventSimulator.Status.RUN + "', '" +
                                    EventSimulator.Status.PAUSE + "', '" + EventSimulator.Status.STOP + "'."))
                            .build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND,
                            "No event simulation configuration available under simulation name '" + simulationName +
                                    "'."))
                    .build();
        }
    }

    /**
     * pause() is used to pause an ongoing feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     */
    private Response pause(String simulationName) {
        ActiveSimulatorData activeSimulatorData = EventSimulatorMap.getInstance().getActiveSimulatorMap()
                .get(simulationName);
        if (activeSimulatorData != null) {
            EventSimulator eventSimulator = activeSimulatorData.getEventSimulator();
            switch (eventSimulator.getStatus()) {
                case RUN:
                    eventSimulator.pause();
                    return Response.ok()
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.OK, "Successfully paused event " +
                                    "simulation '" + simulationName + "'"))
                            .build();
                case PAUSE:
                    return Response.status(Response.Status.CONFLICT)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.CONFLICT, "Simulation '" +
                                    simulationName + "' is already paused."))
                            .build();
                case STOP:
                    return Response.status(Response.Status.FORBIDDEN)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.FORBIDDEN, "Simulation '" +
                                    simulationName + "' is currently stopped, hence it cannot be paused."))
                            .build();
                default:
                    /**
                     *  this statement is never reached since status is an enum. Nevertheless a response is added
                     *  since the method signature mandates it
                     */
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, "Invalid" +
                                    " status '" + eventSimulator.getStatus() + "' allocated for simulation '" +
                                    simulationName + "'. Valid statuses are '" + EventSimulator.Status.RUN + "', '"
                                    + EventSimulator.Status.PAUSE + "', '" + EventSimulator.Status.STOP + "'."))
                            .build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation" +
                            " configuration available under simulation name '" + simulationName + "'."))
                    .build();
        }
    }

    /**
     * resume() is used to resume a paused feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     */
    private Response resume(String simulationName) {
        ActiveSimulatorData activeSimulatorData = EventSimulatorMap.getInstance().getActiveSimulatorMap()
                .get(simulationName);
        if (activeSimulatorData != null) {
            EventSimulator eventSimulator = activeSimulatorData.getEventSimulator();
            switch (eventSimulator.getStatus()) {
                case PAUSE:
                    eventSimulator.resume();
                    return Response.ok()
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.OK, "Successfully resumed event" +
                                    " simulation '" + simulationName + "'."))
                            .build();
                case RUN:
                    return Response.status(Response.Status.CONFLICT)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.CONFLICT, "Event simulation '" +
                                    simulationName + "' is currently in progress, hence it cannot be resumed."))
                            .build();
                case STOP:
                    return Response.status(Response.Status.FORBIDDEN)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.FORBIDDEN, "Event simulation '" +
                                    simulationName + "' is currently stopped, hence it cannot be resumed."))
                            .build();
                default:
                    /**
                     *  this statement is never reached since status is an enum. Nevertheless a response is added
                     *  since the method signature mandates it
                     */
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, "Invalid" +
                                    " status '" + eventSimulator.getStatus() + "' allocated for simulation '" +
                                    simulationName + "'. Valid statuses are '" + EventSimulator.Status.RUN + "', '" +
                                    EventSimulator.Status.PAUSE + "', '" + EventSimulator.Status.STOP + "'."))
                            .build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation " +
                            "configuration available under simulation name '" + simulationName + "'."))
                    .build();
        }
    }

    /**
     * stop() is used to stop an ongoing or paused feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     */
    private Response stop(String simulationName) {
        ActiveSimulatorData activeSimulatorData = EventSimulatorMap.getInstance().getActiveSimulatorMap()
                .get(simulationName);
        if (activeSimulatorData != null) {
            EventSimulator eventSimulator = activeSimulatorData.getEventSimulator();
            switch (eventSimulator.getStatus()) {
                case RUN:
                case PAUSE:
                    eventSimulator.stop();
                    return Response.ok()
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.OK, "Successfully stopped event " +
                                    "simulation '" + simulationName + "'."))
                            .build();
                case STOP:
                    return Response.status(Response.Status.CONFLICT)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.CONFLICT, "Event simulation '" +
                                    simulationName + "' is already stopped."))
                            .build();
                default:
                    /**
                     *  this statement is never reached since status is an enum. Nevertheless a response is added
                     *  since the method signature mandates it
                     */
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, "Invalid " +
                                    "status '" + eventSimulator.getStatus() + "' allocated for simulation '" +
                                    simulationName + "'. Valid statuses are '" + EventSimulator.Status.RUN + "', '" +
                                    EventSimulator.Status.PAUSE + "', '" + EventSimulator.Status.STOP + "'."))
                            .build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation " +
                            "configuration available under simulation name '" + simulationName + "'."))
                    .build();
        }
    }

    /**
     * service to upload csv files
     *
     * @param fileInfo        fileInfo of file being uploaded
     * @param fileInputStream inputStream of file
     * @return Response
     * @throws FileAlreadyExistsException if the file exists in 'deployment/csv-files' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'deployment/csv-files' directory
     * @throws InvalidFileException       if the file is not a csv file
     */
    @POST
    @Path("/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Response uploadFile(@FormDataParam("file") FileInfo fileInfo,
                               @FormDataParam("file") InputStream fileInputStream)
            throws FileAlreadyExistsException, FileOperationsException, InvalidFileException {
        FileUploader.getFileUploaderInstance().uploadFile(fileInfo, fileInputStream,
                (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                        EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
        return Response.status(Response.Status.CREATED)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.CREATED, "Successfully uploaded file '" +
                        fileInfo.getFileName() + "'"))
                .build();
    }

    /**
     * service to retrieve names of csv files
     *
     * @return Response
     * @throws FileOperationsException if an IOException occurs while retrieving file names
     */
    @GET
    @Path("/files")
    @Produces("application/json")
    public Response retrieveFileNamesList() throws FileOperationsException {
        return Response.status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .entity(
                        FileUploader.getFileUploaderInstance()
                                .retrieveFileNameList(EventSimulatorConstants.CSV_FILE_EXTENSION,
                                        (Paths.get(Utils.getCarbonHome().toString(),
                                                EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                EventSimulatorConstants.DIRECTORY_CSV_FILES)))
                )
                .build();
    }

    /**
     * service used to provide the methods allowed for URL 'simulation/files/{fileName}'
     *
     * @return response ok with the methods allowed
     */
    @OPTIONS
    @Path("/files/{fileName}")
    @Produces("application/json")
    public Response allowedOptionsForCSVFiles() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "OPTIONS, POST, PUT, DELETE")
                .build();
    }

    /**
     * service to modify an uploaded csv files
     *
     * @param fileName        name of file being replaced
     * @param fileInfo        fileInfo of file being uploaded
     * @param fileInputStream inputStream of file
     * @return Response
     * @throws FileAlreadyExistsException this exception is not thrown since the file available under 'fileName' will
     *                                    be deleted prior to uploading the new file. Nevertheless this is included
     *                                    in the method signature as it is a checked exception used when uploading a
     *                                    file
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'deployment/csv-files' directory
     */
    @PUT
    @Path("/files/{fileName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Response updateFile(@PathParam("fileName") String fileName, @FormDataParam("file") FileInfo fileInfo,
                               @FormDataParam("file") InputStream fileInputStream)
            throws FileAlreadyExistsException, FileOperationsException, InvalidFileException {
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        if (FilenameUtils.isExtension(fileName, EventSimulatorConstants.CSV_FILE_EXTENSION)) {
            if (FilenameUtils.isExtension(fileInfo.getFileName(), EventSimulatorConstants.CSV_FILE_EXTENSION)) {
                if (fileUploader.validateFileExists(fileName)) {
                    boolean deleted = fileUploader.deleteFile(fileName, (Paths.get(Utils.getCarbonHome().toString(),
                            EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                            EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
                    if (deleted) {
                        fileUploader.uploadFile(fileInfo, fileInputStream,
                                (Paths.get(Utils.getCarbonHome().toString(),
                                        EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                        EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
                        return Response.ok()
                                .header("Access-Control-Allow-Origin", "*")
                                .entity(new ResponseMapper(Response.Status.OK, "Successfully updated CSV" +
                                        " file '" + fileName + "' with file '" + fileInfo.getFileName() + "'."))
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .header("Access-Control-Allow-Origin", "*")
                                .entity(new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName +
                                        "' does not exist"))
                                .build();
                    }
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName + "' " +
                                    "does not exist."))
                            .build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.BAD_REQUEST, "File '" + fileName + "' is" +
                                " not a CSV file"))
                        .build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.BAD_REQUEST, "File '" + fileName + "' is" +
                            " not a CSV file"))
                    .build();
        }
    }

    /**
     * Delete the file
     *
     * @param fileName File Name
     * @return Response
     * @throws FileOperationsException if an IOException occurs while deleting file
     */
    @DELETE
    @Path("/files/{fileName}")
    @Produces("application/json")
    public Response deleteFile(@PathParam("fileName") String fileName) throws FileOperationsException {
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        if (FilenameUtils.isExtension(fileName, EventSimulatorConstants.CSV_FILE_EXTENSION)) {
            boolean deleted = fileUploader.deleteFile(fileName, (Paths.get(Utils.getCarbonHome().toString(),
                    EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                    EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
            if (deleted) {
                return Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.OK, "Successfully deleted file '" +
                                fileName + "'"))
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName +
                                "' does not exist"))
                        .build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName +
                            "' is not a CSV file."))
                    .build();
        }
    }


    /**
     * service used to provide the methods allowed for URL 'simulation/connectToDatabase'
     *
     * @return response ok with the methods allowed
     */
    @OPTIONS
    @Path("/connectToDatabase")
    @Produces("application/json")
    public Response allowedOptionsForTestDatabaseConnection() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                .header("Access-Control-Allow-Methods", "OPTIONS, POST")
                .build();
    }

    /**
     * service used to test a database connection
     *
     * @param connectionDetails connection details(data source location, driver, username, password)
     * @return Response
     * */
    @POST
    @Path("/connectToDatabase")
    @Consumes("application/json")
    @Produces("application/json")
    public Response testDatabaseConnection(DatabaseConnectionDetailsDTO connectionDetails) {
        try {
            DatabaseConnector.testDatabaseConnection(connectionDetails.getDriver(),
                    connectionDetails.getDataSourceLocation(), connectionDetails.getUsername(),
                    connectionDetails.getPassword());
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(new ResponseMapper(Response.Status.OK, "Successfully connected to datasource '"
                            + connectionDetails.getDataSourceLocation() + "'."))
                    .build();
        } catch (Throwable e) {
//            if any exception occurs, inform client that the database connection failed
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(new ResponseMapper(Response.Status.BAD_REQUEST, e.getMessage()))
                    .build();
        }
    }

    /**
     * service used to provide the methods allowed for URL 'simulation/connectToDatabase/retrieveTableNames'
     *
     * @return response ok with the methods allowed
     */
    @OPTIONS
    @Path("/connectToDatabase/retrieveTableNames")
    @Produces("application/json")
    public Response allowedOptionsForRetrieveTableNames() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                .header("Access-Control-Allow-Methods", "OPTIONS, POST")
                .build();
    }

    /**
     * service used to retrieve names of tables in a database
     *
     * @param connectionDetails connection details(data source location, driver, username, password)
     * @return Response
     * */
    @POST
    @Path("/connectToDatabase/retrieveTableNames")
    @Consumes("application/json")
    @Produces("application/json")
    public Response retrieveTableNames(DatabaseConnectionDetailsDTO connectionDetails) {
        try {
            List<String> tableNames = DatabaseConnector.retrieveTableNames(connectionDetails.getDriver(),
                    connectionDetails.getDataSourceLocation(), connectionDetails.getUsername(),
                    connectionDetails.getPassword());
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(tableNames)
                    .build();
        } catch (Throwable e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(new ResponseMapper(Response.Status.BAD_REQUEST, e.getMessage()))
                    .build();
        }
    }

    /**
     * service used to provide the methods allowed for URL
     * 'simulation/connectToDatabase/{tableName}/retrieveColumnNames'
     *
     * @return response ok with the methods allowed
     */
    @OPTIONS
    @Path("/connectToDatabase/{tableName}/retrieveColumnNames")
    @Produces("application/json")
    public Response allowedOptionsForRetrieveColumnNames() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                .header("Access-Control-Allow-Methods", "OPTIONS, POST")
                .build();
    }


    /**
     * service used to retrieve column names of a table in a database
     *
     * @param connectionDetails connection details(data source location, driver, username, password)
     * @param tableName table from which columns must be retrieved
     * @return Response
     * */
    @POST
    @Path("/connectToDatabase/{tableName}/retrieveColumnNames")
    @Consumes("application/json")
    @Produces("application/json")
    public Response retrieveColumnNames(@PathParam("tableName") String tableName, DatabaseConnectionDetailsDTO
            connectionDetails) {
        try {
            List<String> columnNames = DatabaseConnector.retrieveColumnNames(connectionDetails.getDriver(),
                    connectionDetails.getDataSourceLocation(), connectionDetails.getUsername(),
                    connectionDetails.getPassword(), tableName);
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(columnNames)
                    .build();
        } catch (Throwable e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(new ResponseMapper(Response.Status.BAD_REQUEST, e.getMessage()))
                    .build();
        }
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
//        set maximum csv file size to 8MB
        EventSimulatorDataHolder.getInstance().setMaximumFileSize(8388608);
        EventSimulatorDataHolder.getInstance().setCsvFileDirectory(Paths.get(Utils.getCarbonHome().toString(),
                EventSimulatorConstants.DIRECTORY_DEPLOYMENT, EventSimulatorConstants.DIRECTORY_CSV_FILES).toString());
        log.info("Event Simulator service component is activated");
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
        log.info("Simulator service component is deactivated");
    }

    /**
     * This bind method will be called when a class is registered against EventStreamService interface of stream
     * processor
     */
    @Reference(
            name = "event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "stopEventStreamService"
    )
    protected void eventStreamService(EventStreamService eventStreamService) {
        EventSimulatorDataHolder.getInstance().setEventStreamService(eventStreamService);
        if (log.isDebugEnabled()) {
            log.info("@Reference(bind) EventStreamService");
        }

    }

    /**
     * This is the unbind method which gets called at the un-registration of eventStream OSGi service.
     */
    protected void stopEventStreamService(EventStreamService eventStreamService) {
        EventSimulatorDataHolder.getInstance().setEventStreamService(null);
        if (log.isDebugEnabled()) {
            log.info("@Reference(unbind) EventStreamService");
        }

    }
}
