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
import org.wso2.carbon.event.simulator.core.exception.FileNotFoundException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.exception.InvalidFileException;
import org.wso2.carbon.event.simulator.core.internal.generator.SingleEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.util.FileUploader;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.utils.Utils;
import org.wso2.msf4j.Microservice;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
     */
    @POST
    @Path("/single")
    @Produces("application/json")
    public Response singleEventSimulation(String singleEventConfiguration)
            throws InvalidConfigException, InsufficientAttributesException {
        SingleEventGenerator.sendEvent(singleEventConfiguration);
        return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Single Event simulation " +
                "started successfully")).build();
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
        if (!EventSimulationMap.getSimulatorMap().containsKey(simulationConfigUploader
                .getSimulationName(simulationConfiguration))) {
            EventSimulator.validateSimulationConfig(simulationConfiguration);
            simulationConfigUploader.uploadSimulationConfig(simulationConfiguration,
                    (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                            EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
            return Response.status(Response.Status.CREATED).entity(
                    new ResponseMapper(Response.Status.CREATED, "Successfully uploaded simulation " +
                            "configuration '" + simulationConfigUploader.getSimulationName(simulationConfiguration) +
                            "'")).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(
                    new ResponseMapper(Response.Status.CONFLICT, "A simulation already exists under the name "
                            + "'" + simulationConfigUploader.getSimulationName(simulationConfiguration) + "'")).build();
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
        EventSimulator.validateSimulationConfig(simulationConfigDetails);
        SimulationConfigUploader simulationConfigUploader = SimulationConfigUploader.getConfigUploader();
        boolean deleted = simulationConfigUploader.deleteSimulationConfig(simulationName,
                (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                        EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
        if (deleted) {
            simulationConfigUploader.uploadSimulationConfig(simulationConfigDetails,
                    (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                            EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully updated " +
                    "simulation configuration '" + simulationName + "'.")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation configuration " +
                            "available under simulation name '" + simulationName + "'")).build();
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
        if (EventSimulationMap.getSimulatorMap().containsKey(simulationName)) {
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Simulation " +
                    "configuration : " + new JSONObject(SimulationConfigUploader.getConfigUploader()
                    .getSimulationConfig(simulationName, (Paths.get(Utils.getCarbonHome().toString(),
                            EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                            EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString())))).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No simulation configuration" +
                            "available under simulation name '" + simulationName + "'")).build();
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
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully " +
                    "deleted simulation configuration '" + simulationName + "'")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation configuration " +
                            "available under simulation name '" + simulationName + "'")).build();
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
                        return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMapper(
                                Response.Status.BAD_REQUEST, "Invalid action '" + action + "' specified for " +
                                "simulation '" + simulationName + "'. Actions supported are " +
                                EventSimulator.Action.RUN + ", " + EventSimulator.Action.PAUSE + ", " +
                                EventSimulator.Action.RESUME + ", " + EventSimulator.Action.STOP + ".")).build();
                }
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMapper(
                        Response.Status.BAD_REQUEST, "Invalid action '" + action + "' specified for " +
                        "simulation '" + simulationName + "'. Actions supported are '" + EventSimulator.Action.RUN +
                        "', '" + EventSimulator.Action.PAUSE + "', '" + EventSimulator.Action.RESUME + "', '" +
                        EventSimulator.Action.STOP + "'.")).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMapper(
                    Response.Status.BAD_REQUEST, "Invalid action '" + action + "' specified for " +
                    "simulation '" + simulationName + "'. Actions supported are '" + EventSimulator.Action.RUN +
                    "', '" + EventSimulator.Action.PAUSE + "', '" + EventSimulator.Action.RESUME + "', '" +
                    EventSimulator.Action.STOP + "'.")).build();

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
        if (EventSimulationMap.getSimulatorMap().containsKey(simulationName)) {
            EventSimulator eventSimulator = EventSimulationMap.getSimulatorMap().get(simulationName);
            switch (eventSimulator.getStatus()) {
                case STOP:
                    executorServices.execute(eventSimulator);
                    return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully started" +
                            " simulation '" + simulationName + "'.")).build();
                case PAUSE:
                    return Response.status(Response.Status.FORBIDDEN).entity(new ResponseMapper(
                            Response.Status.FORBIDDEN, "Simulation '" + simulationName + "' is currently" +
                            " paused and cannot be restarted.")).build();
                case RUN:
                    return Response.status(Response.Status.CONFLICT).entity(new ResponseMapper(Response.Status.CONFLICT,
                            "Simulation '" + simulationName + "' is currently in progress and cannot be " +
                                    "restarted.")).build();
                default:
                    /**
                     *  this statement is never reached since status is an enum. Nevertheless a response is added
                     *  since the method signature mandates it
                     */
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseMapper(
                            Response.Status.INTERNAL_SERVER_ERROR, "Invalid status '" +
                            eventSimulator.getStatus() + "' allocated for simulation '" + simulationName +
                            "'. Valid statuses are '" + EventSimulator.Status.RUN + "', '" + EventSimulator.Status.PAUSE
                            + "', '" + EventSimulator.Status.STOP + "'.")).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMapper(Response.Status.NOT_FOUND,
                    "No event simulation configuration available under simulation name '" + simulationName +
                            "'.")).build();
        }
    }

    /**
     * pause() is used to pause an ongoing feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     */
    private Response pause(String simulationName) {
        if (EventSimulationMap.getSimulatorMap().containsKey(simulationName)) {
            EventSimulator eventSimulator = EventSimulationMap.getSimulatorMap().get(simulationName);
            switch (eventSimulator.getStatus()) {
                case RUN:
                    eventSimulator.pause();
                    return Response.ok().entity(new ResponseMapper(Response.Status.OK,
                            "Successfully paused event simulation '" + simulationName + "'")).build();
                case PAUSE:
                    return Response.status(Response.Status.CONFLICT).entity(new ResponseMapper(Response.Status.CONFLICT,
                            "Simulation '" + simulationName + "' is already paused.")).build();
                case STOP:
                    return Response.status(Response.Status.FORBIDDEN).entity(new ResponseMapper(
                            Response.Status.FORBIDDEN, "Simulation '" + simulationName + "' is currently" +
                            " stopped, hence it cannot be paused.")).build();
                default:
                    /**
                     *  this statement is never reached since status is an enum. Nevertheless a response is added
                     *  since the method signature mandates it
                     */
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseMapper(
                            Response.Status.INTERNAL_SERVER_ERROR, "Invalid status '" +
                            eventSimulator.getStatus() + "' allocated for simulation '" + simulationName +
                            "'. Valid statuses are '" + EventSimulator.Status.RUN + "', '" + EventSimulator.Status.PAUSE
                            + "', '" + EventSimulator.Status.STOP + "'.")).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMapper(Response.Status.NOT_FOUND,
                    "No event simulation configuration available under simulation name '" + simulationName +
                            "'.")).build();
        }
    }

    /**
     * resume() is used to resume a paused feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     */
    private Response resume(String simulationName) {
        if (EventSimulationMap.getSimulatorMap().containsKey(simulationName)) {
            EventSimulator eventSimulator = EventSimulationMap.getSimulatorMap().get(simulationName);
            switch (eventSimulator.getStatus()) {
                case PAUSE:
                    eventSimulator.resume();
                    return Response.ok().entity(new ResponseMapper(Response.Status.OK,
                            "Successfully resumed event simulation '" + simulationName + "'.")).build();
                case RUN:
                    return Response.status(Response.Status.CONFLICT).entity(new ResponseMapper(Response.Status.CONFLICT,
                            "Event simulation '" + simulationName + "' is currently in progress, hence " +
                                    "it cannot be resumed.")).build();
                case STOP:
                    return Response.status(Response.Status.FORBIDDEN).entity(new ResponseMapper(
                            Response.Status.FORBIDDEN, "Event simulation '" + simulationName + "' is " +
                            "currently stopped, hence it cannot be resumed.")).build();
                default:
                    /**
                     *  this statement is never reached since status is an enum. Nevertheless a response is added
                     *  since the method signature mandates it
                     */
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseMapper(
                            Response.Status.INTERNAL_SERVER_ERROR, "Invalid status '" +
                            eventSimulator.getStatus() + "' allocated for simulation '" + simulationName +
                            "'. Valid statuses are '" + EventSimulator.Status.RUN + "', '" + EventSimulator.Status.PAUSE
                            + "', '" + EventSimulator.Status.STOP + "'.")).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMapper(Response.Status.NOT_FOUND,
                    "No event simulation configuration available under simulation name '" + simulationName +
                            "'.")).build();
        }
    }

    /**
     * stop() is used to stop an ongoing or paused feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     */
    private Response stop(String simulationName) {
        if (EventSimulationMap.getSimulatorMap().containsKey(simulationName)) {
            EventSimulator eventSimulator = EventSimulationMap.getSimulatorMap().get(simulationName);
            switch (eventSimulator.getStatus()) {
                case RUN:
                case PAUSE:
                    eventSimulator.stop();
                    return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully " +
                            "stopped event simulation '" + simulationName + "'.")).build();
                case STOP:
                    return Response.status(Response.Status.CONFLICT).entity(
                            new ResponseMapper(Response.Status.CONFLICT, "Event simulation '" + simulationName
                                    + "' is already stopped.")).build();
                default:
                    /**
                     *  this statement is never reached since status is an enum. Nevertheless a response is added
                     *  since the method signature mandates it
                     */
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseMapper(
                            Response.Status.INTERNAL_SERVER_ERROR, "Invalid status '" +
                            eventSimulator.getStatus() + "' allocated for simulation '" + simulationName +
                            "'. Valid statuses are '" + EventSimulator.Status.RUN + "', '" + EventSimulator.Status.PAUSE
                            + "', '" + EventSimulator.Status.STOP + "'.")).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMapper(Response.Status.NOT_FOUND,
                    "No event simulation configuration available under simulation name '" + simulationName +
                            "'.")).build();
        }
    }

    /**
     * service to upload csv files
     *
     * @param filePath location of file being uploaded
     * @return Response
     * @throws FileAlreadyExistsException if the file exists in 'deployment/csv-files' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'deployment/csv-files' directory
     */
    @POST
    @Path("/files")
    @Produces("application/json")
    public Response uploadFile(String filePath) throws FileAlreadyExistsException, FileOperationsException,
            InvalidFileException, FileNotFoundException {
        FileUploader.getFileUploaderInstance().uploadFile(filePath,
                (Paths.get(Utils.getCarbonHome().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                        EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
        return Response.status(Response.Status.CREATED).entity(
                new ResponseMapper(Response.Status.CREATED, "Successfully uploaded " +
                        "file '" + FilenameUtils.getName(filePath) + "'")).build();
    }

    /**
     * service to modify an uploaded csv files
     *
     * @param filePath location of file being uploaded
     * @return Response
     * @throws FileAlreadyExistsException this exception is not thrown since the file available under 'fileName' will
     *                                    be deleted prior to uploading the new file. Nevertheless this is included
     *                                    in the method signature as it is a checked exception used when uploading a
     *                                    file
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'deployment/csv-files' directory
     * @throws FileNotFoundException      if the file does not exists
     */
    @PUT
    @Path("/files/{fileName}")
    @Produces("application/json")
    public Response updateFile(@PathParam("fileName") String fileName, String filePath)
            throws FileAlreadyExistsException, FileOperationsException, InvalidFileException, FileNotFoundException {
        if (!FilenameUtils.getName(filePath).isEmpty()) {
            FileUploader fileUploader = FileUploader.getFileUploaderInstance();
            fileUploader.validateFileSource(FilenameUtils.concat(Paths.get(Utils.getCarbonHome().toString(),
                    EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                    EventSimulatorConstants.DIRECTORY_CSV_FILES).toString(), fileName));
            fileUploader.validateFileSource(filePath);
            boolean deleted = fileUploader.deleteFile(fileName, (Paths.get(Utils.getCarbonHome().toString(),
                    EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                    EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
            if (deleted) {
                fileUploader.uploadFile(filePath, (Paths.get(Utils.getCarbonHome().toString(),
                        EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                        EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
                return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully updated CSV" +
                        "file '" + fileName + "'")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName + "' does not exist"))
                        .build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ResponseMapper(Response.Status.BAD_REQUEST, "File name cannot be empty."))
                    .build();
        }
    }

    /**
     * Delete the file
     *
     * @param fileName File Name
     * @return Response
     * @throws FileOperationsException if an IOException occurs while deleting file
     * @throws FileNotFoundException   if the file does not exists
     */
    @DELETE
    @Path("/files/{fileName}")
    @Produces("application/json")
    public Response deleteFile(@PathParam("fileName") String fileName) throws FileOperationsException,
            InvalidFileException, FileNotFoundException {
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        fileUploader.validateFileSource(FilenameUtils.concat(Paths.get(Utils.getCarbonHome().toString(),
                EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                EventSimulatorConstants.DIRECTORY_CSV_FILES).toString(), fileName));
        boolean deleted = fileUploader.deleteFile(fileName, (Paths.get(Utils.getCarbonHome().toString(),
                EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
        if (deleted) {
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully " +
                    "deleted file '" + fileName + "'")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName +
                            "' does not exist")).build();
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
        EventSimulationMap.getSimulatorMap().forEach((s, simulator) -> simulator.stop());
        log.info("Simulator service component is deactivated");
    }

    /**
     * This bind method will be called when EventStreamService method of stream processor is called
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
