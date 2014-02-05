package org.wso2.carbon.bam.toolbox.deployer.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.BAMToolBoxDeployerConstants;
import org.wso2.carbon.bam.toolbox.deployer.BasicToolBox;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.config.ToolBoxConfigurationManager;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.util.JaggeryDashboardDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxStatusDTO;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class BAMToolBoxUploaderService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(BAMToolBoxUploaderService.class);

    public boolean uploadBAMToolBox(DataHandler toolbox, String toolboxName) throws BAMToolboxDeploymentException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String repoPath = "";
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            repoPath = ServiceHolder.getConfigurationContextService()
                    .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
        } else {
            String tenantRepoPath = "/repository/tenants/" + tenantId;
            repoPath = CarbonUtils.getCarbonHome() + tenantRepoPath;
        }
        File hotDeploymentDir = new File(repoPath + File.separator + BAMToolBoxDeployerConstants.BAM_DEPLOYMET_FOLDER);
        if (hotDeploymentDir.exists()) {
            File destFile = new File(hotDeploymentDir + File.separator + toolboxName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(destFile);
                toolbox.writeTo(fos);
                fos.flush();
                fos.close();
                return true;
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
        } else {

            throw new BAMToolboxDeploymentException("No deployment folder found for tenant id:" + tenantId);
        }
    }

    public ToolBoxStatusDTO getDeployedToolBoxes(String type, String searchKey) throws BAMToolboxDeploymentException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        ToolBoxStatusDTO toolBoxStatusDTO = new ToolBoxStatusDTO();

        String repoPath = "";
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            repoPath = ServiceHolder.getConfigurationContextService()
                    .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
        } else {
            String tenantRepoPath = "/repository/tenants/" + tenantId;
            repoPath = CarbonUtils.getCarbonHome() + tenantRepoPath;
        }
        File hotDeploymentDir = new File(repoPath + File.separator + BAMToolBoxDeployerConstants.BAM_DEPLOYMET_FOLDER);

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".tbox");
            }
        };

        String[] toolsInDir = hotDeploymentDir.list(filter);

        if (null == searchKey) searchKey = "";

        ToolBoxConfigurationManager configurationManager = ToolBoxConfigurationManager.getInstance();
        ArrayList<String> toolsInConf = configurationManager.getAllToolBoxNames(tenantId);
        if (null == type || "".equals(type) || type.equals("1")) {
            toolBoxStatusDTO.setDeployedTools(getDeployedTools(toolsInDir, toolsInConf, searchKey));
            toolBoxStatusDTO.setToBeDeployedTools(getToBeDeployedTools(toolsInDir, toolsInConf, searchKey));
            toolBoxStatusDTO.setToBeUndeployedTools(getToBeUnDeployedTools(toolsInDir, toolsInConf, searchKey));

        } else if (type.equalsIgnoreCase("2")) {
            toolBoxStatusDTO.setDeployedTools(getDeployedTools(toolsInDir, toolsInConf, searchKey));
        } else if (type.equalsIgnoreCase("3")) {
            toolBoxStatusDTO.setToBeDeployedTools(getToBeDeployedTools(toolsInDir, toolsInConf, searchKey));
        } else {
            toolBoxStatusDTO.setToBeUndeployedTools(getToBeUnDeployedTools(toolsInDir, toolsInConf, searchKey));
        }

        return toolBoxStatusDTO;
    }

    public ArrayList<JaggeryDashboardDTO> getJaggeryDashboards() throws BAMToolboxDeploymentException {
        ArrayList<JaggeryDashboardDTO> jaggeryDashboardDTOs = new ArrayList<JaggeryDashboardDTO>();

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ToolBoxConfigurationManager configurationManager = ToolBoxConfigurationManager.getInstance();
        ArrayList<String> toolsInConf = configurationManager.getAllToolBoxNames(tenantId);
        for (String toolbox : toolsInConf) {
            ToolBoxDTO toolBoxDTO = configurationManager.getToolBox(toolbox, tenantId);
            jaggeryDashboardDTOs.addAll(toolBoxDTO.getJaggeryDashboards());
        }
        return jaggeryDashboardDTOs;
    }


    private String[] getDeployedTools(String[] toolsInDir, ArrayList<String> toolsInConf, String searchKey) {
        ArrayList<String> deployedTools = new ArrayList<String>();
        if (null != toolsInDir) {
            for (String tool : toolsInDir) {
                if (tool.endsWith(".tbox")) {
                    tool = tool.replaceAll(".tbox", "");
                }
                if ((searchKey.equals("") || searchKey.equals("*") || tool.equalsIgnoreCase(searchKey)) && toolsInConf.contains(tool)) {
                    deployedTools.add(tool);
                }
            }
        }
        return deployedTools.toArray(new String[deployedTools.size()]);
    }

    private String[] getToBeDeployedTools(String[] toolsInDir, ArrayList<String> toolsInConf, String searchKey) {
        ArrayList<String> toBedeployedTools = new ArrayList<String>();
        if (null != toolsInDir) {
            for (String tool : toolsInDir) {
                if (tool.endsWith(".tbox")) {
                    tool = tool.replaceAll(".tbox", "");
                }
                if ((searchKey.equals("") || searchKey.equals("*") || tool.equalsIgnoreCase(searchKey)) && !toolsInConf.contains(tool)) {
                    toBedeployedTools.add(tool);
                }
            }
        }
        return toBedeployedTools.toArray(new String[toBedeployedTools.size()]);
    }

    private String[] getToBeUnDeployedTools(String[] toolsInDir, ArrayList<String> toolsInConf, String searchKey) {

        ArrayList<String> toBeUndeployedTools = new ArrayList<String>();
        if (null != toolsInConf) {
            for (String tool : toolsInConf) {
                String toolName = tool;
                tool += ".tbox";
                if (null != toolsInDir) {
                    boolean exists = false;
                    for (String toolDir : toolsInDir) {
                        if (toolDir.equalsIgnoreCase(tool)) {
                            exists = true;
                            break;
                        }
                    }
                    if ((searchKey.equals("") || searchKey.equals("*") || tool.equalsIgnoreCase(searchKey)) && !exists) {
                        toBeUndeployedTools.add(toolName);
                    }
                } else {
                    if (searchKey.equals("") || searchKey.equals("*")) {
                        toBeUndeployedTools.addAll(toolsInConf);
                        break;
                    } else {
                        if (tool.equalsIgnoreCase(searchKey)) {
                            toBeUndeployedTools.add(tool);
                        }
                    }
                }
            }
        }
        return toBeUndeployedTools.toArray(new String[toBeUndeployedTools.size()]);
    }

    public boolean undeployToolBox(String[] toolboxNames) throws BAMToolboxDeploymentException {
        if (null != toolboxNames) {

            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            String repoPath = "";
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                repoPath = ServiceHolder.getConfigurationContextService()
                        .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
            } else {
                String tenantRepoPath = "/repository/tenants/" + tenantId;
                repoPath = CarbonUtils.getCarbonHome() + tenantRepoPath;
            }
            for (String toolboxName : toolboxNames) {
                if (null != toolboxName && !toolboxName.trim().equals("")) {
                    File toolbox = new File(repoPath + File.separator + BAMToolBoxDeployerConstants.BAM_DEPLOYMET_FOLDER +
                            File.separator + toolboxName.trim() + ".tbox");
                    if (toolbox.exists()) {
                        toolbox.delete();
                    } else {
                        throw new BAMToolboxDeploymentException("No Tool Box exists" +
                                " in the deployment folder" + toolboxName);
                    }
                }
            }
            return true;
        } else {
            return true;
        }

    }


    public DataHandler downloadToolBox(String toolboxName) {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String repoPath = "";
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            repoPath = ServiceHolder.getConfigurationContextService()
                    .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
        } else {
            String tenantRepoPath = "/repository/tenants/" + tenantId;
            repoPath = CarbonUtils.getCarbonHome() + tenantRepoPath;
        }
        File toolbox = new File(repoPath + File.separator + BAMToolBoxDeployerConstants.BAM_DEPLOYMET_FOLDER +
                File.separator + toolboxName + ".tbox");
        FileDataSource datasource = new FileDataSource(toolbox);
        return new DataHandler(datasource);
    }


    public void deployBasicToolBox(int sample_id) throws BAMToolboxDeploymentException {
        for (BasicToolBox basicToolBox : BasicToolBox.getAvailableToolBoxes()) {
            if (basicToolBox.getSampleId() == sample_id) {
                String toolboxPath = basicToolBox.getLocation();
                copyArtifact(toolboxPath, basicToolBox.getTBoxFileName());
            }
        }
    }

    private void copyArtifact(String toolBoxSrc, String toolName) throws BAMToolboxDeploymentException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String repoPath = "";
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            repoPath = ServiceHolder.getConfigurationContextService()
                    .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
        } else {
            String tenantRepoPath = "/repository/tenants/" + tenantId;
            repoPath = CarbonUtils.getCarbonHome() + tenantRepoPath;
        }
        File hotDeploymentDir = new File(repoPath + File.separator + BAMToolBoxDeployerConstants.BAM_DEPLOYMET_FOLDER);

        if (hotDeploymentDir.exists()) {
            InputStream in = null;
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tbox");
                }
            };

            String[] toolsInDir = hotDeploymentDir.list(filter);

            if (null != toolsInDir) {
                for (String tool : toolsInDir) {
                    if (null != tool && tool.equals(toolName)) {
                        throw new BAMToolboxDeploymentException("The selected Tool already deployed..");
                    }
                }
            }

            try {
                in = new FileInputStream(toolBoxSrc);

                OutputStream out = new FileOutputStream(hotDeploymentDir + File.separator + toolName);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                log.error(e);
                throw new BAMToolboxDeploymentException(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e);
                throw new BAMToolboxDeploymentException(e.getMessage(), e);
            }
        } else {

            throw new BAMToolboxDeploymentException("No deployment folder found for tenant id:" + tenantId);
        }
    }

    public BasicToolBox[] getBasicToolBoxes() {
        return BasicToolBox.getAvailableToolBoxes();
    }

    public void deployToolBoxFromURL(String url) throws BAMToolboxDeploymentException {
        int slashIndex = url.lastIndexOf('/');
        try {
            String toolName = url.substring(slashIndex + 1);

            URL toolUrl = new URL(url);
            InputStream is = toolUrl.openStream();
            DataHandler handler = new DataHandler(new ByteArrayDataSource(is, "application/octet-stream"));
            is.close();

            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            String repoPath = "";
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                repoPath = ServiceHolder.getConfigurationContextService()
                        .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
            } else {
                String tenantRepoPath = "/repository/tenants/" + tenantId;
                repoPath = CarbonUtils.getCarbonHome() + tenantRepoPath;
            }
            File hotDeploymentDir = new File(repoPath + File.separator + BAMToolBoxDeployerConstants.BAM_DEPLOYMET_FOLDER);
            if (hotDeploymentDir.exists()) {
                File file = new File(hotDeploymentDir + File.separator + toolName);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                handler.writeTo(fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } else {
                throw new BAMToolboxDeploymentException("No deployment folder found for tenant id:" + tenantId);
            }
        } catch (IOException e) {
            log.error(e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }


    }

}
