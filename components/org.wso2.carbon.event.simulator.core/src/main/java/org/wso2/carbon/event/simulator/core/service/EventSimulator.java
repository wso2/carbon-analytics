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

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EventSimulator starts the simulation execution for single Event and
 * Feed Simulation
 */
public class EventSimulator implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EventSimulator.class);
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;
    private List<EventGenerator> generators = new ArrayList<>();
    private SimulationPropertiesDTO simulationProperties;
    private String simulationName;


    /**
     * EventSimulator() constructor initializes an EventSimulator object
     *
     * @param simulationName unique identifies of simulation
     * @param simulationConfiguration a string containing the simulation configuration
     * @throws InsufficientAttributesException is a configuration does not produce data for all stream attributes
     * @throws InvalidConfigException          if the simulation configuration is invalid
     */
    public EventSimulator(String simulationName, String simulationConfiguration)
            throws InsufficientAttributesException, InvalidConfigException {
        this.simulationName = simulationName;
        JSONObject simulationConfig = new JSONObject(simulationConfiguration);
//        first create a simulation properties object
        if (simulationConfig.has(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)
                && !simulationConfig.isNull(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)) {
            simulationProperties = validateSimulationProperties(simulationConfig
                    .getJSONObject(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES));
//            check whether the simulation has source configurations and create event generators for each source config
            if (checkAvailabilityOfArray(simulationConfig,
                    EventSimulatorConstants.EVENT_SIMULATION_SOURCES)) {
                JSONArray streamConfig = simulationConfig.getJSONArray(EventSimulatorConstants
                        .EVENT_SIMULATION_SOURCES);
                EventGeneratorFactoryImpl generatorFactory = new EventGeneratorFactoryImpl();
                for (int i = 0; i < streamConfig.length(); i++) {
                    generators.add(generatorFactory.getEventGenerator(streamConfig.getJSONObject(i),
                            simulationProperties.getStartTimestamp(), simulationProperties.getEndTimestamp()));
                }
            } else {
                throw new InvalidConfigException("Source configuration is required for event simulation '" +
                        simulationProperties.getSimulationName() + "'. Invalid simulation configuration provided : " +
                        simulationConfig.toString());
            }
        } else {
            throw new InvalidConfigException("Simulation properties are required for event simulation. Invalid " +
                    "simulation configuration provided : " + simulationConfig.toString());
        }

    }


    /**
     * eventSimulation() method is responsible for sending events belonging to one simulation configuration in the
     * order of their timestamps
     * Events will be sent at time intervals equal to the delay
     */
    private void eventSimulation() {
        Long minTimestamp;
        EventGenerator generator;
        int eventsRemaining = simulationProperties.getNoOfEventsRequired();
        try {
            while (!isStopped) {
//                if the simulator is paused, wait till it is resumed
                synchronized (this) {
                    while (isPaused) {
                        wait();
                    }
                }
                /**
                 * if there is no limit to the number of events to be sent or is the number of event remaining to be
                 * sent is > 0, send an event, else stop event simulation
                 * */
                if (eventsRemaining == -1 || eventsRemaining > 0) {
                    minTimestamp = -1L;
                    generator = null;
                    /**
                     * 1. for each event generator peek the next event (i.e. the next event with least timestamp)
                     * 2. take the first event generator will a not null nextEvent as the first refferal value for
                     * generator with minimum timestamp event, and take the events timestamp as the minimum timestamp
                     * refferal value
                     * 3. then compare the timestamp of the remaining not null nextEvents with the minimum timestamp and
                     * update the minimum timestamp refferal value accordingly.
                     * 4. once all generators are iterated and the event with minimum timestamp if obtained, send event.
                     * 5. if all generators has nextEvent == null, then stop event simulation
                     * */
                    for (EventGenerator eventGenerator : generators) {
                        if (eventGenerator.peek() != null) {
                            if (minTimestamp == -1L) {
                                minTimestamp = eventGenerator.peek().getTimestamp();
                                generator = eventGenerator;
                            } else if (eventGenerator.peek().getTimestamp() < minTimestamp) {
                                minTimestamp = eventGenerator.peek().getTimestamp();
                                generator = eventGenerator;
                            }
                        }
                    }
                    if (minTimestamp >= 0L && generator != null) {
                        log.info("Input Event (" + simulationName + ") : "
                                + Arrays.deepToString(generator.peek().getData()));
                        EventSimulatorDataHolder.getInstance().getEventStreamService()
                                .pushEvent(generator.getExecutionPlanName(), generator.getStreamName(),
                                        generator.poll());
                    } else {
                        break;
                    }
                    if (eventsRemaining > 0) {
                        eventsRemaining--;
                    }
                    Thread.sleep(simulationProperties.getTimeInterval());
                } else {
                    break;
                }
            }
            stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EventGenerationException e) {
            /**
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
    private SimulationPropertiesDTO validateSimulationProperties(JSONObject simulationPropertiesConfig)
            throws InvalidConfigException {
        /**
         * checkAvailability() method performs the following checks
         * 1. has
         * 2. isNull
         * 3. isEmpty
         *
         * if checks are successful create simulationPropertiesDTO object
         * */
        try {
            if (!checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.EVENT_SIMULATION_NAME)) {
                throw new InvalidConfigException("Simulation name is required for event simulation. Invalid " +
                        "simulation properties configuration provided : " + simulationPropertiesConfig.toString());
            }
            long timeInterval;
            if (checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.SIMULATION_TIME_INTERVAL)) {
                timeInterval = simulationPropertiesConfig.getLong(EventSimulatorConstants.SIMULATION_TIME_INTERVAL);
            } else {
                log.warn("Time interval is required for simulation '" +
                        simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                        "'. Time interval is set to 1 second for simulation configuration : "
                        + simulationPropertiesConfig.toString());
                timeInterval = 1000;
            }
            /**
             * if timestampStartTime no provided or is set to null it implies the current system time must be taken as
             * the timestamp start time
             * if null, set timestampStartTime to system current time
             * else if timestampStartTime is specified, and that value is positive use that value as least possible
             * timestamp value
             * */
            long timestampStartTime;
            if (simulationPropertiesConfig.has(EventSimulatorConstants.START_TIMESTAMP)) {
                if (simulationPropertiesConfig.isNull(EventSimulatorConstants.START_TIMESTAMP)) {
                    timestampStartTime = System.currentTimeMillis();
                } else if (!simulationPropertiesConfig.getString(EventSimulatorConstants.START_TIMESTAMP)
                        .isEmpty()) {
                    timestampStartTime = simulationPropertiesConfig
                            .getLong(EventSimulatorConstants.START_TIMESTAMP);
                    if (timestampStartTime < 0) {
                        throw new InvalidConfigException("TimestampStartTime must be a positive value for simulation " +
                                "'" + simulationPropertiesConfig.getString(EventSimulatorConstants
                                .EVENT_SIMULATION_NAME) + "'. Invalid simulation properties configuration provided : "
                                + simulationPropertiesConfig.toString());
                    }
                } else {
                    throw new InvalidConfigException("TimestampStartTime is required for simulation '" +
                            simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                            + "'. Invalid simulation properties configuration provided : "
                            + simulationPropertiesConfig.toString());
                }
            } else {
                log.warn("TimestampStartTime is required for simulation '" +
                        simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                        + "'. TimestampStartTime is set to current system time for simulation : " +
                        simulationPropertiesConfig.toString());
                timestampStartTime = System.currentTimeMillis();
            }
            /**
             * if timestampEndTime is null set timestampEndTime property as -1. it implies that there is no bound
             * for maximum timestamp possible for an event.
             * */
            long timestampEndTime = -1;
            int noOfEventsRequired = -1;
            if (simulationPropertiesConfig.has(EventSimulatorConstants.END_TIMESTAMP)) {
                if (simulationPropertiesConfig.isNull(EventSimulatorConstants.END_TIMESTAMP)) {
                    timestampEndTime = -1;
                } else if (!simulationPropertiesConfig.getString(EventSimulatorConstants.END_TIMESTAMP)
                        .isEmpty()) {
                    timestampEndTime = simulationPropertiesConfig.getLong(EventSimulatorConstants.END_TIMESTAMP);
                    if (timestampEndTime < 0) {
                        throw new InvalidConfigException("TimestampEndTime must be a positive value for simulation " +
                                "'" + simulationPropertiesConfig.getString(EventSimulatorConstants
                                .EVENT_SIMULATION_NAME) + "'. Invalid simulation properties configuration provided : "
                                + simulationPropertiesConfig.toString());
                    }
                } else {
                    throw new InvalidConfigException("TimestampEndTime is required for simulation '" +
                            simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                            "'. TimestampEndTime must be either specified or set to null. Invalid simulation " +
                            "properties configuration provided : " + simulationPropertiesConfig.toString());
                }
            }
            /**
             * if noOfEventRequired is null it implies that there is no limit on the number of events to be generated
             * */
            if (simulationPropertiesConfig.has(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)) {
                if (simulationPropertiesConfig.isNull(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)) {
                    noOfEventsRequired = -1;
                } else if (!simulationPropertiesConfig.getString(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)
                        .isEmpty()) {
                    noOfEventsRequired = simulationPropertiesConfig
                            .getInt(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED);
                    if (noOfEventsRequired < 0) {
                        throw new InvalidConfigException("Number of event to be generated for simulation '" +
                                simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                                "' must be a positive value. Invalid simulation  configuration provided : " +
                                simulationPropertiesConfig.toString());
                    }
                } else {
                    throw new InvalidConfigException("Number of event to be generated for simulation '" +
                            simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) + "' " +
                            "must be either specified or set to null. Invalid simulation  configuration provided : " +
                            simulationPropertiesConfig.toString());
                }
            }
            if (timestampEndTime != -1 && timestampEndTime < timestampStartTime) {
                throw new InvalidConfigException("Either the timestampEndTime must be set to null " +
                        "or the timestampStartTime must be less than or equal the timestampEndTime. Invalid " +
                        "simulation properties configuration provided : " + simulationPropertiesConfig.toString());
            }
