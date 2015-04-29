/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.manager.core.config;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.manager.commons.utils.Utils;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.SocketException;
import java.util.Iterator;


public class ManagementModeInfo {
    private Mode mode;
    private PersistenceConfiguration persistenceConfiguration;
    private DistributedConfiguration distributedConfiguration;
    private HAConfiguration haConfiguration;

    private static ManagementModeInfo instance;


    private static final Log log = LogFactory.getLog(ManagementModeInfo.class);

    public synchronized static ManagementModeInfo getInstance() throws ManagementConfigurationException {
        if (instance == null) {
            instance = new ManagementModeInfo();
        }
        return instance;
    }

    private ManagementModeInfo() throws ManagementConfigurationException {
        OMElement omElement = loadConfigXML();
        String attribute;
        OMElement processing = omElement.getFirstChildWithName(
                new QName(ConfigurationConstants.PROCESSING_ELEMENT));
        if (processing == null) {
            throw new ManagementConfigurationException("Invalid XML. No element with name "  + ConfigurationConstants.PROCESSING_ELEMENT + " found in file CEP_MANAGEMENT_XML");
        }
        if (processing.getAttribute(new QName(ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE)) == null) {
            throw new ManagementConfigurationException("Invalid XML. No attribute with name "   + ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE + " found in file "+ConfigurationConstants.CEP_MANAGEMENT_XML);
        }
        attribute = processing.getAttribute(new QName(ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE))
                .getAttributeValue();
        if (attribute.equalsIgnoreCase(ConfigurationConstants.PROCESSING_MODE_HA)) {
            mode = Mode.HA;
            haConfiguration = haConfig(processing);
        } else if (attribute.equalsIgnoreCase(ConfigurationConstants.PROCESSING_MODE_SN)) {
            mode = Mode.SingleNode;
            OMElement nodeConfig = processing.getFirstChildWithName(
                    new QName(ConfigurationConstants.SN_PERSISTENCE_ELEMENT));
            if (nodeConfig == null) {
                throw new ManagementConfigurationException("Invalid XML. No element with name " + ConfigurationConstants.SN_PERSISTENCE_ELEMENT + " found in file "+ ConfigurationConstants.CEP_MANAGEMENT_XML);
            } else if(nodeType(ConfigurationConstants.ENABLE_ATTRIBUTE,nodeConfig)) {
                persistenceConfiguration = getPersistConfigurations(nodeConfig);
            }
        } else if (attribute.equalsIgnoreCase(ConfigurationConstants.PROCESSING_MODE_DISTRIBUTED)) {
            mode = Mode.Distributed;
            distributedConfiguration = getDistributedConfiguration(processing);
        }
    }

    private boolean nodeType(String elementName, OMElement element) throws ManagementConfigurationException {
            OMAttribute attribute = element.getAttribute(new QName(elementName));
            if (attribute != null) {
                return attribute.getAttributeValue().equalsIgnoreCase("True");
            } else {
                throw new ManagementConfigurationException("Invalid XML. No attribute with name " + ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE + " found in file "+ConfigurationConstants.CEP_MANAGEMENT_XML);
            }
    }

