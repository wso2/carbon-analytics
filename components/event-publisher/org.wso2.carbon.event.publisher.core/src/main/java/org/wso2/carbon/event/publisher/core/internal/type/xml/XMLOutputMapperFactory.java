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
package org.wso2.carbon.event.publisher.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.OutputMapperFactory;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;

import java.util.Map;

public class XMLOutputMapperFactory implements OutputMapperFactory {


    @Override
    public OutputMapping constructOutputMapping(OMElement omElement)
            throws EventPublisherConfigurationException {
        return XMLOutputMapperConfigurationBuilder.fromOM(omElement);
    }

    @Override
    public OMElement constructOMFromOutputMapping(
            OutputMapping outputMapping, OMFactory factory)
            throws EventPublisherConfigurationException {
        return XMLOutputMapperConfigurationBuilder.outputMappingToOM(outputMapping, factory);
    }

    @Override
    public OutputMapper constructOutputMapper(
            EventPublisherConfiguration eventPublisherConfiguration,
            Map<String, Integer> propertyPositionMap, int tenantId,
            StreamDefinition streamDefinition) throws EventPublisherConfigurationException {
        return new XMLOutputMapper(eventPublisherConfiguration, propertyPositionMap, tenantId, streamDefinition);
    }
}
