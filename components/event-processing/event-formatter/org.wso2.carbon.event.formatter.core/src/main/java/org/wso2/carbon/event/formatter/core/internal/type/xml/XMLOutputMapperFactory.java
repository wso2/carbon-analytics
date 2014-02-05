/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.event.formatter.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.OutputMapperFactory;
import org.wso2.carbon.event.formatter.core.config.OutputMapping;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.OutputMapper;

import java.util.Map;

public class XMLOutputMapperFactory implements OutputMapperFactory {


    @Override
    public OutputMapping constructOutputMapping(OMElement omElement) throws EventFormatterConfigurationException{
        return XMLMapperConfigurationBuilder.fromOM(omElement);
    }

    @Override
    public OMElement constructOutputMappingOM(
            OutputMapping outputMapping, OMFactory factory) throws EventFormatterConfigurationException {
        return XMLMapperConfigurationBuilder.outputMappingToOM(outputMapping, factory);
    }

    @Override
    public OutputMapper constructOutputMapper(
            EventFormatterConfiguration eventFormatterConfiguration,
            Map<String, Integer> propertyPositionMap, int tenantId) throws EventFormatterConfigurationException {
        return new XMLOutputOutputMapper(eventFormatterConfiguration, propertyPositionMap, tenantId);
    }
}
