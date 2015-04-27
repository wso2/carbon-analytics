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
package org.wso2.carbon.event.publisher.core.internal.type.wso2event;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventOutputProperty;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.mapping.WSO2EventOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;

import java.util.*;

public class WSO2EventOutputMapper implements OutputMapper {

    private WSO2EventOutputMapping wso2EventOutputMapping;
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    private Map<String, Integer> propertyPositionMap = null;
    private final int tenantId;
    private final StreamDefinition inputStreamDefinition;
    //private StreamDefinition outputStreamDefinition = null;
    private int noOfMetaData = 0;
    private int noOfCorrelationData = 0;
    private int noOfPayloadData = 0;

    /*public WSO2EventOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
                                 Map<String, Integer> propertyPositionMap,
                                 int tenantId, StreamDefinition inputStreamDefinition) throws
            EventPublisherConfigurationException {
        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        this.tenantId = tenantId;
        this.inputStreamDefinition = inputStreamDefinition;

        String outputStreamName = eventPublisherConfiguration.getToAdapterConfiguration().getStaticProperties().get(EventPublisherConstants.EF_ELE_PROPERTY_STREAM_NAME);
        String outputStreamVersion = eventPublisherConfiguration.getToAdapterConfiguration().getStaticProperties().get(EventPublisherConstants.EF_ATTR_VERSION);

        try {
            wso2EventOutputMapping = (WSO2EventOutputMapping) eventPublisherConfiguration.getOutputMapping();
            if (!wso2EventOutputMapping.isCustomMappingEnabled()) {
                outputStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
                outputStreamDefinition.setMetaData(inputStreamDefinition.getMetaData());
                outputStreamDefinition.setCorrelationData(inputStreamDefinition.getCorrelationData());
                outputStreamDefinition.setPayloadData(inputStreamDefinition.getPayloadData());

                noOfMetaData = outputStreamDefinition.getMetaData() != null ? outputStreamDefinition.getMetaData().size() : 0;
                noOfCorrelationData = outputStreamDefinition.getCorrelationData() != null ? outputStreamDefinition.getCorrelationData().size() : 0;
                noOfPayloadData = outputStreamDefinition.getPayloadData() != null ? outputStreamDefinition.getPayloadData().size() : 0;
            } else {
                validateStreamDefinitionWithOutputProperties();
                outputStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
                addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration(), "meta");
                addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration(), "correlation");
                addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration(), "payload");
            }

        } catch (MalformedStreamDefinitionException e) {
            throw new EventPublisherConfigurationException("Error while creating output stream definition : " + outputStreamName + ":" + outputStreamVersion, e);
        }

    }*/

