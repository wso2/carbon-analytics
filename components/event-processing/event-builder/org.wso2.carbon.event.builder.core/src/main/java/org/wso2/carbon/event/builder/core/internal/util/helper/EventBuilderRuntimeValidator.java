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

package org.wso2.carbon.event.builder.core.internal.util.helper;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderStreamValidationException;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventBuilderRuntimeValidator {

    public static void validateExportedStream(EventBuilderConfiguration eventBuilderConfiguration, StreamDefinition exportedStreamDefinition, InputMapper inputMapper) {
        if (eventBuilderConfiguration != null && exportedStreamDefinition != null) {
            if (eventBuilderConfiguration.getInputMapping().isCustomMappingEnabled()) {
                String streamId = exportedStreamDefinition.getStreamId();
                if (inputMapper.getOutputAttributes() == null || inputMapper.getOutputAttributes().length == 0) {
                    throw new EventBuilderStreamValidationException("The input mapper is not exporting any output attributes for stream " + streamId);
                }
                List<Attribute> outputAttributes = new ArrayList<Attribute>(Arrays.asList(inputMapper.getOutputAttributes()));
                List<Attribute> metaAttributeList = exportedStreamDefinition.getMetaData();
                if (metaAttributeList != null) {
                    for (Attribute attribute : metaAttributeList) {
                        Attribute prependedAttribute = new Attribute(EventBuilderConstants.META_DATA_PREFIX + attribute.getName(), attribute.getType());
                        if (!outputAttributes.contains(prependedAttribute)) {
                            throw new EventBuilderStreamValidationException("The meta data attribute '" + attribute.getName()
                                    + "' in stream : '" + streamId + "' cannot be found under attributes exported by this event builder mapping");
                        }
                    }
                }
                List<Attribute> correlationAttributeList = exportedStreamDefinition.getCorrelationData();
                if (correlationAttributeList != null) {
                    for (Attribute attribute : correlationAttributeList) {
                        Attribute prependedAttribute = new Attribute(EventBuilderConstants.CORRELATION_DATA_PREFIX + attribute.getName(), attribute.getType());
                        if (!outputAttributes.contains(prependedAttribute)) {
                            throw new EventBuilderStreamValidationException("The correlation data attribute '" + attribute.getName()
                                    + "' in stream : '" + streamId + "' cannot be found under attributes exported by this event builder mapping");
                        }
                    }
                }
                List<Attribute> payloadAttributeList = exportedStreamDefinition.getPayloadData();
                if (payloadAttributeList != null) {
                    for (Attribute attribute : payloadAttributeList) {
                        if (!outputAttributes.contains(attribute)) {
                            throw new EventBuilderStreamValidationException("The payload data attribute '" + attribute.getName()
                                    + "' in stream : '" + streamId + "' cannot be found under attributes exported by this event builder mapping");
                        }
                    }
                }
            }
        }
    }
}
