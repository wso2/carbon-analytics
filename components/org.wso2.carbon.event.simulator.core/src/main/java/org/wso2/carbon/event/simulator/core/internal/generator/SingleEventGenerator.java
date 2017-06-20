package org.wso2.carbon.event.simulator.core.internal.generator;

import com.google.gson.Gson;

import org.json.JSONException;
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
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;

import java.util.ArrayList;
import java.util.List;


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
    private static void validateSingleEventConfig(String singleEventConfiguration)
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
                ArrayList dataValues = new Gson().fromJson(singleEventConfig.getJSONArray(EventSimulatorConstants
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
        } catch (JSONException e) {
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
            ArrayList dataValues = new Gson().fromJson(singleEventConfig.getJSONArray(EventSimulatorConstants
                    .SINGLE_EVENT_DATA).toString(), ArrayList.class);
//            create SingleEventSimulationDTO
            SingleEventSimulationDTO singleEventSimulationDTO = new SingleEventSimulationDTO();
            singleEventSimulationDTO.setStreamName(singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME));
            singleEventSimulationDTO.setSiddhiAppName(singleEventConfig
                    .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
            singleEventSimulationDTO.setTimestamp(timestamp);
            singleEventSimulationDTO.setAttributeValues(dataValues.toArray());
            return singleEventSimulationDTO;
        } catch (JSONException e) {
            log.error("Error occurred when accessing single event simulation configuration. ", e);
            throw new InvalidConfigException("Error occurred when accessing single event simulation configuration. ",
                    e);
        }
    }

}
