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
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;

import java.util.ArrayList;
import java.util.List;

/**
 * CommonOperations class is used to perform functions common to all validations
 */
public class CommonOperations {

    /**
     * checkAvailability() performs the following checks on the the json object and key provided
     * 1. has
     * 2. isNull
     * 3. isEmpty
     *
     * @param configuration JSON object containing configuration
     * @param key           name of key
     * @return true if checks are successful, else false
     */
    public static boolean checkAvailability(JSONObject configuration, String key) {
        return configuration.has(key)
                && !configuration.isNull(key)
                && !configuration.getString(key).isEmpty();
    }

    /**
     * checkAvailabilityOfArray() performs the following checks on the the json object and key provided.
     * This method is used for key's that contains json array values.
     * 1. has
     * 2. isNull
     * 3. isEmpty
     *
     * @param configuration JSON object containing configuration
     * @param key           name of key
     * @return true if checks are successful, else false
     */
    public static boolean checkAvailabilityOfArray(JSONObject configuration, String key) {
        return configuration.has(key)
                && !configuration.isNull(key)
                && configuration.getJSONArray(key).length() > 0;
    }

    /**
     * Utility method which can be used to check if a given string instance is null or empty.
     *
     * @param field the string instance to be checked.
     * @return true if the field is null or empty.
     */
    public static boolean isEmpty(String field) {
        return (field == null || field.trim().length() == 0);
    }

    /**
     * Converts a flat string of key/value pairs (e.g. from an annotation) into a list of pairs.
     * Used String[] since Java does not offer tuples.
     *
     * @param annotationString the comma-separated string of key/value pairs.
     * @return a list processed and validated pairs.
     */
    public static List<String[]> processKeyValuePairs(String annotationString) {
        List<String[]> keyValuePairs = new ArrayList<>();
        if (!isEmpty(annotationString)) {
            String[] pairs = annotationString.split(",");
            for (String element : pairs) {
                if (!element.contains(":")) {
                    throw new SimulatorInitializationException(
                            "Property '" + element + "' does not adhere to the expected " +
                                    "format: a property must be a key-value pair separated by a colon (:)");
                }
                String[] pair = element.split(":");
                if (pair.length != 2) {
                    throw new SimulatorInitializationException(
                            "Property '" + pair[0] + "' does not adhere to the expected " +
                                    "format: a property must be a key-value pair separated by a colon (:)");
                } else {
                    keyValuePairs.add(new String[]{pair[0].trim(), pair[1].trim()});
                }
            }
        }
        return keyValuePairs;
    }

}
