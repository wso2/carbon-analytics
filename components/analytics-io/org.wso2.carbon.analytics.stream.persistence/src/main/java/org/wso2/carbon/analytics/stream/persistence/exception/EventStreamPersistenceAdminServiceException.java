/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.stream.persistence.exception;

import org.apache.axis2.AxisFault;

/**
 * This class represent all the Event Stream Persistence Admin Service exceptions
 */
public class EventStreamPersistenceAdminServiceException extends AxisFault {

    private static final long serialVersionUID = -1046906533158581316L;
    private final String message;

    public EventStreamPersistenceAdminServiceException(String message) {
        super(message);
        this.message = message;
    }

    public EventStreamPersistenceAdminServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
