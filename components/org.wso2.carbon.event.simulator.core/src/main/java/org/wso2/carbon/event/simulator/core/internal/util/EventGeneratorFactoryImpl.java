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

package org.wso2.carbon.event.simulator.core.internal.util;

import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.core.CSVEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.database.core.DatabaseEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.core.RandomEventGenerator;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

/**
 * Factory class used to create event generators
 */
public class EventGeneratorFactoryImpl implements EventGeneratorFactory {

    /**
     * getEventGenerator() creates and initializes event generators according to the source configuration provided
     *
     * @param sourceConfig   json object containing source configuration used for simulation
     * @param startTimestamp least possible timestamp an event produced could have
     * @param endTimestamp   maximum possible timestamp an even produced could have
     * @throws InvalidConfigException    if the simulation type is not specified or if an invalid generator type
     *                                   is specified
     * @throws ResourceNotFoundException if a resource required for simulation is not found
     */
    @Override
    public EventGenerator createEventGenerator(JSONObject sourceConfig, long startTimestamp, long endTimestamp)
            throws InvalidConfigException, ResourceNotFoundException {
        if (checkAvailability(sourceConfig, EventSimulatorConstants.EVENT_SIMULATION_TYPE)) {
            EventGenerator.GeneratorType generatorType;
            try {
                generatorType = EventGenerator.GeneratorType.valueOf(sourceConfig.
                        getString(EventSimulatorConstants.EVENT_SIMULATION_TYPE));
            } catch (IllegalArgumentException e) {
                throw new InvalidConfigException("Simulation type must be " +
                        "either '" + EventGenerator.GeneratorType.CSV_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'. Invalid source configuration " +
                        "provided : " + sourceConfig.toString());
            }
//            initialize generators for sources
            EventGenerator eventGenerator = null;
            switch (generatorType) {
                case CSV_SIMULATION:
                    eventGenerator = new CSVEventGenerator();
                    eventGenerator.init(sourceConfig, startTimestamp, endTimestamp);
                    break;
                case DATABASE_SIMULATION:
                    eventGenerator = new DatabaseEventGenerator();
                    eventGenerator.init(sourceConfig, startTimestamp, endTimestamp);
                    break;
                case RANDOM_DATA_SIMULATION:
                    eventGenerator = new RandomEventGenerator();
                    eventGenerator.init(sourceConfig, startTimestamp, endTimestamp);
                    break;
            }
            return eventGenerator;
        } else {
            throw new InvalidConfigException("Simulation type must be specified as either '" +
                    EventGenerator.GeneratorType.CSV_SIMULATION + "' or '" +
                    EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                    EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'. Invalid source configuration " +
                    "provided : " + sourceConfig.toString());
        }
    }

    /**
     * validateGeneratorConfiguration() validates event generator configurations provided
     *
     * @param sourceConfig json object containing source configuration used for simulation
     * @throws InvalidConfigException          if the simulation type is not specified or if an invalid generator type
     *                                         is specified
     * @throws InsufficientAttributesException if the number of attributes produced by generator is not equal to the
     *                                         number of attributes in the stream being simulated
     * @throws ResourceNotFoundException       if a resource required for simulation is not found
     */
    @Override
    public void validateGeneratorConfiguration(JSONObject sourceConfig) throws InvalidConfigException,
            InsufficientAttributesException, ResourceNotFoundException {
        if (checkAvailability(sourceConfig, EventSimulatorConstants.EVENT_SIMULATION_TYPE)) {
            EventGenerator.GeneratorType generatorType;
            try {
                generatorType = EventGenerator.GeneratorType.valueOf(sourceConfig.
                        getString(EventSimulatorConstants.EVENT_SIMULATION_TYPE));
            } catch (IllegalArgumentException e) {
                throw new InvalidConfigException("Simulation type must be " +
                        "either '" + EventGenerator.GeneratorType.CSV_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'. Invalid source configuration " +
                        "provided : " + sourceConfig.toString());
            }
            switch (generatorType) {
                case CSV_SIMULATION:
                    new CSVEventGenerator().validateSourceConfiguration(sourceConfig);
                    break;
                case DATABASE_SIMULATION:
                    new DatabaseEventGenerator().validateSourceConfiguration(sourceConfig);
                    break;
                case RANDOM_DATA_SIMULATION:
                    new RandomEventGenerator().validateSourceConfiguration(sourceConfig);
                    break;
            }
        } else {
            throw new InvalidConfigException("Simulation type must be specified either '" +
                    EventGenerator.GeneratorType.CSV_SIMULATION + "' or '" +
                    EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                    EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'. Invalid source configuration " +
                    "provided : " + sourceConfig.toString());
        }
    }
}
