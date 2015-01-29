/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Date: 8/6/13
 * Time: 11:44 AM
 */

package org.wso2.carbon.bam.jmx.agent.exceptions;

public class JmxProfileException extends Exception {

    private static final long serialVersionUID = 1964250880783971787L;

    private String message;

    public String getMessage() {
        return message;
    }

    public JmxProfileException(String message) {
        this.message = message;
    }

    public JmxProfileException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
