
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;


public interface InputMapperFactory {

    /**
     * Construct an {@link InputMapping} from the give omElement and return it.
     *
     * @param omElement the {@link OMElement} that will be used to construct input mapping
     * @return the constructed {@link InputMapping}
     */
    InputMapping constructInputMappingFromOM(OMElement omElement) throws EventBuilderConfigurationException;

    /**
     * Construct an OMElement from a given input mapping
     *
     * @param inputMapping the {@link InputMapping} that will be used to create the OMElement
     * @param factory      the {@link OMFactory} that will be used in this construction
     * @return the constructed {@link OMElement}
     */
    OMElement constructOMFromInputMapping(InputMapping inputMapping, OMFactory factory);

    /**
     * Constructs an returns an appropriate InputMapper depending on the Factory Implementation
     *
     * @param eventBuilderConfiguration the {@link EventBuilderConfiguration} to be used
     * @param inputStreamDefinition     the input {@link StreamDefinition}
     * @return the {@link InputMapper} instance based on the supplied configuration
     */
    InputMapper constructInputMapper(EventBuilderConfiguration eventBuilderConfiguration, StreamDefinition inputStreamDefinition) throws EventBuilderConfigurationException;
}
