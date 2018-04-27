/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.designview.exceptions;

/**
 * Represents the Exception, that can be thrown by methods of the DesignGeneratorHelper class
 */
public class DesignGeneratorHelperException extends Exception {
    /**
     * Constructs an exception with the given detailed message
     * @param message   Detailed message regarding the exception
     */
    public DesignGeneratorHelperException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the given detailed message and cause
     * @param message   Detailed message regarding the exception
     * @param cause     Cause of the exception
     */
    public DesignGeneratorHelperException(String message, Throwable cause) {
        super(message, cause);
    }
}
