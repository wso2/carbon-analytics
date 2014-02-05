/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal;

import me.prettyprint.hector.api.Cluster;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughSenderConfigurator;
import org.wso2.carbon.event.processor.api.receive.EventReceiver;
import org.wso2.carbon.event.processor.api.receive.exception.EventReceiverException;
import org.wso2.carbon.event.processor.api.send.EventSender;
import org.wso2.carbon.event.processor.api.send.exception.EventProducerException;
import org.wso2.carbon.event.processor.core.*;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.exception.ServiceDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.listener.ExternalStreamConsumer;
import org.wso2.carbon.event.processor.core.internal.listener.ExternalStreamListener;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiOutputStreamListener;
import org.wso2.carbon.event.processor.core.internal.persistence.CassandraPersistenceStore;
import org.wso2.carbon.event.processor.core.internal.stream.EventConsumer;
import org.wso2.carbon.event.processor.core.internal.stream.EventJunction;
import org.wso2.carbon.event.processor.core.internal.stream.EventProducer;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConfigurationFilesystemInvoker;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.processor.core.internal.util.helper.CassandraConnectionValidator;
import org.wso2.carbon.event.processor.core.internal.util.helper.EventProcessorConfigurationHelper;
import org.wso2.carbon.event.processor.core.internal.util.helper.SiddhiExtensionLoader;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.compiler.exception.SiddhiPraserException;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventProcessorService implements EventProcessorService {
    private static final Log log = LogFactory.getLog(CarbonEventProcessorService.class);
    // deployed query plans
    private Map<Integer, Map<String, ExecutionPlan>> tenantSpecificExecutionPlans;
    // not distinguishing between deployed vs failed here.
    private Map<Integer, List<ExecutionPlanConfigurationFile>> tenantSpecificExecutionPlanFiles;
    private Map<Integer, Map<String, EventJunction>> tenantSpecificEventJunctions;


    public CarbonEventProcessorService() {
        tenantSpecificExecutionPlans = new ConcurrentHashMap<Integer, Map<String, ExecutionPlan>>();
        tenantSpecificExecutionPlanFiles = new ConcurrentHashMap<Integer, List<ExecutionPlanConfigurationFile>>();
        tenantSpecificEventJunctions = new ConcurrentHashMap<Integer, Map<String, EventJunction>>();
    }

    private static void populateAttributes(
            org.wso2.siddhi.query.api.definition.StreamDefinition streamDefinition,
            List<Attribute> attributes, String prefix) {
        if (attributes != null) {
            for (Attribute attribute : attributes) {
                org.wso2.siddhi.query.api.definition.Attribute siddhiAttribute = EventProcessorUtil.convertToSiddhiAttribute(attribute, prefix);
                streamDefinition.attribute(siddhiAttribute.getName(), siddhiAttribute.getType());
            }
        }
    }

    @Override
    public void deployExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            AxisConfiguration axisConfiguration) throws
            ExecutionPlanDependencyValidationException,
            ExecutionPlanConfigurationException {

        String executionPlanName = executionPlanConfiguration.getName();

        OMElement omElement = EventProcessorConfigurationHelper.toOM(executionPlanConfiguration);
        EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(omElement);

        File directory = new File(axisConfiguration.getRepository().getPath());
        if (!directory.exists()) {
            if (directory.mkdir()) {
                throw new ExecutionPlanConfigurationException("Cannot create directory to add tenant specific execution plan : " + executionPlanName);
            }
        }
        directory = new File(directory.getAbsolutePath() + File.separator + EventProcessorConstants.EP_ELE_DIRECTORY);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new ExecutionPlanConfigurationException("Cannot create directory " + EventProcessorConstants.EP_ELE_DIRECTORY + " to add tenant specific  execution plan :" + executionPlanName);
            }
        }
        validateToRemoveInactiveExecutionPlanConfiguration(executionPlanName, axisConfiguration);
        EventProcessorConfigurationFilesystemInvoker.save(omElement, executionPlanName, executionPlanName + EventProcessorConstants.XML_EXTENSION, axisConfiguration);


    }

    @Override
    public void undeployInactiveExecutionPlanConfiguration(String filename,
                                                           AxisConfiguration axisConfiguration)
            throws
            ExecutionPlanConfigurationException {

        EventProcessorConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
    }

    @Override
    public void undeployActiveExecutionPlanConfiguration(String name,
                                                         AxisConfiguration axisConfiguration) throws
            ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventProcessorConfigurationFilesystemInvoker.delete(getExecutionPlanConfigurationFileByPlanName(name, tenantId).getFileName(), axisConfiguration);
    }

    public void editActiveExecutionPlanConfiguration(String executionPlanConfiguration,
                                                     String executionPlanName,
                                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(executionPlanConfiguration);
            EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(omElement);
            ExecutionPlanConfiguration executionPlanConfigurationObject = EventProcessorConfigurationHelper.fromOM(omElement);
            if (!(executionPlanConfigurationObject.getName().equals(executionPlanName))) {
                if (!(checkExecutionPlanValidity(executionPlanConfigurationObject.getName(), tenantId))) {
                    throw new ExecutionPlanConfigurationException(executionPlanConfigurationObject.getName() + " already registered as an execution in this tenant");
                }
            }
            if (executionPlanName != null && executionPlanName.length() > 0) {
                String fileName;
                ExecutionPlanConfigurationFile file = getExecutionPlanConfigurationFileByPlanName(executionPlanName, tenantId);
                if (file == null) {
                    fileName = executionPlanName + EventProcessorConstants.EP_CONFIG_FILE_EXTENSION_WITH_DOT;
                } else {
                    fileName = file.getFileName();
                }
                EventProcessorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
                EventProcessorConfigurationFilesystemInvoker.save(executionPlanConfiguration, executionPlanName, fileName, axisConfiguration);
            } else {
                throw new ExecutionPlanConfigurationException("Invalid configuration provided, No execution plan name.");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new ExecutionPlanConfigurationException("Not a valid xml object, ", e);
        }
    }

    public void editInactiveExecutionPlanConfiguration(String executionPlanConfiguration,
                                                       String filename,
                                                       AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(executionPlanConfiguration);
            EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(omElement);
            ExecutionPlanConfiguration config = EventProcessorConfigurationHelper.fromOM(omElement);
            EventProcessorConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
            EventProcessorConfigurationFilesystemInvoker.save(executionPlanConfiguration, config.getName(), filename, axisConfiguration);
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new ExecutionPlanConfigurationException("Not a valid xml object ", e);
        }
    }

    public void addExecutionPlanConfiguration(ExecutionPlanConfiguration executionPlanConfiguration,
                                              AxisConfiguration axisConfiguration)
            throws ExecutionPlanDependencyValidationException, ExecutionPlanConfigurationException,
            ServiceDependencyValidationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, ExecutionPlan> tenantExecutionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (tenantExecutionPlans == null) {
            tenantExecutionPlans = new ConcurrentHashMap<String, ExecutionPlan>();
            tenantSpecificExecutionPlans.put(tenantId, tenantExecutionPlans);
        } else if (tenantExecutionPlans.get(executionPlanConfiguration.getName()) != null) {
            // if an execution plan with the same name already exists, we are not going to override it with this plan.
            throw new ExecutionPlanConfigurationException("Execution plan with the same name already exists. Please remove it and retry.");
        }

        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap == null) {
            eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
            tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
        }

        // This iteration exists only as a check. Actual usage of imported stream configs is further down
        for (StreamConfiguration importedStreamConfig : executionPlanConfiguration.getImportedStreams()) {
            EventJunction eventJunction = eventJunctionMap.get(importedStreamConfig.getStreamId());
            if (eventJunction == null) {
                log.info("Execution plan deployment held back and in inactive state: " + executionPlanConfiguration.getName()
                        + ". Event receiver not found for stream ID : " + importedStreamConfig.getStreamId());
                throw new ExecutionPlanDependencyValidationException(importedStreamConfig.getStreamId(), "Event receiver not found for stream ID : " + importedStreamConfig.getStreamId());
            }
        }


        SiddhiConfiguration siddhiConfig = getSiddhiConfigurationFor(executionPlanConfiguration, tenantId);
        SiddhiManager siddhiManager = getSiddhiManagerFor(executionPlanConfiguration, siddhiConfig);

        Map<String, InputHandler> inputHandlerMap = new ConcurrentHashMap<String, InputHandler>(executionPlanConfiguration.getImportedStreams().size());
        for (StreamConfiguration importedStreamConfiguration : executionPlanConfiguration.getImportedStreams()) {
            org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition = new org.wso2.siddhi.query.api.definition.StreamDefinition();
            siddhiStreamDefinition.name(importedStreamConfiguration.getSiddhiStreamName());
            EventJunction eventJunction = eventJunctionMap.get(importedStreamConfiguration.getStreamId());
            StreamDefinition streamDefinition = eventJunction.getStreamDefinition();

            populateAttributes(siddhiStreamDefinition, streamDefinition.getMetaData(), EventProcessorConstants.META + EventProcessorConstants.ATTRIBUTE_SEPARATOR);
            populateAttributes(siddhiStreamDefinition, streamDefinition.getCorrelationData(), EventProcessorConstants.CORRELATION + EventProcessorConstants.ATTRIBUTE_SEPARATOR);
            populateAttributes(siddhiStreamDefinition, streamDefinition.getPayloadData(), "");
            InputHandler inputHandler = siddhiManager.defineStream(siddhiStreamDefinition);
            inputHandlerMap.put(streamDefinition.getStreamId(), inputHandler);
            log.debug("input handler created for " + siddhiStreamDefinition.getStreamId());
        }

        try {
            siddhiManager.addExecutionPlan(executionPlanConfiguration.getQueryExpressions());
        } catch (Exception e) {
            throw new ExecutionPlanConfigurationException("Invalid query specified, " + e.getMessage(), e);
        }

        // exported/output streams
        Map<String, EventProducer> producerMap = new ConcurrentHashMap<String, EventProducer>(executionPlanConfiguration.getExportedStreams().size());
        List<String> newStreams = new ArrayList<String>();
        List<String> defaultFormatterNeededStreams = new ArrayList<String>();

        for (StreamConfiguration exportedStreamConfiguration : executionPlanConfiguration.getExportedStreams()) {
            org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition = siddhiManager.getStreamDefinition(exportedStreamConfiguration.getSiddhiStreamName());
            StreamDefinition streamDefinition;
            if (siddhiStreamDefinition != null) {
                streamDefinition = EventProcessorUtil.convertToDatabridgeStreamDefinition(siddhiStreamDefinition, exportedStreamConfiguration);
            } else {
                throw new ExecutionPlanConfigurationException("No matching Siddhi stream for " + exportedStreamConfiguration.getStreamId() + " in the name of " + exportedStreamConfiguration.getSiddhiStreamName());
            }

            // adding callbacks
            EventJunction junction = eventJunctionMap.get(exportedStreamConfiguration.getStreamId());
            if (junction == null) {
                junction = createEventJunctionWithoutSubscriptions(tenantId, streamDefinition);
                newStreams.add(junction.getStreamDefinition().getStreamId());

            }
            if (exportedStreamConfiguration.isPassThroughFlowSupported()) {
                defaultFormatterNeededStreams.add(exportedStreamConfiguration.getStreamId());
            }

            SiddhiOutputStreamListener streamCallback = new SiddhiOutputStreamListener(exportedStreamConfiguration.getSiddhiStreamName(), junction, executionPlanConfiguration, tenantId);
            siddhiManager.addCallback(exportedStreamConfiguration.getSiddhiStreamName(), streamCallback);
            producerMap.put(exportedStreamConfiguration.getStreamId(), streamCallback);
        }

        //subscribe input to junction
        for (StreamConfiguration importedStreamConfiguration : executionPlanConfiguration.getImportedStreams()) {
            EventJunction eventJunction = eventJunctionMap.get(importedStreamConfiguration.getStreamId());

            InputHandler inputHandler = inputHandlerMap.get(importedStreamConfiguration.getStreamId());
            SiddhiInputEventDispatcher eventDispatcher = new SiddhiInputEventDispatcher(importedStreamConfiguration.getStreamId(), inputHandler, executionPlanConfiguration, tenantId);

            eventJunction.addConsumer(eventDispatcher);
        }

        //add output to junction
        for (StreamConfiguration exportedStreamConfiguration : executionPlanConfiguration.getExportedStreams()) {
            EventJunction eventJunction = eventJunctionMap.get(exportedStreamConfiguration.getStreamId());
            eventJunction.addProducer(producerMap.get(exportedStreamConfiguration.getStreamId()));
        }
        ExecutionPlan executionPlan = new ExecutionPlan(executionPlanConfiguration.getName(), siddhiManager, executionPlanConfiguration);
        tenantExecutionPlans.put(executionPlanConfiguration.getName(), executionPlan);

        List<PassthroughSenderConfigurator> passthroughSenderConfigurators = EventProcessorValueHolder.getPassthroughSenderConfiguratorList();
        for (String streamId : defaultFormatterNeededStreams) {
            for (PassthroughSenderConfigurator passthroughSenderConfigurator : passthroughSenderConfigurators) {
                passthroughSenderConfigurator.deployDefaultEventSender(streamId, axisConfiguration);
            }
        }
        for (String streamId : newStreams) {
            activateInactiveExecutionPlanConfigurations(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY, streamId, tenantId);
        }
    }

    public List<StreamDefinition> getSiddhiStreams(String[] inputStreamDefinitions, String queryExpressions) throws
             SiddhiPraserException {
        SiddhiManager siddhiManager = createMockSiddhiManager(inputStreamDefinitions, queryExpressions);
        List<org.wso2.siddhi.query.api.definition.StreamDefinition> streamDefinitions = siddhiManager.getStreamDefinitions();
        List<StreamDefinition> databridgeStreamDefinitions = new ArrayList<StreamDefinition>(streamDefinitions.size());
        for (org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDef: streamDefinitions) {
            StreamConfiguration streamConfig = new StreamConfiguration(siddhiStreamDef.getStreamId());
            StreamDefinition databridgeStreamDef = EventProcessorUtil.convertToDatabridgeStreamDefinition(siddhiStreamDef, streamConfig);
            databridgeStreamDefinitions.add(databridgeStreamDef);

        }
        siddhiManager.shutdown();
        return databridgeStreamDefinitions;
    }

    @Override
    public String getExecutionPlanStatusAsString(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFileList = tenantSpecificExecutionPlanFiles.get(tenantId);
        if(executionPlanConfigurationFileList != null) {
            for(ExecutionPlanConfigurationFile executionPlanConfigurationFile: executionPlanConfigurationFileList) {
                if(filename != null && filename.equals(executionPlanConfigurationFile.getFileName())) {
                    String statusMsg = executionPlanConfigurationFile.getDeploymentStatusMessage();
                    if(executionPlanConfigurationFile.getDependency() != null) {
                        statusMsg = statusMsg + " [Dependency: " + executionPlanConfigurationFile.getDependency() + "]";
                    }
                    return statusMsg;
                }
            }
        }

        return EventProcessorConstants.NO_DEPENDENCY_INFO_MSG;
    }

    public boolean validateSiddhiQueries(String[] inputStreamDefinitions, String queryExpressions) throws
             SiddhiPraserException {
        SiddhiManager siddhiManager = createMockSiddhiManager(inputStreamDefinitions, queryExpressions);
        siddhiManager.shutdown();
        return true;
    }

    private SiddhiManager createMockSiddhiManager(String[] inputStreamDefinitions, String executionPlan) throws SiddhiPraserException {
        SiddhiConfiguration siddhiConfig = new SiddhiConfiguration();
        siddhiConfig.setSiddhiExtensions(SiddhiExtensionLoader.loadSiddhiExtensions());
        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfig);
        try {
            List<CarbonDataSource> dataSources = EventProcessorValueHolder.getDataSourceService().getAllDataSources();
            for (CarbonDataSource cds : dataSources) {
                try {
                    if (cds.getDSObject() instanceof DataSource) {
                        siddhiManager.getSiddhiContext().addDataSource(cds.getDSMInfo().getName(), (DataSource) cds.getDSObject());
                    }
                } catch (Exception e) {
                    log.error("Unable to add the datasource" + cds.getDSMInfo().getName(), e);
                }
            }
        } catch (DataSourceException e) {
            log.error("Unable to access the datasource service", e);
        }

        for (String streamDefinition : inputStreamDefinitions) {
            if (streamDefinition.trim().length() > 0) {
                siddhiManager.defineStream(streamDefinition);
            }
        }
        siddhiManager.addExecutionPlan(executionPlan);
        return siddhiManager;
    }

    private SiddhiManager getSiddhiManagerFor(ExecutionPlanConfiguration executionPlanConfiguration, SiddhiConfiguration siddhiConfig) throws ExecutionPlanConfigurationException {
        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfig);
        try {
            List<CarbonDataSource> dataSources = EventProcessorValueHolder.getDataSourceService().getAllDataSources();
            for (CarbonDataSource cds : dataSources) {
                try {
                    if (cds.getDSObject() instanceof DataSource) {
                        siddhiManager.getSiddhiContext().addDataSource(cds.getDSMInfo().getName(), (DataSource) cds.getDSObject());
                    }
                } catch (Exception e) {
                    log.error("Unable to add the datasource" + cds.getDSMInfo().getName(), e);
                }
            }
        } catch (DataSourceException e) {
            log.error("Unable to populate the data sources in Siddhi engine.", e);
        }

        //todo persistence.
        int persistenceTimeInterval = 0;
        try {
            persistenceTimeInterval = Integer.parseInt(executionPlanConfiguration.getSiddhiConfigurationProperties().get(EventProcessorConstants.SIDDHI_SNAPSHOT_INTERVAL));
        } catch (NumberFormatException e) {
            log.error("Unable to parse snapshot time interval.", e);
        }

        if (persistenceTimeInterval > 0) {
            if (null == EventProcessorValueHolder.getPersistenceStore()) {
                if (EventProcessorValueHolder.getClusterInformation() == null) {
                    try {
                        String adminPassword = EventProcessorValueHolder.getUserRealm().
                                getRealmConfiguration().getAdminPassword();
                        String adminUserName = EventProcessorValueHolder.getUserRealm().
                                getRealmConfiguration().getAdminUserName();

                        ClusterInformation clusterInformation = new ClusterInformation(adminUserName,
                                adminPassword);
                        clusterInformation.setClusterName(CassandraPersistenceStore.CLUSTER_NAME);
                        EventProcessorValueHolder.setClusterInformation(clusterInformation);
                    } catch (UserStoreException e) {
                        log.error("Unable to get realm configuration.", e);
                    }
                }
                if (CassandraConnectionValidator.getInstance().checkCassandraConnection(EventProcessorValueHolder.getClusterInformation().getUsername(), EventProcessorValueHolder.getClusterInformation().getPassword())) {
                    Cluster cluster = EventProcessorValueHolder.getDataAccessService().getCluster(EventProcessorValueHolder.getClusterInformation());
                    CassandraPersistenceStore casandraPersistenceStore = new CassandraPersistenceStore(cluster);
                    EventProcessorValueHolder.setPersistenceStore(casandraPersistenceStore);
                } else {
                    throw new ExecutionPlanConfigurationException("Cassandra is not up and running, All connection pools are down. Please enable cassandra with server startup (Command: ./wso2server.sh -Ddisable.cassandra.server.startup=false)");
                }


            }
            siddhiManager.setPersistStore(EventProcessorValueHolder.getPersistenceStore());
        }
        return siddhiManager;
    }

    private SiddhiConfiguration getSiddhiConfigurationFor(ExecutionPlanConfiguration executionPlanConfiguration, int tenantId) throws ServiceDependencyValidationException {
        SiddhiConfiguration siddhiConfig = new SiddhiConfiguration();
        siddhiConfig.setAsyncProcessing(false);
        siddhiConfig.setInstanceIdentifier("org.wso2.siddhi.instance-" + tenantId + "-" + UUID.randomUUID().toString());

        String isDistributedProcessingEnabledString = executionPlanConfiguration.getSiddhiConfigurationProperties().get(EventProcessorConstants.SIDDHI_DISTRIBUTED_PROCESSING);
        if (isDistributedProcessingEnabledString != null && isDistributedProcessingEnabledString.equalsIgnoreCase("true")) {
            siddhiConfig.setDistributedProcessing(true);
            if (EventProcessorValueHolder.getHazelcastInstance() != null) {
                siddhiConfig.setInstanceIdentifier(EventProcessorValueHolder.getHazelcastInstance().getName());
            } else {
                throw new ServiceDependencyValidationException(EventProcessorConstants.HAZELCAST_INSTANCE, "Hazelcast instance is not initialized.");
            }
        } else {
            siddhiConfig.setDistributedProcessing(false);
        }

        siddhiConfig.setQueryPlanIdentifier("org.wso2.siddhi-" + tenantId + "-" + executionPlanConfiguration.getName());
        siddhiConfig.setSiddhiExtensions(SiddhiExtensionLoader.loadSiddhiExtensions());
        return siddhiConfig;
    }

    public void notifyServiceAvailability(String serviceId) {
        for (Integer tenantId : tenantSpecificExecutionPlanFiles.keySet()) {
            try {
                activateInactiveExecutionPlanConfigurations(ExecutionPlanConfigurationFile.Status.WAITING_FOR_OSGI_SERVICE, serviceId, tenantId);
            } catch (ExecutionPlanConfigurationException e) {
                log.error("Error while redeploying distributed execution plans.", e);
            }
        }
    }

    private void removeExecutionPlanConfiguration(String name, int tenantId) {
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null && executionPlanMap.containsKey(name)) {
            ExecutionPlan executionPlan = executionPlanMap.remove(name);
            executionPlan.shutdown();

            ExecutionPlanConfiguration executionPlanConfiguration = executionPlan.getExecutionPlanConfiguration();

            // releasing junction listeners.
            // even if the junction has no consumers after this operations, we don't remove the junction
            // it will only be removed when builder notifies of undeployment.
            for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getImportedStreams()) {
                EventJunction junction = getEventJunction(tenantId, streamConfiguration.getStreamId());
                if (junction != null) {
                    for (EventConsumer consumer : junction.getAllEventConsumers()) {
                        if (consumer.getOwner() == executionPlanConfiguration) {
                            junction.removeConsumer(consumer);
                        }
                    }
                }
            }

            // checking producers from this and dropping junction if its the sole producer for the junction
            // if so remove the producer and notifying formatters
            for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getExportedStreams()) {
                EventJunction junction = getEventJunction(tenantId, streamConfiguration.getStreamId());
                if (junction != null) {
                    for (EventProducer producer : junction.getAllEventProducers()) {
                        if (producer.getOwner() == executionPlanConfiguration) {
                            junction.removeProducer(producer);
                        }
                    }

                    if (junction.getAllEventProducers().size() == 0) {
                        deactivateActiveExecutionPlanConfigurations(streamConfiguration.getStreamId(), tenantId);
                    }

                }
            }

        }
    }

    public void addExecutionPlanConfigurationFile(ExecutionPlanConfigurationFile configurationFile,
                                                  int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles == null) {
            executionPlanConfigurationFiles = new ArrayList<ExecutionPlanConfigurationFile>();
            tenantSpecificExecutionPlanFiles.put(tenantId, executionPlanConfigurationFiles);
        }
        executionPlanConfigurationFiles.add(configurationFile);
    }

    /**
     * Just removes the configuration file
     *
     * @param fileName the filename of the {@link ExecutionPlanConfigurationFile} to be removed
     * @param tenantId the tenantId of the tenant to which this configuration file belongs
     */
    public void removeExecutionPlanConfigurationFile(String fileName, int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        for (Iterator<ExecutionPlanConfigurationFile> iterator = executionPlanConfigurationFiles.iterator(); iterator.hasNext(); ) {
            ExecutionPlanConfigurationFile configurationFile = iterator.next();
            if (configurationFile.getFileName().equals(fileName)) {
                if (configurationFile.getStatus().equals(ExecutionPlanConfigurationFile.Status.DEPLOYED)) {
                    removeExecutionPlanConfiguration(configurationFile.getExecutionPlanName(), tenantId);
                }
                iterator.remove();
                break;
            }
        }
    }

    public String getActiveExecutionPlanConfigurationContent(String planName,
                                                             AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByPlanName(planName, tenantId);
        if (configFile == null) {
            throw new ExecutionPlanConfigurationException("Configuration file for " + planName + "doesn't exist.");
        }
        return EventProcessorConfigurationFilesystemInvoker.readExecutionPlanConfigFile(configFile.getFileName(), axisConfiguration);
    }

    public String getInactiveExecutionPlanConfigurationContent(String filename,
                                                               AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        return EventProcessorConfigurationFilesystemInvoker.readExecutionPlanConfigFile(filename, axisConfiguration);
    }

    @Override
    public Map<String, ExecutionPlanConfiguration> getAllActiveExecutionConfigurations(
            int tenantId) {
        Map<String, ExecutionPlanConfiguration> configurationMap = new HashMap<String, ExecutionPlanConfiguration>();
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            for (Map.Entry<String, ExecutionPlan> entry : executionPlanMap.entrySet()) {
                configurationMap.put(entry.getKey(), entry.getValue().getExecutionPlanConfiguration());
            }
        }
        return configurationMap;
    }

    @Override
    public Map<String, ExecutionPlanConfiguration> getAllExportedStreamSpecificActiveExecutionConfigurations(
            int tenantId, String streamId) {
        Map<String, ExecutionPlanConfiguration> configurationMap = new HashMap<String, ExecutionPlanConfiguration>();
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            for (Map.Entry<String, ExecutionPlan> entry : executionPlanMap.entrySet()) {

                List<StreamConfiguration> streamConfigurationList =entry.getValue().getExecutionPlanConfiguration().getExportedStreams();
                for (StreamConfiguration streamConfiguration : streamConfigurationList){
                    String streamNameWithVersion = streamConfiguration.getName()+":"+streamConfiguration.getVersion();
                    if(streamNameWithVersion.equals(streamId)){
                        configurationMap.put(entry.getKey(), entry.getValue().getExecutionPlanConfiguration());
                    }
                }
            }
        }
        return configurationMap;
    }

    @Override
    public Map<String, ExecutionPlanConfiguration> getAllImportedStreamSpecificActiveExecutionConfigurations(
            int tenantId, String streamId) {
        Map<String, ExecutionPlanConfiguration> configurationMap = new HashMap<String, ExecutionPlanConfiguration>();
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            for (Map.Entry<String, ExecutionPlan> entry : executionPlanMap.entrySet()) {

                List<StreamConfiguration> streamConfigurationList =entry.getValue().getExecutionPlanConfiguration().getImportedStreams();
                for (StreamConfiguration streamConfiguration : streamConfigurationList){
                    String streamNameWithVersion = streamConfiguration.getName()+":"+streamConfiguration.getVersion();
                    if(streamNameWithVersion.equals(streamId)){
                        configurationMap.put(entry.getKey(), entry.getValue().getExecutionPlanConfiguration());
                    }
                }
            }
        }
        return configurationMap;
    }

    @Override
    public ExecutionPlanConfiguration getActiveExecutionConfiguration(String name, int tenantId) {
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            ExecutionPlan executionPlan = executionPlanMap.get(name);
            if (executionPlan != null) {
                return executionPlan.getExecutionPlanConfiguration();
            }
        }
        return null;
    }

    @Override
    public List<ExecutionPlanConfigurationFile> getAllInactiveExecutionPlanConfiguration(
            int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = this.tenantSpecificExecutionPlanFiles.get(tenantId);

        List<ExecutionPlanConfigurationFile> files = new ArrayList<ExecutionPlanConfigurationFile>();
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile configFile : executionPlanConfigurationFiles) {
                if (configFile.getStatus() == ExecutionPlanConfigurationFile.Status.ERROR || configFile.getStatus() == ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY || configFile.getStatus() == ExecutionPlanConfigurationFile.Status.WAITING_FOR_OSGI_SERVICE) {
                    files.add(configFile);
                }
            }
        }
        return files;
    }

    public void subscribeStreamListener(String streamId, EventSender sender) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventJunction> junctionMap = this.tenantSpecificEventJunctions.get(tenantId);
        if (junctionMap == null) {
            junctionMap = new ConcurrentHashMap<String, EventJunction>();
            this.tenantSpecificEventJunctions.put(tenantId, junctionMap);
        }
        EventJunction junction = junctionMap.get(streamId);
        if (junction != null) {
            junction.addConsumer(new ExternalStreamConsumer(sender, sender));
        } else {
            throw new EventProducerException("Junction not found for stream id : " + streamId);
        }
    }

    public void unsubscribeStreamListener(String streamId, EventSender listener,
                                          int tenantId) {
        Map<String, EventJunction> junctionMap = this.tenantSpecificEventJunctions.get(tenantId);
        if (junctionMap != null) {
            EventJunction junction = junctionMap.get(streamId);
            if (junction != null) {
                for (EventConsumer consumer : junction.getAllEventConsumers()) {
                    if (consumer.getOwner() == listener) {
                        junction.removeConsumer(consumer);
                    }
                }
            }
        }
    }

    @Override
    public void setTracingEnabled(String executionPlanName, boolean isEnabled,
                                  AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, ExecutionPlan> executionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlans != null) {
            ExecutionPlan executionPlan = executionPlans.get(executionPlanName);
            executionPlan.getExecutionPlanConfiguration().setTracingEnabled(isEnabled);
            editExecutionPlanConfiguration(executionPlan.getExecutionPlanConfiguration(), executionPlanName, tenantId, axisConfiguration);

        }
    }

    @Override
    public void setStatisticsEnabled(String executionPlanName, boolean isEnabled,
                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, ExecutionPlan> executionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlans != null) {
            ExecutionPlan executionPlan = executionPlans.get(executionPlanName);
            executionPlan.getExecutionPlanConfiguration().setStatisticsEnabled(isEnabled);
            editExecutionPlanConfiguration(executionPlan.getExecutionPlanConfiguration(), executionPlanName, tenantId, axisConfiguration);

        }
    }

    public void registerPassthroughSenderConfigurator(PassthroughSenderConfigurator passthroughSenderConfigurator) {
        EventProcessorValueHolder.addPassthroughSenderConfigurator(passthroughSenderConfigurator);
    }

    /**
     * Activate Inactive Execution Plan Configurations
     *
     * @param tenantId the tenant id of the tenant which triggered this call
     * @param resolvedDependencyId the id of the dependency that was resolved which resulted in triggering this method call
     */
    public void activateInactiveExecutionPlanConfigurations(ExecutionPlanConfigurationFile.Status status, String resolvedDependencyId, int tenantId)
            throws ExecutionPlanConfigurationException {

        List<ExecutionPlanConfigurationFile> reloadFileList = new ArrayList<ExecutionPlanConfigurationFile>();

        if (tenantSpecificExecutionPlanFiles != null && tenantSpecificExecutionPlanFiles.size() > 0) {
            List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);

            if (executionPlanConfigurationFiles != null) {
                for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : executionPlanConfigurationFiles) {
                    if ((executionPlanConfigurationFile.getStatus().equals(status)) && resolvedDependencyId.equalsIgnoreCase(executionPlanConfigurationFile.getDependency())) {
                        reloadFileList.add(executionPlanConfigurationFile);
                    }
                }
            }
        }
        for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : reloadFileList) {
            try {
                EventProcessorConfigurationFilesystemInvoker.reload(executionPlanConfigurationFile.getFileName(), executionPlanConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Execution Plan configuration file : " + new File(executionPlanConfigurationFile.getFileName()).getName());
            }
        }

    }

    public void deactivateActiveExecutionPlanConfigurations(String streamId, int tenantId) {

        EventJunction junction = getEventJunction(tenantId, streamId);

        if (junction != null) {
            int producers = junction.getAllEventProducers().size();
            // if more than 1 producers available, still need to keep the junction.
            if (producers == 0) {

                // junction has no active producers. need to be removed.
                EventJunction removedJunction = this.tenantSpecificEventJunctions.get(tenantId).remove(streamId);

                for (EventConsumer consumer : removedJunction.getAllEventConsumers()) {
                    if (consumer instanceof SiddhiInputEventDispatcher) {
                        ExecutionPlanConfiguration executionPlanConfiguration = (ExecutionPlanConfiguration) consumer.getOwner();
                        ExecutionPlanConfigurationFile executionPlanConfigurationFile = getExecutionPlanConfigurationFileByPlanName(executionPlanConfiguration.getName(), tenantId);
                        try {
                            EventProcessorConfigurationFilesystemInvoker.reload(executionPlanConfigurationFile.getFileName(), executionPlanConfigurationFile.getAxisConfiguration());
//                            EventProcessorDeployer deployer = (EventProcessorDeployer) EventProcessorConfigurationFilesystemInvoker.getDeployer(executionPlanConfigurationFile.getAxisConfiguration(), EventProcessorConstants.EP_ELE_DIRECTORY);
//                            String filePath = new File(executionPlanConfigurationFile.getAxisConfiguration().getRepository().getPath()).getAbsolutePath() + File.separator + EventProcessorConstants.EP_ELE_DIRECTORY + File.separator + executionPlanConfigurationFile.getFileName();
//                            deployer.executeManualUndeployment(filePath);
//                            executionPlanConfigurationFile.setDependency(streamId);
//                            executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY);
//                            addExecutionPlanConfigurationFile(executionPlanConfigurationFile,tenantId);
                        } catch (Exception e) {
                            log.error("Exception occurred while trying to deploy the Execution Plan configuration file : " + new File(executionPlanConfigurationFile.getFileName()).getName());
                        }

                    }
                }
                if (EventProcessorValueHolder.getNotificationListener() != null) {
                    EventProcessorValueHolder.getNotificationListener().removeEventStream(tenantId, streamId);
                }
            }
        }
    }

    public void addExternalStream(String streamId, EventReceiver eventReceiver)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventJunction junction = getEventJunction(tenantId, streamId);
        StreamDefinition streamDefinition;
        try {
            streamDefinition = EventProcessorValueHolder.getEventStreamService().getStreamDefinitionFromStore(streamId, tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new ExecutionPlanConfigurationException("Could not retrieve stream definition with stream ID : " + streamId, e);
        }

        if (streamDefinition == null) {
            throw new ExecutionPlanConfigurationException("The stream definition does not exist with stream ID : " + streamId);
        }
        if (junction == null) {
            junction = createEventJunctionWithoutSubscriptions(tenantId, streamDefinition);
        } else {
            if (!junction.getStreamDefinition().equals(streamDefinition)) {
                //TODO Handle. Shouldn't we throw an exception an stop the subscription proceeding further?
                log.error("Externally defined stream: " + streamDefinition + " is different from the existing stream :" + junction.getStreamDefinition());
            }
        }

        ExternalStreamListener externalStreamListener = new ExternalStreamListener(junction, eventReceiver);
        junction.addProducer(externalStreamListener);
        try {
            eventReceiver.subscribe(junction.getStreamDefinition().getStreamId(), externalStreamListener, tenantId);
        } catch (EventReceiverException e) {
            throw new ExecutionPlanConfigurationException("Error subscribing to event receiver : " + e.getMessage(), e);
        }
        activateInactiveExecutionPlanConfigurations(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY, streamId, tenantId);
    }

    public void removeExternalStream(int tenantId, String streamId, EventReceiver eventReceiver)
            throws ExecutionPlanConfigurationException {
        EventJunction junction = getEventJunction(tenantId, streamId);

        boolean callDeactivateExecutionPlan = true;

        if (junction != null) {
            List<EventProducer> eventProducers = junction.getAllEventProducers();
            for (EventProducer eventProducer : eventProducers) {
                if (eventProducer instanceof ExternalStreamListener) {
                    if (eventProducer.getOwner() == eventReceiver) {
                        junction.removeProducer(eventProducer);
                        try {
                            eventReceiver.unsubsribe(streamId, (ExternalStreamListener) eventProducer, tenantId);
                        } catch (EventReceiverException e) {
                            throw new ExecutionPlanConfigurationException("Error unsubscribing from event receiver : " + e.getMessage(), e);
                        }
                    } else {
                        callDeactivateExecutionPlan = false;
                    }
                }
            }
        }

        if (callDeactivateExecutionPlan) {
            deactivateActiveExecutionPlanConfigurations(streamId, tenantId);
        }
    }

