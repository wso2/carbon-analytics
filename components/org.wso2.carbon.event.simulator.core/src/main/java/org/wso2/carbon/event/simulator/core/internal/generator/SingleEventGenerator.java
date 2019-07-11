/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.simulator.core.internal.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.SingleEventSimulationDTO;
import org.wso2.carbon.event.simulator.core.internal.util.EventConverter;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.carbon.streaming.integrator.common.exception.ResourceNotFoundException;
import io.siddhi.core.event.Event;
import io.siddhi.query.api.definition.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;


/**
 * SingleEventGenerator class is responsible for single event simulation
 */
public class SingleEventGenerator {
    private static final Logger log = LoggerFactory.getLogger(SingleEventGenerator.class);

    public SingleEventGenerator() {
    }

    /**
     * sendEvent() is used to send a single event based on the configuration provided by the SingleEventSimulationDTO
     * object
     *
     * @param singleEventConfiguration configuration of the single event
     * @throws InvalidConfigException          if the single even simulation configuration contains invalid entries
     * @throws InsufficientAttributesException if the number of attributes specified for the event is not equal to
     *                                         the number of stream attributes
     * @throws ResourceNotFoundException       if a resource required for simulation is not found
     */
    public static void sendEvent(String singleEventConfiguration)
            throws InvalidConfigException, InsufficientAttributesException, ResourceNotFoundException {
        validateSingleEventConfig(singleEventConfiguration);
        SingleEventSimulationDTO singleEventConfig = createSingleEventDTO(singleEventConfiguration);
        List<Attribute> streamAttributes;
        try {
            streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                    .getStreamAttributes(singleEventConfig.getSiddhiAppName(),
                            singleEventConfig.getStreamName());
        } catch (ResourceNotFoundException e) {
            log.error(e.getResourceTypeString() + " '" + e.getResourceName() + "' specified for single event " +
                    "simulation does not exist. Invalid single event simulation configuration : " + singleEventConfig
                    .toString(), e);
            throw new ResourceNotFoundException(e.getResourceTypeString() + " '" + e.getResourceName() + "' " +
                    "specified for single event simulation does not exist. Invalid single event simulation " +
                    "configuration : " + singleEventConfig.toString(), e);
        }
        try {
            Event event = EventConverter.eventConverter(streamAttributes,
                    singleEventConfig.getAttributeValues(),
                    singleEventConfig.getTimestamp());
            EventSimulatorDataHolder.getInstance().getEventStreamService().pushEvent(
                    singleEventConfig.getSiddhiAppName(),
                    singleEventConfig.getStreamName(), event);
        } catch (EventGenerationException e) {
            log.error("Event dropped due to an error that occurred during single event simulation of stream '" +
                    singleEventConfig.getStreamName() + "' for configuration '" + singleEventConfig.toString() +
                    "'. ", e);
            throw new EventGenerationException("Event dropped due to an error that occurred during single event " +
                    "simulation of stream '" + singleEventConfig.getStreamName() + "' for configuration '" +
                    singleEventConfig.toString() + "'. ", e);
        }
    }

