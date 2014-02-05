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

package org.wso2.carbon.event.output.adaptor.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.output.adaptor.core.internal.ds.OutputEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * This is a EventAdaptor type. these interface let users to publish subscribe messages according to
 * some type. this type can either be local, jms or ws
 */
public abstract class AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(AbstractOutputEventAdaptor.class);
    private static final String OUTPUT_EVENT_ADAPTOR = "Output Event Adaptor";
    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);
    private OutputEventAdaptorDto outputEventAdaptorDto;
    private MessageDto messageDto;

    protected AbstractOutputEventAdaptor() {

        init();

        this.outputEventAdaptorDto = new OutputEventAdaptorDto();
        this.messageDto = new MessageDto();
        this.outputEventAdaptorDto.setEventAdaptorTypeName(this.getName());
        this.outputEventAdaptorDto.setSupportedMessageTypes(this.getSupportedOutputMessageTypes());

        this.messageDto.setAdaptorName(this.getName());

        outputEventAdaptorDto.setAdaptorPropertyList(((this)).getOutputAdaptorProperties());
        messageDto.setMessageOutPropertyList(((this)).getOutputMessageProperties());

    }

    public OutputEventAdaptorDto getOutputEventAdaptorDto() {
        return outputEventAdaptorDto;
    }

    public MessageDto getMessageDto() {
        return messageDto;
    }

    /**
     * returns the name of the output event adaptor type
     *
     * @return event adaptor type name
     */
    protected abstract String getName();

    /**
     * To get the information regarding supported message types event adaptor
     *
     * @return List of supported output message types
     */
    protected abstract List<String> getSupportedOutputMessageTypes();

    /**
     * any initialization can be done in this method
     */
    protected abstract void init();

    /**
     * the information regarding the adaptor related properties of a specific event adaptor type
     *
     * @return List of properties related to output event adaptor
     */
    protected abstract List<Property> getOutputAdaptorProperties();

    /**
     * to get message related output configuration details
     *
     * @return list of output message configuration properties
     */
    protected abstract List<Property> getOutputMessageProperties();

    /**
     * publish a message to a given connection.
     *
     * @param outputEventAdaptorMessageConfiguration
     *                 - message configuration event adaptor to publish messages
     * @param message  - message to send
     * @param outputEventAdaptorConfiguration
     *
     * @param tenantId
     */
    public void publishCall(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        if (outputEventAdaptorConfiguration.isEnableTracing()) {
            trace.info("TenantId=" + String.valueOf(tenantId) + " : " + OUTPUT_EVENT_ADAPTOR + " : " + outputEventAdaptorConfiguration.getName() + ", sent " + System.getProperty("line.separator") + (message instanceof Object[] ? Arrays.deepToString((Object[]) message) : message));
        }
        if (outputEventAdaptorConfiguration.isEnableStatistics()) {
            EventStatisticsMonitor statisticsMonitor = OutputEventAdaptorServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, OUTPUT_EVENT_ADAPTOR, outputEventAdaptorConfiguration.getName(), null);
            statisticsMonitor.incrementResponse();
        }

        publish(outputEventAdaptorMessageConfiguration, message, outputEventAdaptorConfiguration, tenantId);
    }

    /**
     * publish a message to a given connection.
     *
     * @param outputEventAdaptorMessageConfiguration
     *                 - message configuration event adaptor to publish messages
     * @param message  - message to send
     * @param outputEventAdaptorConfiguration
     *
     * @param tenantId
     */
    protected abstract void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId);


    /**
     * publish test message to check the connection with the event adaptor configuration.
     *
     * @param outputEventAdaptorConfiguration
     *                 - event adaptor configuration to be used
     * @param tenantId
     */
    public abstract void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId);


}
