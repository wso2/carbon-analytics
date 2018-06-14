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

package org.wso2.carbon.event.simulator.core.impl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.event.simulator.core.api.FeedApiService;
import org.wso2.carbon.event.simulator.core.api.NotFoundException;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.util.CommonOperations;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.event.simulator.core.service.EventSimulator;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorMap;
import org.wso2.carbon.event.simulator.core.service.bean.ActiveSimulatorData;
import org.wso2.carbon.event.simulator.core.service.bean.ResourceDependencyData;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;
import org.wso2.carbon.utils.Utils;
import org.wso2.msf4j.Request;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public class FeedApiServiceImpl extends FeedApiService {
    private static final ExecutorService executorServices = Executors.newFixedThreadPool(10);
    private static final String PERMISSION_APP_NAME = "SIM";
    private static final String MANAGE_SIMULATOR_PERMISSION_STRING = "simulator.manage";
    private static final String VIEW_SIMULATOR_PERMISSION_STRING = "simulator.view";

    public Response addFeedSimulation(String simulationConfiguration) throws NotFoundException {
        SimulationConfigUploader simulationConfigUploader = SimulationConfigUploader.getConfigUploader();
        try {
            if (!EventSimulatorMap.getInstance().getActiveSimulatorMap().containsKey(
                    simulationConfigUploader.getSimulationName(simulationConfiguration))) {
                try {
                    EventSimulator.validateSimulationConfig(simulationConfiguration, false);
                } catch (ResourceNotFoundException e) {
                    //do nothing
                }
                simulationConfigUploader.uploadSimulationConfig(simulationConfiguration,
                                                                (Paths.get(Utils.getRuntimePath().toString(),
                                                                           EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                                           EventSimulatorConstants
                                                                                   .DIRECTORY_SIMULATION_CONFIGS))
                                                                        .toString());
                return Response.status(Response.Status.CREATED)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.CREATED, "Successfully uploaded simulation " +
                                "configuration '" + simulationConfigUploader.getSimulationName(simulationConfiguration)
                                +
                                "'"))
                        .build();

            } else {
                return Response.status(Response.Status.CONFLICT)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.CONFLICT, "A simulation already exists under " +
                                "the name '" + simulationConfigUploader.getSimulationName(simulationConfiguration)
                                + "'"))
                        .build();
            }
        } catch (InvalidConfigException | InsufficientAttributesException | FileOperationsException |
                FileAlreadyExistsException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                    .build();
        }
    }

    public Response deleteFeedSimulation(String simulationName) throws NotFoundException {
        boolean deleted = false;
        try {
            CommonOperations.validatePath(simulationName);
            deleted = SimulationConfigUploader.
                    getConfigUploader().
                    deleteSimulationConfig(simulationName,
                                           (Paths.get(Utils.getRuntimePath().toString(),
                                                      EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                      EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS))
                                                   .toString());
            if (deleted) {
                return Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
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
        } catch (FileOperationsException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                    .build();
        }
    }

    public Response getFeedSimulation(String simulationName) throws NotFoundException {
        if (EventSimulatorMap.getInstance().getActiveSimulatorMap().containsKey(simulationName)) {
            JSONObject result = new JSONObject();
            try {
                result.put("Simulation configuration",
                           new JSONObject(SimulationConfigUploader.
                                   getConfigUploader().
                                   getSimulationConfig(simulationName,
                                                       (Paths.get(Utils.getRuntimePath().toString(),
                                                                  EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                                  EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS))
                                                               .
                                                                       toString())));
            } catch (FileOperationsException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                        .build();
            }
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.OK, result.toString()))
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No simulation configuration" +
                            "available under simulation name '" + simulationName + "'"))
                    .build();
        }
    }

    public Response getFeedSimulations() throws NotFoundException {
        EventSimulatorMap.getInstance().retryInActiveSimulatorDeployment(false);
        EventSimulatorMap.getInstance().checkValidityOfActiveSimAfterDependency(false);
        Map<String, ActiveSimulatorData> activeSimulatorMap =
                EventSimulatorMap.getInstance().getActiveSimulatorMap();
        Map<String, ResourceDependencyData> inActiveSimulatorMap =
                EventSimulatorMap.getInstance().getInActiveSimulatorMap();
        JSONObject result = new JSONObject();
        JSONArray activeSimulations = new JSONArray();
        JSONArray inActiveSimulations = new JSONArray();
        try {
            for (Map.Entry<String, ActiveSimulatorData> entry : activeSimulatorMap.entrySet()) {
                activeSimulations.put(new JSONObject(SimulationConfigUploader.
                        getConfigUploader().
                        getSimulationConfig(entry.getKey(),
                                            (Paths.get(Utils.getRuntimePath().toString(),
                                                       EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                       EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).
                                                    toString())));
            }
            for (Map.Entry<String, ResourceDependencyData> entry : inActiveSimulatorMap.entrySet()) {
                JSONObject inactiveSimulation = new JSONObject(SimulationConfigUploader.getConfigUploader()
                                                                       .getSimulationConfig(entry.getKey(), (Paths.get(
                                                                               Utils.getRuntimePath().toString(),
                                                                               EventSimulatorConstants
                                                                                       .DIRECTORY_DEPLOYMENT,
                                                                               EventSimulatorConstants
                                                                                       .DIRECTORY_SIMULATION_CONFIGS))
                                                                               .toString()));
                inactiveSimulation.put("exceptionReason", entry.getValue().getExceptionReason());
                inActiveSimulations.put(inactiveSimulation);
            }
            result.put("activeSimulations", activeSimulations);
            result.put("inActiveSimulations", inActiveSimulations);
            if (activeSimulations.length() > 0 || inActiveSimulations.length() > 0) {
                return Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.OK, result.toString()))
                        .build();
            } else {
                return Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.OK, result.toString()))
                        .build();
            }
        } catch (FileOperationsException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                    .build();
        }
    }

    public Response operateFeedSimulation(String action, String simulationName) throws NotFoundException {
        if (action != null && !action.isEmpty()) {
            try {
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
                             *  this statement is never reached since action is an enum. Nevertheless a response is
                             *  added
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
                } catch (InsufficientAttributesException | FileOperationsException | InvalidConfigException e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
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

    public Response updateFeedSimulation(String simulationName, String simulationConfigDetails)
            throws NotFoundException, FileOperationsException {
        SimulationConfigUploader simulationConfigUploader = SimulationConfigUploader.getConfigUploader();
        CommonOperations.validatePath(simulationName);
        if (simulationConfigUploader.checkSimulationExists(
                simulationName,
                Paths.get(Utils.getRuntimePath().toString(),
                          EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                          EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS).toString())) {
            try {
                EventSimulator.validateSimulationConfig(simulationConfigDetails, false);
            } catch (ResourceNotFoundException | InvalidConfigException | InsufficientAttributesException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                        .build();
            }
            boolean deleted = false;
            try {
                deleted = simulationConfigUploader.
                        deleteSimulationConfig(simulationName,
                                               (Paths.get(Utils.getRuntimePath().toString(),
                                                          EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                          EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS))
                                                       .toString());
            } catch (FileOperationsException e) {
                e.printStackTrace();
            }
            if (deleted) {
                try {
                    simulationConfigUploader.
                            uploadSimulationConfig(
                                    simulationConfigDetails,
                                    (Paths.get(Utils.getRuntimePath().toString(),
                                               EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                               EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                } catch (FileOperationsException | InvalidConfigException | FileAlreadyExistsException e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                            .build();
                }
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

    public Response getFeedSimulationStatus(String simulationName) throws NotFoundException {
        ActiveSimulatorData activeSimulatorData = EventSimulatorMap.getInstance().getActiveSimulatorMap()
                .get(simulationName);
        if (activeSimulatorData != null) {
            EventSimulator eventSimulator = activeSimulatorData.getEventSimulator();
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.OK, eventSimulator.getStatus().name()))
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "No event simulation configuration "
                            + "available under simulation name '" + simulationName + "'."))
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
                case PENDING_STOP:
                    return Response.status(Response.Status.CONFLICT)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.CONFLICT, "Please release the break" +
                                    " points for Simulation '" + simulationName + "'  " +
                                    "to restart."))
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
                                               "No event simulation configuration available under simulation name '"
                                               + simulationName + "'.")).build();
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

    @Override
    public Response getFeedSimulations(Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new Permission
                (PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING)) || getPermissionProvider().hasPermission
                (getUserName(request), new Permission(PERMISSION_APP_NAME, VIEW_SIMULATOR_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return getFeedSimulations();
    }

    @Override
    public Response addFeedSimulation(String body, Request request) throws NotFoundException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return addFeedSimulation(body);
    }

    @Override
    public Response deleteFeedSimulation(String simulationName, Request request) throws NotFoundException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return deleteFeedSimulation(simulationName);
    }

    @Override
    public Response getFeedSimulation(String simulationName, Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new Permission
                (PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING)) || getPermissionProvider().hasPermission
                (getUserName(request), new Permission(PERMISSION_APP_NAME, VIEW_SIMULATOR_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return getFeedSimulation(simulationName);
    }

    @Override
    public Response operateFeedSimulation(String action, String simulationName, Request request) throws NotFoundException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return operateFeedSimulation(action, simulationName);
    }

    @Override
    public Response updateFeedSimulation(String simulationName, String body, Request request)
            throws NotFoundException, FileOperationsException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return updateFeedSimulation(simulationName, body);
    }

    @Override
    public Response getFeedSimulationStatus(String simulationName, Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new Permission
                (PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING)) || getPermissionProvider().hasPermission
                (getUserName(request), new Permission(PERMISSION_APP_NAME, VIEW_SIMULATOR_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return getFeedSimulationStatus(simulationName);
    }

    private static String getUserName(Request request) {
        Object username = request.getProperty("username");
        return username != null ? username.toString() : null;
    }

    private PermissionProvider getPermissionProvider() {
        return EventSimulatorDataHolder.getPermissionProvider();
    }
}
