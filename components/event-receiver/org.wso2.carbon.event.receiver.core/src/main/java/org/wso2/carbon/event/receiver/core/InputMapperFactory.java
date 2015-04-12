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
package org.wso2.carbon.event.receiver.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;


public interface InputMapperFactory {

    /**
     * Construct an {@link org.wso2.carbon.event.receiver.core.config.InputMapping} from the give omElement and return it.
     *
     * @param omElement the {@link OMElement} that will be used to construct input mapping
     * @return the constructed {@link org.wso2.carbon.event.receiver.core.config.InputMapping}
     */
    InputMapping constructInputMappingFromOM(OMElement omElement)
            throws EventReceiverConfigurationException;

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
     * @param eventReceiverConfiguration the {@link org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration} to be used
     * @param exportedStreamDefinition     the {@link StreamDefinition} exported from the event receiver
     * @return the {@link InputMapper} instance based on the supplied configuration
     */
    InputMapper constructInputMapper(EventReceiverConfiguration eventReceiverConfiguration,
                                     StreamDefinition exportedStreamDefinition)
            throws EventReceiverConfigurationException;
}
