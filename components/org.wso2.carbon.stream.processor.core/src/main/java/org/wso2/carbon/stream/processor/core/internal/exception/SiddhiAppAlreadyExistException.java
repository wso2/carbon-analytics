/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.stream.processor.core.internal.exception;

/**
 * Execption class that used when there is a Siddhi app exist with same name
 */
public class SiddhiAppAlreadyExistException extends Exception {

    public SiddhiAppAlreadyExistException() {
    }

    public SiddhiAppAlreadyExistException(String message) {
        super(message);
    }

    public SiddhiAppAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public SiddhiAppAlreadyExistException(Throwable cause) {
        super(cause);
    }
}
