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

}
