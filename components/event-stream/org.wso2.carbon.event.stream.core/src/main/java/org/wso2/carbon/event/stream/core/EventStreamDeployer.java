/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.stream.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.CarbonEventStreamService;
import org.wso2.carbon.event.stream.core.internal.ds.EventStreamServiceValueHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy query plans as axis2 service
 */

public class EventStreamDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(org.wso2.carbon.event.stream.core.EventStreamDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedEventStreamFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> unDeployedEventStreamFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Reads the query-plan.xml and deploys it.
     *
     * @param deploymentFileData information about query plan
     * @throws org.apache.axis2.deployment.DeploymentException
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        try {
            String path = deploymentFileData.getAbsolutePath();

            if (!deployedEventStreamFilePaths.contains(path)) {
                processDeployment(deploymentFileData);
            } else {
                log.debug("Event stream file is already deployed :" + path);
                deployedEventStreamFilePaths.remove(path);
            }
        } catch (Throwable t) {
            log.error("Can't deploy the event stream : " + deploymentFileData.getName(), t);
            throw new DeploymentException("Can't deploy the event stream : " + deploymentFileData.getName(), t);
        }
    }

    @Override
    public void setDirectory(String s) {

    }

    @Override
    public void setExtension(String s) {

    }

    public void undeploy(String filePath) throws DeploymentException {
        try {

            if (!unDeployedEventStreamFilePaths.contains(filePath)) {
                processUndeployment(filePath);
            } else {
                log.debug("Event stream file is already undeployed :" + filePath);
                unDeployedEventStreamFilePaths.remove(filePath);
            }
        } catch (Throwable t) {
            log.error("Can't undeploy the event stream: " + filePath, t);
            throw new DeploymentException("Can't undeploy the event stream: " + filePath, t);
        }

    }

    public synchronized void processDeployment(DeploymentFileData deploymentFileData) throws EventStreamConfigurationException {
        CarbonEventStreamService carbonEventStreamService = EventStreamServiceValueHolder.getCarbonEventStreamService();
        File eventStreamFile = deploymentFileData.getFile();
        boolean isEditable = !eventStreamFile.getAbsolutePath().contains(File.separator + "carbonapps" + File.separator);
        try {
            String content = new Scanner(new File(eventStreamFile.getAbsolutePath())).useDelimiter("\\Z").next();
            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(content);

            if (!carbonEventStreamService.isEventStreamExist(eventStreamFile.getName())) {
                EventStreamConfiguration eventStreamConfiguration = new EventStreamConfiguration();
                eventStreamConfiguration.setStreamDefinition(streamDefinition);
                eventStreamConfiguration.setEditable(isEditable);
                eventStreamConfiguration.setFileName(eventStreamFile.getName());
                carbonEventStreamService.addEventStreamConfig(eventStreamConfiguration);
                log.info("Stream definition is deployed successfully  : " + streamDefinition.getStreamId());
            } else {
                log.info("Event stream definition " + streamDefinition.getStreamId() + " already exist");
            }
        } catch (MalformedStreamDefinitionException e) {
            throw new EventStreamConfigurationException("Error in constructing Stream Definition Object from JSON " + e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new EventStreamConfigurationException("Stream Definition file not found " + eventStreamFile.getAbsolutePath() + "," + e.getMessage(), e);
        }
    }

    public synchronized void processUndeployment(String filePath) throws EventStreamConfigurationException {

        String fileName = new File(filePath).getName();
        log.info("Stream Definition was undeployed successfully : " + fileName);
        CarbonEventStreamService carbonEventStreamService = EventStreamServiceValueHolder.getCarbonEventStreamService();
        carbonEventStreamService.removeEventStreamConfigurationFromMap(fileName);
    }

    public Set<String> getDeployedEventStreamFilePaths() {
        return deployedEventStreamFilePaths;
    }

    public Set<String> getUnDeployedEventStreamFilePaths() {
        return unDeployedEventStreamFilePaths;
    }

    public void executeManualDeployment(String filePath) throws EventStreamConfigurationException {
        processDeployment(new DeploymentFileData(new File(filePath)));
    }

    public void executeManualUndeployment(String filePath) throws EventStreamConfigurationException {
        processUndeployment(new File(filePath).getName());
    }
}