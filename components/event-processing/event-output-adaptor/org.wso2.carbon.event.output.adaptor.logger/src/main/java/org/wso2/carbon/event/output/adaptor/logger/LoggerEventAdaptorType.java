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

package org.wso2.carbon.event.output.adaptor.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.logger.internal.util.LoggerEventAdaptorConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class LoggerEventAdaptorType extends AbstractOutputEventAdaptor {

    private static LoggerEventAdaptorType loggerEventAdaptorType = new LoggerEventAdaptorType();
    private ResourceBundle resourceBundle;
    private static final Log log = LogFactory.getLog(LoggerEventAdaptorType.class);

    private LoggerEventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.TEXT);
        supportOutputMessageTypes.add(MessageType.XML);
        supportOutputMessageTypes.add(MessageType.JSON);
        return supportOutputMessageTypes;
    }

    /**
     * @return logger adaptor instance
     */
    public static LoggerEventAdaptorType getInstance() {

        return loggerEventAdaptorType;
    }

    /**
     * @return name of the logger adaptor
     */
    @Override
    protected String getName() {
        return LoggerEventAdaptorConstants.ADAPTOR_TYPE_LOGGER;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.logger.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {
       return null;
    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set stream definition
        Property streamDefinitionProperty = new Property(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID));
        streamDefinitionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);

        return propertyList;
    }

    /**
     * @param outputEventAdaptorMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     * @param outputEventAdaptorConfiguration
     * @param tenantId
     */
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        if (message instanceof Object[]) {
            log.info("Unique ID : " +outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID)+ " , Event : "+ Arrays.deepToString((Object[]) message));
        } else {
            log.info("Unique ID : " +outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID)+ " , Event : " + message);
        }
    }


    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        //no test needed

    }


}