    private OMElement loadConfigXML() throws ManagementConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + ConfigurationConstants.CEP_MANAGEMENT_XML;

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = ConfigurationConstants.CEP_MANAGEMENT_XML + "cannot be found in the path : " + path;
            throw new ManagementConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + ConfigurationConstants.CEP_MANAGEMENT_XML  + " located in the path : " + path;
            throw new ManagementConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not shutdown the input stream";
                log.error(errorMessage, e);
            }
        }
    }

    private  PersistenceConfiguration getPersistConfigurations(OMElement persistence) {
       if (persistence == null) {
            log.warn("Invalid XML. Using default persistence store :"
                    + ConfigurationConstants.SN_DEFAULT_PERSISTENCE_STORE);
            return  new PersistenceConfiguration(ConfigurationConstants.SN_DEFAULT_PERSISTENCE_STORE,ConfigurationConstants.SN_DEFAULT_PERSISTENCE_INTERVAL,ConfigurationConstants.SN_DEFAULT_PERSISTENCE_THREAD_POOL_SIZE);
        } else {
           String className = persistence.getFirstChildWithName(
                   new QName(ConfigurationConstants.SN_PERSISTENCE_PERSIST_CLASS_ELEMENT)).getAttribute(new QName(ConfigurationConstants.SN_PERSISTENCE_CLASS_ATTRIBUTE)).getAttributeValue();
           String timeIntervalText = persistence.getFirstChildWithName(
                   new QName(ConfigurationConstants.SN_PERSISTENCE_INTERVAL_ELEMENT)).getText();
           String poolSizeText = persistence.getFirstChildWithName(
                   new QName(ConfigurationConstants.SN_PERSISTENCE_THREAD_POOL_SIZE)).getText();
           return new PersistenceConfiguration(className,Long.parseLong(timeIntervalText), Integer.parseInt(poolSizeText));
       }
    }

    private static DistributedConfiguration getDistributedConfiguration(OMElement processingElement) {

        DistributedConfiguration stormDeploymentConfig = new DistributedConfiguration();

        // Reading storm managers
        OMElement management = processingElement.getFirstChildWithName(new QName("management"));
        OMElement managers = management.getFirstChildWithName(new QName("managers"));
        Iterator<OMElement> iterator = managers.getChildElements();
        if (!iterator.hasNext()) {
            try {
                String hostName = Utils.findAddress("localhost");
                int port = 8904;
                stormDeploymentConfig.addManager(hostName, port);
                log.info("No storm managers are provided. Hence automatically electing " + hostName + ":" + port + " node as " +
                        "manager");
            } catch (SocketException e) {
                log.error("Error while automatically populating storm managers. Please check the event-processing.xml" +
                        " at CARBON_HOME/repository/conf", e);
                return null;
            }
        }
        while (iterator.hasNext()) {
            OMElement manager = iterator.next();
            String hostName = manager.getFirstChildWithName(new QName("hostName")).getText();
            int port = Integer.parseInt(manager.getFirstChildWithName(new QName("port")).getText());
            stormDeploymentConfig.addManager(hostName, port);
        }

        if (management.getFirstChildWithName(new QName("heartbeatInterval")) != null) {
            stormDeploymentConfig.setHeartbeatInterval(Integer.parseInt(management.getFirstChildWithName(new QName
                    ("heartbeatInterval")).getText()));
        } else {
            log.info("No heartbeat interval provided. Hence using default heartbeat interval");
        }
        if (management.getFirstChildWithName(new QName("reconnectionInterval")) != null){
            stormDeploymentConfig.setManagementReconnectInterval(Integer.parseInt(management.getFirstChildWithName
                    (new QName("reconnectionInterval")).getText()));
        } else {
            log.info("No reconnection interval provided. Hence using default reconnection interval");
        }
        if (management.getFirstChildWithName(new QName("topologyResubmitInterval")) != null) {
            stormDeploymentConfig.setTopologySubmitRetryInterval(Integer.parseInt(management.getFirstChildWithName(new QName
                    ("topologyResubmitInterval")).getText()));
        } else {
            log.info("No topology resubmit interval provided. Hence using default topology resubmit interval");
        }

        //Reading transport
        OMElement transport = processingElement.getFirstChildWithName(new QName("transport"));
        OMElement portRange = transport.getFirstChildWithName(new QName("portRange"));
        if(portRange != null) {
            stormDeploymentConfig.setTransportMaxPort(Integer.parseInt(portRange.getFirstChildWithName(new QName("max")).getText()));
            stormDeploymentConfig.setTransportMinPort(Integer.parseInt(portRange.getFirstChildWithName(new QName("min")).getText()));
        } else {
            log.info("No port information provided. Hence using default port settings");
        }
        if(transport.getFirstChildWithName(new QName("reconnectionInterval"))!=null) {
            stormDeploymentConfig.setTransportReconnectInterval(Integer.parseInt(transport.getFirstChildWithName(new QName("reconnectionInterval")).getText()));
        }else{
            log.info("No transport reconnection interval provided. Hence using default topology resubmit interval");
        }


        //Reading node info
        OMElement node = processingElement.getFirstChildWithName(new QName("nodeType"));
        if (node != null) {
            OMElement receiver = node.getFirstChildWithName(new QName("receiver"));
            if ("true".equalsIgnoreCase(receiver.getAttributeValue(new QName("enable")))) {
                stormDeploymentConfig.setReceiverNode(true);
            }

            OMElement publisher = node.getFirstChildWithName(new QName("publisher"));
            if ("true".equalsIgnoreCase(publisher.getAttributeValue(new QName("enable")))) {
                stormDeploymentConfig.setPublisherNode(true);
            }

            OMElement manager = node.getFirstChildWithName(new QName("manager"));
            if ("true".equalsIgnoreCase(manager.getAttributeValue(new QName("enable")))) {
                stormDeploymentConfig.setManagerNode(true);
                String hostName = manager.getFirstChildWithName(new QName("hostName")).getText();
                int port = Integer.parseInt(manager.getFirstChildWithName(new QName("port")).getText());
                stormDeploymentConfig.setLocalManagerConfig(hostName, port);
            }
        } else {
            log.info("No node type configurations provided. Hence using default node type configurations");
        }

        OMElement defaultParallelism = processingElement.getFirstChildWithName(new QName("defaultParallelism"));
        if(defaultParallelism != null){
            int receiver = Integer.parseInt(defaultParallelism.getFirstChildWithName(new QName("receiver")).getText());
            int publisher = Integer.parseInt(defaultParallelism.getFirstChildWithName(new QName("publisher")).getText());
            stormDeploymentConfig.setReceiverSpoutParallelism(receiver);
            stormDeploymentConfig.setPublisherBoltParallelism(publisher);
        } else {
            log.info("No parallelism configurations provided. Hence using default parallelism configurations. Event " +
                    "Receiver Spout = 1. Event Publisher Bolt = 1.");
        }

        OMElement distributedUI = processingElement.getFirstChildWithName(new QName("distributedUIUrl"));
        if(distributedUI != null){
            String url = distributedUI.getText();
            stormDeploymentConfig.setDistributedUIUrl(url);
        }

        //Get Jar name
        OMElement jar = processingElement.getFirstChildWithName(new QName("stormJar"));
        stormDeploymentConfig.setJar(jar.getText());

        return stormDeploymentConfig;
    }

    private  HAConfiguration haConfig(OMElement processing) {
        HAConfiguration haConfiguration = new HAConfiguration();
        OMElement transport = processing.getFirstChildWithName(
                new QName(ConfigurationConstants.HA_TRANSPORT_ELEMENT));
        haConfiguration.setTransport(readHostName(transport),
                readPort(transport, ConfigurationConstants.HA_DEFAULT_TRANSPORT_PORT),
                readReconnectionInterval(transport));

        OMElement management = processing.getFirstChildWithName(
                new QName(ConfigurationConstants.HA_MANAGEMENT_ELEMENT));
        haConfiguration.setManagement(readHostName(management),
                readPort(management, ConfigurationConstants.HA_DEFAULT_MANAGEMENT_PORT));

        return haConfiguration;
    }

    private String readHostName(OMElement transport) {
        OMElement receiverHostName = transport.getFirstChildWithName(
                new QName(ConfigurationConstants.RECEIVER_HOST_NAME));
        String hostName = null;
        if (receiverHostName != null && receiverHostName.getText() != null
                && !receiverHostName.getText().trim().equals("")) {
            hostName = receiverHostName.getText();
        }
        if(hostName == null){
            hostName =  "localhost";
        }
        if (hostName == null) {
            try {
                hostName = Utils.findAddress("localhost");
            } catch (SocketException e) {
                log.error("Unable to find the address of localhost.", e);
            }
        }
        return hostName;
    }

    private static int readPort(OMElement transport, int defaultPort) {
        OMElement receiverPort = transport.getFirstChildWithName(
                new QName(ConfigurationConstants.PORT_ELEMENT));
        int portOffset = haReadPortOffset();
        int port;
        if (receiverPort != null) {
            try {
                return (Integer.parseInt(receiverPort.getText()) + portOffset);
            } catch (NumberFormatException e) {
                port = defaultPort + portOffset;
                log.warn("Invalid port for HA configuration. Using default port " + port, e);
            }
        } else {
            port = defaultPort + portOffset;
            log.warn("Missing port for HA configuration. Using default port" + port);
        }
        return port;
    }

    private static int readReconnectionInterval(OMElement transport) {
        OMElement reconnectionInterval = transport.getFirstChildWithName(
                new QName(ConfigurationConstants.HA_RECONNECTION_INTERVAL_ELEMENT));
        int interval;
        if (reconnectionInterval != null && reconnectionInterval.getText() != null
                && !reconnectionInterval.getText().trim().equals("")) {
            try {
                return Integer.parseInt(reconnectionInterval.getText().trim());
            } catch (NumberFormatException e) {
                interval = ConfigurationConstants.HA_DEFAULT_RECONNECTION_INTERVAL;
                log.warn("Invalid reconnection interval for HA configuration. Using default: " + interval, e);
            }
        } else {
            interval = ConfigurationConstants.HA_DEFAULT_RECONNECTION_INTERVAL;
            log.warn("Missing reconnection interval for HA configuration. Using default: " + interval);
        }
        return interval;
    }


    public static int haReadPortOffset() {
        return org.wso2.carbon.utils.CarbonUtils.
                getPortFromServerConfig(ConfigurationConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }

    public Mode getMode() {
        return mode;
    }

    public PersistenceConfiguration getPersistenceConfiguration() {
        return persistenceConfiguration;
    }

    public DistributedConfiguration getDistributedConfiguration() {
        return distributedConfiguration;
    }

    public HAConfiguration getHaConfiguration() {
        return haConfiguration;
    }

}
