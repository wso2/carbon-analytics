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
package org.wso2.carbon.event.receiver.core.internal.type.wso2event;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.InputMapper;
import org.wso2.carbon.event.receiver.core.InputMapperFactory;
import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;

public class WSO2EventInputMapperFactory implements InputMapperFactory {


    @Override
    public InputMapping constructInputMappingFromOM(OMElement omElement)
            throws EventReceiverConfigurationException {
        return WSO2EventInputMapperConfigurationBuilder.getInstance().fromOM(omElement);
    }

    @Override
    public OMElement constructOMFromInputMapping(
            InputMapping inputMapping, OMFactory factory) {
        return WSO2EventInputMapperConfigurationBuilder.getInstance().inputMappingToOM(inputMapping, factory);
    }

    @Override
    public InputMapper constructInputMapper(EventReceiverConfiguration eventReceiverConfiguration,
                                            StreamDefinition exportedStreamDefinition)
            throws EventReceiverConfigurationException {
        return new WSO2EventInputMapper(eventReceiverConfiguration, exportedStreamDefinition);
    }

}
