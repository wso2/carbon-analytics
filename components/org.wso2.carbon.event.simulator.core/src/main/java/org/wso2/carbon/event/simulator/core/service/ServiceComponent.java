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
import org.wso2.carbon.event.simulator.core.internal.generator.SingleEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.util.FileUploader;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.msf4j.Microservice;

import java.io.File;
import java.nio.file.Paths;
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
     * <p>
     * http://localhost:9090/simulation/single
     * <pre>
     * curl -X POST -d'{"streamName":"FooStream",
     *                 "executionPlanName" : "TestExecutionPlan",
     *                 "timestamp" : "1488615136958"
     *                 "attributeValues":["WSO2","345", "45"]}'
     *  http://localhost:9090/eventSimulation/singleEventSimulation
     * </pre>
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
        if (log.isDebugEnabled()) {
            log.debug("Single Event Simulation");
        }
        SingleEventGenerator.sendEvent(singleEventConfiguration);
        return Response.status(Response.Status.ACCEPTED).entity(
                new ResponseMapper(Response.Status.ACCEPTED, "Single Event simulation " +
                "started successfully")).build();
    }


    /**
     * service used to upload feed simulation configuration to the system
     * <p>
     * http://localhost:9090/eventSimulation/feedSimulation
     *
     * @param simulationConfiguration jsonString to be converted to EventSimulationDto object from the request
     *                                Json body.
     * @return Response
     * @throws InvalidConfigException if the simulation configuration does not contain a simulation name
     * @throws FileAlreadyExistsException if a configuration already exists in the system under the given simulation
     * name
     * @throws FileOperationsException if an IOException occurs while uploading the simulation configuration
     */
    @POST
    @Path("/feed")
    @Produces("application/json")
    public Response uploadFeedSimulationConfig(String simulationConfiguration)
            throws InvalidConfigException, FileAlreadyExistsException, FileOperationsException {
        SimulationConfigUploader.getConfigUploader().uploadSimulationConfig(simulationConfiguration);
        return Response.status(Response.Status.CREATED).entity(
                new ResponseMapper(Response.Status.CREATED, "success")).build();
    }
