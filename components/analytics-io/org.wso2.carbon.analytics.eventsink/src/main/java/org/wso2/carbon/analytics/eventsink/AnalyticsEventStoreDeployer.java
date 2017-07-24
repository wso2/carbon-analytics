/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.eventsink;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreDeploymentException;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;
import org.wso2.carbon.analytics.eventsink.internal.AnalyticsEventSinkServerStartupObserver;
import org.wso2.carbon.analytics.eventsink.internal.AnalyticsEventStoreManager;
import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkConstants;
import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkUtil;
import org.wso2.carbon.analytics.eventsink.internal.util.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Analytics Event Store deployer which basically combines the streams and tables connectivity.
 */
public class AnalyticsEventStoreDeployer extends AbstractDeployer {

    private final static Log log = LogFactory.getLog(AnalyticsEventStoreDeployer.class);
    private static List<DeploymentFileData> pausedDeployments = new ArrayList<>();
    private boolean eventSinkEnabled;

    public AnalyticsEventStoreDeployer(){
        String disableEventSink = System.getProperty(AnalyticsEventSinkConstants.DISABLE_EVENT_SINK_JVM_OPTION);
        this.eventSinkEnabled = !(disableEventSink != null && Boolean.parseBoolean(disableEventSink));
    }

    @Override
    public void init(ConfigurationContext configurationContext) {
        File deployementDir = new File(MultitenantUtils.getAxis2RepositoryPath(CarbonContext.getThreadLocalCarbonContext().
                getTenantId()) + AnalyticsEventSinkConstants.DEPLOYMENT_DIR_NAME);
        if (!deployementDir.exists()) {
            if (!deployementDir.mkdir()) {
                log.warn("Unable to create the deployment dir at: " + deployementDir.getPath());
            }
        }
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        if (AnalyticsEventSinkServerStartupObserver.getInstance().isServerStarted()) {
            log.info("Deploying analytics event store: " + deploymentFileData.getName());
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            try {
                AnalyticsEventStore eventStoreConfiguration = AnalyticsEventStoreManager.getInstance()
                        .getAnalyticsEventStore(deploymentFileData.getFile());
                if (AnalyticsEventSinkUtil.getAnalyticsEventStoreName(deploymentFileData.getName()).
                        equalsIgnoreCase(eventStoreConfiguration.getName())) {
                    addEventStore(tenantId, eventStoreConfiguration);
                } else {
                    throw new AnalyticsEventStoreDeploymentException("Invalid configuration provided! File name: " +
                            AnalyticsEventSinkUtil.getAnalyticsEventStoreName(deploymentFileData.getName() + " should be " +
                                    "matched with deduced table name: " + eventStoreConfiguration.getName() + " for the streams"));
                }
            } catch (AnalyticsEventStoreException e) {
                String errMsg = "Error while deploying file: " + deploymentFileData.getName() + " for tenant id: " + tenantId;
                log.error(errMsg, e);
                throw new AnalyticsEventStoreDeploymentException(errMsg, e);
            } catch (Exception e) {
                String errorMsg = "Unable to deploy the event store: " + deploymentFileData.getName() + ". " + e.getMessage();
                log.error(errorMsg, e);
                throw new AnalyticsEventStoreDeploymentException(errorMsg, e);
            }
            log.info("Deployed successfully analytics event store: " + deploymentFileData.getName());
        } else {
            pausedDeployments.add(deploymentFileData);
        }
    }


    private void addEventStore(int tenantId, AnalyticsEventStore eventStore)
            throws AnalyticsEventStoreException {
        try {
            AnalyticsEventStoreManager.getInstance().addEventStoreConfiguration(tenantId, eventStore);
            if (this.eventSinkEnabled) {
                if (eventStore.getRecordStore() == null) {
                    ServiceHolder.getAnalyticsDataAPI().createTable(tenantId, eventStore.getName());
                } else {
                    ServiceHolder.getAnalyticsDataAPI().createTable(tenantId, eventStore.getRecordStore(),
                            eventStore.getName());
                }
                ServiceHolder.getAnalyticsDataAPI().setTableSchema(tenantId, eventStore.getName(),
                        this.resolveAndMergeSchemata(tenantId, eventStore));
                for (String streamId : eventStore.getEventSource().getStreamIds()) {
                    if (ServiceHolder.getStreamDefinitionStoreService().getStreamDefinition(streamId, tenantId) != null) {
                        ServiceHolder.getAnalyticsEventStreamListener().subscribeForStream(tenantId, streamId);
                    }
                }
            } else {
                    log.info("Event store is disabled in this node, hence ignoring the event sink configuration: "
                            + eventStore.getName());
            }
        } catch (AnalyticsException e) {
            String errorMsg = "Error while creating the table Or setting the schema for table: " + eventStore.getName();
            log.error(errorMsg, e);
            throw new AnalyticsEventStoreException(errorMsg, e);
        } catch (StreamDefinitionStoreException e) {
            String errorMsg = "Error when subscribing to the stream: " + e.getMessage();
            log.error(errorMsg, e);
            throw new AnalyticsEventStoreException(errorMsg, e);
        }
    }

    private AnalyticsSchema resolveAndMergeSchemata(int tenantId, AnalyticsEventStore eventStoreConfig) throws AnalyticsException {
        AnalyticsSchema incomingSchema;
        try {
            incomingSchema = AnalyticsEventSinkUtil.getAnalyticsSchema(eventStoreConfig.getAnalyticsTableSchema());

            if (!eventStoreConfig.isMergeSchema()) {
                return incomingSchema;
            }
            AnalyticsSchema liveSchema = ServiceHolder.getAnalyticsDataAPI().getTableSchema(tenantId, eventStoreConfig.getName());
            return AnalyticsDataServiceUtils.createMergedSchema(liveSchema, incomingSchema.getPrimaryKeys(),
                                                                new ArrayList<>(incomingSchema.getColumns().values()),
                                                                new ArrayList<>(incomingSchema.getIndexedColumns().keySet()));
        } catch (AnalyticsEventStoreException e) {
            log.error("Error while retrieving the eventStore config for table: " + eventStoreConfig.getName() + ", " + e.getMessage(), e);
            throw new AnalyticsException("Error while retrieving the eventStore config for stream: " +
                                         eventStoreConfig.getName() + ", " + e.getMessage(), e);
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        log.info("Undeploying analytics event store: " + fileName);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String eventStoreName = AnalyticsEventSinkUtil.getAnalyticsEventStoreName(new File(fileName).getName());
        AnalyticsEventStore existingEventStore = AnalyticsEventStoreManager.getInstance().removeEventStoreConfiguration(tenantId,
                eventStoreName);
        if (eventSinkEnabled) {
            if (existingEventStore != null) {
                for (String streamId : existingEventStore.getEventSource().getStreamIds()) {
                    ServiceHolder.getAnalyticsEventStreamListener().unsubscribeFromStream(tenantId, streamId);
                }
            }
        } else {
            log.info("Ignored event sink configuration: " + fileName + " since the event sink is disabled in this node.");
        }
        log.info("Undeployed successfully analytics event store: " + fileName);
    }

    public static List<DeploymentFileData> getPausedDeployments() {
        return pausedDeployments;
    }

    public static void clearPausedDeployments() {
        pausedDeployments = null;
    }

    @Override
    public void setDirectory(String s) {
    }

    @Override
    public void setExtension(String s) {
    }

}
