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

package org.wso2.carbon.event.simulator.core.exception;


import org.wso2.carbon.streaming.integrator.common.exception.ResourceNotFoundException;

/**
 * customized exception class for parsing simulation and stream configurations
 */
public class InvalidConfigException extends SimulationValidationException {

    /**
     * Throws customizes exception when parsing simulation and stream configurations
     *
     * @param message Error Message
     */
    public InvalidConfigException(String message) {
        super(message);
    }

    public InvalidConfigException(ResourceNotFoundException.ResourceType resourceType,
                                           String resourceName, String message) {
        super(message, resourceType, resourceName);
    }

    public InvalidConfigException(ResourceNotFoundException.ResourceType resourceType,
                                  String resourceName, String message, Throwable cause) {
        super(message, resourceType, resourceName, cause);
    }

    /**
     * Throws customizes exception when parsing simulation and stream configurations
     *
     * @param message Error Message
     * @param cause   Throwable that caused the InvalidConfigException
     */
    public InvalidConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
