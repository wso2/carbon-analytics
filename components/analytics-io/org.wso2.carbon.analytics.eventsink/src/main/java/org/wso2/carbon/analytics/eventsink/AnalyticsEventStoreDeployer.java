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
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreDeploymentException;
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
    private static Log log = LogFactory.getLog(AnalyticsEventStoreDeployer.class);
    private static List<DeploymentFileData> pausedDeployments = new ArrayList<>();

    @Override
    public void init(ConfigurationContext configurationContext) {
        File deployementDir = new File(MultitenantUtils.getAxis2RepositoryPath(CarbonContext.getThreadLocalCarbonContext().
                getTenantId()) + AnalyticsEventSinkConstants.DEPLOYMENT_DIR_NAME);
        if (!deployementDir.exists()) {
            if (!deployementDir.mkdir()) {
                log.warn("Unable to create the deployment dir at : " + deployementDir.getPath());
            }
        }
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        if (AnalyticsEventSinkServerStartupObserver.getInstance().isServerStarted()) {
            log.info("Deploying analytics event store :" + deploymentFileData.getName());
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
                                    "matched with deduced table name : " + eventStoreConfiguration.getName() + " for the streams"));
                }
            } catch (AnalyticsEventStoreException e) {
                String errMsg = "Error while deploying file : " + deploymentFileData.getName() + " for tenant id : " + tenantId;
                log.error(errMsg, e);
                throw new AnalyticsEventStoreDeploymentException(errMsg, e);
            } catch (Throwable throwable) {
                String errorMsg = "Unable to deploy the event store : " + deploymentFileData.getName() + ". " + throwable.getMessage();
                log.error(errorMsg, throwable);
                throw new AnalyticsEventStoreDeploymentException(errorMsg, throwable);
            }
            log.info("Deployed successfully analytics event store :" + deploymentFileData.getName());
        } else {
            pausedDeployments.add(deploymentFileData);
        }
    }

    private void addEventStore(int tenantId, AnalyticsEventStore eventStore)
            throws AnalyticsEventStoreException {
        try {
            AnalyticsEventStoreManager.getInstance().addEventStoreConfiguration(tenantId, eventStore);
            if (eventStore.getRecordStore() == null) {
                ServiceHolder.getAnalyticsDataAPI().createTable(tenantId, eventStore.getName());
            } else {
                ServiceHolder.getAnalyticsDataAPI().createTable(tenantId, eventStore.getRecordStore(),
                        eventStore.getName());
            }
            ServiceHolder.getAnalyticsDataAPI().setTableSchema(tenantId, eventStore.getName(),
                    AnalyticsEventSinkUtil.getAnalyticsSchema(eventStore.getAnalyticsTableSchema()));
            for (String streamId : eventStore.getEventSource().getStreamIds()) {
                if (ServiceHolder.getStreamDefinitionStoreService().getStreamDefinition(streamId, tenantId) != null) {
                    ServiceHolder.getAnalyticsEventStreamListener().subscribeForStream(tenantId, streamId);
                }
            }
        } catch (AnalyticsException e) {
            String errorMsg = "Error while creating the table Or setting the schema for table :" + eventStore.getName();
            log.error(errorMsg, e);
            throw new AnalyticsEventStoreException(errorMsg, e);
        } catch (StreamDefinitionStoreException e) {
            String errorMsg = "Error when subscribing to the stream." + e.getMessage();
            log.error(errorMsg, e);
            throw new AnalyticsEventStoreException(errorMsg, e);
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        log.info("Undeploying analytics event store : " + fileName);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String eventStoreName = AnalyticsEventSinkUtil.getAnalyticsEventStoreName(new File(fileName).getName());
        AnalyticsEventStore existingEventStore = AnalyticsEventStoreManager.getInstance().removeEventStoreConfiguration(tenantId,
                eventStoreName);
        if (existingEventStore != null) {
            for (String streamId : existingEventStore.getEventSource().getStreamIds()) {
                ServiceHolder.getAnalyticsEventStreamListener().unsubscribeFromStream(tenantId, streamId);
            }
        }
        log.info("Undeployed successfully analytics event store : " + fileName);
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
