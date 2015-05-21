/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.output.adapter.core;

import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;

import java.util.Map;

/**
 * This is a EventAdapter type. these interface let users to publish subscribe messages according to
 * some type. this type can either be local, jms or ws
 */
public interface OutputEventAdapter {

    /**
     * The init of the adapter, this will be called only once be for connect() and testConnect()
     * @throws OutputEventAdapterException if there are any configuration errors
     */
    void init() throws OutputEventAdapterException;

    /**
     * Used to test the connection
     * @throws TestConnectionNotSupportedException if test connection is not supported by the adapter
     * @throws ConnectionUnavailableException if it cannot connect to the backend
     */
    void testConnect() throws TestConnectionNotSupportedException, ConnectionUnavailableException;

    /**
     * Will be called to connect to the backend before events are published
     * @throws ConnectionUnavailableException if it cannot connect to the backend
     */
    void connect() throws ConnectionUnavailableException;

    /**
     * To publish the events
     * @param message event to be published, it can be Map,OMElement or String
     * @param dynamicProperties  the dynamic properties of the event
     * @throws ConnectionUnavailableException if it cannot connect to the backend
     */
    void publish(Object message, Map<String, String> dynamicProperties) throws ConnectionUnavailableException;

    /**
     * Will be called after all publishing is done, or when ConnectionUnavailableException is thrown
     */
    void disconnect();

    /**
     * Will be called at the end to clean all the resources consumed
     */
    void destroy();

}
