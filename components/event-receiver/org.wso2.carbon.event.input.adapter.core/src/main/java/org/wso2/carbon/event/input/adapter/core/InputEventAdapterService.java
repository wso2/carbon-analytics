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
package org.wso2.carbon.event.input.adapter.core;


import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;

import java.util.List;

/**
 * OSGI interface for the EventAdapter Service
 */

public interface InputEventAdapterService {


    /**
     * this method returns all the available event adapter types. UI use this details to
     * show the types and the properties to be set to the user when creating the
     * event adapter objects.
     *
     * @return list of available types
     */
    List<String> getInputEventAdapterTypes();

    /**
     * This method returns the event adapter dto for a specific event adapter type
     *
     * @param eventAdapterType
     * @return
     */
    InputEventAdapterSchema getInputEventAdapterSchema(String eventAdapterType);


    void create(InputEventAdapterConfiguration inputEventAdapterConfiguration, InputEventAdapterSubscription inputEventAdapterSubscription) throws InputEventAdapterException;

    /**
     * publish testConnect message using the given event adapter.
     * @param inputEventAdapterConfiguration - Configuration Details of the event adapter
     */
    void testConnection(InputEventAdapterConfiguration inputEventAdapterConfiguration) throws InputEventAdapterException, TestConnectionNotSupportedException;

    void destroy(String name);
}
