/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.restapi;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * Exception class representing the user authentication failures
 */
public class UnauthenticatedUserException extends AnalyticsException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1147741480686821351L;
    /**
     * Instantiates a new analytics rest exception.
     * @param message
     *            the message
     */
    public UnauthenticatedUserException(String message) {
        super(message);
    }

    /**
     * Instantiates a new analytics rest exception.
     * @param message
     *            the message
     * @param e
     *            the throwable
     */
    public UnauthenticatedUserException(String message, Throwable e) {
        super(message, e);
    }
}
