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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.carbon.event.simulator.core.internal.bean.SimulationPropertiesDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventGeneratorFactoryImpl;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.util.LogEncoder;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.concurrent.NotThreadSafe;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;

/**
 * EventSimulator starts the simulation execution for single Event and
 * Feed Simulation
 */
@NotThreadSafe
public class EventSimulator implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EventSimulator.class);
    private List<EventGenerator> generators = new ArrayList<>();
    private SimulationPropertiesDTO simulationProperties;
    private String simulationName;
    private Status status = Status.STOP;
    private String statusMessage = null;
    // lock is used to pause a simulation
    private final Semaphore lock = new Semaphore(1, true);
    // lockStop is used to ensure that stop() will not be called in the middle of an event generation(avoid IOException)
    private final ReentrantLock lockStop = new ReentrantLock();


    /**
     * EventSimulator() constructor initializes an EventSimulator object
     *
     * @param simulationConfiguration a string containing the simulation configuration
     * @param simulationName          name of simulation
     * @throws InsufficientAttributesException is a configuration does not produce data for all stream attributes
     * @throws InvalidConfigException          if the simulation configuration is invalid
     * @throws ResourceNotFoundException       if a resource required for simulation is not found
     */
    public EventSimulator(String simulationName, String simulationConfiguration, boolean isTriggeredFromDeploy)
            throws InsufficientAttributesException, InvalidConfigException, ResourceNotFoundException {
        if (!simulationConfiguration.isEmpty()) {
            //validate simulation configuration
            validateSimulationConfig(simulationConfiguration, isTriggeredFromDeploy);
            //create generators and configurationDTO's
            JSONObject simulationConfig = new JSONObject(simulationConfiguration);
            simulationProperties = createSimulationPropertiesDTO(simulationConfig.getJSONObject(
                    EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES));
            this.simulationName = simulationName;
            JSONArray sourceConfig = simulationConfig.getJSONArray(EventSimulatorConstants
                                                                           .EVENT_SIMULATION_SOURCES);
            EventGeneratorFactoryImpl generatorFactory = new EventGeneratorFactoryImpl();
            for (int i = 0; i < sourceConfig.length(); i++) {
                generators.add(generatorFactory.createEventGenerator(sourceConfig.getJSONObject(i),
                                                                     simulationProperties.getStartTimestamp(),
                                                                     simulationProperties.getEndTimestamp(),
                                                                     simulationName));
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully created simulator for simulation configuration '" + simulationName + "'");
            }
        } else {
            log.error("Simulation '" + simulationName + "' does not have a configuration specified.");
            throw new InvalidConfigException("Simulation '" + simulationName + "' does not have a configuration" +
                                                     " specified.");
        }
    }

    /**
     * validateSimulationConfig() validates a simulation configuraiton provided
     *
     * @param simulationConfiguration simulation configuration
     * @throws InsufficientAttributesException is a configuration does not produce data for all stream attributes
     * @throws InvalidConfigException          if the simulation configuration is invalid
     * @throws ResourceNotFoundException       if a resource required for simulation is not found
     */
    public static void validateSimulationConfig(String simulationConfiguration, boolean isTriggeredFromDeploy)
            throws InvalidConfigException, InsufficientAttributesException, ResourceNotFoundException {
        try {
            JSONObject simulationConfig = new JSONObject(simulationConfiguration);
            String simulationName = simulationConfig.getJSONObject(
                    EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES).
                    getString(EventSimulatorConstants.EVENT_SIMULATION_NAME);
            //first create a simulation properties object
            if (simulationConfig.has(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)
                    && !simulationConfig.isNull(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)) {
                validateSimulationProperties(
                        simulationConfig.getJSONObject(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES));
                //check whether the simulation has source configurations and create event generators for each
                // source config
                if (checkAvailabilityOfArray(simulationConfig, EventSimulatorConstants.EVENT_SIMULATION_SOURCES)) {
                    JSONArray sourceConfig =
                            simulationConfig.getJSONArray(EventSimulatorConstants.EVENT_SIMULATION_SOURCES);
                    EventGeneratorFactoryImpl generatorFactory = new EventGeneratorFactoryImpl();
                    for (int i = 0; i < sourceConfig.length(); i++) {
                        generatorFactory
                                .validateGeneratorConfiguration(sourceConfig.getJSONObject(i),
                                                                simulationName);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully validated simulation configuration '" + simulationName + "'");
                    }
                } else {
                    throw new InvalidConfigException(
                            "Source configuration is required for event simulation '"
                                    + simulationName + "'. Invalid simulation configuration provided : "
                                    + simulationConfig.toString());
                }
            } else {
                throw new InvalidConfigException("Simulation properties are required for '" + simulationName
                                                         + "'event simulation. Invalid simulation configuration "
                                                         + "provided : " + simulationConfig.toString());
            }
        } catch (JSONException e) {
            log.error("Error occurred when accessing simulation configuration of " +
                    "simulation. Invalid JSON simulation properties configuration provided : " +
                    LogEncoder.removeCRLFCharacters(simulationConfiguration), e);
            throw new InvalidConfigException("Error occurred when accessing simulation configuration. "
                                                     + "Invalid JSON simulation properties configuration provided : "
                                                     + simulationConfiguration, e);
        }
    }

    /**
     * validateSimulationConfiguration() is used to validate the simulation configuration provided
     *
     * @param simulationPropertiesConfig a JSON object containing simulation properties
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */
    private static void validateSimulationProperties(JSONObject simulationPropertiesConfig) throws
                                                                                            InvalidConfigException {
        /**
         * checkAvailability() method performs the following checks
         * 1. has
         * 2. isNull
         * 3. isEmpty
         * */
        try {
            if (!checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.EVENT_SIMULATION_NAME)) {
                throw new InvalidConfigException("Simulation name is required for event simulation. "
                                                         + "Invalid simulation properties configuration provided : "
                                                         + simulationPropertiesConfig.toString());
            }
            long startTimestamp = System.currentTimeMillis();
            if (simulationPropertiesConfig.has(EventSimulatorConstants.START_TIMESTAMP)) {
                if (!simulationPropertiesConfig.isNull(EventSimulatorConstants.START_TIMESTAMP)) {
                    if (!simulationPropertiesConfig.getString(EventSimulatorConstants.START_TIMESTAMP).isEmpty()) {
                        startTimestamp = simulationPropertiesConfig.getLong(EventSimulatorConstants.START_TIMESTAMP);
                        if (startTimestamp < 0) {
                            throw new InvalidConfigException(
                                    "StartTimestamp must be a positive value for simulation '"
                                            + simulationPropertiesConfig.getString(
                                            EventSimulatorConstants.EVENT_SIMULATION_NAME)
                                            + "'. Invalid simulation properties configuration provided : "
                                            + simulationPropertiesConfig.toString());
                        }
                    }
                }
            }
            long endTimestamp = -1;
            if (simulationPropertiesConfig.has(EventSimulatorConstants.END_TIMESTAMP)) {
                if (!simulationPropertiesConfig.isNull(EventSimulatorConstants.END_TIMESTAMP)) {
                    if (!simulationPropertiesConfig.getString(EventSimulatorConstants.END_TIMESTAMP).isEmpty()) {
                        endTimestamp = simulationPropertiesConfig.getLong(EventSimulatorConstants.END_TIMESTAMP);
                        if (endTimestamp < 0) {
                            throw new InvalidConfigException(
                                    "EndTimestamp must be a positive value for simulation '"
                                            + simulationPropertiesConfig.getString(
                                            EventSimulatorConstants.EVENT_SIMULATION_NAME)
                                            + "'. Invalid simulation properties configuration provided : "
                                            + simulationPropertiesConfig.toString());
                        }
                    }
                }
            }
            if (endTimestamp != -1 && endTimestamp < startTimestamp) {
                throw new InvalidConfigException(
                        "Simulation '"
                                + simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                                + "' has incompatible startTimestamp and endTimestamp values. EndTimestamp must be "
                                + "either greater than the startTimestamp or must be set to null. "
                                + "Invalid simulation properties configuration provided : "
                                + simulationPropertiesConfig.toString());
            }
            if (simulationPropertiesConfig.has(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)) {
                if (!simulationPropertiesConfig.isNull(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)) {
                    if (!simulationPropertiesConfig.getString(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)
                            .isEmpty()) {
                        if (simulationPropertiesConfig.getInt(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED) < 0) {
                            throw new InvalidConfigException(
                                    "Number of event to be generated for simulation '"
                                            + simulationPropertiesConfig.getString(
                                            EventSimulatorConstants.EVENT_SIMULATION_NAME)
                                            + "' must be a positive value. Invalid simulation  configuration provided: "
                                            + simulationPropertiesConfig.toString());
                        }
                    }
                }
            }
        } catch (JSONException e) {
            log.error("Error occurred when accessing simulation configuration of simulation '"
                              + simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                              + "'. Invalid simulation properties configuration provided : "
                              +  simulationPropertiesConfig.toString(), e);
            throw new InvalidConfigException(
                    "Error occurred when accessing simulation configuration of simulation '"
                            + simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                            + "'. Invalid simulation properties configuration provided : "
                            + simulationPropertiesConfig.toString(), e);
        }
    }

    /**
     * eventSimulation() method is responsible for sending events belonging to one simulation configuration in the
     * order of their timestamps
     * Events will be sent at time intervals equal to the delay
     */
    @SuppressWarnings("SWL_SLEEP_WITH_LOCK_HELD")
    private void eventSimulation() {
        long minTimestamp;
        EventGenerator generator;
        int eventsRemaining = simulationProperties.getNoOfEventsRequired();
        try {
            while (!status.equals(Status.STOP) && !status.equals(Status.PENDING_STOP)) {
//                if the simulator is paused, wait till it is resumed
                if (status.equals(Status.PAUSE)) {
                    lock.acquire();
                    lock.release();
                }

                /*
                 * if there is no limit to the number of events to be sent or is the number of event remaining to be
                 * sent is > 0, send an event, else stop event simulation
                 * */
                if (eventsRemaining == -1 || eventsRemaining > 0) {
                    minTimestamp = -1L;
                    generator = null;
                    /*
                     * 1. for each event generator peek the next event (i.e. the next event with least timestamp)
                     * 2. take the first event generator will a not null nextEvent as the first refferal value for
                     * generator with minimum timestamp event, and take the events timestamp as the minimum
                     * timestamp refferal value
                     * 3. then compare the timestamp of the remaining not null nextEvents with the minimum timestamp
                     * and update the minimum timestamp refferal value accordingly.
                     * 4. once all generators are iterated and the event with minimum timestamp if obtained, send
                     * event.
                     * 5. if all generators has nextEvent == null, then stop event simulation
                     * */
                    lockStop.lock();
                    try {
                        for (EventGenerator eventGenerator : generators) {
                            if (eventGenerator.peek() != null) {
                                if (minTimestamp == -1L) {
                                    minTimestamp = eventGenerator.peek().getTimestamp();
                                    generator = eventGenerator;
                                } else if (eventGenerator.peek().getTimestamp() < minTimestamp) {
                                    minTimestamp = eventGenerator.peek().getTimestamp();
                                    generator = eventGenerator;
                                }
                            } else {
                                generator = null;
                            }
                        }
                        if (minTimestamp >= 0L && generator != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Input Event (Simulation : '" + simulationName + "') : "
                                                  + Arrays.deepToString(generator.peek().getData()));
                            }
                            EventSimulatorDataHolder.getInstance().getEventStreamService()
                                    .pushEvent(generator.getSiddhiAppName(), generator.getStreamName(),
                                               generator.poll());
                        } else {
                            break;
                        }
                        if (eventsRemaining > 0) {
                            eventsRemaining--;
                        }
                    } finally {
                        lockStop.unlock();
                    }
                    Thread.sleep(simulationProperties.getTimeInterval());
                } else {
                    break;
                }
            }

            if(status.equals(Status.PENDING_STOP)){
                status = Status.STOP;
            }
            stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EventGenerationException e) {
            /*
             * catch exception so that any resources opened could be closed and rethrow an exception indicating which
             * simulation failed
             * */
            stop();
            throw new EventGenerationException("Error occurred when generating an event for simulation '" +
                                                       simulationProperties.getSimulationName() + "'. ", e);
        }
    }

    /**
     * validateSimulationConfiguration() is used to parse the simulation configuration
     *
     * @param simulationPropertiesConfig a JSON object containing simulation properties
     * @return SimulationPropertiesDTO object containing simulation properties
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */
    private SimulationPropertiesDTO createSimulationPropertiesDTO(JSONObject simulationPropertiesConfig)
            throws InvalidConfigException {
        /*
         * checkAvailability() method performs the following checks
         * 1. has
         * 2. isNull
         * 3. isEmpty
         *
         * if checks are successful create simulationPropertiesDTO object
         * */
        try {
            String description = null;
            if (checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.EVENT_SIMULATION_DESCRIPTION)) {
                description = simulationPropertiesConfig.getString(
                        EventSimulatorConstants.EVENT_SIMULATION_DESCRIPTION);
            }
            long timeInterval;
            if (checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.SIMULATION_TIME_INTERVAL)) {
                timeInterval = simulationPropertiesConfig.getLong(EventSimulatorConstants.SIMULATION_TIME_INTERVAL);
            } else {
                timeInterval = 1000;
                log.warn("Time interval is required for simulation '"
                                 + simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                                 + "'. Time interval is set to 1 second for simulation configuration : "
                                 + simulationPropertiesConfig.toString());
            }
            /*
             * if startTimestamp no provided or is set to null it implies the current system time must be taken as
             * the timestamp start time
             * else if startTimestamp is specified, and that value is positive use that value as least possible
             * timestamp value
             * */
            long startTimestamp = -1;
            if (checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.START_TIMESTAMP)) {
                startTimestamp = simulationPropertiesConfig.getLong(EventSimulatorConstants.START_TIMESTAMP);
            }
            /*
             * if endTimestamp is null set endTimestamp property as -1. it implies that there is no bound
             * for maximum timestamp possible for an event.
             * */
            long endTimestamp = -1;
            if (checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.END_TIMESTAMP)) {
                endTimestamp = simulationPropertiesConfig.getLong(EventSimulatorConstants.END_TIMESTAMP);
            }
            /*
             * if noOfEventRequired is null it implies that there is no limit on the number of events to be generated
             * */
            int noOfEventsRequired = -1;
            if (checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)) {
                noOfEventsRequired = simulationPropertiesConfig.getInt(EventSimulatorConstants.
                                                                               NUMBER_OF_EVENTS_REQUIRED);
            }