    public WSO2EventOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
            Map<String, Integer> propertyPositionMap,
            int tenantId, StreamDefinition inputStreamDefinition) throws
            EventPublisherConfigurationException {
        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        this.tenantId = tenantId;
        this.inputStreamDefinition = inputStreamDefinition;

        //try {
        wso2EventOutputMapping = (WSO2EventOutputMapping) eventPublisherConfiguration.getOutputMapping();
        if (!wso2EventOutputMapping.isCustomMappingEnabled()) {
            //outputStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
            //outputStreamDefinition.setMetaData(inputStreamDefinition.getMetaData());
            //outputStreamDefinition.setCorrelationData(inputStreamDefinition.getCorrelationData());
            //outputStreamDefinition.setPayloadData(inputStreamDefinition.getPayloadData());

            noOfMetaData = inputStreamDefinition.getMetaData() != null ? inputStreamDefinition.getMetaData().size() : 0;
            noOfCorrelationData = inputStreamDefinition.getCorrelationData() != null ? inputStreamDefinition.getCorrelationData().size() : 0;
            noOfPayloadData = inputStreamDefinition.getPayloadData() != null ? inputStreamDefinition.getPayloadData().size() : 0;
        } else {
            validateStreamDefinitionWithOutputProperties();
            //outputStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
            //addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration(), "meta");
            //addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration(), "correlation");
            //addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration(), "payload");
        }

        /*} catch (MalformedStreamDefinitionException e) {
            throw new EventPublisherConfigurationException("Error while creating output stream definition : " + outputStreamName + ":" + outputStreamVersion, e);
        }*/

    }

    private void validateStreamDefinitionWithOutputProperties()
            throws EventPublisherConfigurationException {
        WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) eventPublisherConfiguration.getOutputMapping();
        List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
        List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
        List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

        Iterator<EventOutputProperty> metaWSO2EventOutputPropertyConfigurationIterator = metaWSO2EventOutputPropertyConfiguration.iterator();
        for (; metaWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = metaWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventPublisherStreamValidationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ",inputStreamDefinition.getStreamId());
            }
        }


        Iterator<EventOutputProperty> correlationWSO2EventOutputPropertyConfigurationIterator = correlationWSO2EventOutputPropertyConfiguration.iterator();
        for (; correlationWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = correlationWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventPublisherStreamValidationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ",inputStreamDefinition.getStreamId());
            }
        }

        Iterator<EventOutputProperty> payloadWSO2EventOutputPropertyConfigurationIterator = payloadWSO2EventOutputPropertyConfiguration.iterator();
        for (; payloadWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = payloadWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventPublisherStreamValidationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ", inputStreamDefinition.getStreamId());
            }
        }


    }

    private void addAttributeToStreamDefinition(StreamDefinition streamDefinition,
            List<EventOutputProperty> wso2EventOutputPropertyList,
            String propertyType) {

        if (propertyType.equals("meta")) {
            for (EventOutputProperty wso2EventOutputProperty : wso2EventOutputPropertyList) {
                streamDefinition.addMetaData(wso2EventOutputProperty.getName(), wso2EventOutputProperty.getType());
            }
        } else if (propertyType.equals("correlation")) {
            for (EventOutputProperty wso2EventOutputProperty : wso2EventOutputPropertyList) {
                streamDefinition.addCorrelationData(wso2EventOutputProperty.getName(), wso2EventOutputProperty.getType());
            }
        } else if (propertyType.equals("payload")) {
            for (EventOutputProperty wso2EventOutputProperty : wso2EventOutputPropertyList) {
                streamDefinition.addPayloadData(wso2EventOutputProperty.getName(), wso2EventOutputProperty.getType());
            }
        }


    }

    @Override
    public Object convertToMappedInputEvent(Object[] eventData)
            throws EventPublisherConfigurationException {
        Event eventObject = new Event();
        if (eventData.length > 0) {

            List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
            List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
            List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

            if (metaWSO2EventOutputPropertyConfiguration.size() != 0) {
                List<Object> metaData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : metaWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    metaData.add(eventData[position]);
                }
                eventObject.setMetaData(metaData.toArray());
            }

            if (correlationWSO2EventOutputPropertyConfiguration.size() != 0) {
                List<Object> correlationData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : correlationWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    correlationData.add(eventData[position]);
                }
                eventObject.setCorrelationData(correlationData.toArray());
            }

            if (payloadWSO2EventOutputPropertyConfiguration.size() != 0) {
                List<Object> payloadData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : payloadWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    payloadData.add(eventData[position]);
                }
                eventObject.setPayloadData(payloadData.toArray());
            }
        }

        return eventObject;
    }

    @Override
    public Object convertToTypedInputEvent(Object[] eventData) throws EventPublisherConfigurationException {

        Event eventObject = new Event();

        if (noOfMetaData > 0) {
            List<Object> metaData = new ArrayList<Object>();
            metaData.addAll(Arrays.asList(eventData).subList(0, noOfMetaData));
            eventObject.setMetaData(metaData.toArray());
        }

        if (noOfCorrelationData > 0) {
            List<Object> correlationData = new ArrayList<Object>();
            correlationData.addAll(Arrays.asList(eventData).subList(noOfMetaData, noOfMetaData + noOfCorrelationData));
            eventObject.setCorrelationData(correlationData.toArray());
        }

        if (noOfPayloadData > 0) {
            List<Object> payloadData = new ArrayList<Object>();
            payloadData.addAll(Arrays.asList(eventData).subList(noOfCorrelationData + noOfMetaData, noOfPayloadData + noOfCorrelationData + noOfMetaData));
            eventObject.setPayloadData(payloadData.toArray());
        }

        return eventObject;
    }

}