//            create simulationPropertiesDTO object
            SimulationPropertiesDTO simulationPropertiesDTO = new SimulationPropertiesDTO();
            simulationPropertiesDTO.setSimulationName(simulationPropertiesConfig
                    .getString(EventSimulatorConstants.EVENT_SIMULATION_NAME));
            simulationPropertiesDTO.setTimeInterval(timeInterval);
            simulationPropertiesDTO.setStartTimestamp(timestampStartTime);
            simulationPropertiesDTO.setEndTimestamp(timestampEndTime);
            simulationPropertiesDTO.setNoOfEventsRequired(noOfEventsRequired);
            return simulationPropertiesDTO;
        } catch (JSONException e) {
            log.error("Error occurred when accessing simulation configuration for simulation '" +
                            simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                            "'. Invalid  simulation properties configuration provided : " +
                            simulationPropertiesConfig.toString() + ". ",
                    e);
            throw new InvalidConfigException("Error occurred when accessing simulation configuration for simulation '" +
                    simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) + "'. Invalid" +
                    " simulation properties configuration provided : " + simulationPropertiesConfig.toString() + ". ",
                    e);
        }
    }


    /**
     * run() method of runnable associated with the event simulator
     * This method starts all the event generators belonging to the simulations and begins the event simulation
     */
    @Override
    public void run() {
        isPaused = false;
        isStopped = false;
        try {
            generators.forEach(EventGenerator::start);
            if (log.isDebugEnabled()) {
                log.debug("Event generators started. Begin event simulation of '" + simulationName + "'");
            }
        } catch (SimulatorInitializationException e) {
            /**
             * catch exception so that any resources opened could be closed and rethrow an exception indicating which
             * simulation failed
             * */
            stop();
            throw new SimulatorInitializationException("Error occurred when initializing event generators for " +
                    "simulation '" + simulationProperties.getSimulationName() + "'. ", e);
        }
        eventSimulation();
    }

    /**
     * stop() is used to stop event simulation
     *
     * @see ServiceComponent#stop(String)
     * @see EventGenerator#stop()
     */
    public synchronized void stop() {
        if (!isStopped()) {
            isStopped = true;
            generators.forEach(EventGenerator::stop);
            if (log.isDebugEnabled()) {
                log.debug("Stop simulation '" + simulationName + "'");
            }
        }
    }


    /**
     * pause() is used to pause event simulation
     *
     * @see ServiceComponent#pause(String)
     */
    public synchronized void pause() {
        if (!isPaused) {
            isPaused = true;
            if (log.isDebugEnabled()) {
                log.debug("Pause event simulation '" + simulationName + "'");
            }
        }
    }


    /**
     * resume() is used to resume event simulation
     *
     * @see ServiceComponent#resume(String)
     */
    public synchronized void resume() {
        if (isPaused) {
            isPaused = false;
            notifyAll();
            if (log.isDebugEnabled()) {
                log.debug("Resume event simulation '" + simulationName + "'");
            }
        }
    }

    /**
     * isPaused() is used to check whether a simulation is paused or not
     *
     * return true of simulation is paused else return false
     * */
    public boolean isPaused() {
        return isPaused;
    }
    /**
     * isStopped() is used to check whether a simulation is stopped or not
     *
     * return true of simulation is stopped else return false
     * */
    public boolean isStopped() {
        return isStopped;
    }
    /**
     * getSimulationName() is required to retrieve the simulation name of the simulation when pausing, resuming and
     * stopping simulations
     *
     * @return simulation name
     * */
    public String getSimulationName() {
        return simulationProperties.getSimulationName();
    }

    /**
     * Action class specifies the possible actions user can take for a given simulation
     * */
    public enum Action { RUN, PAUSE, RESUME, STOP}
}
