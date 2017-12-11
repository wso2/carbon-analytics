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

package org.wso2.carbon.event.simulator.core.util;


import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;

public class SourceConfigLogger {
    public static String getLoggedEnabledSourceConfig(JSONObject sourceConfig) {
        EventGenerator.GeneratorType generatorType =
                EventGenerator.GeneratorType.valueOf(
                        sourceConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_TYPE));

        switch (generatorType) {
            case CSV_SIMULATION:
                return getCSVLoggingConfig(sourceConfig);
            case DATABASE_SIMULATION:
                return getDatabaseLoggingConfig(sourceConfig);
            case RANDOM_DATA_SIMULATION:
                return getRandomLoggingConfig(sourceConfig);
        }
        return null;
    }

    private static String getCSVLoggingConfig(JSONObject sourceConfig) {
        StringBuilder builder = new StringBuilder("Type: CSV Simulation\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.STREAM_NAME)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.FILE_NAME)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.FILE_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)) {
            builder.append("\tTimestamp attribute: ")
                    .append(sourceConfig.getInt(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)).append("\n");
            if (sourceConfig.has(EventSimulatorConstants.IS_ORDERED) && !sourceConfig
                    .isNull(EventSimulatorConstants.IS_ORDERED)) {
                builder.append("\t\tOrdered: ").append(sourceConfig.getInt(EventSimulatorConstants.IS_ORDERED))
                        .append("\n");
            }
        } else if (checkAvailability(sourceConfig, EventSimulatorConstants.TIMESTAMP_INTERVAL)) {
            builder.append("\tTimestamp interval: ")
                    .append(sourceConfig.getInt(EventSimulatorConstants.TIMESTAMP_INTERVAL));
        } else {
            builder.append("\tTimestamp interval or attribute not provided");
        }
        builder.append("\n");
        builder.append("\tDelimiter: ");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.DELIMITER)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.DELIMITER));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        builder.append("\tIndices: ");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.INDICES)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.INDICES));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        return builder.toString();
    }

    private static String getDatabaseLoggingConfig(JSONObject sourceConfig) {
        StringBuilder builder = new StringBuilder("Type: Database Simulation\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.STREAM_NAME)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.DATA_SOURCE_LOCATION)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.DATA_SOURCE_LOCATION));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        builder.append("Driver: ");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.DRIVER)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.DRIVER));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        builder.append("Username: ");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.USER_NAME)) {
            builder.append("Username: ").append(sourceConfig.getString(EventSimulatorConstants.USER_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        builder.append("Password: ");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.PASSWORD)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.PASSWORD));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        builder.append("Table Name: ");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.TABLE_NAME)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.TABLE_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)) {
            builder.append("Event Timestamp attribute: ")
                    .append(sourceConfig.getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE));
        } else if (checkAvailability(sourceConfig, EventSimulatorConstants.TIMESTAMP_INTERVAL)) {
            builder.append("Time interval between event timestamps: ")
                    .append(sourceConfig.getString(EventSimulatorConstants.TABLE_NAME));
        } else {
            builder.append("\tTimestamp interval or attribute not provided.");
        }
        builder.append("\n");
        builder.append("Selected column list: ");
        if (sourceConfig.has(EventSimulatorConstants.COLUMN_NAMES_LIST) && !sourceConfig.isNull
                (EventSimulatorConstants.COLUMN_NAMES_LIST) &&
                !sourceConfig.getString(EventSimulatorConstants.COLUMN_NAMES_LIST).isEmpty()) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.COLUMN_NAMES_LIST));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        return builder.toString();
    }

    private static String getRandomLoggingConfig(JSONObject sourceConfig) {
        StringBuilder builder = new StringBuilder("Type: Random Simulation\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        if (checkAvailability(sourceConfig, EventSimulatorConstants.STREAM_NAME)) {
            builder.append(sourceConfig.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        builder.append("Attribute Config: ");
        if (checkAvailabilityOfArray(sourceConfig, EventSimulatorConstants.ATTRIBUTE_CONFIGURATION)) {
            builder.append("\n");
            for (int i = 0; i < sourceConfig.getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION).length();
                 i++) {
                JSONObject attributeConfig =
                        sourceConfig.getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION).getJSONObject(i);
                builder.append("\t").append(i+1).append(": ");
                if (checkAvailability(attributeConfig, EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)) {
                    String generatorType =
                            attributeConfig.getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE);
                    builder.append(generatorType);
                } else {
                    builder.append("not available");
                }
                builder.append("\n");
            }
        } else {
            builder.append("not available");
        }
        builder.append("\n");
        return builder.toString();
    }
}
