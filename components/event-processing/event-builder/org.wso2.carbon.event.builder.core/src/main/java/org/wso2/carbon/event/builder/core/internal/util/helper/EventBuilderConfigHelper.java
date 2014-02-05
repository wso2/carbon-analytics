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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.EventBuilderDeployer;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class EventBuilderConfigHelper {

    public static InputEventAdaptorMessageConfiguration getInputEventMessageConfiguration(
            String eventAdaptorTypeName) {
        MessageDto messageDto = EventBuilderServiceValueHolder.getInputEventAdaptorService().getEventMessageDto(eventAdaptorTypeName);
        InputEventAdaptorMessageConfiguration inputEventMessageConfiguration = null;
        if (messageDto != null && messageDto.getMessageInPropertyList() != null) {
            inputEventMessageConfiguration = new InputEventAdaptorMessageConfiguration();
            for (Property property : messageDto.getMessageInPropertyList()) {
                inputEventMessageConfiguration.addInputMessageProperty(property.getPropertyName(), property.getDefaultValue());
            }
        }

        return inputEventMessageConfiguration;
    }

    public static String getInputMappingType(OMElement eventBuilderOMElement) {
        OMElement mappingElement = eventBuilderOMElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_MAPPING));
        return mappingElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE));
    }

    public static EventBuilderDeployer getEventBuilderDeployer(
            AxisConfiguration axisConfiguration) {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfiguration.getConfigurator();
        return (EventBuilderDeployer) deploymentEngine.getDeployer(EventBuilderConstants.EB_CONFIG_DIRECTORY, EventBuilderConstants.EB_CONFIG_FILE_EXTENSION);
    }

    public static Attribute[] getAttributes(List<InputMappingAttribute> inputMappingAttributes) {
        List<Attribute> metaAttributes = new ArrayList<Attribute>();
        List<Attribute> correlationAttributes = new ArrayList<Attribute>();
        List<Attribute> payloadAttributes = new ArrayList<Attribute>();
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
            if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.META_DATA_PREFIX)) {
                metaAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            } else if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.CORRELATION_DATA_PREFIX)) {
                correlationAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            } else {
                payloadAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            }
        }
        Attribute[] outputAttributes = new Attribute[metaAttributes.size() + correlationAttributes.size() + payloadAttributes.size()];
        int attributeCount = 0;
        for (Attribute attribute : metaAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        for (Attribute attribute : correlationAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        for (Attribute attribute : payloadAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        return outputAttributes;
    }

    public static StreamDefinition deriveStreamDefinition(String streamName, String streamVersion, List<InputMappingAttribute> inputMappingAttributes) throws EventBuilderConfigurationException {
        StreamDefinition streamDefinition;
        try {
            streamDefinition = new StreamDefinition(streamName, streamVersion);
        } catch (MalformedStreamDefinitionException e) {
            throw new EventBuilderConfigurationException("Cannot create exported stream definition with name :" + streamName
                    + ", version : " + streamVersion);
        }
        List<Attribute> metaAttributes = new ArrayList<Attribute>();
        List<Attribute> correlationAttributes = new ArrayList<Attribute>();
        List<Attribute> payloadAttributes = new ArrayList<Attribute>();
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
            if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.META_DATA_PREFIX)) {
                metaAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            } else if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.CORRELATION_DATA_PREFIX)) {
                correlationAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            } else {
                payloadAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            }
        }
        streamDefinition.setMetaData(metaAttributes);
        streamDefinition.setCorrelationData(correlationAttributes);
        streamDefinition.setPayloadData(payloadAttributes);

        return streamDefinition;
    }
}