//            create simulationPropertiesDTO object
            SimulationPropertiesDTO simulationPropertiesDTO = new SimulationPropertiesDTO();
            simulationPropertiesDTO.setSimulationName(simulationPropertiesConfig
                                                              .getString(
                                                                      EventSimulatorConstants.EVENT_SIMULATION_NAME));
            simulationPropertiesDTO.setDescription(description);
            simulationPropertiesDTO.setTimeInterval(timeInterval);
            simulationPropertiesDTO.setStartTimestamp(startTimestamp);
            simulationPropertiesDTO.setEndTimestamp(endTimestamp);
            simulationPropertiesDTO.setNoOfEventsRequired(noOfEventsRequired);
            return simulationPropertiesDTO;

        } catch (JSONException e) {
            log.error("Error occurred when accessing simulation configuration for simulation '"
                              + simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                              + "'. Invalid  simulation properties configuration provided : "
                              + simulationPropertiesConfig.toString() + ". ", e);
            throw new InvalidConfigException(
                    "Error occurred when accessing simulation configuration for simulation '"
                            + simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                            + "'. Invalid simulation properties configuration provided : "
                            + simulationPropertiesConfig.toString() + ". ", e);
        }
    }


    /**
     * run() method of runnable associated with the event simulator
     * This method starts all the event generators belonging to the simulations and begins the event simulation
     */
    @Override
    public void run() {
        try {
            generators.forEach(EventGenerator::start);
            if (log.isDebugEnabled()) {
                log.debug("Event generators started. Begin event simulation of '" + simulationName + "'");
            }
            status = Status.RUN;
            eventSimulation();
        } catch (SimulatorInitializationException e) {
            /*
             * catch exception so that any resources opened could be closed and rethrow an exception indicating which
             * simulation failed
             * */
            stop();
            throw new SimulatorInitializationException("Error occurred when initializing event generators for "
                                                               + "simulation '"
                                                               + simulationProperties.getSimulationName() + "'. ", e);
        }
    }

    /**
     * stop() is used to stop event simulation
     *
     * @see org.wso2.carbon.event.simulator.core.impl.FeedApiServiceImpl#stop(String)
     * @see EventGenerator#stop()
     */
    public void stop() {
        if (!status.equals(Status.STOP)) {
            if(!lockStop.tryLock()){
                status = Status.PENDING_STOP;
            } else{
                status = Status.STOP;
                lockStop.unlock();
            }
            generators.forEach(EventGenerator::stop);
            if (log.isDebugEnabled()) {
                log.debug("Stop simulation '" + simulationName + "'");
            }
        }
    }


    /**
     * pause() is used to pause event simulation
     *
     * @see org.wso2.carbon.event.simulator.core.impl.FeedApiServiceImpl#pause(String)
     */
    public void pause() {
        if (!status.equals(Status.PAUSE)) {
            try {
                lock.acquire();
                status = Status.PAUSE;
                if (log.isDebugEnabled()) {
                    log.debug("Pause event simulation '" + simulationName + "'");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    /**
     * resume() is used to resume event simulation
     *
     * @see org.wso2.carbon.event.simulator.core.impl.FeedApiServiceImpl#resume(String)
     */
    public void resume() {
        if (status.equals(Status.PAUSE)) {
            lock.release();
            status = Status.RUN;
            if (log.isDebugEnabled()) {
                log.debug("Resume event simulation '" + simulationName + "'");
            }
        }
    }

    public Status getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * Action class specifies the possible actions user can take for a given simulation
     */
    public enum Action {
        RUN,
        PAUSE,
        RESUME,
        STOP
    }

    /**
     * Status class specifies the possible statuses a simulator can be in
     */
    public enum Status {
        RUN,
        PAUSE,
        STOP,
        ERROR,
        PENDING_STOP
    }
}
