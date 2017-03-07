/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.processor.manager.core.internal.util;


import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.manager.commons.utils.Utils;
import org.wso2.carbon.event.processor.manager.core.config.*;
import org.wso2.carbon.event.processor.manager.core.exception.ManagementConfigurationException;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ManagementModeConfigurationLoader {

    private static final Log log = LogFactory.getLog(ManagementModeConfigurationLoader.class);

    public static ManagementModeInfo loadManagementModeInfo()
            throws ManagementConfigurationException {
        ManagementModeInfo managementModeInfo = new ManagementModeInfo();
        OMElement omElement = ManagementModeConfigurationLoader.loadConfigXML();

        Iterator<OMElement> iterator = omElement.getChildrenWithName(new QName(ConfigurationConstants.MODE_ELEMENT));

        while (iterator.hasNext()) {
            OMElement processingMode = iterator.next();
            if (processingMode.getAttribute(new QName(ConfigurationConstants.PROCESSING_MODE_NAME_ATTRIBUTE)) == null) {
                throw new ManagementConfigurationException("Invalid Mode Element with no mode attribute '" +
                        ConfigurationConstants.PROCESSING_MODE_NAME_ATTRIBUTE + "' in file " +
                        ConfigurationConstants.CEP_MANAGEMENT_XML);
            }
            String attribute = processingMode
                    .getAttribute(new QName(ConfigurationConstants.PROCESSING_MODE_NAME_ATTRIBUTE)).getAttributeValue();

            if (processingMode.getAttribute(new QName(ConfigurationConstants.ENABLE_ATTRIBUTE)) == null) {
                throw new ManagementConfigurationException("Invalid Mode Element '" + attribute +
                        "' with no mode attribute '" + ConfigurationConstants.ENABLE_ATTRIBUTE +
                        "' in file " + ConfigurationConstants.CEP_MANAGEMENT_XML);
            }
            String enabled = processingMode
                    .getAttribute(new QName(ConfigurationConstants.ENABLE_ATTRIBUTE))
                    .getAttributeValue();

            if (enabled.equalsIgnoreCase("true")) {
                if (attribute.equalsIgnoreCase(ConfigurationConstants.PROCESSING_MODE_HA)) {
                    managementModeInfo.setMode(Mode.HA);
                    // Set HA Configurations.
                    managementModeInfo.setHaConfiguration(getHAConfiguration(processingMode));
                    // Set Persist Configurations.
                    OMElement nodeConfig = processingMode
                            .getFirstChildWithName(new QName(ConfigurationConstants.SN_PERSISTENCE_ELEMENT));

                    if (nodeConfig != null) {
                        boolean isPersistenceEnabled = nodeType(ConfigurationConstants.ENABLE_ATTRIBUTE, nodeConfig);
                        if(isPersistenceEnabled){
                            managementModeInfo.setPersistenceConfiguration(getPersistConfigurations(nodeConfig,isPersistenceEnabled));
                            log.info("CEP started in Persistence enabled HA mode");
                        }
                    } else {
                        log.info("CEP started in HA mode");
                    }
                } else if (attribute.equalsIgnoreCase(ConfigurationConstants.PROCESSING_MODE_DISTRIBUTED)) {
                    managementModeInfo.setMode(Mode.Distributed);
                    log.info("CEP started in Distributed mode");
                    managementModeInfo.setDistributedConfiguration(getDistributedConfiguration(processingMode));
                } else {
                    managementModeInfo.setMode(Mode.SingleNode);
                    log.info("CEP started in Single node mode");
                }
                return managementModeInfo;
            }
        }
        managementModeInfo.setMode(Mode.SingleNode);
        log.info("CEP started in Single node mode");
        return managementModeInfo;
    }

    private static OMElement loadConfigXML() throws ManagementConfigurationException {

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
            throw new ManagementConfigurationException(ConfigurationConstants.CEP_MANAGEMENT_XML + "cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            throw new ManagementConfigurationException("Invalid XML for " + ConfigurationConstants.CEP_MANAGEMENT_XML + " located in the path : " + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Can not shutdown the input stream", e);
            }
        }
    }

    private static boolean nodeType(String elementName, OMElement element)
            throws ManagementConfigurationException {
        OMAttribute attribute = element.getAttribute(new QName(elementName));
        if (attribute != null) {
            return attribute.getAttributeValue().equalsIgnoreCase("True");
        } else {
            throw new ManagementConfigurationException("Invalid XML. No attribute with name " + ConfigurationConstants.PROCESSING_MODE_NAME_ATTRIBUTE + " found in file " + ConfigurationConstants.CEP_MANAGEMENT_XML);
        }
    }

    private static PersistenceConfiguration getPersistConfigurations(OMElement persistence, boolean isPersistenceEnabled) {
        OMElement classElement = persistence.getFirstChildWithName(new QName(ConfigurationConstants.SN_PERSISTENCE_PERSIST_CLASS_ELEMENT));
        Map propertiesMap = new HashMap();
        String className;
        if (classElement == null) {
            log.warn("Invalid XML. Using default persistence store :" + ConfigurationConstants.SN_DEFAULT_PERSISTENCE_STORE);
            className = ConfigurationConstants.SN_DEFAULT_PERSISTENCE_STORE;
        } else {
            className = classElement.getAttribute(new QName(ConfigurationConstants.SN_PERSISTENCE_CLASS_ATTRIBUTE)).getAttributeValue();
            Iterator propertyElements = classElement.getChildrenWithName(new QName(ConfigurationConstants.SN_PERSISTENCE_PERSIST_CLASS_PROPERTY));
            while (propertyElements.hasNext()) {
                OMElement propertyElement = (OMElement) propertyElements.next();
                String key = propertyElement.getAttribute(new QName(ConfigurationConstants.SN_PERSISTENCE_PERSIST_CLASS_PROPERTY_KEY)).getAttributeValue();
                propertiesMap.put(key, propertyElement.getText());
            }
        }
        OMElement timeElement = persistence.getFirstChildWithName(new QName(ConfigurationConstants.SN_PERSISTENCE_INTERVAL_ELEMENT));

        long timeInterval;
        if (timeElement == null) {
            timeInterval = ConfigurationConstants.SN_DEFAULT_PERSISTENCE_INTERVAL;
        } else {
            try {
                timeInterval = Long.parseLong(persistence.getFirstChildWithName(new QName(ConfigurationConstants.SN_PERSISTENCE_INTERVAL_ELEMENT)).getText());
            } catch (NumberFormatException ex) {
                log.warn("Invalid persistenceInterval. Using default persistenceInterval");
                timeInterval = ConfigurationConstants.SN_DEFAULT_PERSISTENCE_INTERVAL;
            }
        }

        int poolSize;
        OMElement omElement = persistence.getFirstChildWithName(new QName(ConfigurationConstants.SN_PERSISTENCE_THREAD_POOL_SIZE));
        if (omElement == null) {
            poolSize = ConfigurationConstants.SN_DEFAULT_PERSISTENCE_THREAD_POOL_SIZE;
        } else {
            try {
                poolSize = Integer.parseInt(persistence.getFirstChildWithName(
                        new QName(ConfigurationConstants.SN_PERSISTENCE_THREAD_POOL_SIZE)).getText());
            } catch (NumberFormatException ex) {
                log.warn("Invalid persisterSchedulerPoolSize. Using default persisterSchedulerPoolSize");
                poolSize = ConfigurationConstants.SN_DEFAULT_PERSISTENCE_THREAD_POOL_SIZE;
            }
        }
        return new PersistenceConfiguration(className, timeInterval, poolSize, propertiesMap, isPersistenceEnabled);

    }

    private static DistributedConfiguration getDistributedConfiguration(
            OMElement processingElement) {

        DistributedConfiguration stormDeploymentConfig = new DistributedConfiguration();

        // Reading storm managers
        OMElement management = processingElement.getFirstChildWithName(new QName(ConfigurationConstants.MANAGEMENT_ELEMENT));
        OMElement managers = management.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_MANAGERS_ELEMENT));
        Iterator<OMElement> iterator = managers.getChildElements();
        if (!iterator.hasNext()) {
            try {
                String hostName = Utils.findAddress("localhost");
                int port = 8904;
                stormDeploymentConfig.addManager(hostName, port);
                log.info("No storm managers are provided. Hence automatically electing " + hostName + ":" + port + " node as " +
                        "manager");
            } catch (SocketException e) {
                log.error("Error while automatically populating storm managers. Please check the event-processor.xml" +
                        " at CARBON_HOME/repository/conf", e);
                return null;
            }
        }
        while (iterator.hasNext()) {
            OMElement manager = iterator.next();
            String hostName = manager.getFirstChildWithName(new QName(ConfigurationConstants.HOST_NAME_ELEMENT)).getText();
            int port = Integer.parseInt(manager.getFirstChildWithName(new QName(ConfigurationConstants.PORT_ELEMENT)).getText());
            stormDeploymentConfig.addManager(hostName, port);
        }

        if (management.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_HEARTBEAT_INTERVAL_ELEMENT)) != null) {
            stormDeploymentConfig.setManagementHeartbeatInterval(Integer.parseInt(management.getFirstChildWithName(new QName
                    (ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_HEARTBEAT_INTERVAL_ELEMENT)).getText()));
        } else {
            log.info("No heartbeat interval provided. Hence using default heartbeat interval "
                    + stormDeploymentConfig.getManagementHeartbeatInterval());
        }
        if (management.getFirstChildWithName(new QName(ConfigurationConstants.RECONNECTION_INTERVAL_ELEMENT)) != null) {
            stormDeploymentConfig.setManagementReconnectInterval(Integer.parseInt(management.getFirstChildWithName
                    (new QName(ConfigurationConstants.RECONNECTION_INTERVAL_ELEMENT)).getText()));
        } else {
            log.info("No reconnection interval provided. Hence using default reconnection interval "
                    + stormDeploymentConfig.getManagementReconnectInterval());
        }
        if (management.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TOPOLOGY_RESUBMIT_INTERVAL_ELEMENT)) != null) {
            stormDeploymentConfig.setTopologySubmitRetryInterval(Integer.parseInt(management.getFirstChildWithName(new QName
                    (ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TOPOLOGY_RESUBMIT_INTERVAL_ELEMENT)).getText()));
        } else {
            log.info("No topology resubmit interval provided. Hence using default topology resubmit interval "
                    + stormDeploymentConfig.getTopologySubmitRetryInterval());
        }

        //Reading transport
        OMElement transport = processingElement.getFirstChildWithName(new QName(ConfigurationConstants.TRANSPORT_ELEMENT));
        OMElement portRange = transport.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_PORT_RANGE_ELEMENT));
        if (portRange != null) {
            stormDeploymentConfig.setTransportMaxPort(Integer.parseInt(portRange.getFirstChildWithName(new QName("max")).getText()));
            stormDeploymentConfig.setTransportMinPort(Integer.parseInt(portRange.getFirstChildWithName(new QName("min")).getText()));
        } else {
            log.info("No port information provided. Hence using default port range " +
                    stormDeploymentConfig.getTransportMinPort() + " - " + stormDeploymentConfig.getTransportMaxPort());
        }
        if (transport.getFirstChildWithName(new QName(ConfigurationConstants.RECONNECTION_INTERVAL_ELEMENT)) != null) {
            stormDeploymentConfig.setTransportReconnectInterval(Integer.parseInt(transport.getFirstChildWithName(
                    new QName(ConfigurationConstants.RECONNECTION_INTERVAL_ELEMENT)).getText()));
        } else {
            log.info("No transport reconnection interval provided. Hence using default reconnection interval "
                    + stormDeploymentConfig.getTransportReconnectInterval());
        }
        if (transport.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_CEP_RECEIVER_QUEUE_SIZE)) != null) {
            int queueSize = Integer.parseInt(transport.getFirstChildWithName(
                    new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_CEP_RECEIVER_QUEUE_SIZE)).getText());

            if (isPowerOfTwo(queueSize)) {
                stormDeploymentConfig.setCepReceiverOutputQueueSize(queueSize);
            } else {
                // Disruptor queue size only allows powers of two
                throw new IllegalArgumentException(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_CEP_RECEIVER_QUEUE_SIZE + " must be a power of two.");
            }

        } else {
            log.info("No CEP receiver output queue size specified. Hence using default queue size " + stormDeploymentConfig.getCepReceiverOutputQueueSize());
        }
        if (transport.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_STORM_PUBLISHER_QUEUE_SIZE)) != null) {
            int queueSize = Integer.parseInt(transport.getFirstChildWithName(
                    new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_STORM_PUBLISHER_QUEUE_SIZE)).getText());

            if (isPowerOfTwo(queueSize)) {
                stormDeploymentConfig.setStormPublisherOutputQueueSize(queueSize);
            } else {
                // Disruptor queue size only allows powers of two
                throw new IllegalArgumentException(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_CEP_RECEIVER_QUEUE_SIZE + " must be a power of two.");
            }

        } else {
            log.info("No storm publisher output queue size specified. Hence using default queue size " + stormDeploymentConfig.getStormPublisherOutputQueueSize());
        }
        if (transport.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TCP_PUBLISHER_BUFFER_SIZE)) != null) {
            int bufferSize = Integer.parseInt(transport.getFirstChildWithName(
                    new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TCP_PUBLISHER_BUFFER_SIZE)).getText());
            stormDeploymentConfig.setTransportPublisherTcpSendBufferSize(bufferSize);
        } else {
            log.info("No TCP publisher buffer size not specified. Hence using default buffer size " + stormDeploymentConfig.getTransportPublisherTcpSendBufferSize());
        }
        if (transport.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TCP_PUBLISHER_CHAR_SET)) != null) {
            String tcpEventPublisherCharSet = transport.getFirstChildWithName(
                    new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TCP_PUBLISHER_CHAR_SET)).getText();
            stormDeploymentConfig.setTransportPublisherCharSet(tcpEventPublisherCharSet);
        } else {
            log.info("TCP event publisher Char-Set not set. Hence using default value " + stormDeploymentConfig.getTransportPublisherCharSet());
        }
        if (transport.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_STORM_SPOUT_BUFFER_SIZE)) != null) {
            int bufferSize = Integer.parseInt(transport.getFirstChildWithName(
                    new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_STORM_SPOUT_BUFFER_SIZE)).getText());
            stormDeploymentConfig.setStormSpoutBufferSize(bufferSize);
        } else {
            log.info("No Storm Spout buffer size not specified. Hence using default buffer size " + stormDeploymentConfig.getStormSpoutBufferSize());
        }
        if (transport.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_CON_STATUS_CHECK_INTERVAL)) != null) {
            int connectionStatusCheckInterval = Integer.parseInt(transport.getFirstChildWithName(
                    new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_CON_STATUS_CHECK_INTERVAL)).getText());
            stormDeploymentConfig.setTransportPublisherConnectionStatusCheckInterval(connectionStatusCheckInterval);
        } else {
            log.info("No transport connection status check interval specified. Hence using default interval "
                    + stormDeploymentConfig.getTransportPublisherConnectionStatusCheckInterval() + "ms");
        }

        //Reading node info
        OMElement node = processingElement.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_ELEMENT));
        if (node != null) {
            OMElement worker = node.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_WORKER_ELEMENT));
            if ("true".equalsIgnoreCase(worker.getAttributeValue(new QName(ConfigurationConstants.ENABLE_ATTRIBUTE)))) {
                stormDeploymentConfig.setWorkerNode(true);
            }

            OMElement manager = node.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_MANAGER_ELEMENT));
            if ("true".equalsIgnoreCase(manager.getAttributeValue(new QName(ConfigurationConstants.ENABLE_ATTRIBUTE)))) {
                stormDeploymentConfig.setManagerNode(true);
                String hostName = manager.getFirstChildWithName(new QName(ConfigurationConstants.HOST_NAME_ELEMENT)).getText();
                int port = Integer.parseInt(manager.getFirstChildWithName(new QName(ConfigurationConstants.PORT_ELEMENT)).getText());
                stormDeploymentConfig.setLocalManagerConfig(hostName, port);
            }

            OMElement presenter = node.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_PRESENTER_ELEMENT));
            if ("true".equalsIgnoreCase(presenter.getAttributeValue(new QName(ConfigurationConstants.ENABLE_ATTRIBUTE)))) {
                stormDeploymentConfig.setPresenterNode(true);
                String hostName = presenter.getFirstChildWithName(new QName(ConfigurationConstants.HOST_NAME_ELEMENT)).getText();
                int port = Integer.parseInt(presenter.getFirstChildWithName(new QName(ConfigurationConstants.PORT_ELEMENT)).getText());
                stormDeploymentConfig.setLocalPresenterConfig(hostName, port);
            }
        } else {
            log.info("No node type configurations provided. Hence using default node type configurations");
        }

        OMElement distributedUI = processingElement.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_DISTRIBUTED_UI_URL_ELEMENT));
        if (distributedUI != null) {
            String url = distributedUI.getText();
            stormDeploymentConfig.setDistributedUIUrl(url);
        }

        OMElement memberUpdateCheckInterval = processingElement.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_MEMBER_UPDATE_CHECK_INTERVAL));
        if (memberUpdateCheckInterval != null) {
            int interval = Integer.parseInt(memberUpdateCheckInterval.getText());
            stormDeploymentConfig.setMemberUpdateCheckInterval(interval);
        } else {
            log.info("No member update check interval specified. Hence using default interval " + stormDeploymentConfig.getMemberUpdateCheckInterval());
        }

        //Get Jar name
        OMElement jar = processingElement.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_STORM_JAR_ELEMENT));
        stormDeploymentConfig.setJar(jar.getText());

        OMElement presentation = processingElement.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_PRESENTATION_ELEMENT));
        if (presentation != null) {

            if (presentation.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_PRESENTER_QUEUE_SIZE)) != null) {
                int queueSize = Integer.parseInt(presentation.getFirstChildWithName(
                        new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_PRESENTER_QUEUE_SIZE)).getText());

                if (isPowerOfTwo(queueSize)) {
                    stormDeploymentConfig.setPresentationOutputQueueSize(queueSize);
                } else {
                    // Disruptor queue size only allows powers of two
                    throw new IllegalArgumentException(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_PRESENTER_QUEUE_SIZE + " must be a power of two.");
                }
            } else {
                log.info("No presentation output queue size provided. Hence using default queue size " + stormDeploymentConfig.getPresentationOutputQueueSize());
            }

            if (presentation.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TCP_PUBLISHER_BUFFER_SIZE)) != null) {
                int bufferSize = Integer.parseInt(transport.getFirstChildWithName(
                        new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TCP_PUBLISHER_BUFFER_SIZE)).getText());
                stormDeploymentConfig.setPresentationPublisherTcpSendBufferSize(bufferSize);
            } else {
                log.info("No TCP publisher buffer size not specified for presenter. Hence using default buffer size " + stormDeploymentConfig.getPresentationPublisherTcpSendBufferSize());
            }

            if (presentation.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TCP_PUBLISHER_CHAR_SET)) != null) {
                String tcpEventPublisherCharSet = transport.getFirstChildWithName(
                        new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_TCP_PUBLISHER_CHAR_SET)).getText();
                stormDeploymentConfig.setPresentationPublisherCharSet(tcpEventPublisherCharSet);
            } else {
                log.info("TCP event publisher Char-Set not set for presenter. Hence using default value " + stormDeploymentConfig.getPresentationPublisherCharSet());
            }

            if (presentation.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_CON_STATUS_CHECK_INTERVAL)) != null) {
                int connectionStatusCheckInterval = Integer.parseInt(transport.getFirstChildWithName(
                        new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_CON_STATUS_CHECK_INTERVAL)).getText());
                stormDeploymentConfig.setPresentationPublisherConnectionStatusCheckInterval(connectionStatusCheckInterval);
            } else {
                log.info("No transport connection status check interval specified for presenter. Hence using default interval "
                        + stormDeploymentConfig.getPresentationPublisherConnectionStatusCheckInterval() + "ms");
            }
        } else {
            log.info("No presentation configurations provided. Hence using default configurations");
        }

        //Reading Status Monitor Info
        OMElement statusMonitor = processingElement.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_STATUS_MONITOR_ELEMENT));
        if (statusMonitor != null) {
            OMElement lockTimeoutElement = statusMonitor.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_STATUS_MONITOR_LOCK_TIMEOUT));
            if (lockTimeoutElement != null) {
                int lockTimeout = Integer.parseInt(lockTimeoutElement.getText());
                stormDeploymentConfig.setStatusLockTimeout(lockTimeout);
            } else {
                log.info("No lockTimeout value specified in Status Monitor configurations. Hence using default lock timeout value: "
                        + stormDeploymentConfig.getStatusLockTimeout() + " seconds.");
            }
            OMElement updateRateElement = statusMonitor.getFirstChildWithName(new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_STATUS_MONITOR_UPDATE_RATE));
            if (updateRateElement != null) {
                int updateRate = Integer.parseInt(updateRateElement.getText());
                stormDeploymentConfig.setStatusUpdateInterval(updateRate);
            } else {
                log.info("No updateRate value specified in Status Monitor configurations. Hence using default update rate: "
                        + stormDeploymentConfig.getStatusUpdateInterval() + " milliseconds.");
            }
        } else {
            log.info("No Status Monitor configurations provided. Hence using default Status Monitor configurations. Lock timeout: " +
                    stormDeploymentConfig.getStatusLockTimeout() + " seconds, Update rate: " + stormDeploymentConfig.getStatusUpdateInterval() + " milliseconds.");
        }


        return stormDeploymentConfig;
    }

    private static HAConfiguration getHAConfiguration(OMElement processing) {

        HAConfiguration haConfiguration = new HAConfiguration();

        OMElement nodeType = processing.getFirstChildWithName(new QName(ConfigurationConstants.HA_NODE_TYPE));
        String isWorkerEnabled = nodeType.getFirstChildWithName(new QName(ConfigurationConstants.HA_WORKER))
                .getAttribute(new QName(ConfigurationConstants.ENABLE_ATTRIBUTE)).getAttributeValue();
        String isPresenterEnabled = nodeType.getFirstChildWithName(new QName(ConfigurationConstants.HA_PRESENTER))
                .getAttribute(new QName(ConfigurationConstants.ENABLE_ATTRIBUTE)).getAttributeValue();
        haConfiguration.setCheckMemberUpdateInterval(Integer.parseInt(readOMElementValue(processing, ConfigurationConstants
                .HA_NODE_CONFIG_CHECK_MEMBER_UPDATE_INTERVAL)));

        OMElement management = processing.getFirstChildWithName(new QName(ConfigurationConstants.MANAGEMENT_ELEMENT));
        haConfiguration.setManagement(readHostName(management),
                readPort(management, ConfigurationConstants.HA_DEFAULT_MANAGEMENT_PORT));
        haConfiguration.setManagementTryStateChangeInterval(Integer.parseInt(readOMElementValue(management, ConfigurationConstants
                .HA_NODE_CONFIG_MANAGEMENT_TRY_STATE_CHANGE_INTERVAL)));
        haConfiguration.setManagementStateSyncRetryInterval(Integer.parseInt(readOMElementValue(management, ConfigurationConstants
                .HA_NODE_CONFIG_MANAGEMENT_STATE_RETRY_INTERVAL)));

        if (isWorkerEnabled.equalsIgnoreCase("true")) {
            haConfiguration.setWorkerNode(true);
            OMElement eventSync = processing.getFirstChildWithName(new QName(ConfigurationConstants.EVENT_SYNC_ELEMENT));
            haConfiguration.setEventSyncConfig(readHostName(eventSync),
                    readPort(eventSync, ConfigurationConstants.HA_DEFAULT_TRANSPORT_PORT));
            haConfiguration.setEventSyncPublisherTcpSendBufferSize(Integer.parseInt(readOMElementValue(eventSync, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_TCP_SEND_BUFFER_SIZE)));
            haConfiguration.setEventSyncPublisherCharSet(readOMElementValue(eventSync, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_CHAR_SET));
            haConfiguration.setEventSyncPublisherBufferSize(Integer.parseInt(readOMElementValue(eventSync, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_BUFFER_SIZE)));
            haConfiguration.setEventSyncPublisherConnectionStatusCheckInterval(Integer.parseInt(readOMElementValue(eventSync, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_CONNECTION_STATUS_CHECK_INTERVAL)));
            haConfiguration.setEventSyncReceiverQueueSize(Integer.parseInt(readOMElementValue(eventSync, ConfigurationConstants
                    .HA_NODE_CONFIG_RECEIVER_QUEUE_SIZE)));
            haConfiguration.setEventSyncReceiverMaxQueueSizeInMb(Integer.parseInt(readOMElementValue(eventSync, ConfigurationConstants
                    .HA_NODE_CONFIG_RECEIVER_QUEUE_MAX_SIZE_MB)));
            haConfiguration.setEventSyncPublisherQueueSize(Integer.parseInt(readOMElementValue(eventSync, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_QUEUE_SIZE)));
            haConfiguration.setEventSyncPublisherMaxQueueSizeInMb(Integer.parseInt(readOMElementValue(eventSync, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_QUEUE_MAX_SIZE_MB)));
        }
        if (isPresenterEnabled.equalsIgnoreCase("true")) {
            haConfiguration.setPresenterNode(true);
            OMElement presentation = processing.getFirstChildWithName(new QName(ConfigurationConstants.PRESENTER_ELEMENT));
            haConfiguration.setLocalPresenterConfig(readHostName(presentation),
                    readPort(presentation, ConfigurationConstants.HA_DEFAULT_PRESENTER_PORT));
            haConfiguration.setPresentationPublisherTcpSendBufferSize(Integer.parseInt(readOMElementValue(presentation, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_TCP_SEND_BUFFER_SIZE)));
            haConfiguration.setPresentationPublisherCharSet(readOMElementValue(presentation, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_CHAR_SET));
            haConfiguration.setPresentationPublisherBufferSize(Integer.parseInt(readOMElementValue(presentation, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_BUFFER_SIZE)));
            haConfiguration.setPresentationPublisherConnectionStatusCheckInterval(Integer.parseInt(readOMElementValue(presentation, ConfigurationConstants
                    .HA_NODE_CONFIG_PUBLISHER_CONNECTION_STATUS_CHECK_INTERVAL)));
        }

        return haConfiguration;
    }

    private static String readHostName(OMElement transport) {
        OMElement receiverHostName = transport.getFirstChildWithName(
                new QName(ConfigurationConstants.HOST_NAME_ELEMENT));
        String hostName = null;
        if (receiverHostName != null && receiverHostName.getText() != null
                && !receiverHostName.getText().trim().equals("")) {
            hostName = receiverHostName.getText();
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
        if (receiverPort != null) {
            try {
                return (Integer.parseInt(receiverPort.getText()));
            } catch (NumberFormatException e) {
                log.warn("Invalid port for HA configuration. Using default port " + defaultPort, e);
            }
        } else {
            log.warn("Missing port for HA configuration. Using default port" + defaultPort);
        }
        return defaultPort;
    }

    private static int readReconnectionInterval(OMElement transport) {
        OMElement reconnectionInterval = transport.getFirstChildWithName(
                new QName(ConfigurationConstants.RECONNECTION_INTERVAL_ELEMENT));
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

    public static boolean isPowerOfTwo(int value) {
        while (((value % 2) == 0) && value > 1) /* While x is even and > 1 */ {
            value /= 2;
        }
        return (value == 1);
    }

    public static String readOMElementValue(OMElement parentElement, String elementName) {
        OMElement element = parentElement.getFirstChildWithName(new QName(elementName));
        String elementValue = null;
        if (!element.getText().trim().isEmpty()) {
            elementValue = element.getText();
        }
        return elementValue;
    }
}
