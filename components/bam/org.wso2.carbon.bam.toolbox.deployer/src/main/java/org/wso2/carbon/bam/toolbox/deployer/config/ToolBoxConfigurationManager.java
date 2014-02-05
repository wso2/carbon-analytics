/**
* Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.bam.toolbox.deployer.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.BAMToolBoxDeployerConstants;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.util.DashBoardTabDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.JasperTabDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxDTO;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;

public class ToolBoxConfigurationManager {
    private static ToolBoxConfigurationManager instance;

    private static final Log log = LogFactory.getLog(ToolBoxConfigurationManager.class);

    private ToolBoxConfigurationManager() {
        //to avoid instantiation
    }

    public static ToolBoxConfigurationManager getInstance() {
        if (null == instance) {
            instance = new ToolBoxConfigurationManager();
        }
        return instance;
    }

    public void addNewToolBoxConfiguration(ToolBoxDTO toolboxDTO, int tenantId) throws BAMToolboxDeploymentException {
        ToolBoxConfiguration configuration = createToolBoxConf(toolboxDTO);
        try {
            Registry registry = ServiceHolder.getRegistry(tenantId);
            saveNewConfiguration(registry, configuration.toString(),
                    toolboxDTO.getName());
        } catch (RegistryException e) {
            log.error("Error while retrieving the registry services for tenent id:" + tenantId);
            throw new BAMToolboxDeploymentException("Error while retrieving " +
                    "the registry services for tenent id:" + tenantId, e);
        }
    }

    public ToolBoxDTO getToolBox(String aToolName, int tenantId) throws BAMToolboxDeploymentException {
        ToolBoxConfiguration configuration = getToolBoxConfiguration(aToolName, tenantId);
        ToolBoxDTO aToolBox = new ToolBoxDTO(aToolName);
        aToolBox.setScriptNames(configuration.getScriptNames());
        aToolBox.setDashboardTabs(configuration.getDashboardTabs());
        aToolBox.setJaggeryDashboards(configuration.getJaggeryDashboards());
        aToolBox.setJasperTabs(configuration.getJasperTabs());
        aToolBox.setDataSource(configuration.getDataSource());
        aToolBox.setDataSourceConfiguration(configuration.getDataSourceConfiguration());
        return aToolBox;
    }

    public void deleteToolBoxConfiguration(String toolBoxName, int tenantId) throws BAMToolboxDeploymentException {
      String path = BAMToolBoxDeployerConstants.BAM_BASE_PATH + BAMToolBoxDeployerConstants.TOOL_BOX_CONF +
                    BAMToolBoxDeployerConstants.FILE_SEPERATOR +toolBoxName;
        try {
            Registry registry = ServiceHolder.getRegistry(tenantId);
            registry.delete(path);
        } catch (RegistryException e) {
            log.error("Error while loading the registry for tenant:"+tenantId
                    +" for delete tool box configuration", e);
            throw new BAMToolboxDeploymentException("Error while loading the registry for tenant:"+tenantId
                    +" for delete tool box configuration", e);

        }
    }

    public ArrayList<String> getAllToolBoxNames(int tenantId) throws BAMToolboxDeploymentException {
        try {
            ArrayList<String> toolNames = new ArrayList<String>();
            Registry registry = ServiceHolder.getRegistry(tenantId);
            String path = BAMToolBoxDeployerConstants.BAM_BASE_PATH + BAMToolBoxDeployerConstants.TOOL_BOX_CONF;
            if(registry.resourceExists(path)){
                if(registry.get(path) instanceof Collection){
                    Collection collection = (Collection) registry.get(path);
                    for(String aToolPath:collection.getChildren()){
                       Resource childResource = registry.get(aToolPath);

                        if (!(childResource instanceof Collection)) {
                            String name = ((ResourceImpl) childResource).getName();
                            toolNames.add(name);
                        }
                    }
                }
            }
            return toolNames;
        } catch (RegistryException e) {
            log.error("Error while retrieving registry for tenant:"+tenantId+" to get " +
                    "list of toolbox in configuration", e);
            throw new BAMToolboxDeploymentException("Error while retrieving registry for tenant:"+tenantId+" to get " +
                    "list of toolbox in configuration", e);
        }
    }

    private ToolBoxConfiguration createToolBoxConf(ToolBoxDTO toolboxDTO)
            throws BAMToolboxDeploymentException {
        ToolBoxConfiguration configuration = new ToolBoxConfiguration(toolboxDTO.getName());
        for (String script : toolboxDTO.getScriptNames()) {
            configuration.addScript(script);
        }

        for (DashBoardTabDTO tabDTO : toolboxDTO.getDashboardTabs()) {
            ArrayList<String> gadgets = tabDTO.getGadgets();
            for (String aGadget:gadgets){
                configuration.addGadgetToTab(tabDTO.getTabId(), aGadget);
            }
        }

        for (JasperTabDTO tabDTO : toolboxDTO.getJasperTabs()) {
            String jrxmlFileName = tabDTO.getJrxmlFileName();
            String tabName = tabDTO.getTabName();
            configuration.addJRXMLToTab(tabDTO.getTabId(), jrxmlFileName, tabName);
        }

        configuration.addJaggeryDashboards(toolboxDTO.getJaggeryDashboards());

        configuration.addDataSource(toolboxDTO.getDataSource());
        configuration.addDataSourceConfiguration(toolboxDTO.getDataSourceConfiguration());

        return configuration;
    }

    private ToolBoxConfiguration getToolBoxConfiguration(String toolBoxName, int tenantId) throws BAMToolboxDeploymentException {
           try {
            Registry registry = ServiceHolder.getRegistry(tenantId);
            String path = BAMToolBoxDeployerConstants.BAM_BASE_PATH + BAMToolBoxDeployerConstants.TOOL_BOX_CONF
                    + BAMToolBoxDeployerConstants.FILE_SEPERATOR + toolBoxName;
            if(registry.resourceExists(path)){
               Resource resource = registry.get(path);
                InputStream content = resource.getContentStream();
                OMElement config = new StAXOMBuilder(content).getDocumentElement();
                return new ToolBoxConfiguration(toolBoxName, config);
            }
            return new ToolBoxConfiguration(toolBoxName, null);
        } catch (RegistryException e) {
            log.error("Error while retrieving registry for tenant:"+tenantId+" to get " +
                    "configuration file for toolbox:" +toolBoxName, e);
            throw new BAMToolboxDeploymentException("Error while retrieving registry for tenant:"+tenantId+" to get " +
                    "configuration file for toolbox:" +toolBoxName, e);
        } catch (XMLStreamException e) {
              log.error("Error while processing configuration xml for tenant:"+tenantId+" for " +
                    " toolbox:" +toolBoxName, e);
               throw new BAMToolboxDeploymentException("Error while processing configuration xml for tenant:"+tenantId+" for " +
                    " toolbox:" +toolBoxName, e);
           }
    }


    private void saveNewConfiguration(Registry registry,
                                      String configContent, String toolboxName)
            throws BAMToolboxDeploymentException {
        try {
            Resource resource = registry.newResource();
            resource.setContent(configContent);
            String path = BAMToolBoxDeployerConstants.BAM_BASE_PATH + BAMToolBoxDeployerConstants.TOOL_BOX_CONF +
                    BAMToolBoxDeployerConstants.FILE_SEPERATOR +toolboxName;
            registry.put(path, resource);
        } catch (RegistryException e) {
            log.error("Error while saving the configuration file " +
                    "for the tool box:" + toolboxName + " in registry", e);
            throw new BAMToolboxDeploymentException("Error while saving the " +
                    "configuration file for the tool box:" + toolboxName + " in registry", e);
        }
    }

}
