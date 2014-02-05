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

package org.wso2.carbon.event.builder.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.config.InputMapperFactory;
import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

public class XMLInputMapperFactory implements InputMapperFactory {


    @Override
    public InputMapping constructInputMappingFromOM(OMElement omElement) throws EventBuilderConfigurationException {
        return XMLInputMappingConfigBuilder.getInstance().fromOM(omElement);
    }

    @Override
    public OMElement constructOMFromInputMapping(
            InputMapping inputMapping, OMFactory factory) {
        return XMLInputMappingConfigBuilder.getInstance().inputMappingToOM(inputMapping, factory);
    }

    @Override
    public InputMapper constructInputMapper(EventBuilderConfiguration eventBuilderConfiguration, StreamDefinition inputStreamDefinition) throws EventBuilderConfigurationException {
        return new XMLInputMapper(eventBuilderConfiguration);
    }
}
