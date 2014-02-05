/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.config;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

public interface InputMapper {
    /**
     * Converts the passed in object and returns an object array(s) with attributes as array elements.
     * The conversion will happen according to a predefined mapping
     *
     * @param obj the object to be converted
     * @return the converted object. This can be an object array or an array of object arrays
     *         in the case of the mapper supporting processing of batched events
     * @throws EventBuilderConfigurationException
     *
     */
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderConfigurationException;

    /**
     * Converts the passed in object and returns an object array(s) with attributes as array elements.
     * The conversion will happen based on the mapping type where all incoming attributes of an event
     * will be passed directly to consumers of event builder without any mapping happening.
     *
     * @param obj the object to be converted
     * @return the converted object. This can be an object array or an array of object arrays
     *         in the case of the mapper supporting processing of batched events
     * @throws EventBuilderConfigurationException
     *
     */
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderConfigurationException;

    /**
     * Returns an array of attributes that will be output from the stream definition.
     * All implementing classes need to output these output attributes in the specific
     * order of meta attributes, correlation attributes, payload attributes.
     *
     * @return an array of {@link Attribute} elements that will be used to create the exported stream definition
     */
    public Attribute[] getOutputAttributes();

}