//    @POST
//    @Path("/feed")
//    @Produces("application/json")
//    public Response feedSimulation(String simulationConfigDetails)
//            throws InvalidConfigException, InsufficientAttributesException {
//        EventSimulator simulator = new EventSimulator(simulationConfigDetails);
//        EventSimulatorDataHolder.getInstance().getSimulatorMap().put(simulator.getUuid(), simulator);
//        executorServices.execute(simulator);
//        return Response.status(Response.Status.CREATED).entity(
//                new ResponseMapper(Response.Status.CREATED, "Event simulation submitted successfully | " +
//                        "uuid : " + simulator.getUuid())).build();
//    }

    /**
     * service used to update an uploaded simulation configuration
     *
     * @param simulationName unique identifies of the simulation configuration
     * @param simulationConfigDetails new simulation configuration
     * @return response     *
     * @throws InvalidConfigException if the simulation configuration does not contain a simulation name
     * @throws FileAlreadyExistsException if a configuration already exists in the system under the given simulation
     * name
     * @throws FileOperationsException if an IOException occurs while uploading the simulation configuration
     * */
    @PUT
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response updateFeedSimulationConfig(@PathParam("simulationName") String simulationName, String
            simulationConfigDetails)
            throws InvalidConfigException, FileOperationsException, FileAlreadyExistsException {
        boolean deleted = SimulationConfigUploader.getConfigUploader().deleteSimulationConfig(simulationName);
        if (deleted) {
            if (EventSimulatorDataHolder.getInstance().getSimulatorMap().containsKey(simulationName)) {
                EventSimulatorDataHolder.getInstance().getSimulatorMap().remove(simulationName);
            }
            SimulationConfigUploader.getConfigUploader().uploadSimulationConfig(simulationConfigDetails);
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Event simulation " +
                    "update request submitted successfully for simulation" + simulationName)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation available under"
                            + simulationName)).build();
        }
    }

    /**
     * service used to retrieve a simulation configuration
     * @param simulationName name of simulation configuration being retrieved
     * @return response
     * @throws FileOperationsException if an IOException occurs when reading the configuration file
     * */
    @GET
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response getFeedSimulationConfig(@PathParam("simulationName") String simulationName) throws
            FileOperationsException {
        String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig(simulationName);
        if (simulationConfig != null) {
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully " +
                    "retrieved the configuration of simulation " + simulationName + ". " + simulationConfig)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation available under" +
                            simulationName)).build();
        }
    }

    /**
     * service used to delete a simulation configuration from the system
     *
     * @param simulationName name of simulation being deleted
     * @return response
     * @throws FileOperationsException if an IOException occurs while deleting simulation configuration file
     * */
    @DELETE
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response deleteFeedSimulationConfig(@PathParam("simulationName") String simulationName) throws
            FileOperationsException {
        boolean deleted = SimulationConfigUploader.getConfigUploader().deleteSimulationConfig(simulationName);
        if (deleted) {
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully " +
                    "deleted simulation configuration " + simulationName)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation available under" +
                            simulationName)).build();
        }
    }

    /**
     * service used to change status of a simulation
     *
     * @param simulationName name of simulation whose status is being changed
     * @return response
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     * @throws InsufficientAttributesException if the simulation configuration cannot produce events with values for
     * all stream attributes
     * @throws FileOperationsException if an IOException occurs during file manipulations
     * */
    @POST
    @Path("/feed/{simulationName}")
    @Produces("application/json")
    public Response changeSimulationStatus(@PathParam("simulationName") String simulationName, @QueryParam("action")
            String action) throws InvalidConfigException, InsufficientAttributesException, FileOperationsException {
        try {
            switch (EventSimulator.Action.valueOf(action)) {
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
                     *  this statement is never reached since action is an enum.
                     */
            }
        } catch (IllegalArgumentException e) {
            /**
             *  Ignore this catch block and let it flow.
             */
        }
        /**
         * If this point is reached, it means either default : was reached or and IllegalArgumentException has
         * occurred, In both situations Return a bad request.
         */
        return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMapper(
                Response.Status.BAD_REQUEST, "Invalid action '" + action + "'. Actions supported" +
                " are " + EventSimulator.Action.RUN + ", " + EventSimulator.Action.PAUSE + ", " +
                EventSimulator.Action.RESUME + ", " + EventSimulator.Action.STOP + ".")).build();
    }

    /**
     * run() is used to start a feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     * @throws InsufficientAttributesException if the simulation configuration cannot produce events with values for
     * all stream attributes
     * @throws FileOperationsException if an IOException occurs during file manipulations
     * */
    private Response run(String simulationName) throws FileOperationsException, InvalidConfigException,
            InsufficientAttributesException {
        if (EventSimulatorDataHolder.getInstance().getSimulatorMap().containsKey(simulationName)) {
            executorServices.execute(EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName));
            return Response.status(Response.Status.ACCEPTED).entity(new ResponseMapper(Response.Status.ACCEPTED,
                    "Successfully submitted simulation " + simulationName)).build();
        } else {
            String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig(simulationName);
            if (simulationConfig != null) {
                EventSimulator simulator = new EventSimulator(simulationName, simulationConfig);
                EventSimulatorDataHolder.getInstance().getSimulatorMap().put(simulationName, simulator);
                executorServices.execute(simulator);
                return Response.status(Response.Status.ACCEPTED).entity(new ResponseMapper(Response.Status.ACCEPTED,
                        "Successfully submitted simulation " + simulationName)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMapper(Response.Status.NOT_FOUND,
                        "No event simulation configuration available under " + simulationName + " in" +
                                " directory " + (Paths.get(System.getProperty("java.io.tmpdir"),
                                        EventSimulatorConstants.DIRECTORY_NAME)).toString())).build();
            }
        }
    }

    /**
     * pause() is used to pause an ongoing feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     * */
    private Response pause(String simulationName) {
        if (EventSimulatorDataHolder.getInstance().getSimulatorMap().containsKey(simulationName)) {
            if (!EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName).isStopped()) {
                if (!EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName).isPaused()) {
                    EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName).pause();
                    return Response.ok().entity(new ResponseMapper(Response.Status.OK,
                            "Successfully paused event simulation '" + simulationName)).build();
                } else {
                    return Response.status(Response.Status.CONFLICT).entity(new ResponseMapper(Response.Status.CONFLICT,
                            "Simulation " + simulationName + " is already paused")).build();
                }
            } else {
                return Response.status(Response.Status.FORBIDDEN).entity(new ResponseMapper(Response.Status.FORBIDDEN,
                        "Simulation " + simulationName + " is currently stopped, hence it cannot be paused"))
                        .build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMapper(Response.Status.NOT_FOUND,
                    "No event simulation available under " + simulationName)).build();
        }
    }

    /**
     * resume() is used to resume a paused feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     * */
    private Response resume(String simulationName) {
        if (EventSimulatorDataHolder.getInstance().getSimulatorMap().containsKey(simulationName)) {
            if (!EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName).isStopped()) {
                if (EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName).isPaused()) {
                    EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName).resume();
                    return Response.ok().entity(new ResponseMapper(Response.Status.OK,
                            "Successfully resumed event simulation '" + simulationName)).build();
                } else {
                    return Response.status(Response.Status.CONFLICT).entity(new ResponseMapper(Response.Status.CONFLICT,
                            "Event simulation '" + simulationName + "' is currently in progress, hence " +
                                    "it cannot be resumed")).build();
                }
            } else {
                return Response.status(Response.Status.FORBIDDEN).entity(new ResponseMapper(Response.Status.FORBIDDEN,
                        "Simulation " + simulationName + " is currently stopped, hence it cannot be " +
                                "resumed")).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMapper(Response.Status.NOT_FOUND,
                    "No event simulation available under " + simulationName)).build();
        }
    }

    /**
     * stop() is used to stop an ongoing or paused feed simulation
     *
     * @param simulationName name of simulation being started
     * @return response
     * */
    private Response stop(String simulationName) {
        if (EventSimulatorDataHolder.getInstance().getSimulatorMap().containsKey(simulationName)) {
            if (!EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName).isStopped()) {
                EventSimulatorDataHolder.getInstance().getSimulatorMap().get(simulationName).stop();
                return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully" +
                        "stopped event simulation " + simulationName)).build();
            } else {
                return Response.status(Response.Status.CONFLICT).entity(
                        new ResponseMapper(Response.Status.CONFLICT, "Event simulation '" + simulationName +
                                "' is already stopped")).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation available under " +
                            simulationName)).build();
        }
    }

    /**
     * Stop the simulation process of simulation configuration related to the provided UUID
     * <p>
     * http://localhost:9090/simulation/feed/{uuid}/stop
     *
     * @param uuid uuid of simulation that needs to be stopped
     * @return Response
     * @throws InterruptedException Interrupted Exception
     */
    @POST
    @Path("/feed/{simulationName}/?action=stop")
    @Produces("application/json")
    public Response stoped(@PathParam("uuid") String uuid) throws InterruptedException {
        if (EventSimulatorDataHolder.getInstance().getSimulatorMap().containsKey(uuid)) {
            EventSimulatorDataHolder.getInstance().getSimulatorMap().get(uuid).stop();
            EventSimulatorDataHolder.getInstance().getSimulatorMap().remove(uuid);
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Terminate event " +
                    "simulation | uuid : " + uuid)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation available under" +
                            " uuid : " + uuid)).build();
        }
    }

    /**
     * pause the simulation process of simulation configuration related to the provided UUID
     * <p>
     * http://localhost:9090/simulation/feed/{uuid}/pause
     *
     * @param uuid uuid of simulation that needs to be paused
     * @return Response
     * @throws InterruptedException Interrupted Exception
     */
    @POST
    @Path("/feed/{uuid}/pause")
    @Produces("application/json")
    public Response paused(@PathParam("uuid") String uuid) throws InterruptedException {
        if (EventSimulatorDataHolder.getInstance().getSimulatorMap().containsKey(uuid)) {
//            boolean paused = EventSimulatorDataHolder.getInstance().getSimulatorMap().get(uuid).pause();
//            if (paused) {
//                return Response.ok().entity(new ResponseMapper(Response.Status.ACCEPTED, "Successfully " +
//                        "paused event simulation '" + EventSimulatorDataHolder.getInstance().getSimulatorMap().get
//                        (uuid).getSimulationName() + "' | uuid : " + uuid)).build();
//            } else {
                return Response.ok().entity(new ResponseMapper(Response.Status.CONFLICT, "Simulation is " +
                        "already paused | uuid : " + uuid)).build();
//            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND, "No event" +
                    " simulation available under uuid : " + uuid)).build();
        }
    }

    /**
     * resume the simulation of simulation configuration related to the provided UUID
     * <p>
     * http://localhost:9090/simulation/feed/{uuid}/resume
     *
     * @param uuid uuid of simulation that needs to be resumed
     * @return Response
     * @throws InterruptedException Interrupted Exception
     */
    @POST
    @Path("/feed/{uuid}/resume")
    @Produces("application/json")
    public Response resumed(@PathParam("uuid") String uuid) throws InterruptedException {
        /**
         * resume a paused event simulation
         * check whether a simulation with the specified uuid exists
         * if yes call resume method of that simulator
         * else, inform the uuid specified does not have an event simulation associated with it
         * */
        if (EventSimulatorDataHolder.getInstance().getSimulatorMap().containsKey(uuid)) {
//            boolean resumed = EventSimulatorDataHolder.getInstance().getSimulatorMap().get(uuid).resume();
//            if (resumed) {
//                return Response.ok().entity(new ResponseMapper(Response.Status.ACCEPTED, "Successfully" +
//                        " resumed event simulation '" + EventSimulatorDataHolder.getInstance().getSimulatorMap().get
//                        (uuid).getSimulationName() + "' | uuid : " + uuid)).build();
//            } else {
                return Response.ok().entity(new ResponseMapper(Response.Status.CONFLICT, "Event " +
                        "simulation '" + EventSimulatorDataHolder.getInstance().getSimulatorMap().get(uuid).
                        getSimulationName() + "'is currently in progress and cannot be resumed | uuid : " + uuid))
                        .build();
//            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMapper(Response.Status.BAD_REQUEST,
                    "No event simulation available under uuid : " + uuid)).build();
        }
    }

    /**
     * service to upload csv files
     * <p>
     * http://localhost:9090/simulation/files
     *
     * @param filePath location of file being uploaded
     * @return Response
     * @throws FileAlreadyExistsException if the file exists in 'temp/eventSimulator' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'temp/eventSimulator' directory
     */
    @POST
    @Path("/files")
    @Produces("application/json")
    public Response uploadFile(String filePath)
            throws FileAlreadyExistsException, FileOperationsException {
        String fileName = new File(filePath).getName();
        FileUploader.getFileUploaderInstance().uploadFile(fileName, filePath);
        return Response.status(Response.Status.CREATED).entity(
                new ResponseMapper(Response.Status.CREATED,  "Successfully uploaded " +
                "file '" + fileName + "' to directory " + (Paths.get(System.getProperty("java.io.tmpdir"),
                EventSimulatorConstants.DIRECTORY_NAME)).toString())).build();
    }

    /**
     * service to modify an uploaded csv files
     * <p>
     * http://localhost:9090/simulation/files
     *
     * @param filePath location of file being uploaded
     * @return Response
     * @throws FileAlreadyExistsException if the file exists in 'temp/eventSimulator' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'temp/eventSimulator' directory
     */
    @PUT
    @Path("/files")
    @Produces("application/json")
    public Response updateFile(String filePath)
            throws FileAlreadyExistsException, FileOperationsException {
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        String fileName = new File(filePath).getName();
        boolean deleted = fileUploader.deleteFile(fileName);
        if (deleted) {
            fileUploader.uploadFile(fileName, filePath);
//            todo 19/04/2017 check whether ok response is sent even if exception is thrown
            return Response.ok().entity(new ResponseMapper(Response.Status.OK, "Successfully updated " +
                    "file '" + fileName + "' available in  directory " + (Paths.get(System.getProperty("java.io" +
                            ".tmpdir"), EventSimulatorConstants.DIRECTORY_NAME)).toString())).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND,  "File '" + fileName +
                    "' is not available in  directory " + Paths.get(System.getProperty("java"
                    + ".io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME, fileName).toString())).build();
        }
    }

    /**
     * Delete the file
     * <p>
     * http://localhost:9090/simulation/files/{fileName}/delete
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
        boolean deleted = fileUploader.deleteFile(fileName);
        if (deleted) {
            return Response.ok().entity(new ResponseMapper(Response.Status.OK,  "Successfully " +
                    "deleted file '" + fileName + "' from directory " + Paths.get(System.getProperty
                    ("java.io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME, fileName).toString())).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ResponseMapper(Response.Status.NOT_FOUND,  "File '" + fileName +
                    "' is not available in  directory " + Paths.get(System.getProperty("java"
                    + ".io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME, fileName).toString())).build();
        }
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
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
        EventSimulatorDataHolder.getInstance().getSimulatorMap().forEach((s, simulator) -> simulator.stop());
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