/*
    public List<String> getStreamIds(int tenantId) {
        if (tenantSpecificEventJunctions.get(tenantId) != null) {
            return new ArrayList<String>(tenantSpecificEventJunctions.get(tenantId).keySet());
        }

        return new ArrayList<String>(0);
    }
*/

//    /**
//     * gets the relevant stream definition if already available with a junction.
//     *
//     * @param streamId format-  streamName:version
//     * @param tenantId
//     * @return
//     */
//    public StreamDefinition getStreamDefinition(String streamId, int tenantId) {
//        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
//        if (eventJunctionMap != null) {
//            EventJunction junction = eventJunctionMap.get(streamId);
//            if (junction != null) {
//                return junction.getStreamDefinition();
//            }
//        }
//        return null;
//    }

    private EventJunction getEventJunction(int tenantId, String streamWithVersion) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            return eventJunctionMap.get(streamWithVersion);
        }
        return null;
    }

    public void notifyAllStreamsToFormatter() {
        for (Map.Entry<Integer, Map<String, EventJunction>> entry : tenantSpecificEventJunctions.entrySet()) {
            Map<String, EventJunction> junctions = entry.getValue();
            for (Map.Entry<String, EventJunction> junctionEntry : junctions.entrySet()) {
                EventProcessorValueHolder.getNotificationListener().addedNewEventStream(entry.getKey(), junctionEntry.getValue().getStreamDefinition().getStreamId());
            }
        }
    }

    // gets file by name.
    private ExecutionPlanConfigurationFile getExecutionPlanConfigurationFileByPlanName(String name,
                                                                                       int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile file : executionPlanConfigurationFiles) {
                if (name.equals(file.getExecutionPlanName()) && file.getStatus().equals(ExecutionPlanConfigurationFile.Status.DEPLOYED)) {
                    return file;
                }
            }
        }
        return null;
    }

    private void editExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            String executionPlanName, int tenantId, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

        ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByPlanName(executionPlanName, tenantId);
        String fileName = configFile.getFileName();
        EventProcessorConfigurationFilesystemInvoker.delete(configFile.getFileName(), axisConfiguration);
        OMElement omElement = EventProcessorConfigurationHelper.toOM(executionPlanConfiguration);
        EventProcessorConfigurationFilesystemInvoker.save(omElement, executionPlanName, fileName, axisConfiguration);
    }

    private EventJunction createEventJunctionWithoutSubscriptions(int tenantId,
                                                                  StreamDefinition streamDefinition) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap == null) {
            eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
            tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
        }
        EventJunction junction = new EventJunction(streamDefinition);
        eventJunctionMap.put(streamDefinition.getStreamId(), junction);
        if (EventProcessorValueHolder.getNotificationListener() != null) {
            EventProcessorValueHolder.getNotificationListener().addedNewEventStream(tenantId, streamDefinition.getStreamId());
        }
        return junction;
    }

    private void validateToRemoveInactiveExecutionPlanConfiguration(String executionPlanName,
                                                                    AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = executionPlanName + EventProcessorConstants.EP_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : executionPlanConfigurationFiles) {
                if ((executionPlanConfigurationFile.getFileName().equals(fileName))) {
                    if (!(executionPlanConfigurationFile.getStatus().equals(ExecutionPlanConfigurationFile.Status.DEPLOYED))) {
                        EventProcessorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
                        break;
                    }
                }
            }
        }

    }

    private boolean checkExecutionPlanValidity(String executionPlanName, int tenantId)
            throws ExecutionPlanConfigurationException {

        Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap;
        executionPlanConfigurationMap = getAllActiveExecutionConfigurations(tenantId);
        if (executionPlanConfigurationMap != null) {
            for (String executionPlan : executionPlanConfigurationMap.keySet()) {
                if (executionPlanName.equalsIgnoreCase(executionPlan)) {
                    return false;
                }
            }
        }

        return true;
    }

}
