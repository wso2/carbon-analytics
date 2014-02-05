/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.processor.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.exception.ServiceDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.util.helper.EventProcessorConfigurationHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy query plans as axis2 service
 */
@SuppressWarnings("unused")
public class EventProcessorDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(org.wso2.carbon.event.processor.core.EventProcessorDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedExecutionPlanFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> unDeployedExecutionPlanFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());


    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Reads the query-plan.xml and deploys it.
     *
     * @param deploymentFileData information about query plan
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        try {
            String path = deploymentFileData.getAbsolutePath();

            if (!deployedExecutionPlanFilePaths.contains(path)) {
                try {
                    processDeploy(deploymentFileData);
                } catch (ExecutionPlanConfigurationException e) {
                    throw new DeploymentException("Execution plan not deployed properly.", e);
                }
            } else {
                log.debug("Execution plan file is already deployed :" + path);
                deployedExecutionPlanFilePaths.remove(path);
            }
        } catch (Throwable t) {
            log.error("Can't deploy the execution plan: " + deploymentFileData.getName(), t);
            throw new DeploymentException("Can't deploy the execution plan: " + deploymentFileData.getName(), t);
        }
    }


    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed bucket
     *
     * @param filePath the path to the bucket to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void undeploy(String filePath) throws DeploymentException {
        try {
            if (!unDeployedExecutionPlanFilePaths.contains(filePath)) {
                processUndeploy(filePath);
            } else {
                log.debug("Execution plan file is already undeployed :" + filePath);
                unDeployedExecutionPlanFilePaths.remove(filePath);
            }
        } catch (Throwable t) {
            log.error("Can't undeploy the execution plan: " + filePath, t);
            throw new DeploymentException("Can't undeploy the execution plan: " + filePath, t);
        }

    }

    public synchronized void processDeploy(DeploymentFileData deploymentFileData)
            throws ExecutionPlanConfigurationException {
        // can't be null at this point
        CarbonEventProcessorService carbonEventProcessorService = EventProcessorValueHolder.getEventProcessorService();

        File executionPlanFile = deploymentFileData.getFile();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ExecutionPlanConfigurationFile executionPlanConfigurationFile = new ExecutionPlanConfigurationFile();
        String executionPlanName = "";
        try {
            OMElement executionPlanOMElement = getExecutionPlanOMElement(executionPlanFile);
            ExecutionPlanConfiguration executionPlanConfiguration = EventProcessorConfigurationHelper.fromOM(executionPlanOMElement);

            if(executionPlanConfiguration.getName() == null || executionPlanConfiguration.getName().trim().isEmpty()){
                throw new ExecutionPlanConfigurationException(executionPlanFile.getName() + " is not a valid execution plan configuration file, does not contain a valid execution plan name");
            }

            executionPlanName = executionPlanConfiguration.getName();
            EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(executionPlanOMElement);
            carbonEventProcessorService.addExecutionPlanConfiguration(executionPlanConfiguration, configurationContext.getAxisConfiguration());
            executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.DEPLOYED);
            executionPlanConfigurationFile.setExecutionPlanName(executionPlanConfiguration.getName());
            executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
            executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
            carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            log.info("Execution plan is deployed successfully and in active state  : " + executionPlanConfiguration.getName());

        } catch (ServiceDependencyValidationException ex) {
            executionPlanConfigurationFile.setDependency(ex.getDependency());
            executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.WAITING_FOR_OSGI_SERVICE);
            executionPlanConfigurationFile.setExecutionPlanName(executionPlanName);
            executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
            executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
            carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            log.info("Execution plan deployment held back and in inactive state : " + executionPlanName + " ,waiting for dependency : " + ex.getDependency());

        } catch (ExecutionPlanDependencyValidationException ex) {
            executionPlanConfigurationFile.setDependency(ex.getDependency());
            executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY);
            executionPlanConfigurationFile.setExecutionPlanName(executionPlanName);
            executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
            executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
            carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            log.info("Execution plan deployment held back and in inactive state : " + executionPlanName + " ,waiting for dependency : " + ex.getDependency());

        } catch (ExecutionPlanConfigurationException ex) {
            executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.ERROR);
            executionPlanConfigurationFile.setExecutionPlanName(executionPlanName);
            executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
            executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
            carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            log.error("Execution plan is not deployed and in inactive state : " + executionPlanFile.getName() + ", " + ex.getMessage(), ex);
            throw new ExecutionPlanConfigurationException(ex.getMessage(), ex);
        }catch (DeploymentException ex) {
            executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.ERROR);
            executionPlanConfigurationFile.setExecutionPlanName(executionPlanName);
            executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
            executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
            carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            log.error("Execution plan is not deployed and in inactive state : " + executionPlanConfigurationFile.getFileName() + ", " + ex.getMessage(), ex);
            throw new ExecutionPlanConfigurationException(ex.getMessage(), ex);
        }

    }

    public synchronized void processUndeploy(String filePath) {

        String fileName=new File(filePath).getName();
        log.info("Execution Plan was undeployed successfully : " + fileName);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CarbonEventProcessorService carbonEventProcessorService = EventProcessorValueHolder.getEventProcessorService();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        carbonEventProcessorService.removeExecutionPlanConfigurationFile(fileName, tenantId);
    }

    public void setDirectory(String directory) {

    }

    public void executeManualDeployment(String filePath) throws DeploymentException, ExecutionPlanConfigurationException {
        processDeploy(new DeploymentFileData(new File(filePath)));
    }

    public void executeManualUndeployment(String filePath) {
        processUndeploy(filePath);
    }


    private OMElement getExecutionPlanOMElement(File executionPlanFile)
            throws DeploymentException {
        OMElement executionPlanElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(executionPlanFile));
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader parser = xif.createXMLStreamReader(inputStream);
            xif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE); //for CDATA
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            executionPlanElement = builder.getDocumentElement();
            executionPlanElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = "file cannot be found : " + executionPlanFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + executionPlanFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (OMException e) {
            String errorMessage = "XML tags are not properly closed in " + executionPlanFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
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
        return executionPlanElement;
    }

    public Set<String> getDeployedExecutionPlanFilePaths() {
        return deployedExecutionPlanFilePaths;
    }

    public Set<String> getUnDeployedExecutionPlanFilePaths() {
        return unDeployedExecutionPlanFilePaths;
    }
}


