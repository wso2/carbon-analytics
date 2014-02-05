package org.wso2.carbon.event.stream.manager.core.internal.util;

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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.manager.core.internal.CarbonEventStreamService;
import org.wso2.carbon.event.stream.manager.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Iterator;

public class EventStreamConfigurationHelper {

    private static final Log log = LogFactory.getLog(EventStreamConfigurationHelper.class);

    private EventStreamConfigurationHelper() {
    }

    private static OMElement getEventDefinitionOMElement(File eventStreamConfigurationFile)
            throws EventStreamConfigurationException {
        String fileName = eventStreamConfigurationFile.getName();
        OMElement eventDefinitionElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(eventStreamConfigurationFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            eventDefinitionElement = builder.getDocumentElement();
            eventDefinitionElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " .xml file cannot be found in the path : " + fileName;
            log.error(errorMessage, e);
            throw new EventStreamConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + eventStreamConfigurationFile.getName() + " located in the path : " + fileName;
            log.error(errorMessage, e);
            throw new EventStreamConfigurationException(errorMessage, e);
        } catch (OMException e) {
            String errorMessage = "XML tags are not properly closed " + fileName;
            log.error(errorMessage, e);
            throw new EventStreamConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
        return eventDefinitionElement;
    }


    public static void loadEventStreamDefinitionFromConfigurationFile()
            throws EventStreamConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + EventStreamConstants.SM_CONF;

        File configFile = new File(path);

        if (!configFile.exists()) {
            log.info("The " + EventStreamConstants.SM_CONF + " can not found ");
            return;
        } else {

            OMElement eventStreamManagerConfig = getEventDefinitionOMElement(configFile);
            if (eventStreamManagerConfig != null) {
                if (!eventStreamManagerConfig.getQName().equals(new QName(EventStreamConstants.SM_CONF_NS,
                                                                          EventStreamConstants.SM_ELE_ROOT_ELEMENT))) {
                    throw new EventStreamConfigurationException("Invalid root element "
                                                                + eventStreamManagerConfig.getQName());
                }
                Iterator eventDefinitionIter = eventStreamManagerConfig.getChildrenWithName(
                        new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_STREAM_CONFIGURATION));
                for (; eventDefinitionIter.hasNext(); ) {
                    OMElement eventStreamDefinitionOMElement = (OMElement) eventDefinitionIter.next();
                    StreamDefinition streamDefinition = fromOM(eventStreamDefinitionOMElement);
                    if (streamDefinition != null) {
                        CarbonEventStreamService eventStreamService = EventStreamServiceValueHolder.getCarbonEventStreamService();
                        StreamDefinition existingDefinition;
                        existingDefinition = eventStreamService.getStreamDefinitionFromStore(streamDefinition.getName(), streamDefinition.getVersion(), MultitenantConstants.SUPER_TENANT_ID);
                        if (existingDefinition == null) {
                            if(EventStreamServiceValueHolder.getPassthroughReceiverConfigurator() != null){
                                eventStreamService.addEventStreamDefinitionToStore(streamDefinition);
                            }else {
                                EventStreamServiceValueHolder.getPendingStreamIdList().add(streamDefinition);
                            }
                        }
                    }
                }
            }

        }
    }

    private static StreamDefinition fromOM(OMElement eventStreamDefinitionOMElement)
            throws EventStreamConfigurationException {

        try {
            String streamName = eventStreamDefinitionOMElement.getAttributeValue(
                    new QName("", EventStreamConstants.SM_ATTR_NAME));

            String streamVersion = eventStreamDefinitionOMElement.getAttributeValue(
                    new QName("", EventStreamConstants.SM_ATTR_VERSION));


            StreamDefinition streamDefinition = new StreamDefinition(streamName, streamVersion);


            OMElement descriptionOMElement = eventStreamDefinitionOMElement.getFirstChildWithName(new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_STREAM_DESCRIPTION));
            if (descriptionOMElement != null) {
                String description = descriptionOMElement.getText();
                if (description != null && (!description.isEmpty())) {
                    streamDefinition.setDescription(description);
                }
            }

            OMElement nickNameOMElement = eventStreamDefinitionOMElement.getFirstChildWithName(new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_STREAM_NICKNAME));

            if (nickNameOMElement != null) {
                String nickName = nickNameOMElement.getText();
                if (nickName != null && (!nickName.isEmpty())) {
                    streamDefinition.setNickName(nickName);
                }
            }

            OMElement metaDataElement = eventStreamDefinitionOMElement.getFirstChildWithName(
                    new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_META_DATA));

            if (metaDataElement != null) {
                Iterator metaPropertyIterator = metaDataElement.getChildrenWithName(new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_PROPERTY));
                while (metaPropertyIterator.hasNext()) {
                    OMElement propertyOMElement = (OMElement) metaPropertyIterator.next();
                    String name = propertyOMElement.getAttributeValue(new QName(EventStreamConstants.SM_ATTR_NAME));
                    String type = propertyOMElement.getAttributeValue(new QName(EventStreamConstants.SM_ATTR_TYPE)).toLowerCase();
                    streamDefinition.addMetaData(name, EventStreamConstants.STRING_ATTRIBUTE_TYPE_MAP.get(type));
                }
            }

            OMElement correlationDataElement = eventStreamDefinitionOMElement.getFirstChildWithName(
                    new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_CORRELATION_DATA));

            if (correlationDataElement != null) {
                Iterator correlationPropertyIterator = correlationDataElement.getChildrenWithName(new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_PROPERTY));
                while (correlationPropertyIterator.hasNext()) {
                    OMElement propertyOMElement = (OMElement) correlationPropertyIterator.next();
                    String name = propertyOMElement.getAttributeValue(new QName(EventStreamConstants.SM_ATTR_NAME));
                    String type = propertyOMElement.getAttributeValue(new QName(EventStreamConstants.SM_ATTR_TYPE)).toLowerCase();
                    streamDefinition.addCorrelationData(name, EventStreamConstants.STRING_ATTRIBUTE_TYPE_MAP.get(type));
                }
            }

            OMElement payloadDataElement = eventStreamDefinitionOMElement.getFirstChildWithName(
                    new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_PAYLOAD_DATA));

            if (payloadDataElement != null) {
                Iterator payloadPropertyIterator = payloadDataElement.getChildrenWithName(new QName(EventStreamConstants.SM_CONF_NS, EventStreamConstants.SM_ELE_PROPERTY));
                while (payloadPropertyIterator.hasNext()) {
                    OMElement propertyOMElement = (OMElement) payloadPropertyIterator.next();
                    String name = propertyOMElement.getAttributeValue(new QName(EventStreamConstants.SM_ATTR_NAME));
                    String type = propertyOMElement.getAttributeValue(new QName(EventStreamConstants.SM_ATTR_TYPE)).toLowerCase();
                    streamDefinition.addPayloadData(name, EventStreamConstants.STRING_ATTRIBUTE_TYPE_MAP.get(type));
                }
            }

            return streamDefinition;

        } catch (MalformedStreamDefinitionException e) {
            throw new EventStreamConfigurationException(e);
        }
    }

}
