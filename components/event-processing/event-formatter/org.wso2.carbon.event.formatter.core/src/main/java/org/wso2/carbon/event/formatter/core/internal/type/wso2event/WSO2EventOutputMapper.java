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

package org.wso2.carbon.event.formatter.core.internal.type.wso2event;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.OutputMapper;
import org.wso2.carbon.event.formatter.core.internal.config.EventOutputProperty;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.*;

public class WSO2EventOutputMapper implements OutputMapper {

    WSO2EventOutputMapping wso2EventOutputMapping;
    EventFormatterConfiguration eventFormatterConfiguration = null;
    Map<String, Integer> propertyPositionMap = null;
    private StreamDefinition outputStreamDefinition = null;
    private int noOfMetaData = 0;
    private int noOfCorrelationData = 0;
    private int noOfPayloadData = 0;

    public WSO2EventOutputMapper(EventFormatterConfiguration eventFormatterConfiguration,
                                 Map<String, Integer> propertyPositionMap,
                                 int tenantId) throws
            EventFormatterConfigurationException {
        this.eventFormatterConfiguration = eventFormatterConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        validateStreamDefinitionWithOutputProperties();
        String outputStreamName = eventFormatterConfiguration.getToPropertyConfiguration().getOutputEventAdaptorMessageConfiguration().getOutputMessageProperties().get(EventFormatterConstants.EF_ELE_PROPERTY_STREAM_NAME);
        String outputStreamVersion = eventFormatterConfiguration.getToPropertyConfiguration().getOutputEventAdaptorMessageConfiguration().getOutputMessageProperties().get(EventFormatterConstants.EF_ATTR_VERSION);

        try {
            String inputStreamName = eventFormatterConfiguration.getFromStreamName();
            String inputStreamVersion = eventFormatterConfiguration.getFromStreamVersion();

            wso2EventOutputMapping = (WSO2EventOutputMapping) eventFormatterConfiguration.getOutputMapping();

            if (!wso2EventOutputMapping.isCustomMappingEnabled()) {
                StreamDefinition inputStreamDefinition = EventFormatterServiceValueHolder.getEventStreamService().getStreamDefinitionFromStore(inputStreamName, inputStreamVersion, tenantId);
                outputStreamDefinition = EventFormatterServiceValueHolder.getEventStreamService().getStreamDefinitionFromStore(outputStreamName, outputStreamVersion, tenantId);

                if (outputStreamDefinition != null && (!inputStreamDefinition.equals(outputStreamDefinition))) {
                    throw new EventFormatterConfigurationException("input and output stream definitions are different in pass-through event formatter");
                } else {
                    outputStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
                    outputStreamDefinition.setMetaData(inputStreamDefinition.getMetaData());
                    outputStreamDefinition.setCorrelationData(inputStreamDefinition.getCorrelationData());
                    outputStreamDefinition.setPayloadData(inputStreamDefinition.getPayloadData());

                    noOfMetaData = outputStreamDefinition.getMetaData() != null ? outputStreamDefinition.getMetaData().size() : 0;
                    noOfCorrelationData = outputStreamDefinition.getCorrelationData() != null ? outputStreamDefinition.getCorrelationData().size() : 0;
                    noOfPayloadData = outputStreamDefinition.getPayloadData() != null ? outputStreamDefinition.getPayloadData().size() : 0;

                }
            } else {

                if (wso2EventOutputMapping != null) {
                    outputStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
                    addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration(), "meta");
                    addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration(), "correlation");
                    addAttributeToStreamDefinition(outputStreamDefinition, wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration(), "payload");
                }
            }

        } catch (EventStreamConfigurationException e) {
            throw new EventFormatterConfigurationException("Error while retrieving stream definition from registry", e);
        } catch (MalformedStreamDefinitionException e) {
            throw new EventFormatterConfigurationException("Error while creating output stream definition : " + outputStreamName + ":" + outputStreamVersion, e);
        }

    }

    private void validateStreamDefinitionWithOutputProperties()
            throws EventFormatterConfigurationException {
        WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) eventFormatterConfiguration.getOutputMapping();
        List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
        List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
        List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

        Iterator<EventOutputProperty> metaWSO2EventOutputPropertyConfigurationIterator = metaWSO2EventOutputPropertyConfiguration.iterator();
        for (; metaWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = metaWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventFormatterConfigurationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ");
            }
        }


        Iterator<EventOutputProperty> correlationWSO2EventOutputPropertyConfigurationIterator = correlationWSO2EventOutputPropertyConfiguration.iterator();
        for (; correlationWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = correlationWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventFormatterConfigurationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ");
            }
        }

        Iterator<EventOutputProperty> payloadWSO2EventOutputPropertyConfigurationIterator = payloadWSO2EventOutputPropertyConfiguration.iterator();
        for (; payloadWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = payloadWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventFormatterConfigurationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ");
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
    public Object convertToMappedInputEvent(Object obj)
            throws EventFormatterConfigurationException {
        Object[] inputObjArray = (Object[]) obj;
        Event eventObject = new Event();
        if (inputObjArray.length > 0) {

            List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
            List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
            List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

            if (metaWSO2EventOutputPropertyConfiguration.size() != 0) {
                List<Object> metaData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : metaWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    metaData.add(inputObjArray[position]);
                }
                eventObject.setMetaData(metaData.toArray());
            }

            if (correlationWSO2EventOutputPropertyConfiguration.size() != 0) {
                List<Object> correlationData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : correlationWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    correlationData.add(inputObjArray[position]);
                }
                eventObject.setCorrelationData(correlationData.toArray());
            }

            if (payloadWSO2EventOutputPropertyConfiguration.size() != 0) {
                List<Object> payloadData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : payloadWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    payloadData.add(inputObjArray[position]);
                }
                eventObject.setPayloadData(payloadData.toArray());
            }
        }

        return new Object[]{eventObject, outputStreamDefinition};
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventFormatterConfigurationException {

        Object[] inputObjArray = (Object[]) obj;
        Event eventObject = new Event();

        if (noOfMetaData > 0) {
            List<Object> metaData = new ArrayList<Object>();
            metaData.addAll(Arrays.asList(inputObjArray).subList(0, noOfMetaData));
            eventObject.setMetaData(metaData.toArray());
        }

        if (noOfCorrelationData > 0) {
            List<Object> correlationData = new ArrayList<Object>();
            correlationData.addAll(Arrays.asList(inputObjArray).subList(noOfMetaData, noOfMetaData + noOfCorrelationData));
            eventObject.setCorrelationData(correlationData.toArray());
        }

        if (noOfPayloadData > 0) {
            List<Object> payloadData = new ArrayList<Object>();
            payloadData.addAll(Arrays.asList(inputObjArray).subList(noOfCorrelationData + noOfMetaData, noOfPayloadData + noOfCorrelationData + noOfMetaData));
            eventObject.setPayloadData(payloadData.toArray());
        }

        return new Object[]{eventObject, outputStreamDefinition};
    }

}