    /**
     * validateSingleEventConfig() is used to validate single event simulation provided a
     *
     * @param singleEventConfiguration SingleEventSimulationDTO containing single event simulation configuration
     * @throws InvalidConfigException          if the single even simulation configuration contains invalid entries
     * @throws InsufficientAttributesException if the number of attributes specified for the event is not equal to
     *                                         the number of stream attributes
     * @throws ResourceNotFoundException       if a resource required for simulation is not found
     */
    private static void
    validateSingleEventConfig(String singleEventConfiguration)
            throws InvalidConfigException, InsufficientAttributesException, ResourceNotFoundException {
        try {
            JSONObject singleEventConfig = new JSONObject(singleEventConfiguration);
            if (!checkAvailability(singleEventConfig, EventSimulatorConstants.STREAM_NAME)) {
                throw new InvalidConfigException("Stream name is required for single event simulation. Invalid " +
                        "configuration provided : " + singleEventConfig.toString());
            }
            if (!checkAvailability(singleEventConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
                throw new InvalidConfigException("Siddhi app name is required for single event simulation of " +
                        "stream '" + singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME) + ". " +
                        "Invalid configuration provided : " + singleEventConfig.toString());
            }
            List<Attribute> streamAttributes;
            try {
                streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                        .getStreamAttributes(singleEventConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME),
                                singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME));
            } catch (ResourceNotFoundException e) {
                log.error(e.getResourceTypeString() + " '" + e.getResourceName() + "'" + " specified for single " +
                        "event simulation does not exist. Invalid single event simulation configuration : " +
                        singleEventConfig.toString(), e);
                throw new ResourceNotFoundException(e.getResourceTypeString() + " '" + e.getResourceName() + "' " +
                        "specified for single event simulation does not exist. Invalid single event simulation " +
                        "configuration : " + singleEventConfig.toString(), e);
            }
            if (checkAvailability(singleEventConfig, EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP)) {
                long timestamp = singleEventConfig.getLong(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP);
                if (timestamp < 0) {
                    throw new InvalidConfigException("Timestamp must be a positive value for single event" +
                            " simulation of stream '" + singleEventConfig.getString(EventSimulatorConstants
                            .STREAM_NAME) + "'. Invalid configuration provided : " +
                            singleEventConfig.toString());
                }
            }
            if (checkAvailabilityOfArray(singleEventConfig, EventSimulatorConstants.SINGLE_EVENT_DATA)) {
                ArrayList dataValues = new ObjectMapper().readValue(singleEventConfig.getJSONArray(EventSimulatorConstants
                        .SINGLE_EVENT_DATA).toString(), ArrayList.class);
                if (dataValues.size() != streamAttributes.size()) {
                    log.error("Simulation of stream '" + singleEventConfig.getString(EventSimulatorConstants
                            .STREAM_NAME) + "' requires " + streamAttributes.size() + " attribute(s). Single event " +
                            "configuration contains values for " + dataValues.size() + " attribute(s)");
                    throw new InsufficientAttributesException("Simulation of stream '" +
                            singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME) + "' requires " +
                            streamAttributes.size() + " attribute(s). Single event configuration contains" +
                            " values for " + dataValues.size() + " attribute(s). Invalid single event simulation " +
                            "configuration : " + singleEventConfig.toString());
                }
            } else {
                throw new InvalidConfigException("Single event simulation requires a attribute value for " +
                        "stream '" + singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid " +
                        "configuration provided : " + singleEventConfig.toString());
            }
        } catch (JsonProcessingException e) {
            log.error("Error occurred when processing single event simulation configuration. ", e);
            throw new InvalidConfigException("Error occurred when processing single event simulation configuration. ",
                    e);
        } catch (IOException e) {
            log.error("Error occurred when accessing single event simulation configuration. ", e);
            throw new InvalidConfigException("Error occurred when accessing single event simulation configuration. ",
                    e);
        }
    }

    /**
     * createSingleEventDTO() is used create a SingleEventSimulationDTO object containing simulation configuration
     *
     * @param singleEventConfiguration SingleEventSimulationDTO containing single event simulation configuration
     * @return SingleEventSimulationDTO if required configuration is provided
     * @throws InvalidConfigException if the single even simulation configuration contains invalid entries
     */
    private static SingleEventSimulationDTO createSingleEventDTO(String singleEventConfiguration)
            throws InvalidConfigException {
        try {
            JSONObject singleEventConfig = new JSONObject(singleEventConfiguration);
//            if timestamp is set to null, take current system time as the timestamp of the event
            long timestamp = System.currentTimeMillis();
            if (checkAvailability(singleEventConfig, EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP)) {
                timestamp = singleEventConfig.getLong(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP);
                if (timestamp < 0) {
                    throw new InvalidConfigException("Timestamp must be a positive value for single event" +
                            " simulation of stream '" + singleEventConfig.getString(EventSimulatorConstants
                            .STREAM_NAME) + "'. Invalid configuration provided : " +
                            singleEventConfig.toString());
                }
            }
            ArrayList dataValues = new ObjectMapper().readValue(singleEventConfig.getJSONArray(EventSimulatorConstants
                    .SINGLE_EVENT_DATA).toString(), ArrayList.class);
            SingleEventSimulationDTO singleEventSimulationDTO = new SingleEventSimulationDTO();
            singleEventSimulationDTO.setStreamName(singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME));
            singleEventSimulationDTO.setSiddhiAppName(singleEventConfig
                    .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
            singleEventSimulationDTO.setTimestamp(timestamp);
            singleEventSimulationDTO.setAttributeValues(dataValues.toArray());
            return singleEventSimulationDTO;
        } catch (JsonProcessingException e) {
            log.error("Error occurred when processing single event simulation configuration. ", e);
            throw new InvalidConfigException("Error occurred when processing single event simulation configuration. ",
                    e);
        } catch (IOException e) {
            log.error("Error occurred when accessing single event simulation configuration. ", e);
            throw new InvalidConfigException("Error occurred when accessing single event simulation configuration. ",
                    e);
        }
    }
}
